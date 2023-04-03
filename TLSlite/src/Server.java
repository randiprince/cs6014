import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class Server {
    static byte[] serverEncrypt;
    static byte[] clientEncrypt;
    static byte[] serverMAC;
    static byte[] clientMAC;
    static byte[] serverIV;
    static byte[] clientIV;
    static byte[] serverNonce;
    public static Certificate serverCert;
    static BigInteger DHPrivateKeyServer;
    static BigInteger DHPublicKeyServer;
    static PrivateKey privateKeyFromFile;
    static BigInteger signedDHPublicKeyServer;

    static BigInteger sharedSecretKey;

    static ServerSocket serverSocket;

    static Socket socket;

    public Server() throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException {
        // initialize nonce
        this.serverNonce = new SecureRandom().generateSeed(32);
        // get signed ca client cert
        this.serverCert = Helpers.generateCertificate("src/CASignedServerCertificate.pem");

        // generate private key to get DH public key
        this.DHPrivateKeyServer = new BigInteger( new SecureRandom().generateSeed(32));
        this.DHPublicKeyServer = Helpers.getDHPublicKey(this.DHPrivateKeyServer);

        // now get signed DHPublicKeyClient
        this.privateKeyFromFile = Helpers.getKeyFromFile("src/serverPrivateKey.der");
        this.signedDHPublicKeyServer = Helpers.getSignedKey(this.privateKeyFromFile, this.DHPublicKeyServer);
        this.serverSocket = new ServerSocket(3333);
    }

    public void makeSecretKeys(byte[] nonce, BigInteger sharedSecretKey) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(nonce, "HmacSHA256");
        HMAC.init(secretKeySpec);
        HMAC.update(sharedSecretKey.toByteArray());
        byte[] prk = HMAC.doFinal(sharedSecretKey.toByteArray());
        serverEncrypt = Helpers.hkdfExpand(prk, "server encrypt");
        clientEncrypt = Helpers.hkdfExpand(serverEncrypt, "client encrypt");
        serverMAC = Helpers.hkdfExpand(clientEncrypt, "server MAC");
        clientMAC = Helpers.hkdfExpand(serverMAC, "client MAC");
        serverIV = Helpers.hkdfExpand(clientMAC, "server IV");
        clientIV = Helpers.hkdfExpand(serverIV, "client IV");
    }

    public static void main(String[] args) throws IOException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, SignatureException, InvalidKeyException, ClassNotFoundException, InvalidAlgorithmParameterException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
        System.out.println("initialize server");
        Server myServer = new Server();
        System.out.println("done initializing server");

        Certificate caCertificate = Helpers.generateCertificate("src/CAcertificate.pem");
        PublicKey caCertPublicKey = caCertificate.getPublicKey();

        while(true) {
            try {

//        ServerSocket serverSocket = new ServerSocket(8080);
                myServer.socket = myServer.serverSocket.accept();
                System.out.println("server accepted socket....");
                System.out.println("making IO streams in server!");
                ObjectInputStream inputStreamServer = new ObjectInputStream(myServer.socket.getInputStream());
                ObjectOutputStream outputStreamServer = new ObjectOutputStream(myServer.socket.getOutputStream());
                System.out.println("made server streams@!");

                System.out.println("making msg log  in server!");
                ByteArrayOutputStream messageLog = new ByteArrayOutputStream();

                myServer.serverNonce = (byte[]) inputStreamServer.readObject();
                messageLog.write(myServer.serverNonce);

                outputStreamServer.writeObject(myServer.serverCert);
                outputStreamServer.writeObject(myServer.DHPublicKeyServer);
                outputStreamServer.writeObject(myServer.signedDHPublicKeyServer);

                messageLog.write(myServer.serverCert.getEncoded());
                messageLog.write(myServer.DHPublicKeyServer.toByteArray());
                messageLog.write(myServer.signedDHPublicKeyServer.toByteArray());

                // get stuff from client
                Certificate clientCert = (Certificate) inputStreamServer.readObject();
                BigInteger DHPublicKeyClient = (BigInteger) inputStreamServer.readObject();
                BigInteger signedDHPublicKeyClient = (BigInteger) inputStreamServer.readObject();

                try {
                    clientCert.verify(caCertPublicKey);
                } catch (NoSuchProviderException e) {
                    throw new RuntimeException(e);
                }

                messageLog.write(clientCert.getEncoded());
                messageLog.write(DHPublicKeyClient.toByteArray());
                messageLog.write(signedDHPublicKeyClient.toByteArray());

                myServer.sharedSecretKey = Helpers.getSharedSecretKey(DHPublicKeyClient, myServer.DHPrivateKeyServer);
                //make the other secret keys
                myServer.makeSecretKeys(myServer.serverNonce, myServer.sharedSecretKey);

                // send mac history to client
                byte[] macHistory = Helpers.getMessageToCompare(messageLog.toByteArray(), myServer.serverMAC);
                outputStreamServer.writeObject(macHistory);
                messageLog.write(macHistory);

                // compare history/mac
                byte[] macClient = (byte[]) inputStreamServer.readObject();
                byte[] macServerToCompare = Helpers.getMessageToCompare(messageLog.toByteArray(), myServer.clientMAC);

                assert Arrays.equals(macClient, macServerToCompare);

                messageLog.write(macClient);
                System.out.println("handshake finished server!");

                // encrypt data to send to client
                String msgToClient = "hello hello hello hellllllo!!!!!!! from the server!";
                System.out.println("The message to encrypt is: " + msgToClient);
                byte[] encryptMsg = Helpers.encrypt(msgToClient.getBytes(), myServer.serverEncrypt, myServer.serverIV, myServer.serverMAC);
                System.out.println("The cipher message is: " + new String(encryptMsg));
                outputStreamServer.writeObject(encryptMsg); // send encrypted msg to client

                byte[] ackToReceive = (byte[]) inputStreamServer.readObject(); // should get ack back from client
                byte[] ackOne = Helpers.decrypt(ackToReceive, myServer.clientEncrypt, myServer.clientIV, myServer.clientMAC);
                byte[] noMac = Arrays.copyOf(ackOne, ackOne.length - 32); // -32 bytes bc a mac is a 256 bit
                if (Helpers.doMacsMatch(ackOne, myServer.clientMAC)) { //confirm that macs match
                    System.out.println("here is the first ack from client: " +  new String(noMac));
                } else {
                    throw new RuntimeException("macs dont match");
                }
                String msgToClient2 = "hello from the server again!!";
                System.out.println("The next message to encrypted is: " + msgToClient2);
                byte[] encryptMsg2 = Helpers.encrypt(msgToClient2.getBytes(), myServer.serverEncrypt, myServer.serverIV, myServer.serverMAC);
                System.out.println("The cipher message is: " + new String(encryptMsg2));
                outputStreamServer.writeObject(encryptMsg2); // send encrypted message to client

                byte[] ackToReceive2 = (byte[]) inputStreamServer.readObject(); //client should send another ack back
                byte[] ack2 = Helpers.decrypt(ackToReceive2, myServer.clientEncrypt, myServer.clientIV, myServer.clientMAC);
                byte[] noMac2 = Arrays.copyOf(ack2, ack2.length - 32); // -32 bytes bc a mac is a 256 bit
                if (Helpers.doMacsMatch(ack2, myServer.clientMAC)) { // check that macs match
                    System.out.println("here is the next ack from client: " +  new String(noMac2));
                } else {
                    throw new RuntimeException("macs dont match");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

    }
}

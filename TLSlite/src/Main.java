import javax.crypto.SecretKeyFactory;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Arrays;

public class Main {


    public static void main(String[] args) throws FileNotFoundException, CertificateException {
        String wut = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B" +
                "80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9" +
                "519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7E" +
                "C6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C" +
                "4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA" +
                "8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C" +
                "354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039" +
                "B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956A" +
                "E515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF";
        BigInteger whatisthis = new BigInteger(wut, 16);
        // CA STUFF!
        File certificateFile = new File("src/CAcertificate.pem");
        FileInputStream cfInput = new FileInputStream(certificateFile);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert = cf.generateCertificate(cfInput);
        PublicKey CAPubKey = caCert.getPublicKey();

        // SERVER STUFF!
        File serverFile = new File("src/CASignedServerCertificate.pem");
        FileInputStream serverInput = new FileInputStream(serverFile);
        CertificateFactory cfServer = CertificateFactory.getInstance("X.509");
        Certificate serverCert = cfServer.generateCertificate(serverInput);

        byte serverNonce[] = new SecureRandom().generateSeed(32);
        byte DHServerPrivateKey[] = new SecureRandom().generateSeed(32);
        BigInteger DHPrivateKeyServer = new BigInteger(DHServerPrivateKey);

        // 2 is g, the generator
        // generate public key using g, N, and own private key
        BigInteger DHPublicKeyServer = new BigInteger("2").modPow(DHPrivateKeyServer, whatisthis);


        // CLIENT STUFF!
        File clientFile = new File("src/CASignedClientCertificate.pem");
        FileInputStream clientInput = new FileInputStream(clientFile);
        CertificateFactory cfClient = CertificateFactory.getInstance("X.509");
        Certificate clientCert = cfClient.generateCertificate(clientInput);

        byte clientNonce[] = new SecureRandom().generateSeed(32);
        byte DHClientPrivateKey[] = new SecureRandom().generateSeed(32);
        BigInteger DHPrivateKeyClient = new BigInteger(DHClientPrivateKey);
        // 2 is g, the generator
        // generate public keys using g, N, and own private key
        BigInteger DHPublicKeyClient = new BigInteger("2").modPow(DHPrivateKeyClient, whatisthis);

        // get shared keys using own private key and others public key. if both the client and server
        // do this the shared secret key they each get should be the same
        BigInteger sharedClientKey = DHPublicKeyServer.modPow(DHPrivateKeyClient, whatisthis);
        BigInteger sharedServerKey = DHPublicKeyClient.modPow(DHPrivateKeyServer, whatisthis);
        System.out.println("shared secret key CLIENT: " + sharedClientKey);
        System.out.println("shared secret key server: " + sharedServerKey);
        assert sharedClientKey.equals(sharedServerKey);
    }
}
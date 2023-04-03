import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
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
// things that the CA, client, and server all do
    // make a certificate - need get certificate function
// things that the client and server both do:
    //make DH public key - need get public key function
    // get shared secret key - need get shared secret function
    // get private key from der file
    // sign private key
    // mac keys
    // hdkfExpand
    // make shared secret (may want to put in each client and server tho)
    // encrypt and decrypt

public class Helpers {
    static public String prime = "FFFFFFFFFFFFFFFFC90FDAA22168C234C4C6628B" +
            "80DC1CD129024E088A67CC74020BBEA63B139B22514A08798E3404DDEF9" +
            "519B3CD3A431B302B0A6DF25F14374FE1356D6D51C245E485B576625E7E" +
            "C6F44C42E9A637ED6B0BFF5CB6F406B7EDEE386BFB5A899FA5AE9F24117C" +
            "4B1FE649286651ECE45B3DC2007CB8A163BF0598DA48361C55D39A69163FA" +
            "8FD24CF5F83655D23DCA3AD961C62F356208552BB9ED529077096966D670C" +
            "354E4ABC9804F1746C08CA18217C32905E462E36CE3BE39E772C180E86039" +
            "B2783A2EC07A28FB5C55DF06F4C52C9DE2BCBF6955817183995497CEA956A" +
            "E515D2261898FA051015728E5A8AACAA68FFFFFFFFFFFFFFFF";
    static public BigInteger n = new BigInteger(prime, 16);
    static public BigInteger g = new BigInteger("2");

    static public Certificate generateCertificate(String fileName) throws FileNotFoundException, CertificateException {
//        File certificateFile = new File(fileName);
        FileInputStream cfInput = new FileInputStream(fileName);
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        Certificate caCert = cf.generateCertificate(cfInput);
        return caCert;
    }

    static PrivateKey getKeyFromFile(String fileName) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        FileInputStream inputStream = new FileInputStream(fileName);
        byte[] privateKey = inputStream.readAllBytes();
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    static BigInteger getSignedKey(PrivateKey privateKey, BigInteger DHPublicKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(DHPublicKey.toByteArray());
        return new BigInteger(sign.sign());
    }

    static BigInteger getSharedSecretKey(BigInteger DHPublicKey, BigInteger DHPrivateKey) {
        return DHPublicKey.modPow(DHPrivateKey, n);
    }

    static BigInteger getDHPublicKey(BigInteger DHPrivateKey) {
        return g.modPow(DHPrivateKey, n);
    }

    static byte[] hkdfExpand(byte[] input, String tag) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(input, "HmacSHA256");
        HMAC.init(secretKeySpec);
        HMAC.update(tag.getBytes());
        HMAC.update((byte) 1);
        return Arrays.copyOf(HMAC.doFinal(), 16);
    }

    public static byte[] getMessageToCompare(byte[] msg, byte[] mac) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(mac, "HmacSHA256");
        HMAC.init(secretKeySpec);
        HMAC.update(msg);
        return HMAC.doFinal();
    }

    static byte[] encrypt(byte[] msg, byte[] encryptKey, byte[] ivKey, byte[] macKey) throws IOException, NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException {
        ByteArrayOutputStream encrypted = new ByteArrayOutputStream();
        encrypted.write(msg);
        byte[] msgMac = Helpers.getMessageToCompare(msg, macKey);
        encrypted.write(msgMac);
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivKey);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        return cipher.doFinal(encrypted.toByteArray());
    }

    static byte[] decrypt(byte[] cipherText, byte[] encryptKey, byte[] ivKey, byte[] macKey) throws NoSuchPaddingException, NoSuchAlgorithmException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        SecretKeySpec secretKeySpec = new SecretKeySpec(encryptKey, "AES");
        IvParameterSpec ivParameterSpec = new IvParameterSpec(ivKey);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] plainText = cipher.doFinal(cipherText);
        return plainText;
    }

    static boolean doMacsMatch(byte[] plainText, byte[] macKey) throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] plainTextWithoutMac = Arrays.copyOf(plainText, plainText.length - 32); // rm mac from plain text
        byte[] actualMac = Arrays.copyOfRange(plainText, plainText.length - 32, plainText.length); // get mac msg
        byte[] messageWithMac = Helpers.getMessageToCompare(plainTextWithoutMac, macKey); // create mac msg with plain text and our actual key
        if (Arrays.equals(messageWithMac, actualMac)) { // our mac msg and message generated with our actual macKey should match
            return true;
        }
        return false;
    }
}

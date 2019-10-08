package com.fd.tryout.csrf.actual.config;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public final class RSAUtil {

    private static final String ALGORITHM = "RSA";
    private static KeyFactory rsaKeyFactory;
    private static Cipher cipherEncrypt;
    private static Cipher cipherDecrypt;

    static {
        init();
    }

    private static void init() {
        try {
            rsaKeyFactory = KeyFactory.getInstance(ALGORITHM);
            cipherEncrypt = Cipher.getInstance(ALGORITHM);
            cipherDecrypt = Cipher.getInstance(ALGORITHM);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static PublicKey base64ToPublicKey(String base64PublicKey) throws InvalidKeySpecException {
        return byteToPublicKey(Base64.decodeBase64(base64PublicKey.getBytes()));
    }

    public static PrivateKey base64ToPrivateKey(String base64PrivateKey) throws InvalidKeySpecException {
        return byteToPrivateKey(Base64.decodeBase64(base64PrivateKey.getBytes()));
    }

    public static PrivateKey readBytePrivateKey(String filePath) throws IOException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return byteToPrivateKey(bytes);
    }

    public static PublicKey readBytePublicKey(String filePath) throws IOException, InvalidKeySpecException {
        byte[] bytes = Files.readAllBytes(Paths.get(filePath));
        return byteToPublicKey(bytes);
    }

    public static PrivateKey byteToPrivateKey(byte[] bytes) throws InvalidKeySpecException {
        return rsaKeyFactory.generatePrivate(new PKCS8EncodedKeySpec(bytes));
    }

    public static PublicKey byteToPublicKey(byte[] bytes) throws InvalidKeySpecException {
        return rsaKeyFactory.generatePublic(new X509EncodedKeySpec(bytes));
    }

    public static byte[] encrypt(byte[] data, PublicKey publicKey) throws GeneralSecurityException {
        cipherEncrypt.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipherEncrypt.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, PrivateKey privateKey) throws GeneralSecurityException {
        cipherDecrypt.init(Cipher.DECRYPT_MODE, privateKey);
        return cipherDecrypt.doFinal(data);
    }

    public static String encryptText(String data, PublicKey publicKey) throws GeneralSecurityException, UnsupportedEncodingException {
        return Base64.encodeBase64String(encrypt(data.getBytes("UTF-8"), publicKey));
    }

    public static String decryptText(String msg, PrivateKey privateKey) throws GeneralSecurityException, UnsupportedEncodingException {
        return new String(decrypt(Base64.decodeBase64(msg), privateKey), "UTF-8");
    }

}

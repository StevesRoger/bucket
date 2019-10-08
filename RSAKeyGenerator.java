package com.fd.tryout.csrf.actual.config;

import java.io.*;
import java.security.*;
import java.util.Base64;

public class RSAKeyGenerator {

    public static final int LENGTH_256 = 256;
    public static final int LENGTH_512 = 512;
    public static final int LENGTH_1024 = 1024;
    public static final int LENGTH_2048 = 2048;

    public static final String BEGIN_RSA_PUBLIC_KEY = "-----BEGIN RSA PUBLIC KEY-----\n";
    public static final String END_RSA_PUBLIC_KEY = "\n-----END RSA PUBLIC KEY-----\n";

    public static final String BEGIN_RSA_PRIVATE_KEY = "-----BEGIN RSA PRIVATE KEY-----\n";
    public static final String END_RSA_PRIVATE_KEY = "\n-----END RSA PRIVATE KEY-----\n";

    private KeyPairGenerator keyGen;
    private KeyPair keyPair;
    private PrivateKey privateKey;
    private PublicKey publicKey;

    public RSAKeyGenerator() throws NoSuchAlgorithmException, NoSuchProviderException {
        this(LENGTH_1024);
    }

    public RSAKeyGenerator(int keyLength) throws NoSuchAlgorithmException, NoSuchProviderException {
        this.keyGen = KeyPairGenerator.getInstance("RSA");
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        this.keyGen.initialize(keyLength <= 0 ? LENGTH_1024 : keyLength, random);
    }

    public RSAKeyGenerator generateKey() {
        this.keyPair = this.keyGen.generateKeyPair();
        this.privateKey = keyPair.getPrivate();
        this.publicKey = keyPair.getPublic();
        return this;
    }

    public PrivateKey getPrivateKey() {
        return this.privateKey;
    }

    public PublicKey getPublicKey() {
        return this.publicKey;
    }

    public void writePrivateKeyAsBase64(String path) throws IOException {
        writeKeyAsBase64(path, privateKey, BEGIN_RSA_PRIVATE_KEY, END_RSA_PRIVATE_KEY);
    }

    public void writePublicKeyAsBase64(String path) throws IOException {
        writeKeyAsBase64(path, publicKey, BEGIN_RSA_PUBLIC_KEY, END_RSA_PUBLIC_KEY);
    }

    public void writeBinaryToFile(String path, byte[] key) throws IOException {
        File file = new File(path);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        FileOutputStream fos = new FileOutputStream(file);
        fos.write(key);
        fos.flush();
        fos.close();
    }

    private void writeKeyAsBase64(String path, Key key, String begin, String end) throws IOException {
        File file = new File(path);
        if (!file.getParentFile().exists())
            file.getParentFile().mkdirs();
        Writer out = new FileWriter(file);
        out.write(begin);
        out.write(Base64.getEncoder().encodeToString(key.getEncoded()));
        out.write(end);
        out.close();
    }

}

package com.PVZ.database;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;

public class EncryptionEngine {
    private static byte[] dataKey = "DefaultDataKey16".getBytes();
    private static final byte[] AUTH_KEY = "0123456789ABCDEF".getBytes(); // 16 byte

    public static void initDataKey(String masterKey) throws Exception {
        dataKey = hash(masterKey).substring(0, 16).getBytes();
    }

    // === استفاده از کلید dataKey  ===
    public static String encrypt(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(dataKey, "AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decrypt(String encryptedData) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(dataKey, "AES"));
        return new String(cipher.doFinal(decoded));
    }

    // ==استفاده از کلید ثابت برای Auth ==
    public static String encryptAuth(String data) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(AUTH_KEY, "AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decryptAuth(String encryptedData) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(AUTH_KEY, "AES"));
        return new String(cipher.doFinal(decoded));
    }

    public static String hash(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hashBytes = digest.digest(input.getBytes());
        StringBuilder hex = new StringBuilder();
        for (byte b : hashBytes) hex.append(String.format("%02x", b));
        return hex.toString();
    }

    // ===== جدید: AES با key دلخواه — برای UserDatabase =====
    public static String encryptWithKey(String data, byte[] key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"));
        return Base64.getEncoder().encodeToString(cipher.doFinal(data.getBytes()));
    }

    public static String decryptWithKey(String encryptedData, byte[] key) throws Exception {
        byte[] decoded = Base64.getDecoder().decode(encryptedData);
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"));
        return new String(cipher.doFinal(decoded));
    }

    public static byte[] makeKey(String password) throws Exception {
        return hash(password).substring(0, 16).getBytes();
    }
}

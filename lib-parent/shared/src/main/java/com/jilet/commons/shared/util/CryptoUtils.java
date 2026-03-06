package com.jilet.commons.shared.util;

import com.jilet.commons.shared.exception.JiletException;
import com.jilet.commons.shared.exception.JiletExceptions;
import lombok.extern.log4j.Log4j2;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

/**
 * Utility class for encryption and decryption operations.
 * Uses AES-GCM for encryption with PBKDF2 key derivation.
 */
@Log4j2
public final class CryptoUtils {

    private static final String ENCRYPTED_PREFIX = "{cipher}";
    private static final String ALGORITHM = "AES/GCM/NoPadding";
    private static final String KEY_ALGORITHM = "AES";
    private static final String KEY_FACTORY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int GCM_IV_LENGTH = 12;
    private static final int GCM_TAG_LENGTH = 128;
    private static final int SALT_LENGTH = 16;
    private static final int KEY_LENGTH = 256;
    private static final int ITERATION_COUNT = 65536;

    private CryptoUtils() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * Checks whether a string is encrypted (has the {cipher} prefix).
     *
     * @param value the string to check
     * @return true if the string is encrypted, false otherwise
     */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(ENCRYPTED_PREFIX);
    }

    /**
     * Encrypts an arbitrary string with an arbitrary key.
     * The result is prefixed with {cipher} and Base64 encoded.
     *
     * @param plainText the string to encrypt
     * @param key       the encryption key
     * @return the encrypted string with {cipher} prefix
     * @throws com.jilet.commons.shared.exception.JiletException if encryption fails
     */
    public static String encrypt(String plainText, String key) throws JiletException {
        return encryptInternal(plainText, key, Base64.getEncoder());
    }

    /**
     * Decrypts an arbitrary string with the same key used for encryption.
     *
     * @param encryptedText the encrypted string
     * @param key           the decryption key
     * @return the decrypted string
     * @throws com.jilet.commons.shared.exception.JiletException if decryption fails
     */
    public static String decrypt(String encryptedText, String key) throws JiletException {
        return decryptInternal(encryptedText, key, Base64.getDecoder());
    }

    /**
     * Encrypts an arbitrary string and returns a URL-safe result.
     * Uses Base64 URL-safe encoding without padding.
     *
     * @param plainText the string to encrypt
     * @param key       the encryption key
     * @return the URL-safe encrypted string with {cipher} prefix
     * @throws JiletException if encryption fails
     */
    public static String encryptUrlSafe(String plainText, String key) throws JiletException {
        return encryptInternal(plainText, key, Base64.getUrlEncoder().withoutPadding());
    }

    /**
     * Decrypts a URL-safe encrypted string with the same key used for encryption.
     *
     * @param encryptedText the URL-safe encrypted string
     * @param key           the decryption key
     * @return the decrypted string
     * @throws JiletException if decryption fails
     */
    public static String decryptUrlSafe(String encryptedText, String key) throws JiletException {
        return decryptInternal(encryptedText, key, Base64.getUrlDecoder());
    }

    private static String encryptInternal(String plainText, String key, Base64.Encoder encoder) throws JiletException {
        if (plainText == null) {
            log.error("Input to encrypt is null");
            throw JiletExceptions.ENCRYPTION_INPUT_NULL;
        }

        if (key == null) {
            log.error("Encryption key is null");
            throw JiletExceptions.ENCRYPTION_KEY_NULL;
        }

        try {
            SecureRandom secureRandom = new SecureRandom();

            // Generate salt
            byte[] salt = new byte[SALT_LENGTH];
            secureRandom.nextBytes(salt);

            // Generate IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            secureRandom.nextBytes(iv);

            // Derive key from password
            SecretKey secretKey = deriveKey(key, salt);

            // Encrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            byte[] cipherText = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            // Combine salt + iv + cipherText
            ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + cipherText.length);
            byteBuffer.put(salt);
            byteBuffer.put(iv);
            byteBuffer.put(cipherText);
            return ENCRYPTED_PREFIX + encoder.encodeToString(byteBuffer.array());
        } catch (Exception e) {
            log.error("Encryption failed with exception", e);
            throw JiletExceptions.ENCRYPTION_ERROR;
        }
    }

    private static String decryptInternal(String encryptedText, String key, Base64.Decoder decoder) throws JiletException {
        if (encryptedText == null) {
            log.error("Input to decrypt is null");
            throw JiletExceptions.DECRYPTION_INPUT_NULL;
        }

        if (key == null) {
            log.error("Decryption key is null");
            throw JiletExceptions.DECRYPTION_KEY_NULL;
        }

        if (!encryptedText.startsWith(ENCRYPTED_PREFIX)) {
            log.error("Attempting to decrypt a value that does not appear to be encrypted");
            throw JiletExceptions.DECRYPTING_NON_ENCRYPTED_VALUE;
        }

        try {
            String cipherText = encryptedText.substring(ENCRYPTED_PREFIX.length());
            byte[] decoded = decoder.decode(cipherText);
            ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

            // Extract salt
            byte[] salt = new byte[SALT_LENGTH];
            byteBuffer.get(salt);

            // Extract IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            byteBuffer.get(iv);

            // Extract cipherText
            byte[] cipherBytes = new byte[byteBuffer.remaining()];
            byteBuffer.get(cipherBytes);

            // Derive key from password
            SecretKey secretKey = deriveKey(key, salt);

            // Decrypt
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            byte[] plainText = cipher.doFinal(cipherBytes);

            return new String(plainText, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("Decryption failed with exception", e);
            throw JiletExceptions.DECRYPTION_ERROR;
        }
    }

    private static SecretKey deriveKey(String password, byte[] salt) throws Exception {
        SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_FACTORY_ALGORITHM);
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKey tmp = factory.generateSecret(spec);
        return new SecretKeySpec(tmp.getEncoded(), KEY_ALGORITHM);
    }

}

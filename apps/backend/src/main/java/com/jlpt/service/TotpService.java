/* (c) JLPT E-Learning Platform */
package com.jlpt.service;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TotpService {

    private static final int DIGITS = 6;
    private static final long STEP_SECONDS = 30L;
    private static final int WINDOW = 1;
    private static final String AES_TRANSFORM = "AES/CBC/PKCS5Padding";
    private static final int IV_LENGTH = 16;

    @Value("${totp.encryption-key}")
    private String base64EncryptionKey;

    /**
     * AES-256/CBC encrypt a TOTP secret before storing in DB (BR-35-02).
     * Output: Base64(IV || cipherText)
     */
    public String encrypt(String plainSecret) {
        try {
            byte[] key = Base64.getDecoder().decode(base64EncryptionKey);
            byte[] iv = new byte[IV_LENGTH];
            new SecureRandom().nextBytes(iv);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(plainSecret.getBytes());
            byte[] combined = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, combined, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, combined, IV_LENGTH, encrypted.length);
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            log.error("[TotpService] Encryption error: {}", e.getMessage());
            throw new RuntimeException("TOTP secret encryption failed", e);
        }
    }

    /**
     * AES-256/CBC decrypt — reverses encrypt().
     */
    public String decrypt(String encryptedSecret) {
        try {
            byte[] key = Base64.getDecoder().decode(base64EncryptionKey);
            byte[] combined = Base64.getDecoder().decode(encryptedSecret);
            byte[] iv = Arrays.copyOfRange(combined, 0, IV_LENGTH);
            byte[] cipherText = Arrays.copyOfRange(combined, IV_LENGTH, combined.length);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORM);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            return new String(cipher.doFinal(cipherText));
        } catch (Exception e) {
            log.error("[TotpService] Decryption error: {}", e.getMessage());
            throw new RuntimeException("TOTP secret decryption failed", e);
        }
    }

    /**
     * Verify a 6-digit TOTP code against an AES-encrypted secret (RFC 6238, ±1 window).
     * BR-35-04: allows ±1 time window (30 s) to compensate for clock drift.
     */
    public boolean verifyTotp(String encryptedSecret, String inputCode) {
        if (inputCode == null || !inputCode.matches("\\d{6}")) return false;
        try {
            String plainSecret = decrypt(encryptedSecret);
            byte[] secretBytes = decodeBase32(plainSecret);
            long currentWindow = System.currentTimeMillis() / 1000L / STEP_SECONDS;
            for (int delta = -WINDOW; delta <= WINDOW; delta++) {
                String expected = String.format("%0" + DIGITS + "d", totpCode(secretBytes, currentWindow + delta));
                if (expected.equals(inputCode)) return true;
            }
            return false;
        } catch (Exception e) {
            log.error("[TotpService] TOTP verify error: {}", e.getMessage());
            return false;
        }
    }

    /** RFC 6238 / RFC 4226 HOTP using HmacSHA1. */
    private int totpCode(byte[] secret, long timeWindow) throws Exception {
        byte[] msg = ByteBuffer.allocate(8).putLong(timeWindow).array();
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(new SecretKeySpec(secret, "RAW"));
        byte[] hash = mac.doFinal(msg);
        int offset = hash[hash.length - 1] & 0x0F;
        int truncated = ((hash[offset] & 0x7F) << 24)
                | ((hash[offset + 1] & 0xFF) << 16)
                | ((hash[offset + 2] & 0xFF) << 8)
                | (hash[offset + 3] & 0xFF);
        return truncated % (int) Math.pow(10, DIGITS);
    }

    /** Minimal Base32 decoder (RFC 4648) — no external dependency required. */
    private byte[] decodeBase32(String base32) {
        String upper = base32.toUpperCase().replaceAll("[=\\s]", "");
        int[] lut = new int[128];
        Arrays.fill(lut, -1);
        for (int i = 0; i < 26; i++) lut['A' + i] = i;
        for (int i = 0; i < 6; i++) lut['2' + i] = 26 + i;
        byte[] out = new byte[upper.length() * 5 / 8];
        int bits = 0, value = 0, idx = 0;
        for (char c : upper.toCharArray()) {
            value = (value << 5) | lut[c];
            bits += 5;
            if (bits >= 8) {
                out[idx++] = (byte) ((value >> (bits - 8)) & 0xFF);
                bits -= 8;
            }
        }
        return Arrays.copyOf(out, idx);
    }
}

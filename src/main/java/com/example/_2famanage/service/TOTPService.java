package com.example._2famanage.service;

import org.apache.commons.codec.binary.Base32;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class TOTPService {

    private static final Base32 BASE32 = new Base32();
    private static final String ALGORITHM = "HmacSHA1";
    private static final int DIGITS = 6;
    private static final int PERIOD = 30; // seconds

    /**
     * 根据 Base32 编码的密钥生成当前 TOTP 验证码
     */
    public String generateCode(String base32Secret) {
        try {
            byte[] key = BASE32.decode(base32Secret);
            long counter = System.currentTimeMillis() / 1000 / PERIOD;
            byte[] hash = hmacSha1(key, ByteBuffer.allocate(8).putLong(counter).array());
            int offset = hash[hash.length - 1] & 0x0F;

            int binary = ((hash[offset] & 0x7F) << 24)
                       | ((hash[offset + 1] & 0xFF) << 16)
                       | ((hash[offset + 2] & 0xFF) << 8)
                       | (hash[offset + 3] & 0xFF);

            int otp = binary % (int) Math.pow(10, DIGITS);
            return String.format("%0" + DIGITS + "d", otp);

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate TOTP code", e);
        }
    }

    /**
     * 获取当前周期剩余秒数
     */
    public Integer getRemainingSeconds() {
        return PERIOD - (int) ((System.currentTimeMillis() / 1000) % PERIOD);
    }

    private byte[] hmacSha1(byte[] key, byte[] data)
            throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);
        mac.init(new SecretKeySpec(key, ALGORITHM));
        return mac.doFinal(data);
    }
}

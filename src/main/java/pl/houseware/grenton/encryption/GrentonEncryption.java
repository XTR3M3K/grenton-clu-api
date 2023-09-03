package pl.houseware.grenton.encryption;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GrentonEncryption {
    private static final Logger logger = Logger.getLogger(GrentonEncryption.class.getName());

    private final SecretKeySpec secretKey;
    private final IvParameterSpec iv;

    public GrentonEncryption(String secretKey, String iv) {
        this.secretKey = new SecretKeySpec(Base64.getDecoder().decode(secretKey), "AES");
        this.iv = new IvParameterSpec(Base64.getDecoder().decode(iv));
    }

    public byte[] encode(String text) {
        return process(text.getBytes(StandardCharsets.UTF_8), Cipher.ENCRYPT_MODE);
    }

    public String decode(byte[] bytes) {
        return new String(process(bytes, Cipher.DECRYPT_MODE), StandardCharsets.UTF_8);
    }

    private byte[] process(byte[] bytes, int mode) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(mode, secretKey, iv);

            return cipher.doFinal(bytes);
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
        }

        return new byte[0];
    }

}

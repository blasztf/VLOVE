package com.navers.vlove.apis;

import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

class generator {
    private static generator mInstance;

    static String generate(String encrypted, String key) {
        if (mInstance == null) {
            mInstance = new generator();
        }

        return mInstance.decrypt(encrypted, key);
    }

    private String decrypt(String encrypted, String key) {
        try {
            byte[] decry = Base64.decodeBase64(encrypted);

            byte[] ivByte = subBytes(decry, 0x00000000, 0x00000010);
            byte[] decrypt = subBytes(decry, 0x00000010, decry.length - 0x00000010);

            IvParameterSpec iv = new IvParameterSpec(ivByte);
            SecretKeySpec skeySpec = new SecretKeySpec(Base64.decodeBase64(key), new String(Base64.decodeBase64("QUVT")));

            Cipher cipher = Cipher.getInstance(new String(Base64.decodeBase64("QUVTL0NCQy9QS0NTNVBBRERJTkc=")));
            cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv);

            byte[] decrypted = cipher.doFinal(decrypt);

            return new String(decrypted);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private byte[] subBytes(byte[] bytes, int start, int length) {
        byte[] subBytes = new byte[length];
        for (int i = 0; i < length; i++) {
            subBytes[i] = bytes[start];
            start++;
        }
        return subBytes;
    }
}

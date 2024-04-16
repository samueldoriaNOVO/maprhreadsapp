package com.example.mapthreadsapp;

import android.os.Build;
import android.util.Log;

import com.example.regionlibrary.Region;
import com.example.regionlibrary.RestrictedRegion;
import com.example.regionlibrary.SubRegion;
import com.google.gson.Gson;

import java.util.Base64;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class Cryptography {
    private static final String AES_ALGORITHM = "AES";
    private static final String SECRET_KEY = "1234567890abcdef";
    private static final byte[] SECRET_KEY_BYTES = SECRET_KEY.getBytes();

    public static String encrypt(Region region) {
        AtomicReference<String> result = new AtomicReference<>("");
        Thread thread = new Thread(() -> {
            try {
                Gson gson = new Gson();
                SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_BYTES, AES_ALGORITHM);
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
                byte[] encryptedBytes = cipher.doFinal(gson.toJson(region).getBytes());
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    result.set(Base64.getEncoder().encodeToString(encryptedBytes));
                }
            } catch (Exception e) {
                Log.d("Cryptography", "Error encrypting region");

                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
            Log.d("Cryptography", "Encrypted: " + result.get());
            return result.get();
        } catch (InterruptedException e) {
            return null;
        }

    }

    public static Region decrypt(String str) {
        Log.d("Cryptography", "decrypt start ");

        AtomicReference<Region> region = new AtomicReference<>(null);
        Thread thread = new Thread(() -> {
            try {
                Gson gson = new Gson();
                SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET_KEY_BYTES, AES_ALGORITHM);
                Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(str));
                    Region r = gson.fromJson(new String(decryptedBytes), Region.class);
                    switch (r.getType()) {
                        case "SubRegion":
                            region.set(gson.fromJson(new String(decryptedBytes), SubRegion.class));
                            break;
                        case "RestrictedRegion":
                            region.set(gson.fromJson(new String(decryptedBytes), RestrictedRegion.class));
                            break;
                        default:
                            region.set(gson.fromJson(new String(decryptedBytes), Region.class));
                            break;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        thread.start();
        try {
            thread.join();
            return region.get();
        } catch (InterruptedException e) {
            return null;
        }
    }
}

package com.example.mapthreadsapp;

import android.util.Log;

import com.example.regionlibrary.Region;
import com.example.regionlibrary.RestrictedRegion;
import com.example.regionlibrary.SubRegion;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;

public class RegionQueue {
    private Queue<String> regionsQueue;
    private Semaphore semaphore;

    public RegionQueue() {
        regionsQueue = new LinkedList<>();
        semaphore = new Semaphore(1, true);

    }

    public List<Region> getRegions() {
        List<Region> regions = new LinkedList<>();
        for (String regionEncrypted : regionsQueue)
            regions.add(Cryptography.decrypt(regionEncrypted));
        return regions;
    }

    public Region remove() {
        return Cryptography.decrypt(regionsQueue.remove());
    }

    public Boolean isEmpty() {
        return regionsQueue.isEmpty();
    }

    public Region checkNewRegion(Region newRegion, CallbackToast callbackToast) {
        // atomic serve para nao deixar a variavel ser alterada por mais de uma thread
        AtomicReference<Region> nearbyRegion = new AtomicReference<>(null);
        Thread thread = new Thread(() -> {
            try {
                semaphore.acquire();
                for (String regionEncrypted : regionsQueue) {
                    Region region = Cryptography.decrypt(regionEncrypted);
                    if(region instanceof SubRegion || region instanceof RestrictedRegion) continue;

                    if (region.isNear(newRegion)) {
//                        callbackToast.onComplete("Região à menos de 30 metros da " + region.getName());
                        nearbyRegion.set(region);
                        semaphore.release();
                        return;
                    }
                }
                semaphore.release();
            } catch (Exception e) {
                callbackToast.onComplete("Erro ao verificar região");
                e.printStackTrace();
                semaphore.release();
            }
        });
        thread.start();
        try {
            // espera o processamento acabar
            thread.join();
            return nearbyRegion.get();
        } catch (InterruptedException e) {
            return null;
        }
    }

    public void add(Region newRegion, CallbackToast callbackToast) {
        Thread thread = new Thread(() -> {
            try {
                semaphore.acquire();
                regionsQueue.add(Cryptography.encrypt(newRegion));
                callbackToast.onComplete("Região adicionada com sucesso");
                semaphore.release();
            } catch (InterruptedException e) {
                callbackToast.onComplete("Erro ao adicionar região");
                e.printStackTrace();
                semaphore.release();
            }
        });
        thread.start();
        try {
            thread.join();
            Log.d("Cryptography", "End ");

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

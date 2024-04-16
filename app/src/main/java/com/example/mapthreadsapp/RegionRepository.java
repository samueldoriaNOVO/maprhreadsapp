package com.example.mapthreadsapp;

import androidx.annotation.NonNull;

import com.example.regionlibrary.Region;
import com.example.regionlibrary.RestrictedRegion;
import com.example.regionlibrary.SubRegion;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class RegionRepository {
    private DatabaseReference database;
    private List<String> regions = new ArrayList<>();

    public RegionRepository() {
        database = FirebaseDatabase
                .getInstance("<link>")
                .getReference();

        // Busca as regions do banco de dados no momento da inicialização
        database.child("regions").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot document : snapshot.getChildren()) {
                    regions.add(document.getValue(String.class));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                error.toException().printStackTrace();
            }
        });
    }

    public void add(Region region, CallbackToast callbackToast) {
        regions.add(Cryptography.encrypt(region));
        database.child("regions").push().setValue(Cryptography.encrypt(region));
        callbackToast.onComplete(region.getName() + " adicionada com sucesso na base de dados");
    }

    public Region checkNewRegion(Region newRegion, CallbackToast callbackToast) {
        // atomic serve para nao deixar a variavel ser alterada por mais de uma thread
        AtomicReference<Region> nearbyRegion = new AtomicReference<>(null);
        Thread thread = new Thread(() -> {
            try {
                for (String regionEncrypted : this.regions) {
                    Region region = Cryptography.decrypt(regionEncrypted);
                    if(region instanceof SubRegion) continue;
                    if(region instanceof RestrictedRegion) continue;


                    if (region.isNear(newRegion)) {
                        callbackToast.onComplete("Região à menos de 30 metros da " + region.getName());
                        nearbyRegion.set(region);
                        return;
                    }
                }
            } catch (Exception e) {
                callbackToast.onComplete("Erro ao verificar região");
                e.printStackTrace();
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

    public List<Region> getRegions(CallbackToast callbackToast) {
        List<Region> result = new ArrayList<>();
        Thread thread = new Thread(() -> {
            for (String regionEncrypted : this.regions)
                result.add(Cryptography.decrypt(regionEncrypted));
        });
        thread.start();
        try {
            thread.join();
            return result;
        } catch (InterruptedException e) {
            return null;
        }
    }
}

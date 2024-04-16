package com.example.mapthreadsapp;

import com.example.regionlibrary.Region;
import com.example.regionlibrary.RestrictedRegion;
import com.example.regionlibrary.SubRegion;

import java.util.LinkedList;
import java.util.List;

public class RegionService {
    private CallbackToast callbackToast;
    private RegionQueue regionQueue;
    private RegionRepository regionRepository;

    public RegionService(CallbackToast callbackToast) {
        this.callbackToast = callbackToast;
        this.regionQueue = new RegionQueue();
        this.regionRepository = new RegionRepository();
    }

    public List<Region > getAllRegions() {
        List<Region> regions = regionRepository.getRegions(callbackToast);
        List<Region> regionsQueue = regionQueue.getRegions();
        regions.addAll(regionsQueue);
        return regions;
    }


    // retorna apenas as sub e restricted regions que pertencem a uma mainRegion
    private List<Region> getMainRegion(Region mainRegion) {
        List<Region> regions = new LinkedList<>();
        for(Region region : this.getAllRegions()) {
            if(region instanceof SubRegion && ((SubRegion)region).getMainRegionId().equals(mainRegion.getId()))
                    regions.add(region);
            if(region instanceof RestrictedRegion && ((RestrictedRegion)region).getMainRegionId().equals(mainRegion.getId()))
                regions.add(region);
        }
        return regions;
    }

    public boolean checkSubRegion(Region mainRegion, Region newRegion) {
        for(Region region : getMainRegion(mainRegion)) {
            if(region instanceof SubRegion && ((SubRegion)region).isNear(newRegion) ) {
                callbackToast.onComplete("Região à menos de 5 metros da " + region.getName());
                return false;
            }
            if(region instanceof RestrictedRegion && ((RestrictedRegion)region).isNear(newRegion) ) {
                callbackToast.onComplete("Região à menos de 5 metros da " + region.getName());
                return false;
            }
        }
        return true;
    }

    public void addRegion(Region region) {
        // verifica se existe um REGION que é proximo a newRegion
        Region nearbyRegionRepository = regionRepository.checkNewRegion(region, callbackToast);

        // checkSubRegion -> busca todas as Sub e Restricted Region que pertencem a nearbyRegionRepository
        if(nearbyRegionRepository != null && checkSubRegion(nearbyRegionRepository, region)) {
            if(getMainRegion(nearbyRegionRepository).size() % 2 == 0) // se o numero de filhos de uma regiao for par, cria uma subregion
                regionQueue.add(new SubRegion(region.getLatitude(), region.getLongitude(), nearbyRegionRepository), callbackToast);
            else // se o numero de filhos de uma regiao for impar, cria uma restricted region
                regionQueue.add(new RestrictedRegion(region.getLatitude(), region.getLongitude(), nearbyRegionRepository), callbackToast);
            return;
        }

        Region nearbyRegionQueue = regionQueue.checkNewRegion(region, callbackToast);
        if(nearbyRegionQueue != null && checkSubRegion(nearbyRegionQueue, region)) {
            if(getMainRegion(nearbyRegionQueue).size() % 2 == 0)
                regionQueue.add(new SubRegion(region.getLatitude(), region.getLongitude(), nearbyRegionQueue), callbackToast);
            else
                regionQueue.add(new RestrictedRegion(region.getLatitude(), region.getLongitude(), nearbyRegionQueue), callbackToast);
            return;
        }
        if(nearbyRegionRepository == null && nearbyRegionQueue == null)
            regionQueue.add(region, callbackToast);
    }

    public void saveDatabase() {

        if(regionQueue.isEmpty()) {
            callbackToast.onComplete("Nenhuma região para salvar");
            return;
        }
        new Thread(() -> {
            // Roda enquanto a fila nao estiver vazia
            while (!regionQueue.isEmpty()) {
                Region region = regionQueue.remove();
                regionRepository.add(region, callbackToast);
            };
        }).start();
    }

}

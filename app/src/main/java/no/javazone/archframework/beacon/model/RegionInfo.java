package no.javazone.archframework.beacon.model;


import com.estimote.sdk.Region;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class RegionInfo {

    @SerializedName("regions")
    @Expose
    private List<Region> regions = new ArrayList<Region>();

    /**
     *
     * @return
     * The regions
     */
    public List<Region> getRegions() {
        return regions;
    }

    /**
     *
     * @param regions
     * The regions
     */
    public void setRegions(List<Region> regions) {
        this.regions = regions;
    }

}
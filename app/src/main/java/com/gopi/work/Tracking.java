package com.gopi.work;

/**
 * Created by Gopi on 04-01-2018.
 */

public class Tracking {

    private String lat;
    private String lng;
    private String ph;
    private String uid;

    public Tracking(String lat, String lng, String ph, String uid) {
        this.lat = lat;
        this.lng = lng;
        this.ph = ph;
        this.uid = uid;
    }

    public Tracking() {
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

}

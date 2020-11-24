package com.example.final_uber_rider.model;

import java.util.ArrayList;

/**
 * Created by QuocKM on 23,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public class GeoQueryModel {
    private String g;
    private ArrayList<Double> l;

    public GeoQueryModel() {

    }

    public String getG() {
        return g;
    }

    public void setG(String g) {
        this.g = g;
    }

    public ArrayList<Double> getL() {
        return l;
    }

    public void setL(ArrayList<Double> l) {
        this.l = l;
    }
}

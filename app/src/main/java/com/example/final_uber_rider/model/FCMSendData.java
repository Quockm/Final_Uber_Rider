package com.example.final_uber_rider.model;

import java.util.Map;

/**
 * Created by QuocKM on 27,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public class FCMSendData {
    private String to;
    private Map<String,String> data;

    public FCMSendData(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}

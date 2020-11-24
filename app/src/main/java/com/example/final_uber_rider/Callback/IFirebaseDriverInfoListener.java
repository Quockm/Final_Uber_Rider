package com.example.final_uber_rider.Callback;

import com.example.final_uber_rider.model.DriverGeoModel;

/**
 * Created by QuocKM on 22,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public interface IFirebaseDriverInfoListener {
    void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel);
}

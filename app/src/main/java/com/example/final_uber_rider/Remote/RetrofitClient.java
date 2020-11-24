package com.example.final_uber_rider.Remote;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;


/**
 * Created by QuocKM on 24,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public class RetrofitClient {
    private static Retrofit instance;

    public static Retrofit getInstance() {
        return instance == null ? new Retrofit.Builder()
                .baseUrl("https://maps.googleapis.com/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build() : instance;
    }
}

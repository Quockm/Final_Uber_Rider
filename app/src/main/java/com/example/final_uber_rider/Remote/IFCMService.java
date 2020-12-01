package com.example.final_uber_rider.Remote;

import com.example.final_uber_rider.model.FCMResponse;
import com.example.final_uber_rider.model.FCMSendData;

import io.reactivex.rxjava3.core.Observable;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Created by QuocKM on 27,November,2020
 * EbizWorld company,
 * HCMCity, VietNam.
 */
public interface IFCMService {
    @Headers({
            "Content-Type:application/json",
            "Authorization:key=AAAAvIIfK_E:APA91bGD9l2-3VuxnuL95svNWEogejSyTflizhB87XsVkGFA1WIjk64suAtXfg0YUDQHiNGOlwefpzD69Dl0nzM5R63AYjIJfvxXMJdlWo3bdkmpz5eSi9-OdXVOM_XCY5k-tkbCJLMW"
    })
    @POST("fcm/send")
    Observable<FCMResponse> sendNotification(@Body FCMSendData body);
}

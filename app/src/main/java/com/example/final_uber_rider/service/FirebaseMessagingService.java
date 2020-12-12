package com.example.final_uber_rider.service;

import androidx.annotation.NonNull;

import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.model.EventBus.DeclineRequestFromDriver;
import com.example.final_uber_rider.model.EventBus.DriverAcceptTripEvent;
import com.example.final_uber_rider.utils.RiderUtils;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.messaging.RemoteMessage;

import org.greenrobot.eventbus.EventBus;

import java.util.Map;
import java.util.Random;


public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    //generate Token to catch exceptions
    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            RiderUtils.updateToken(this, s);
        }
    }

    // Recived Messsage thorugh getting token
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> dataRev = remoteMessage.getData();
        if (dataRev != null) {
            if (dataRev.get(Common.NOTI_TITLE) != null) {
                if (dataRev.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_DECLINE)) {
                    EventBus.getDefault().postSticky(new DeclineRequestFromDriver());
                }

                else if (dataRev.get(Common.NOTI_TITLE).equals(Common.REQUEST_DRIVER_ACCEPT)) {

                    String tripKey = dataRev.get(Common.TRIP_KEY);
                    EventBus.getDefault().postSticky(new DriverAcceptTripEvent(tripKey));
                } else
                    Common.ShowNofication(this, new Random().nextInt(),
                            dataRev.get(Common.NOTI_TITLE),
                            dataRev.get(Common.NOTI_CONTENT),
                            null);
            }
        }

    }
}

package com.example.final_uber_rider.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.R;
import com.example.final_uber_rider.Remote.IFCMService;
import com.example.final_uber_rider.Remote.RetrofitFCMClient;
import com.example.final_uber_rider.model.DriverGeoModel;
import com.example.final_uber_rider.model.DriverInfoModel;
import com.example.final_uber_rider.model.EventBus.SelectPlaceEvent;
import com.example.final_uber_rider.model.FCMSendData;
import com.example.final_uber_rider.model.TokenModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.sql.Driver;
import java.util.HashMap;
import java.util.Map;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RiderUtils {
    public static void updateUser(View view, Map<String, Object> updateData) {
        FirebaseDatabase.getInstance()
                .getReference(Common.RIDER_INFO_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .updateChildren(updateData)
                .addOnFailureListener(e -> Snackbar.make(view, e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> Snackbar.make(view, "Update information Successfully!", Snackbar.LENGTH_SHORT).show());
    }

    public static void updateToken(Context context, String token) {
        TokenModel tokenModel = new TokenModel(token);
        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_RIDER_REFERENCE)
                //.getReference(Common.TOKEN_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .setValue(tokenModel)
                .addOnFailureListener(e -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show())
                .addOnSuccessListener(aVoid -> {

                });

    }

    public static void sendRequestToDriver(Context context, RelativeLayout main_layout,
                                           DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {

        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Log.d("Quocdev_id", foundDriver.getKey());

                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_TITLE);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for request driver action");
                            notificationData.put(Common.RIDER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());

                            notificationData.put(Common.RIDER_PICKUP_LOCATION_STRING, selectPlaceEvent.getOriginString());
                            notificationData.put(Common.RIDER_PICKUP_LOCATION, new StringBuilder("")
                                    .append(selectPlaceEvent.getOrigin().latitude)
                                    .append(",")
                                    .append(selectPlaceEvent.getOrigin().longitude)
                                    .toString());

                            notificationData.put(Common.RIDER_DESTINATION_STRING, selectPlaceEvent.getAddress());
                            notificationData.put(Common.RIDER_DESTINATION, new StringBuilder("")
                                    .append(selectPlaceEvent.getDestination().latitude)
                                    .append(",")
                                    .append(selectPlaceEvent.getDestination().longitude)
                                    .toString());

                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(main_layout, context.getString(R.string.request_driver_failed)
                                                    , Snackbar.LENGTH_LONG).show();
                                        }

                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(main_layout, throwable.getMessage()
                                                , Snackbar.LENGTH_LONG).show();

                                    }));


                        } else {

                            Snackbar.make(main_layout, context.getString(R.string.token_not_found)
                                    , Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });

    }

    public static void sendnotificationtouser(Context context, RelativeLayout main_layout,
                                              DriverGeoModel foundDriver, SelectPlaceEvent selectPlaceEvent) {


        CompositeDisposable compositeDisposable = new CompositeDisposable();
        IFCMService ifcmService = RetrofitFCMClient.getInstance().create(IFCMService.class);

        //get token
        FirebaseDatabase
                .getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(foundDriver.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()) {

                            Log.d("Quocdev_id", foundDriver.getKey());

                            TokenModel tokenModel = dataSnapshot.getValue(TokenModel.class);
                            Map<String, String> notificationData = new HashMap<>();
                            notificationData.put(Common.NOTI_TITLE, Common.SEND_NOTE);
                            notificationData.put(Common.NOTI_CONTENT, "This message represent for request driver action");
                            notificationData.put(Common.RIDER_KEY, FirebaseAuth.getInstance().getCurrentUser().getUid());



                            FCMSendData fcmSendData = new FCMSendData(tokenModel.getToken(), notificationData);

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(fcmResponse -> {
                                        if (fcmResponse.getSuccess() == 0) {
                                            compositeDisposable.clear();
                                            Snackbar.make(main_layout, context.getString(R.string.request_driver_failed)
                                                    , Snackbar.LENGTH_LONG).show();
                                        }

                                    }, throwable -> {
                                        compositeDisposable.clear();
                                        Snackbar.make(main_layout, throwable.getMessage()
                                                , Snackbar.LENGTH_LONG).show();

                                    }));


                        } else {

                            Snackbar.make(main_layout, context.getString(R.string.token_not_found)
                                    , Snackbar.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                });
    }
}

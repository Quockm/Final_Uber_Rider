package com.example.final_uber_rider.model;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.R;
import com.example.final_uber_rider.Remote.IGoogleAPI;
import com.example.final_uber_rider.model.EventBus.DriverAcceptTripEvent;
import com.example.final_uber_rider.model.EventBus.SelectPlaceEvent;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryItem> {

    private List<TripPlanModel> mTripHistory;
    private DatabaseReference databaseReference;
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;


    public HistoryAdapter(List<TripPlanModel> listTripHistory) {
        this.mTripHistory = listTripHistory;
    }

    @NonNull
    @NotNull
    @Override
    public HistoryItem onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        View itemHistory = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_item_history, parent, false);

        HistoryItem viewHolder = new HistoryItem(itemHistory);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull HistoryItem holder, int position) {
        TripPlanModel tripPlanModel = mTripHistory.get(position);
        GetTripInfo("",tripPlanModel);
        try {


            if (tripPlanModel != null) {

                String riderFullName = "";

                if (tripPlanModel.getDriverInfoModel() != null
                        && !tripPlanModel.getDriverInfoModel().getLastname().isEmpty()
                        && !tripPlanModel.getDriverInfoModel().getFisrtnasme().isEmpty()) {

                    riderFullName = tripPlanModel.getDriverInfoModel().getLastname()
                            + " " + tripPlanModel.getDriverInfoModel().getFisrtnasme();

                }

                holder.txt_rider_name.setText("Khong Minh Quoc");
                holder.txt_trip_datetime.setText("Sunday, 3-20-2020");
                holder.txt_trip_des_address.setText("123 Le Quang Dinh, Quan 1, TPHCM");
                holder.txt_trip_ori_address.setText("13 Tran Quang Khai, Quan 10, TPHCM");

                Log.d("QuocDev_tripplan", tripPlanModel.getDestinationString());


                if (tripPlanModel.isDone()) {
                    holder.txt_trip_status.setText("DONE");
                } else if (tripPlanModel.isCancel()) {
                    holder.txt_trip_status.setText("CANCEL");
                    holder.txt_trip_status.setTextColor(Color.RED);
                }
            }

        } catch (Exception e) {
            Log.e(HistoryAdapter.class.getName(), e.getClass() + ": " + e.getMessage());
        }
    }


    @Override
    public int getItemCount() {
        return mTripHistory.size();
    }

    static class HistoryItem extends RecyclerView.ViewHolder {

        @BindView(R.id.txt_trip_des_address)
        TextView txt_trip_des_address;

        @BindView(R.id.txt_rider_name)
        TextView txt_rider_name;

        @BindView(R.id.txt_trip_datetime)
        TextView txt_trip_datetime;

        @BindView(R.id.txt_trip_status)
        TextView txt_trip_status;

        @BindView(R.id.txt_trip_ori_address)
        TextView txt_trip_ori_address;

        public HistoryItem(@NonNull @NotNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }

    private void GetTripInfo(String tripIp, TripPlanModel tripPlanModel) {
        Query mDatabaseReference = FirebaseDatabase
                .getInstance()
                .getReference()
                .child("Trips");
        mDatabaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {

                if (snapshot.exists()) { // check exist


                }
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });

    }
}

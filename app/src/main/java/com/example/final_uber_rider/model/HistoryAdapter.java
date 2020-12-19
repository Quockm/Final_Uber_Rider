package com.example.final_uber_rider.model;

import android.graphics.Color;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.final_uber_rider.R;

import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryItem> {

    private List<TripPlanModel> mTripHistory;

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
        try{
            TripPlanModel trip = mTripHistory.get(position);

            if (trip != null){

                String riderFullName = "";

                if (trip.getRiderModel() != null
                        && !trip.getRiderModel().getLastname().isEmpty()
                        && !trip.getRiderModel().getFisrtnasme().isEmpty()) {

                    riderFullName = trip.getRiderModel().getLastname()
                            + " " + trip.getRiderModel().getFisrtnasme();

                }

                holder.txt_rider_name.setText(riderFullName);
                holder.txt_trip_datetime.setText(String.valueOf(new Date().getDate()));

                if (trip.isDone()){
                    holder.txt_trip_status.setText("DONE");
                }else if (trip.isCancel()){
                    holder.txt_trip_status.setText("CANCEL");
                    holder.txt_trip_status.setTextColor(Color.RED);
                }
            }

        }catch (Exception e){
            Log.e(HistoryAdapter.class.getName(), e.getClass() + ": " + e.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return mTripHistory.size();
    }

    static class HistoryItem extends RecyclerView.ViewHolder{

        @BindView(R.id.txt_trip_des_address)
        TextView txt_trip_des_address;

        @BindView(R.id.txt_rider_name)
        TextView txt_rider_name;

        @BindView(R.id.txt_trip_datetime)
        TextView txt_trip_datetime;

        @BindView(R.id.txt_trip_status)
        TextView txt_trip_status;

        public HistoryItem(@NonNull @NotNull View itemView) {
            super(itemView);

            ButterKnife.bind(this, itemView);
        }
    }
}

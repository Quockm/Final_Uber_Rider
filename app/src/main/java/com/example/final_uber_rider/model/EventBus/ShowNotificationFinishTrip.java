package com.example.final_uber_rider.model.EventBus;

public class ShowNotificationFinishTrip {
    private String TripKey;

    public ShowNotificationFinishTrip(String tripKey) {
        TripKey = tripKey;
    }

    public String getTripKey() {
        return TripKey;
    }

    public void setTripKey(String tripKey) {
        TripKey = tripKey;
    }
}

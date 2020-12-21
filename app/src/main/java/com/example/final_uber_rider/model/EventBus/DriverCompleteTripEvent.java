package com.example.final_uber_rider.model.EventBus;

public class DriverCompleteTripEvent {
    private String tripKey;

    public DriverCompleteTripEvent(String tripKey) {
        this.tripKey = tripKey;
    }

    public String getTripKey() {
        return tripKey;
    }

    public void setTripKey(String tripKey) {
        this.tripKey = tripKey;
    }
}

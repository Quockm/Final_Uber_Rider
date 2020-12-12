package com.example.final_uber_rider.model.EventBus;

public class DriverAcceptTripEvent {
    private String tripIp;

    public DriverAcceptTripEvent(String tripIp) {
        this.tripIp = tripIp;
    }

    public String getTripIp() {
        return tripIp;
    }

    public void setTripIp(String tripIp) {
        this.tripIp = tripIp;
    }
}

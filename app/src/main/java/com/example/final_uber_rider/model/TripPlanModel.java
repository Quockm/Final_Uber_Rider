package com.example.final_uber_rider.model;

public class TripPlanModel {
    private String rider,driver;
    private DriverInfoModel driverInfoModel;
    private RiderInfoModel riderModel;
    private String origin,originString;
    private String destination,destinationString;
    private String distancePickup, distanceDestination;
    private String durationPickup, durationDestination;
    private double currentLat, currentLng;
    private boolean isDone, isCancel;
    private String time;



    public TripPlanModel() {
    }

    public TripPlanModel(String rider, String driver, DriverInfoModel driverInfoModel,
                         RiderInfoModel riderModel, String origin, String originString,
                         String destination, String destinationString, String distancePickup,
                         String distanceDestination, String durationPickup, String durationDestination, String time,
                         double currentLat, double currentLng, boolean isDone, boolean isCancel) {
        this.rider = rider;
        this.driver = driver;
        this.driverInfoModel = driverInfoModel;
        this.riderModel = riderModel;
        this.origin = origin;
        this.originString = originString;
        this.destination = destination;
        this.destinationString = destinationString;
        this.distancePickup = distancePickup;
        this.distanceDestination = distanceDestination;
        this.durationPickup = durationPickup;
        this.durationDestination = durationDestination;
        this.currentLat = currentLat;
        this.currentLng = currentLng;
        this.isDone = isDone;
        this.isCancel = isCancel;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getRider() {
        return rider;
    }

    public void setRider(String rider) {
        this.rider = rider;
    }

    public String getDriver() {
        return driver;
    }

    public void setDriver(String driver) {
        this.driver = driver;
    }

    public DriverInfoModel getDriverInfoModel() {
        return driverInfoModel;
    }

    public void setDriverInfoModel(DriverInfoModel driverInfoModel) {
        this.driverInfoModel = driverInfoModel;
    }

    public RiderInfoModel getRiderModel() {
        return riderModel;
    }

    public void setRiderModel(RiderInfoModel riderModel) {
        this.riderModel = riderModel;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getOriginString() {
        return originString;
    }

    public void setOriginString(String originString) {
        this.originString = originString;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDestinationString() {
        return destinationString;
    }

    public void setDestinationString(String destinationString) {
        this.destinationString = destinationString;
    }

    public String getDistancePickup() {
        return distancePickup;
    }

    public void setDistancePickup(String distancePickup) {
        this.distancePickup = distancePickup;
    }

    public String getDistanceDestination() {
        return distanceDestination;
    }

    public void setDistanceDestination(String distanceDestination) {
        this.distanceDestination = distanceDestination;
    }

    public String getDurationPickup() {
        return durationPickup;
    }

    public void setDurationPickup(String durationPickup) {
        this.durationPickup = durationPickup;
    }

    public String getDurationDestination() {
        return durationDestination;
    }

    public void setDurationDestination(String durationDestination) {
        this.durationDestination = durationDestination;
    }

    public double getCurrentLat() {
        return currentLat;
    }

    public void setCurrentLat(double currentLat) {
        this.currentLat = currentLat;
    }

    public double getCurrentLng() {
        return currentLng;
    }

    public void setCurrentLng(double currentLng) {
        this.currentLng = currentLng;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public boolean isCancel() {
        return isCancel;
    }

    public void setCancel(boolean cancel) {
        isCancel = cancel;
    }




}


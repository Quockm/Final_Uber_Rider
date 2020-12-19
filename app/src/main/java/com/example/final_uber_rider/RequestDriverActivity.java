package com.example.final_uber_rider;

import android.animation.ValueAnimator;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.Remote.IGoogleAPI;
import com.example.final_uber_rider.Remote.RetrofitClient;
import com.example.final_uber_rider.model.DriverGeoModel;
import com.example.final_uber_rider.model.EventBus.DeclineRequestAndRemoveTripFromDriver;
import com.example.final_uber_rider.model.EventBus.DeclineRequestFromDriver;
import com.example.final_uber_rider.model.EventBus.DriverAcceptTripEvent;
import com.example.final_uber_rider.model.EventBus.SelectPlaceEvent;
import com.example.final_uber_rider.model.TripPlanModel;
import com.example.final_uber_rider.utils.RiderUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RequestDriverActivity extends FragmentActivity implements OnMapReadyCallback {

    TextView txt_time;
    TextView txt_origin;

    //Slowly camera spining
    private ValueAnimator animator;
    private static final int DESIRED_NUM_OF_SPINS = 5;
    private static final int DESIRED_SECONDS_PER_ONE_FULL_360_SPIN = 40;
    //effects
    private Circle lastUserCircle;
    private final long duration = 1000;
    private ValueAnimator lastPulseAnimator;

    //View
    @BindView(R.id.main_layout)
    RelativeLayout main_layout;
    @BindView(R.id.finding_your_ride_layout)
    CardView finding_your_ride_layout;
    @BindView(R.id.confirm_uber_layout)
    CardView confirm_uber_layout;
    @BindView(R.id.btn_confirm_uber)
    Button btn_confirm_uber;
    @BindView(R.id.confirm_pickup_layout)
    CardView confirm_pickup_layout;
    @BindView(R.id.btn_confirm_pickup)
    Button btn_confirm_layout;
    @BindView(R.id.txt_pickup_adrress)
    TextView txt_pickup_adrress;

    @BindView(R.id.txt_distance_trip)
    TextView txt_distance_trip;
    @BindView(R.id.txt_trip_fee)
    TextView txt_trip_fee;

    @BindView(R.id.driver_info_layout)
    CardView driver_info_layout;
    @BindView(R.id.txt_driver_name)
    TextView driver_name;
    @BindView(R.id.img_driver)
    ImageView img_driver;

    @BindView(R.id.fill_map)
    View fill_map;
    private DriverGeoModel lastDriverCall;
    private String driverOldPosition = "";
    private Handler handler;
    private float v;
    private double lat, lng;
    private int index, next;
    private LatLng start, end;


    @OnClick(R.id.btn_confirm_uber)
    void onConfirmUber() {
        confirm_pickup_layout.setVisibility(View.VISIBLE); // Show pickup Layout
        confirm_uber_layout.setVisibility(View.GONE); //Hide Uber Layout

        setDataPickup();

    }

    @OnClick({R.id.btn_confirm_pickup})
    void onConfirmPickup() {
        if (mMap == null) return;
        if (selectPlaceEvent == null) return;


        //clear map
        mMap.clear();
        //Titls
        CameraPosition cameraPosition = CameraPosition.builder()
                .target(selectPlaceEvent.getOrigin())
                .tilt(45f)
                .zoom(16f)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

        //Start Animation
        addMarkerWithPulseAnimation();
    }

    private void addMarkerWithPulseAnimation() {
        confirm_pickup_layout.setVisibility(View.GONE);

        fill_map.setVisibility(View.VISIBLE);

        finding_your_ride_layout.setVisibility(View.VISIBLE);

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.defaultMarker())
                .position(selectPlaceEvent.getOrigin()));

        addPulsatingEffect(selectPlaceEvent);
    }

    private void addPulsatingEffect(SelectPlaceEvent selectPlaceEvent) {
        if (lastPulseAnimator != null) lastPulseAnimator.cancel();
        if (lastUserCircle != null) lastUserCircle.setCenter(selectPlaceEvent.getOrigin());

        lastPulseAnimator = Common.valueAnimate(duration, animation -> {
            if (lastUserCircle != null)
                lastUserCircle.setRadius((Float) animation.getAnimatedValue());
            else {
                lastUserCircle = mMap.addCircle(new CircleOptions()
                        .center(selectPlaceEvent.getOrigin())
                        .radius((Float) animation.getAnimatedValue())
                        .strokeColor(Color.WHITE)
                        .fillColor(Color.parseColor("#33333333"))
                );
            }
        });

        startMapCameraSpinningAnimation(selectPlaceEvent);

    }

    private void startMapCameraSpinningAnimation(SelectPlaceEvent selectPlaceEvent) {
        if (animator != null) animator.cancel();
        animator = ValueAnimator.ofFloat(0, DESIRED_NUM_OF_SPINS * 360);
        animator.setDuration(DESIRED_SECONDS_PER_ONE_FULL_360_SPIN * DESIRED_NUM_OF_SPINS * 1000);
        animator.setInterpolator(new LinearInterpolator());
        animator.setStartDelay(100);
        animator.addUpdateListener(valueAnimator -> {
            Float newBearingValue = (Float) valueAnimator.getAnimatedValue();
            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                    .target(selectPlaceEvent.getOrigin())
                    .zoom(16f)
                    .tilt(45f)
                    .bearing(newBearingValue)
                    .build()));
        });
        animator.start();

        //After start animation, find driver
        findNearbyDriver(selectPlaceEvent);
    }

    private void findNearbyDriver(SelectPlaceEvent selectPlaceEvent) {
        if (Common.driverfound.size() > 0) {

            Log.d("Nhi_Request", "Driver list size: " + Common.driverfound.size());

            float min_distance = 0; //default min distance = 0
            DriverGeoModel foundDriver = null;
            Location currentRiderLocation = new Location("");
            currentRiderLocation.setLatitude(selectPlaceEvent.getOrigin().latitude);
            currentRiderLocation.setLongitude(selectPlaceEvent.getOrigin().longitude);

            for (String key : Common.driverfound.keySet()) {
                Log.e("Nhi_Request", key);
                Location driverLocation = new Location("");
                driverLocation.setLatitude(Common.driverfound.get(key).getGeoLocation().latitude);
                driverLocation.setLongitude(Common.driverfound.get(key).getGeoLocation().longitude);

                //Compare 2 Location
                if (min_distance == 0) {

                    min_distance = driverLocation.distanceTo(currentRiderLocation);//first default min_distance

                    if (!Common.driverfound.get(key).isDecline()) // if not decline before
                    {
                        foundDriver = Common.driverfound.get(key);
                        break; // exit loop because  we found driver
                    } /*else
                        continue;*/ // if already decline before,just skip and continue

                } else if (driverLocation.distanceTo(currentRiderLocation) < min_distance) {

                    //if have any driver driver smaller min_distance, get all!
                    min_distance = driverLocation.distanceTo(currentRiderLocation);//first default min_distance

                    if (!Common.driverfound.get(key).isDecline()) // if not decline before
                    {
                        foundDriver = Common.driverfound.get(key);
                        break; // exit loop because  we found driver
                    }/* else
                        continue; */// if already decline before,just skip and continue

//                Founded driver!
//                Snackbar.make(main_layout, new StringBuilder("Found driver: ")
//                                .append(foundDriver.getDriverInfoModel().getPhonenumber()),
//                        Snackbar.LENGTH_LONG).show();
                }
            }
            //after loop,
            if (foundDriver != null) {
                RiderUtils.sendRequestToDriver(this, main_layout, foundDriver, selectPlaceEvent);
                lastDriverCall = foundDriver;

            } else {
                Toast.makeText(this, getString(R.string.There_are_no_driver_accpect_request), Toast.LENGTH_LONG)
                        .show();
                lastDriverCall = null;
                finish();
            }
        } else {
            Log.e("Nhi_Request", "Driver list is EMPTY");
            //Not found
            Snackbar.make(main_layout, getString(R.string.drivers_not_found), Snackbar.LENGTH_LONG)
                    .show();
            lastDriverCall = null;
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        if (animator != null) animator.end();
        super.onDestroy();
    }

    private void setDataPickup() {
        txt_pickup_adrress.setText(txt_origin != null ? txt_origin.getText() : "None");
        mMap.clear(); /// clear all on map
        //add PickupMarker
        addPickupMarker();
    }

    private void addPickupMarker() {
        View view = getLayoutInflater().inflate(R.layout.pickup_info_windows, null);


        //Create Icon for Marker
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getDestination()));
    }

    private GoogleMap mMap;

    private SelectPlaceEvent selectPlaceEvent;

    //Routes
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private Marker originMarker, destinationMarker;

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class))
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);

        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestFromDriver.class);

        if (EventBus.getDefault().hasSubscriberForEvent(DriverAcceptTripEvent.class))
            EventBus.getDefault().removeStickyEvent(DriverAcceptTripEvent.class);

        if (EventBus.getDefault().hasSubscriberForEvent(DeclineRequestAndRemoveTripFromDriver.class))
            EventBus.getDefault().removeStickyEvent(DeclineRequestAndRemoveTripFromDriver.class);

        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDriverAcceptEvent(DriverAcceptTripEvent event) {


        //Get Trip Information
        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(event.getTripIp())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            TripPlanModel tripPlanModel = snapshot.getValue(TripPlanModel.class);
                            mMap.clear();
                            fill_map.setVisibility(View.GONE);
                            if (animator != null) animator.end();
                            CameraPosition cameraPosition = new CameraPosition.Builder()
                                    .target(mMap.getCameraPosition().target)
                                    .tilt(0f)
                                    .zoom(mMap.getCameraPosition().zoom)
                                    .build();
                            mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                            //Get Routes
                            String driverLocation = new StringBuilder()
                                    .append(tripPlanModel.getCurrentLat())
                                    .append(",")
                                    .append(tripPlanModel.getCurrentLng())
                                    .toString();

                            //request an api
                            compositeDisposable.add(iGoogleAPI.getDirections("driving",
                                    "less_driving",
                                    tripPlanModel.getOrigin(), driverLocation,
                                    getString(R.string.google_api_key))
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe(returnResult -> {

                                        PolylineOptions blackPolylineOptions = null;
                                        List<LatLng> polylineList = null;
                                        Polyline blackPolyline = null;

                                        try {
                                            //parse json
                                            JSONObject jsonObject = new JSONObject(returnResult);
                                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
                                            for (int i = 0; i < jsonArray.length(); i++) {
                                                JSONObject route = jsonArray.getJSONObject(i);
                                                JSONObject poly = route.getJSONObject("overview_polyline");
                                                String polyline = poly.getString("points");
                                                polylineList = Common.decodePoly(polyline);

                                            }

                                            blackPolylineOptions = new PolylineOptions();
                                            blackPolylineOptions.color(Color.BLACK);
                                            blackPolylineOptions.width(5);
                                            blackPolylineOptions.startCap(new SquareCap());
                                            blackPolylineOptions.jointType(JointType.ROUND);
                                            blackPolylineOptions.addAll(polylineList);
                                            blackPolyline = mMap.addPolyline(blackPolylineOptions);

                                            JSONObject object = jsonArray.getJSONObject(0);
                                            JSONArray legs = object.getJSONArray("legs");
                                            JSONObject legObjects = legs.getJSONObject(0);

                                            JSONObject time = legObjects.getJSONObject("duration");
                                            String duration = time.getString("text");

                                            JSONObject distanceEstimate = legObjects.getJSONObject("distance");
                                            String distance = distanceEstimate.getString("text");



                                            LatLng origin = new LatLng(
                                                    Double.parseDouble(tripPlanModel.getOrigin().split(",")[0]),
                                                    Double.parseDouble(tripPlanModel.getOrigin().split(",")[1]));


                                            LatLng destination = new LatLng(tripPlanModel.getCurrentLat()
                                                    , tripPlanModel.getCurrentLng());

                                            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                                    .include(origin)
                                                    .include(destination)
                                                    .build();



                                            addPickupMarkerWithDuration(duration, origin);
                                            addDriverMarker(destination);

                                            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                                            mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 2));

                                            initDriverForMoving(event.getTripIp(), tripPlanModel);

                                            //Load driver avatar
                                            Glide.with(RequestDriverActivity.this)
                                                    .load(tripPlanModel.getDriverInfoModel().getAvatar())
                                                    .into(img_driver);
                                            driver_name.setText(tripPlanModel.getDriverInfoModel().getFisrtnasme());

                                            confirm_pickup_layout.setVisibility(View.GONE);
                                            confirm_uber_layout.setVisibility(View.GONE);
                                            driver_info_layout.setVisibility(View.VISIBLE);


                                        } catch (Exception e) {
                                            //Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                                            Toast.makeText(RequestDriverActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();

                                        }
                                    })
                            );

                        } else
                            Snackbar.make(main_layout, getString(R.string.trip_not_found) + event.getTripIp(),
                                    Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void initDriverForMoving(String tripIp, TripPlanModel tripPlanModel) {
        driverOldPosition = new StringBuilder()
                .append(tripPlanModel.getCurrentLat())
                .append(",")
                .append(tripPlanModel.getCurrentLng())
                .toString();

        FirebaseDatabase.getInstance()
                .getReference(Common.TRIP)
                .child(tripIp)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                        TripPlanModel newData = snapshot.getValue(TripPlanModel.class);

                        String driverNewLocation = new StringBuilder()
                                .append(newData.getCurrentLat())
                                .append(",")
                                .append(newData.getCurrentLng())
                                .toString();

                        if (!driverOldPosition.equals(driverNewLocation)) // if not equal
                            moveMarkerAnimation(destinationMarker, driverOldPosition, driverNewLocation);

                    }

                    @Override
                    public void onCancelled(@NonNull @NotNull DatabaseError error) {
                        Snackbar.make(main_layout, error.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void moveMarkerAnimation(Marker marker, String from, String to) {
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                from, to,
                getString(R.string.google_api_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {

                    try {
                        //parse json
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");

                            polylineList = Common.decodePoly(polyline);
                        }

                        //moving
                        handler = new Handler();
                        index = -1;
                        next = -1;
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (index < polylineList.size() - 2) {

                                    index++;
                                    next = index + 1;
                                    start = polylineList.get(index);
                                    end = polylineList.get(next);

                                }

                                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                                valueAnimator.setDuration(1500);
                                valueAnimator.setInterpolator(new LinearInterpolator());
                                valueAnimator.addUpdateListener(valueAnimator1 -> {
                                    v = valueAnimator1.getAnimatedFraction();
                                    lng = v * end.longitude + (1 - v) * start.longitude;
                                    lat = v * end.latitude + (1 - v) * start.latitude;
                                    LatLng newPos = new LatLng(lat, lng);
                                    marker.setPosition(newPos);
                                    marker.setAnchor(0.5f, 0.5f);
                                    marker.setRotation(Common.getBearing(start, newPos));

                                    mMap.moveCamera(CameraUpdateFactory.newLatLng(newPos));
                                });

                                valueAnimator.start();
                                if (index < polylineList.size() - 2)
                                    handler.postDelayed( this, 1500);
                                else if (index < polylineList.size() - 1) {

                                }

                            }
                        }, 1500);

                        driverOldPosition = to; // set new driver position
                    } catch (Exception e) {
                        Snackbar.make(main_layout, e.getMessage(), Snackbar.LENGTH_LONG).show();
                    }
                }, throwable -> {
                    if (throwable != null)
                        Snackbar.make(main_layout, throwable.getMessage(), Snackbar.LENGTH_SHORT)
                                .show();
                })
        );
    }

    private void addDriverMarker(LatLng destination) {
        destinationMarker = mMap.addMarker(new MarkerOptions().position(destination).flat(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.car)));
    }

    private void addPickupMarkerWithDuration(String duration, LatLng origin) {
        Bitmap icon = Common.createIconWithDuration(this, duration);
        originMarker = mMap.addMarker(new MarkerOptions().icon(BitmapDescriptorFactory.fromBitmap(icon)).position(origin));
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event) {
        selectPlaceEvent = event;
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineRequestEvent(DeclineRequestFromDriver event) {
        if (lastDriverCall != null) {
            Common.driverfound.get(lastDriverCall.getKey()).setDecline(true);

            //Driver has been declined request, just find a new one
            findNearbyDriver(selectPlaceEvent);
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onDeclineAndRemoveRequestEvent(DeclineRequestAndRemoveTripFromDriver event) {
        if (lastDriverCall != null) {
            Common.driverfound.get(lastDriverCall.getKey()).setDecline(true);

            //Driver has been declined request, just finish this activity
            finish();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
        init();
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


    }

    private void init() {
        ButterKnife.bind(this);
        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
//
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
//                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            //Snackbar.make(this, getString(R.string.permisson_require), Snackbar.LENGTH_SHORT).show();
//            return;
//        }
//        mMap.setMyLocationEnabled(true);
//        mMap.getUiSettings().setMyLocationButtonEnabled(true);
//        mMap.setOnMyLocationButtonClickListener(() -> {
//
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(selectPlaceEvent.getOrigin(), 18f));
//            return true;
//        });

        drawPath(selectPlaceEvent);

//        //set layout buttons
//        View locationButton = ((View) findViewById(Integer.parseInt("1"))
//                .getParent())
//                .findViewById(Integer.parseInt("2"));
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
//        //Right bottom
//        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
//        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
//        params.setMargins(0, 0, 0, 350); // move view to zoom Control
//
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.uber_maps_style));
            if (!success)
                Toast.makeText(this, "Load map style failed", Toast.LENGTH_SHORT).show();
        } catch (Resources.NotFoundException e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void drawPath(SelectPlaceEvent selectPlaceEvent) {
        //request an api
        compositeDisposable.add(iGoogleAPI.getDirections("driving",
                "less_driving",
                selectPlaceEvent.getOriginString(), selectPlaceEvent.getDestinationString(),
                getString(R.string.google_api_key))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(returnResult -> {
                    Log.d("API_RETURN", returnResult);

                    try {
                        //parse json
                        JSONObject jsonObject = new JSONObject(returnResult);
                        JSONArray jsonArray = jsonObject.getJSONArray("routes");
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject route = jsonArray.getJSONObject(i);
                            JSONObject poly = route.getJSONObject("overview_polyline");
                            String polyline = poly.getString("points");
                            polylineList = Common.decodePoly(polyline);

                        }
                        polylineOptions = new PolylineOptions();
                        polylineOptions.color(Color.YELLOW);
                        polylineOptions.width(12);
                        polylineOptions.startCap(new SquareCap());
                        polylineOptions.jointType(JointType.ROUND);
                        polylineOptions.addAll(polylineList);
                        greyPolyline = mMap.addPolyline(polylineOptions);

                        blackPolylineOptions = new PolylineOptions();
                        blackPolylineOptions.color(Color.BLACK);
                        blackPolylineOptions.width(5);
                        blackPolylineOptions.startCap(new SquareCap());
                        blackPolylineOptions.jointType(JointType.ROUND);
                        blackPolylineOptions.addAll(polylineList);
                        blackPolyline = mMap.addPolyline(blackPolylineOptions);


                        //Animator
                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 100);
                        valueAnimator.setDuration(1100);
                        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
                        valueAnimator.setInterpolator(new LinearInterpolator());
                        valueAnimator.addUpdateListener(value -> {
                            List<LatLng> points = greyPolyline.getPoints();
                            int percentValue = (int) value.getAnimatedValue();
                            int size = points.size();
                            int newPoints = (int) (size * (percentValue / 100.0f));
                            List<LatLng> p = points.subList(0, newPoints);
                            blackPolyline.setPoints(p);
                        });

                        valueAnimator.start();

                        LatLngBounds latLngBounds = new LatLngBounds.Builder()
                                .include(selectPlaceEvent.getOrigin())
                                .include(selectPlaceEvent.getDestination())
                                .build();

                        //add car icon for origin
                        JSONObject object = jsonArray.getJSONObject(0);
                        JSONArray legs = object.getJSONArray("legs");
                        JSONObject legObjects = legs.getJSONObject(0);

                        JSONObject time = legObjects.getJSONObject("duration");
                        String duration = time.getString("text");

                        String start_address = legObjects.getString("start_address");
                        String end_address = legObjects.getString("end_address");

                        addOriginMarker(start_address, duration);

                        addDestinationMarker(end_address);

                        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 160));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 2));

                        String tripDistanceText, tripFeeText;
                        double distanceValue, tripFeeValue;
                        tripDistanceText = "Your Trip: " + legObjects.getJSONObject("distance").getString("text");
                        distanceValue = legObjects.getJSONObject("distance").getDouble("value");

                        tripFeeValue = (distanceValue / 1000) * 15000;
                        tripFeeText = "The Fee: " + Common.formatDecimal(tripFeeValue, "#,##0d",
                                '.', ',');

                        if (txt_distance_trip != null && txt_trip_fee != null) {
                            txt_distance_trip.setText(tripDistanceText);
                            txt_trip_fee.setText(tripFeeText);
                        }


                    } catch (Exception e) {
                        //Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();

                    }
                })
        );
    }

    private void addDestinationMarker(String end_address) {
        View view = getLayoutInflater().inflate(R.layout.destination_info_windows, null);

        TextView txt_destination = (TextView) view.findViewById(R.id.txt_destination);

        txt_destination.setText(Common.formatAdrress(end_address));

        //Create Icon for Marker
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        destinationMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getDestination()));
    }

    private void addOriginMarker(String start_address, String duration) {
        View view = getLayoutInflater().inflate(R.layout.origin_info_windows, null);

        txt_time = (TextView) view.findViewById(R.id.txt_time);
        txt_origin = (TextView) view.findViewById(R.id.txt_origin);

        txt_time.setText(Common.formatDuration(duration));
        txt_origin.setText(Common.formatAdrress(start_address));

        //Create Icon for Marker
        IconGenerator generator = new IconGenerator(this);
        generator.setContentView(view);
        generator.setBackground(new ColorDrawable(Color.TRANSPARENT));
        Bitmap icon = generator.makeIcon();

        originMarker = mMap.addMarker(new MarkerOptions()
                .icon(BitmapDescriptorFactory.fromBitmap(icon))
                .position(selectPlaceEvent.getOrigin()));

    }

}
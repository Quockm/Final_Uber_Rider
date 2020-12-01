package com.example.final_uber_rider.ui.home;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.EventLog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.final_uber_rider.Callback.IFireFailedListener;
import com.example.final_uber_rider.Callback.IFirebaseDriverInfoListener;
import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.R;
import com.example.final_uber_rider.Remote.IGoogleAPI;
import com.example.final_uber_rider.Remote.RetrofitClient;
import com.example.final_uber_rider.RequestDriverActivity;
import com.example.final_uber_rider.model.AnimationModel;
import com.example.final_uber_rider.model.DriverGeoModel;
import com.example.final_uber_rider.model.DriverInfoModel;
import com.example.final_uber_rider.model.EventBus.SelectPlaceEvent;
import com.example.final_uber_rider.model.GeoQueryModel;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, IFirebaseDriverInfoListener, IFireFailedListener {

    @BindView(R.id.sliding_layout)
    SlidingUpPanelLayout slidingUpPanelLayout;
    @BindView(R.id.txtMes_Welcome)
    TextView txtMesWelcome;

    private GoogleMap mMap;

    private AutocompleteSupportFragment autocompleteSupportFragment;


    private HomeViewModel homeViewModel;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    SupportMapFragment mapFragment;


    //Load Driver
    private double dinstance = 1.0;
    private static final double LIMIT_RANGE = 10.0;// < 10m
    private Location previousLocation, currentLocation;//use to calculate distance

    private boolean firsttime = true;


    //Listener
    IFirebaseDriverInfoListener iFirebaseDriverInfoListener;
    IFireFailedListener iFireFailedListener;
    private String cityname;

    //
    private final CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;


    //Moving marker
    private List<LatLng> polylinelist;
    private Handler handler;
    private int index, next;
    private LatLng start, end;
    private float v;
    private double lat, lng;

//    private boolean isFirstTime = true;


    @Override
    public void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    //Online system
    DatabaseReference onlineRef, currentRiderRef, riderLocationRef;
    GeoFire geoFire;

    ValueEventListener onlineValueListener = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            if (snapshot.exists() && currentLocation != null) {
                currentRiderRef.onDisconnect().removeValue();
//                isFirstTime = true;
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                    .show();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);




        //Obtain the SupportMapFragment and get notified when the map is ready to used
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        initView(root);
        init();
        return root;
    }

    private void initView(View root) {
        ButterKnife.bind(this, root);

        Common.setMesWellcome(txtMesWelcome);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        //geoFire.removeLocation(FirebaseAuth.getInstance().getCurrentUser().getUid());
        onlineRef.removeEventListener(onlineValueListener);
    }

    @Override
    public void onResume() {
        registerOnlineSystem();
        geoFire.getLocation("new location", new com.firebase.geofire.LocationCallback() {
            @Override
            public void onLocationResult(String key, GeoLocation location) {
                if (location != null) {
                    //set marker to display on map
                    FirebaseAuth.getInstance().getCurrentUser().getUid();
                } else {
                    //When location is null
                    //Toast.makeText(getContext(), "Error",Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Error", Toast.LENGTH_SHORT).show();

            }
        });
        super.onResume();
    }

    private void registerOnlineSystem() {
        onlineRef.addValueEventListener(onlineValueListener);
    }

    private void init() {

        Places.initialize(getContext(), getString(R.string.google_api_key));
        autocompleteSupportFragment = (AutocompleteSupportFragment) getChildFragmentManager()
                .findFragmentById(R.id.autocomplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID
                , Place.Field.ADDRESS
                , Place.Field.NAME
                , Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.where_to));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                //Snackbar.make(getView(), "" + place.getLatLng(), Snackbar.LENGTH_LONG).show();
                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                fusedLocationProviderClient.getLastLocation()
                        .addOnSuccessListener(location -> {

                            LatLng origin = new LatLng(location.getLatitude(), location.getLongitude());
                            LatLng destination = new LatLng(place.getLatLng().latitude, place.getLatLng().longitude);

                            startActivity(new Intent(getContext(), RequestDriverActivity.class));
                            EventBus.getDefault().postSticky(new SelectPlaceEvent(origin, destination));

                        });
            }

            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(), "" + status.getStatusMessage(), Snackbar.LENGTH_LONG).show();
            }
        });

        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);


        iFirebaseDriverInfoListener = this;
        iFireFailedListener = this;

        onlineRef = FirebaseDatabase.getInstance().getReference().child(".info/connected");
        riderLocationRef = FirebaseDatabase.getInstance().getReference(Common.RIDER_LOCATION_REFERENCE);
        currentRiderRef = FirebaseDatabase.getInstance().getReference(Common.RIDER_LOCATION_REFERENCE)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        geoFire = new GeoFire(riderLocationRef);

        buildLocationRequest();

        buildLocationCallback();

        updateLocation();
        //load end all driver
        loadAvaiableDrivers();

    }

    private void updateLocation() {
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    private void buildLocationCallback() {
        if(locationCallback == null)
        {
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude()
                            , locationResult.getLastLocation().getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                    //if user change location, calculate and load driver again
                    if (firsttime) {
                        previousLocation = currentLocation = locationResult.getLastLocation();
                        firsttime = false;

                        setRestricPlacesInCountry(locationResult.getLastLocation());
                    } else {
                        previousLocation = currentLocation;
                        currentLocation = locationResult.getLastLocation();
                    }

                    if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE)//not over the range
                        loadAvaiableDrivers();
                    else {
                        // do nothing
                    }

                    //update Location
                    geoFire.setLocation(FirebaseAuth.getInstance().getCurrentUser().getUid(),
                            new GeoLocation(locationResult.getLastLocation().getLatitude(),
                                    locationResult.getLastLocation().getLongitude()),
                            (key, error) -> {
                                if (error != null)
                                    Snackbar.make(mapFragment.getView(), error.getMessage(), Snackbar.LENGTH_LONG)
                                            .show();
//                            else {
//                                if (isFirstTime) {
//                                    Snackbar.make(mapFragment.getView(), "You're Online", Snackbar.LENGTH_LONG)
//                                            .show();
//                                    isFirstTime = false;
//                                }
//                            }
                            });
                }
            };
        }
    }

    private void buildLocationRequest() {
        if(locationRequest == null)
        {
            locationRequest = new LocationRequest();
            locationRequest.setSmallestDisplacement(10f);
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(3000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        }
    }

    private void setRestricPlacesInCountry(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addressList.size() > 0)
                autocompleteSupportFragment.setCountry(addressList.get(0).getCountryCode());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void loadAvaiableDrivers() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(e -> Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show())
                .addOnSuccessListener(location -> {
                    // load all drivers
                    Context context;
                    Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                    List<Address> addressList;
                    try {
                        addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        if (addressList.size() > 0)
                            cityname = addressList.get(0).getLocality();
                        if (!TextUtils.isEmpty(cityname)) {
                            //Query
                            DatabaseReference driver_location_ref = FirebaseDatabase.getInstance()
                                    .getReference(Common.DRIVER_LOCATION_REFERENCE)
                                    .child(cityname);
                            GeoFire gf = new GeoFire(driver_location_ref);
                            GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                    location.getLongitude()), dinstance);
                            geoQuery.removeAllListeners();

                            geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                @Override
                                public void onKeyEntered(String key, GeoLocation location) {
                                    //Common.driverfound.add(new DriverGeoModel(key, location));
                                    if(!Common.driverfound.containsKey(key))
                                        Common.driverfound.put(key,new DriverGeoModel(key,location));//add if it not exists

                                }

                                @Override
                                public void onKeyExited(String key) {

                                }

                                @Override
                                public void onKeyMoved(String key, GeoLocation location) {

                                }

                                @Override
                                public void onGeoQueryReady() {
                                    if (dinstance <= LIMIT_RANGE) {
                                        dinstance++;
                                        loadAvaiableDrivers(); // continue searching in new distance
                                    } else {
                                        dinstance = 1.0; // reset it
                                        addDriverMarker();

                                    }
                                }

                                @Override
                                public void onGeoQueryError(DatabaseError error) {
                                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }
                            });

                            //Listen to new driver in city and range
                            driver_location_ref.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    //Have new driver
                                    GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                                    GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                            geoQueryModel.getL().get(1));
                                    DriverGeoModel driverGeoModel = new DriverGeoModel(snapshot.getKey(),
                                            geoLocation);
                                    Location newDriverLocation = new Location("");
                                    newDriverLocation.setLatitude(geoLocation.latitude);
                                    newDriverLocation.setLongitude(geoLocation.longitude);
                                    float newDistance = location.distanceTo(newDriverLocation) / 1000; //in km
                                    if (newDistance <= LIMIT_RANGE)
                                        findDriverByKey(driverGeoModel); // if driver in range, go to map

                                }

                                @Override
                                public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                }

                                @Override
                                public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {

                                }
                            });

                        } else
                            Snackbar.make(getView(), getString(R.string.city_name_empty), Snackbar.LENGTH_SHORT).show();

                    } catch (IOException e) {
                        e.printStackTrace();
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void addDriverMarker() {
        if (Common.driverfound.size() > 0) {
            Observable.fromIterable(Common.driverfound.keySet())
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(key -> {
                        //On next call
                        findDriverByKey(Common.driverfound.get(key));
                    }, throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_SHORT).show();

                    }, () -> {
                    });


        } else {
            Snackbar.make(getView(), getString(R.string.drivers_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChildren()) {
                            driverGeoModel.setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            Common.driverfound.get(driverGeoModel.getKey()).setDriverInfoModel(snapshot.getValue(DriverInfoModel.class));
                            iFirebaseDriverInfoListener.onDriverInfoLoadSuccess(driverGeoModel);
                        } else {
                            iFireFailedListener.onFireLoadFailed(getString(R.string.key_no_found) + driverGeoModel.getKey());
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        iFireFailedListener.onFireLoadFailed(error.getMessage());

                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Check Permission
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            Snackbar.make(getView(), getString(R.string.permisson_require), Snackbar.LENGTH_SHORT).show();
                            return;

                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(() -> {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnFailureListener(e -> Toast.makeText(getContext(), "" + e.getMessage(), Toast.LENGTH_SHORT).show())
                                    .addOnSuccessListener(location -> {
                                        LatLng userLacing = new LatLng(location.getLatitude(), location.getLongitude());
                                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLacing, 18f));
                                    });
                            return true;
                        });

                        //set layout buttons
                        View locationButton = ((View) mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //Right bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0, 0, 0, 350); // move view to zoom Control

                        //update location
                        buildLocationRequest();

                        buildLocationCallback();

                        updateLocation();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Toast.makeText(getContext(), "Permission" + permissionDeniedResponse.getPermissionName() + "" +
                                "was denied!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                }).check();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.uber_maps_style));
            if (!success)
                Log.e("STYLE ERROR", "Style parsing error");
        } catch (Resources.NotFoundException e) {
            Log.e("STYLE ERROR", e.getMessage());
        }
    }

    @Override
    public void onFireLoadFailed(String message) {
        Snackbar.make(getView(), message, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {
        // if it already have marker this key, doesn't do again
        if (!Common.makerList.containsKey(driverGeoModel.getKey()))
            Common.makerList.put(driverGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(driverGeoModel.getGeoLocation().latitude,
                                    driverGeoModel.getGeoLocation().longitude))
                            .flat(true)
                            .title(Common.buildName(driverGeoModel.getDriverInfoModel().getFisrtnasme(),
                                    driverGeoModel.getDriverInfoModel().getLastname()))
                            .snippet(driverGeoModel.getDriverInfoModel().getPhonenumber())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));


        if (!TextUtils.isEmpty(cityname)) {
            DatabaseReference drivelocation = FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVER_LOCATION_REFERENCE)
                    .child(cityname)
                    .child(driverGeoModel.getKey());
            drivelocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.hasChildren()) {
                        if (Common.makerList.get(driverGeoModel.getKey()) != null)
                            Common.makerList.get(driverGeoModel.getKey()).remove();//remove makers
                        Common.makerList.remove(driverGeoModel.getKey());//remove marker info from hash map
                        Common.driverLocationSubcribe.remove(driverGeoModel.getKey());//remove driver information
                        drivelocation.removeEventListener(this);// remove eventListener
                    } else {
                        if (Common.makerList.get(driverGeoModel.getKey()) != null) {
                            GeoQueryModel geoQueryModel = snapshot.getValue(GeoQueryModel.class);
                            AnimationModel animationModel = new AnimationModel(false, geoQueryModel);
                            if (Common.driverLocationSubcribe.get(driverGeoModel.getKey()) != null) {
                                Marker currentMarker = Common.makerList.get(driverGeoModel.getKey());
                                AnimationModel oldPosition = Common.driverLocationSubcribe.get(driverGeoModel.getKey());

                                String from = new StringBuilder()
                                        .append(oldPosition.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(oldPosition.getGeoQueryModel().getL().get(1))
                                        .toString();


                                String to = new StringBuilder()
                                        .append(animationModel.getGeoQueryModel().getL().get(0))
                                        .append(",")
                                        .append(animationModel.getGeoQueryModel().getL().get(1))
                                        .toString();

                                moveMarkerAnimation(driverGeoModel.getKey(), animationModel, currentMarker, from, to);

                            } else {
                                //First location init
                                Common.driverLocationSubcribe.put(driverGeoModel.getKey(), animationModel);
                            }
                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();
                }
            });
        }

    }

    private void moveMarkerAnimation(String key, AnimationModel animationModel, Marker
            currentMarker, String from, String to) {

        if (!animationModel.isRun()) {
            //request an api
            compositeDisposable.add(iGoogleAPI.getDirections("driving",
                    "less_driving",
                    from, to,
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
                                        //polylinelist = Common.decodePoly(polyline);
                                        animationModel.setPolylineList(Common.decodePoly(polyline));

                                    }

                                    //moving
                                    handler = new Handler();
//                            index = -1;
//                            next = 1;

                                    animationModel.setIndex(-1);
                                    animationModel.setNext(1);
                                    Runnable runnable = new Runnable() {
                                        @Override
                                        public void run() {
                                            if (animationModel.getPolylineList() != null &&
                                                    animationModel.getPolylineList().size() > 1) {
                                                if (animationModel.getIndex() < animationModel.getPolylineList().size() - 2) {
//                                            index++;
//                                            next = index + 1;
//                                            start = animationModel.getPolylineList().get(index);
//                                            end = animationModel.getPolylineList().get(next);
                                                    animationModel.setIndex(animationModel.getIndex() + 1);
                                                    animationModel.setNext(animationModel.getIndex() + 1);
                                                    animationModel.setStart(animationModel.getPolylineList().get(animationModel.getIndex()));
                                                    animationModel.setEnd(animationModel.getPolylineList().get(animationModel.getIndex()));
                                                }

                                                ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 1);
                                                valueAnimator.setDuration(3000);
                                                valueAnimator.setInterpolator(new LinearInterpolator());
                                                valueAnimator.addUpdateListener(value -> {
//                                            v = value.getAnimatedFraction();
//                                            lat = v * end.latitude + (1 - v) * start.latitude;
//                                            lng = v * end.longitude + (1 - v) * start.longitude;
//                                            LatLng newPos = new LatLng(lat, lng);
//                                            currentMarker.setPosition(newPos);
//                                            currentMarker.setAnchor(0.5f, 0.5f);
//                                            currentMarker.setRotation(Common.getBearing(start, newPos));
                                                    animationModel.setV(value.getAnimatedFraction());
                                                    animationModel.setLat(animationModel.getV() * animationModel.getEnd().latitude
                                                            + (1 - animationModel.getV()) * animationModel.getStart().latitude);
                                                    animationModel.setLng(animationModel.getV() * animationModel.getEnd().longitude
                                                            + (1 - animationModel.getV()) * animationModel.getStart().longitude);
                                                    LatLng newPos = new LatLng(animationModel.getLat(), animationModel.getLng());
                                                    currentMarker.setPosition(newPos);
                                                    currentMarker.setAnchor(0.5f, 0.5f);
                                                    currentMarker.setRotation(Common.getBearing(animationModel.getStart(), newPos));

                                                });

                                                valueAnimator.start();
                                                if (animationModel.getIndex() < animationModel.getPolylineList().size() - 2)//Reach distination
                                                    animationModel.getHandler().postDelayed(this, 1500);
                                                else if (animationModel.getIndex() < animationModel.getPolylineList().size() - 1)//done
                                                {
                                                    animationModel.setRun(false);
                                                    Common.driverLocationSubcribe.put(key, animationModel); // update data
                                                }
                                            }
                                        }
                                    };

                                    //Run handler
                                    animationModel.getHandler().postDelayed(runnable, 1500);

                                } catch (Exception e) {
                                    Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_LONG).show();
                                }
                            })
            );
        }
    }
}
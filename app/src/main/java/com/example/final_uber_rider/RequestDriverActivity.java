package com.example.final_uber_rider;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.final_uber_rider.Remote.IGoogleAPI;
import com.example.final_uber_rider.Remote.RetrofitClient;
import com.example.final_uber_rider.model.EventBus.SelectPlaceEvent;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.SquareCap;
import com.google.maps.android.ui.IconGenerator;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
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
    //View
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

    @OnClick(R.id.btn_confirm_uber)
    void onConfirmUber() {
        confirm_pickup_layout.setVisibility(View.VISIBLE); // Show pickup Layout
        confirm_uber_layout.setVisibility(View.GONE); //Hide Uber Layout

        setDataPickup();

    }

    private void setDataPickup() {
        txt_pickup_adrress.setText(txt_origin !=null ? txt_origin.getText() : "None");
        mMap.clear(); /// clear all on map
        //add PickupMarker
        addPickupMarker();
    }

    private void addPickupMarker() {
        View view = getLayoutInflater().inflate(R.layout.pickup_info_windows,null);


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
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private IGoogleAPI iGoogleAPI;
    private Polyline blackPolyline, greyPolyline;
    private PolylineOptions polylineOptions, blackPolylineOptions;
    private List<LatLng> polylineList;

    private Marker originMarker, destinationMarker;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
        if (EventBus.getDefault().hasSubscriberForEvent(SelectPlaceEvent.class))
            EventBus.getDefault().removeStickyEvent(SelectPlaceEvent.class);
        EventBus.getDefault().unregister(this);
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    public void onSelectPlaceEvent(SelectPlaceEvent event) {
        selectPlaceEvent = event;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request_driver);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();
    }

    private void init() {
        ButterKnife.bind(this);
        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
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
                        polylineOptions.color(Color.GRAY);
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
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(mMap.getCameraPosition().zoom - 1));


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
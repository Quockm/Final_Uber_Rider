package com.example.final_uber_rider.Callback.Common;

import android.animation.ValueAnimator;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.example.final_uber_rider.R;
import com.example.final_uber_rider.model.AnimationModel;
import com.example.final_uber_rider.model.DriverGeoModel;
import com.example.final_uber_rider.model.RiderInfoModel;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Common {
    public static final String RIDER_INFO_REFERENCE = "RiderInfo";
    public static final String RIDER_LOCATION_REFERENCE = "RiderLocation";
    public static final String DRIVER_LOCATION_REFERENCE = "DriverLocation";
    public static final String DRIVER_INFO_REFERENCE = "DriverInfo";
    public static final String REQUEST_DRIVER_TITLE = "RequestDriver";
    public static final String RIDER_PICKUP_LOCATION = "PickupLocation";
    public static final String RIDER_KEY = "RiderKey" ;
    public static final String REQUEST_DRIVER_DECLINE = "Decline";
    public static final String RIDER_PICKUP_LOCATION_STRING = "PickupLocationString";
    public static final String RIDER_DESTINATION_STRING = "DestinationLocationString";
    public static final String RIDER_DESTINATION = "DestinationLocation";
    public static final String REQUEST_DRIVER_ACCEPT = "Accept" ;
    public static final String TRIP_KEY = "TripKey";
    public static final String TRIP = "Trips";

    public static RiderInfoModel currentRider;

    public static final String TOKEN_REFERENCE = "Token";
    public static final String TOKEN_RIDER_REFERENCE = "Token_Rider";

    public static final String NOTI_TITLE = "Title";
    public static final String NOTI_CONTENT = "body";
    public static Map<String,DriverGeoModel> driverfound = new HashMap<>();
    public static HashMap<String, Marker> makerList = new HashMap<>();
    public static HashMap<String, AnimationModel> driverLocationSubcribe = new HashMap<String, AnimationModel>();


    public static String buildWelcomeMessage() {
        if (Common.currentRider != null ) {
            return new StringBuilder("Welcome ")
                    .append(Common.currentRider.getFisrtnasme())
                    .append(" ")
                    .append(Common.currentRider.getLastname()).toString();
        } else {
            return "";
        }
    }

    public static void ShowNofication(Context context, int id, String title, String body, Intent intent) {
        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String NOFICATION_CHANNEL_ID = "DemoUber";
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            //Setting Vibrate and lights when notification come out
            NotificationChannel notificationChannel = new NotificationChannel(NOFICATION_CHANNEL_ID,
                    "Uber", NotificationManager.IMPORTANCE_HIGH);
            notificationChannel.setDescription("Uber");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.RED);
            notificationChannel.setVibrationPattern(new long[]{0, 1000, 500, 1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }
        // setting style when notification come out
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NOFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(false)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setDefaults(Notification.DEFAULT_VIBRATE)
                .setSmallIcon(R.drawable.ic_baseline_directions_car_24)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_baseline_directions_car_24));
        if (pendingIntent != null) {
            builder.setContentIntent(pendingIntent);
        }
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static String buildName(String fisrtnasme, String lastname) {
        return new StringBuilder(fisrtnasme).append("").append(lastname).toString();
    }

    public static List<LatLng> decodePoly(String encoded) {
        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;

            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }

    public static float getBearing(LatLng begin, LatLng end) {
        //You can copy this function by link at description
        double lat = Math.abs(begin.latitude - end.latitude);
        double lng = Math.abs(begin.longitude - end.longitude);

        if (begin.latitude < end.latitude && begin.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)));
        else if (begin.latitude >= end.latitude && begin.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 90);
        else if (begin.latitude >= end.latitude && begin.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng / lat)) + 180);
        else if (begin.latitude < end.latitude && begin.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng / lat))) + 270);
        return -1;
    }

    public static void setMesWellcome(TextView txtMesWelcome) {
        int hours = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (hours >= 1 && hours <= 12) {
            txtMesWelcome.setText(new StringBuilder("Good Morning"));
        } else if (hours >= 13 && hours <= 17) {
            txtMesWelcome.setText(new StringBuilder("Good Afternoon"));
        } else {
            txtMesWelcome.setText(new StringBuilder("Good Evening"));
        }
    }

    public static String formatDuration(String duration) {
        if(duration.contains("mins"))
            return duration .substring(0,duration.length()-1); // remove letter "s"
        else
            return duration;
    }

    public static String formatAdrress(String start_address) {
        int firstIndexOfComma = start_address.indexOf(",");
        return start_address.substring(0,firstIndexOfComma); // get only address
    }

    public static String formatDecimal(double value, String formPattern,
                                       char decimalSeparator, char groupingSeparator){
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setDecimalSeparator(decimalSeparator);
        symbols.setGroupingSeparator(groupingSeparator);

        DecimalFormat decimalFormat = new DecimalFormat(formPattern, symbols);
        return decimalFormat.format(value);
    }

    public static ValueAnimator valueAnimate(long duration, ValueAnimator.AnimatorUpdateListener listener){
        ValueAnimator va = ValueAnimator.ofFloat(0, 100);
        va.setDuration(duration);
        va.addUpdateListener(listener); 
        va.setRepeatCount(ValueAnimator.INFINITE);
        va.setRepeatMode(ValueAnimator.RESTART);

        va.start();
        return va;
    }
}

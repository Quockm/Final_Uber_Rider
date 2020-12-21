package com.example.final_uber_rider;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Menu;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.final_uber_rider.Callback.Common.Common;
import com.example.final_uber_rider.model.EventBus.ShowNotificationFinishTrip;
import com.example.final_uber_rider.utils.RiderUtils;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class RiderHomeActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 6170;
    private AppBarConfiguration mAppBarConfiguration;

    private DrawerLayout drawer;
    private NavigationView navigationView;
    private NavController navController;

    private AlertDialog waitingDialog;
    private StorageReference storageReference;

    private Uri imageuri;

    private ImageView img_avatar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_history, R.id.nav_sign_out)
                .setDrawerLayout(drawer)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        init();

    }

    private void init() {

        //waiting for upload image avatar
        waitingDialog = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setMessage("Waiting.....")
                .create();

        storageReference = FirebaseStorage.getInstance().getReference();


        /*Already have NavigateController to handle action NavigationItemClicked
         * -> No need to setup Listener for Navigation Menu
         * */
        /*
        navigationView.setNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_sign_out) {
                Context context;
                AlertDialog.Builder builder = new AlertDialog.Builder(RiderHomeActivity.this);
                builder.setTitle("Sign Out")
                        .setMessage("Do you want to sign out!!")
                        .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                        .setPositiveButton("SIGN OUT", (dialogInterface, i) -> {
                            FirebaseAuth.getInstance().signOut();
                            Intent intent = new Intent(RiderHomeActivity.this, SplashScreenActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        })
                        .setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(dialogInterface -> {
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                            .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                    dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                            .setTextColor(getResources().getColor(R.color.black));
                });
                dialog.show();
            }
            return true;
        });*/


        //set data for user
        View headerView = navigationView.getHeaderView(0);
        TextView txt_name = (TextView) headerView.findViewById(R.id.txt_name);
        TextView txt_phonenumber = (TextView) headerView.findViewById(R.id.txt_phonenumber);
        TextView txt_star = (TextView) headerView.findViewById(R.id.txt_star);
        img_avatar = (ImageView) headerView.findViewById(R.id.img_avatar);

        txt_name.setText(Common.buildWelcomeMessage());
        txt_phonenumber.setText(Common.currentRider != null ? Common.currentRider.getPhonenumber() : "");
        txt_star.setText(Common.currentRider != null ? String.valueOf(Common.currentRider.getRating()) : "0.0");
        img_avatar.setOnClickListener(view ->

        {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });
        if (Common.currentRider != null && Common.currentRider.getAvatar() != null &&
                !TextUtils.isEmpty(Common.currentRider.getAvatar())) ;

        {
            //Glide to add img_avatar
            Glide
                    .with(this)
                    .load(Common.currentRider.getAvatar())
                    .into(img_avatar);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.rider_home, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                imageuri = data.getData();
                img_avatar.setImageURI(imageuri);

                showDialogUpload();
            }
        }
    }

    private void showDialogUpload() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RiderHomeActivity.this);
        builder.setTitle("Change Avatar")
                .setMessage("Do you want to change your avatar?")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("UPLOAD", (dialogInterface, i) -> {
                    if (imageuri != null) {
                        waitingDialog.setMessage("Uploading....");
                        waitingDialog.show();

                        String unique_name = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        StorageReference avatarFolder = storageReference.child("avatars/" + unique_name);

                        avatarFolder.putFile(imageuri)
                                .addOnFailureListener(e -> {
                                    waitingDialog.dismiss();
                                    Snackbar.make(drawer, e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                }).addOnCompleteListener(task -> {
                            if (task.isSuccessful()) {
                                avatarFolder.getDownloadUrl().addOnSuccessListener(uri -> {
                                    Map<String, Object> updateData = new HashMap<>();
                                    updateData.put("avatar", uri.toString());

                                    RiderUtils.updateUser(drawer, updateData);
                                });
                                waitingDialog.dismiss();
                            }
                        }).addOnProgressListener(snapshot -> {
                            double progress = (100.0 * snapshot.getTotalByteCount() / snapshot.getTotalByteCount());
                            waitingDialog.setMessage(new StringBuilder("Uploading: ").append(progress).append("%"));
                        });
                    }
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.black));

        });
        dialog.show();
    }

    @Override
    public void onStart() {
        super.onStart();
//        if (!EventBus.getDefault().isRegistered(this))
//            EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
//        if (EventBus.getDefault().hasSubscriberForEvent(ShowNotificationFinishTrip.class))
//            EventBus.getDefault().removeStickyEvent(ShowNotificationFinishTrip.class);
//        EventBus.getDefault().unregister(this);
    }


}
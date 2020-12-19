package com.example.final_uber_rider;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import android.content.Intent;
import com.example.final_uber_rider.SplashScreenActivity;

import org.jetbrains.annotations.NotNull;

import com.google.firebase.auth.FirebaseAuth;

public class LogoutDialog extends DialogFragment {

    private AppCompatActivity appCompatActivity;

    @Override
    public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        appCompatActivity = (AppCompatActivity)getActivity();
    }

    @NonNull
    @NotNull
    @Override
    public Dialog onCreateDialog(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(appCompatActivity);
        builder.setTitle("Sign Out")
                .setMessage("Do you want to sign out!!")
                .setNegativeButton("CANCEL", (dialogInterface, i) -> dialogInterface.dismiss())
                .setPositiveButton("SIGN OUT", (dialogInterface, i) -> {
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(appCompatActivity, SplashScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    appCompatActivity.startActivity(intent);
                    appCompatActivity.finish();
                })
                .setCancelable(false);
        AlertDialog dialog = builder.create();
        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                    .setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
                    .setTextColor(getResources().getColor(R.color.black));
        });

        return dialog;
    }
}

package com.example.ruplaga.paisavasool;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;

public class LauncherActivity extends Activity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

    }

    @Override
    protected void onStart() {
        super.onStart();
        System.out.println("User:" + mAuth.getCurrentUser());
        //mAuth.signOut();
        if (mAuth.getCurrentUser() != null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
            System.out.println("MainActivity Opened");

        }

        else {
            finish();
            startActivity(new Intent(this, LoginActivity.class));
            System.out.println("LoginActivity Opened");

        }
    }
}

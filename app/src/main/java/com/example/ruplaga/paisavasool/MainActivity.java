package com.example.ruplaga.paisavasool;

import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    DrawerLayout dl;
    ActionBarDrawerToggle actionBarDrawerToggle;

    FirebaseAuth mAuth;
    String displayName, displayEmail;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.actionmenu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //set default fragment on start
        if (savedInstanceState == null) {
            Fragment newFragment = new HomeFragment();
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.add(R.id.main, newFragment);
            ft.commit();
        }

        //side menu
        NavigationView navigationView = findViewById(R.id.navView);

        //username in header
        View navHeaderView = navigationView.getHeaderView(0);
        TextView displayNameView = navHeaderView.findViewById(R.id.displayName);
        TextView displayEmailView = navHeaderView.findViewById(R.id.displayEmail);

        mAuth = FirebaseAuth.getInstance();
        displayName = mAuth.getCurrentUser().getDisplayName();
        displayEmail = mAuth.getCurrentUser().getEmail();

        displayNameView.setText(displayName);
        displayEmailView.setText(displayEmail);

        //navigation drawer
        dl = (DrawerLayout) findViewById(R.id.dl);
        actionBarDrawerToggle = new ActionBarDrawerToggle(this, dl,R.string.open, R.string.close);
        dl.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setupDrawerContent(navigationView);

        //notification
        int intervalMillis = 1000 * 60 * 60;
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        Intent intent = new Intent("apurva.action.DISPLAY_NOTIFICATION");
        PendingIntent broadcast = PendingIntent.getBroadcast(this, 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), intervalMillis, broadcast );
    }

    public void selectIterDrawer(MenuItem menuItem){

        Fragment fragment = null;
        Class fragmentClass;
        switch (menuItem.getItemId()){

            case R.id.home:
                fragmentClass = HomeFragment.class;
                break;

            case R.id.overview:
                fragmentClass = OverviewFragment.class;
                break;

            case R.id.accounts:
                fragmentClass = AccountsFragment.class;
                break;

            case R.id.history:
                fragmentClass = HistoryFragment.class;
                break;

            case R.id.settings:
                fragmentClass = SettingsFragment.class;
                break;

            default:
                fragmentClass = OverviewFragment.class;

        }

        try {
            fragment = (Fragment) fragmentClass.newInstance();
        }
        catch(Exception e) {
            e.printStackTrace();
        }

       // finishAffinity();
        FragmentManager fragmentManager = getSupportFragmentManager();
        getFragmentManager().popBackStack();
        fragmentManager.beginTransaction().replace(R.id.main, fragment).addToBackStack("tag").commit();
        dl.closeDrawers();
    }

    private void setupDrawerContent(NavigationView navigationView){
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                selectIterDrawer(item);
                return false;
            }
        });
    }

    public void switchToOtherFragment(Fragment newFragment){

        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.replace(R.id.main, newFragment).addToBackStack("tag");
        transaction.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.income:
                switchToOtherFragment(new AddIncomeFragment());
                return(true);
            case R.id.expense:
                switchToOtherFragment(new AddExpenseFragment());
                return(true);

        }
        return(super.onOptionsItemSelected(item));
    }
}

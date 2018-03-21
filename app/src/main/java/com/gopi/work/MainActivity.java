package com.gopi.work;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;


public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    private Toolbar mToolbar;
    private DatabaseReference databaseusers;

    private DatabaseReference mUserRef;

    private HomeFragment homeFragment;
    private ChatFrament chatFrament;
    private SettingFragment settingFragment;

    private FrameLayout mFrameLayout;

    private BottomNavigationView mBottomNavigationView;

    private int position = 0;
    public static Boolean check = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.color.white);

        mAuth = FirebaseAuth.getInstance();

        databaseusers = FirebaseDatabase.getInstance().getReference().child("User");
        databaseusers.keepSynced(true);

        mToolbar = (Toolbar) findViewById(R.id.main_page);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Tan Tana Tan");

        homeFragment = new HomeFragment();
        chatFrament = new ChatFrament();
        settingFragment = new SettingFragment();

        changeFragment(position);

        mBottomNavigationView = (BottomNavigationView) findViewById(R.id.main_nav);
        mFrameLayout = (FrameLayout) findViewById(R.id.main_frame);

        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){

                    case R.id.nav_home:
                        position = 0;
                        changeFragment(position);
                        return true;
                    case R.id.nav_chat:
                        position = 1;
                        changeFragment(position);
                        return true;
                    case R.id.nav_settings:
                        position = 2;
                        changeFragment(position);
                        return true;
                    default:
                        return false;

                }
            }
        });


    }

    private void changeFragment(int position) {
        FragmentTransaction fragmentTransaction = null;

        switch (position){

            case 0:
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame,homeFragment);
                fragmentTransaction.commit();
                break;
            case 1:
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame,chatFrament);
                fragmentTransaction.commit();
                break;
            case 2:
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame,settingFragment);
                fragmentTransaction.commit();
                break;
            default:
                fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.main_frame,homeFragment);
                fragmentTransaction.commit();

        }

    }

    private void checkUser() {

        if(mAuth.getCurrentUser() != null) {

            final String userid = mAuth.getCurrentUser().getUid();
            
            databaseusers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {

                    if (!dataSnapshot.hasChild(userid)) {

                        Intent amain = new Intent(MainActivity.this, AccountSetup.class);
                        //amain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(amain);
                    }else {
                        if (dataSnapshot.hasChild(userid)){
                            if (isMyServiceRunning(LocationMonitoringService.class)){
                            }else {
                                mUserRef = databaseusers.child(userid);
                                mUserRef.child("online").setValue(true);
                            }

                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }else {
            sendtoStart();
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        checkUser();

    }

    private void sendtoStart() {
        Intent login = new Intent(MainActivity.this ,Login.class);
        login.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(login);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_logout){
            logout();
        }

        if(item.getItemId() == R.id.action_user){
            Intent user = new Intent(MainActivity.this ,SearchUser.class);
            startActivity(user);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }



    private void logout() {
        mUserRef.child("online").setValue(ServerValue.TIMESTAMP);
        mAuth.signOut();
        sendtoStart();
    }

    @Override
    protected void onStop() {
        super.onStop();

            if (mAuth.getCurrentUser() != null) {
                startService(new Intent(getApplicationContext(), LocationMonitoringService.class));
            }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mAuth.getCurrentUser() != null) {
            startService(new Intent(getApplicationContext(), LocationMonitoringService.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        stopService(new Intent(getApplicationContext(), LocationMonitoringService.class));
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAuth.getCurrentUser() != null) {
                startService(new Intent(getApplicationContext(), LocationMonitoringService.class));
            }
    }
}

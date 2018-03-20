package com.gopi.work;


import android.*;
import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class ChatFrament extends Fragment implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    private RecyclerView mUsersList;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUserReference;
    private DatabaseReference mLocation;

    private FirebaseAuth mAuth;

    private static final int MY_PERMISSION_REQUEST_CODE = 7171;
    private static final int PLAY_SERVICES_REQUEST_CODE = 7172;
    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private static int UPDATE_INTERVAL = 5000;
    private static int FAST_INTERVAL = 3000;
    private static int DISTANCE = 10;

    private static String nam;
    private String mCurrentUser;
    private String userPh;
    private String userName;


    public ChatFrament() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_chat_frament, container, false);

        mUsersList = (RecyclerView) v.findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser().getUid();


        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrentUser);
        mLocation = FirebaseDatabase.getInstance().getReference().child("Location");
        mUserReference = FirebaseDatabase.getInstance().getReference().child("User");

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(getActivity(),new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            },MY_PERMISSION_REQUEST_CODE);
        }else {

            if (checkPlayServices()){

                buildGoogleApiClient();
                createLocationRequest();
                displayLocation();
            }
        }

        return v;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_CODE:{

                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                    if (checkPlayServices()){

                        buildGoogleApiClient();
                        createLocationRequest();
                        displayLocation();
                    }

                }

            }
            break;
        }
    }

    private void displayLocation() {

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return;

        }

        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null){

            mLocation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                    .setValue(new Tracking(String.valueOf(mLastLocation.getLatitude()),
                            String.valueOf(mLastLocation.getLongitude()),
                            FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber(),FirebaseAuth.getInstance().getCurrentUser().getUid()));
        }else {
            //Toast.makeText(this,"Could not get Location", Toast.LENGTH_SHORT).show();
        }
    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FAST_INTERVAL);
        mLocationRequest.setSmallestDisplacement(DISTANCE);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    private void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {

        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getContext());
        if (resultCode != ConnectionResult.SUCCESS){

            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)){

                GooglePlayServicesUtil.getErrorDialog(resultCode,getActivity(),PLAY_SERVICES_REQUEST_CODE).show();
            }else {
                Toast.makeText(getContext(),"This device is not supported", Toast.LENGTH_SHORT).show();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onStop() {

        if (mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }

        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkPlayServices();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (mGoogleApiClient != null){

            mGoogleApiClient.connect();
        }

        FirebaseRecyclerAdapter<Friends, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Friends, UserViewHolder>(
                Friends.class,
                R.layout.user_single,
                UserViewHolder.class,
                mDatabaseReference

        ) {
            @Override
            protected void populateViewHolder(final UserViewHolder viewHolder, final Friends model, int position) {

                viewHolder.setDate(model.getDate());
                //viewHolder.setName(model.getName());
                //viewHolder.setStatus(model.getStatus());
                //viewHolder.setThumb_image(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();
                mUserReference.child(user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        userName = dataSnapshot.child("name").getValue().toString();
                        String userThumb = dataSnapshot.child("thumb_image").getValue().toString();
                        userPh = dataSnapshot.child("ph").getValue().toString();

                        String userOnline = "";

                        if (dataSnapshot.hasChild("online")){
                            userOnline = dataSnapshot.child("online").getValue().toString();
                        }


                        if (userOnline.equals("true")){

                            viewHolder.view.findViewById(R.id.status).setVisibility(View.VISIBLE);

                        }else viewHolder.view.findViewById(R.id.status).setVisibility(View.INVISIBLE);

                        viewHolder.setName(userName);
                        viewHolder.setThumb_image(userThumb,getContext());

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                viewHolder.view.findViewById(R.id.locate).setVisibility(View.VISIBLE);

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        CharSequence options[] = new CharSequence[]{"Open Profile", "Send Message"};

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("Select");
                        builder.setItems(options, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                if (i == 0){
                                    Intent profileIntent = new Intent(getContext(), Profile.class);
                                    profileIntent.putExtra("user_id", user_id);
                                    startActivity(profileIntent);
                                }else if (i == 1){

                                    Intent chatIntent = new Intent(getContext(), Chat.class);
                                    chatIntent.putExtra("user_id", user_id);
                                    //Toast.makeText(getContext(),userName,Toast.LENGTH_LONG).show();
                                    chatIntent.putExtra("name", nam);
                                    startActivity(chatIntent);


                                }

                            }
                        });

                        builder.show();

                    }
                });

                viewHolder.view.findViewById(R.id.locate).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (!userPh.equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())){

                            Intent map = new Intent(getContext(), MapTracking.class);
                            map.putExtra("ph", user_id);
                            map.putExtra("lat",mLastLocation.getLatitude());
                            map.putExtra("lng",mLastLocation.getLongitude());
                            map.putExtra("name",nam);
                            startActivity(map);
                        }

                    }
                });
            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    @Override
    public void onLocationChanged(Location location) {

        mLastLocation = location;
        displayLocation();

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdate();

    }

    private void startLocationUpdate() {

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            return;

        }

        //   LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (com.google.android.gms.location.LocationListener) this);

    }

    @Override
    public void onConnectionSuspended(int i) {

        mGoogleApiClient.connect();

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View view;
        public UserViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setDate(String date){
            TextView usernameview = (TextView) view.findViewById(R.id.user_stat);
            usernameview.setText(date);
        }
        public void setName(String name){
            TextView usernameview = (TextView) view.findViewById(R.id.user_nam);
            nam = name;
            usernameview.setText(name);
        }
        /*public void setStatus(String status){
            TextView userstatusview = (TextView) view.findViewById(R.id.user_stat);
            userstatusview.setText(status);
        }*/
        public void setThumb_image(final String thumb_image, final Context ctx){
            final CircleImageView thumb = (CircleImageView) view.findViewById(R.id.user_image);
            //Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.if_user_925901).into(thumb);
            Picasso.with(ctx).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.if_user_925901).into(thumb, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(ctx).load(thumb_image).placeholder(R.drawable.if_user_925901).into(thumb);

                }
            });
        }
    }

}

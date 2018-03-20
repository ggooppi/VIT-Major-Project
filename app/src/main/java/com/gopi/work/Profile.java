package com.gopi.work;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Profile extends AppCompatActivity {

    private TextView uName;
    private TextView uDescp;

    private Button uRequest;
    private Button uDelete;

    private ImageView uImage;

    private DatabaseReference mUserDAtabase;
    private DatabaseReference mUserRequest;
    private DatabaseReference mUserfriendDAtabase;
    private DatabaseReference mUserNovification;
    private DatabaseReference mRootRef;

    private FirebaseUser mCurrentUser;

    private ProgressDialog progressDialog;

    private String currentState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        final String user_id =  getIntent().getStringExtra("user_id");

        mUserDAtabase = FirebaseDatabase.getInstance().getReference().child("User").child(user_id);
        mUserRequest = FirebaseDatabase.getInstance().getReference().child("friendrequest");
        mUserfriendDAtabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mUserNovification = FirebaseDatabase.getInstance().getReference().child("notification");
        mRootRef = FirebaseDatabase.getInstance().getReference();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        uImage = (ImageView) findViewById(R.id.userImage);
        uName = (TextView) findViewById(R.id.userName);
        uDescp = (TextView) findViewById(R.id.userDes);
        uRequest = (Button) findViewById(R.id.request);
        uDelete = (Button) findViewById(R.id.delete);

        currentState = "not_friends";

        progressDialog = new ProgressDialog(this);
        progressDialog.setTitle("Loading User Data");
        progressDialog.setMessage("Please wait while we load the data");
        progressDialog.setCanceledOnTouchOutside(false);

        progressDialog.show();

        mUserDAtabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String display_name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();

                uName.setText(display_name);
                uDescp.setText(status);

                //Picasso.with(Profile.this).load(image).placeholder(R.drawable.if_user_925901).into(uImage);
                Picasso.with(Profile.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.if_user_925901).into(uImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(Profile.this).load(image).placeholder(R.drawable.if_user_925901).into(uImage);

                    }
                });


                mUserRequest.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        if(dataSnapshot.hasChild(user_id)){
                            String request = dataSnapshot.child(user_id).child("request_type").getValue().toString();

                            if (request.equals("received")){

                                currentState = "req_received";
                                uRequest.setText("Accept Friend Request");

                                uDelete.setVisibility(View.VISIBLE);
                                uDelete.setEnabled(true);



                            }else if(request.equals("sent")){

                                currentState = "req_sent";
                                uRequest.setText("Cancle friend Request");

                                uDelete.setVisibility(View.INVISIBLE);
                                uDelete.setEnabled(false);

                            }

                            progressDialog.dismiss();

                        } else {

                            mUserfriendDAtabase.child(mCurrentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    if (dataSnapshot.hasChild(user_id)){

                                        currentState = "friends";
                                        uRequest.setText("Unfriend");

                                        uDelete.setVisibility(View.INVISIBLE);
                                        uDelete.setEnabled(false);

                                    }

                                    progressDialog.dismiss();
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                    progressDialog.dismiss();

                                }
                            });
                        }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        uRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                uRequest.setEnabled(false);



                if(currentState.equals("not_friends")){

                    DatabaseReference newNotificationRef = mRootRef.child("notification").child(user_id).push();
                    String newNotificationId = newNotificationRef.getKey();

                    HashMap<String, String> notificationData = new HashMap<>();
                    notificationData.put("from", mCurrentUser.getUid());
                    notificationData.put("type", "request");

                    Map requestmap = new HashMap();
                    requestmap.put("friendrequest/" + mCurrentUser.getUid() + "/" + user_id + "/request_type", "sent");
                    requestmap.put("friendrequest/" + user_id + "/" + mCurrentUser.getUid() + "/request_type", "received");
                    requestmap.put("notification/" + user_id + "/" + newNotificationId, notificationData);

                    mRootRef.updateChildren(requestmap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError != null){

                                Toast.makeText(Profile.this,"There was some error in sending", Toast.LENGTH_LONG).show();
                            }

                            uRequest.setEnabled(true);

                            currentState = "req_sent";
                            uRequest.setText("Cancel Friend Request");

                        }
                    });

                }

                if (currentState.equals("req_sent")){

                    mUserRequest.child(mCurrentUser.getUid()).child(user_id).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {

                            mUserRequest.child(user_id).child(mCurrentUser.getUid()).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                    uRequest.setEnabled(true);
                                    currentState = "not_friends";
                                    uRequest.setText("Send Friend Request");

                                    uDelete.setVisibility(View.INVISIBLE);
                                    uDelete.setEnabled(false);

                                }
                            });

                            //Toast.makeText(Profile.this, "Sending Failed",Toast.LENGTH_SHORT).show();

                        }
                    });

                }

                if(currentState.equals("req_received")){

                    final String currentDate = DateFormat.getDateInstance().format(new Date());

                    Map friendsMap = new HashMap();
                    friendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/date", currentDate);
                    friendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/date", currentDate);

                    friendsMap.put("friendrequest/" + mCurrentUser.getUid() + "/" + user_id, null);
                    friendsMap.put("friendrequest/" + user_id + "/" + mCurrentUser.getUid(), null);

                    mRootRef.updateChildren(friendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){

                                uRequest.setEnabled(true);

                                currentState = "friends";
                                uRequest.setText("Unfriend");

                                uDelete.setVisibility(View.INVISIBLE);
                                uDelete.setEnabled(false);
                            } else {

                                Toast.makeText(Profile.this,"There was some error in sending", Toast.LENGTH_LONG).show();
                            }

                        }
                    });



                }

                if (currentState.equals("friends")){

                    Map unFriendsMap = new HashMap();
                    unFriendsMap.put("Friends/" + mCurrentUser.getUid() + "/" + user_id + "/", null);
                    unFriendsMap.put("Friends/" + user_id + "/" + mCurrentUser.getUid() + "/", null);

                    mRootRef.updateChildren(unFriendsMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if (databaseError == null){

                                currentState = "not_friends";
                                uRequest.setText("Send Request");

                                uDelete.setVisibility(View.INVISIBLE);
                                uDelete.setEnabled(false);
                            } else {

                                Toast.makeText(Profile.this,"There was some error in sending", Toast.LENGTH_LONG).show();
                            }

                            uRequest.setEnabled(true);

                        }
                    });

                }
            }
        });

    }
}

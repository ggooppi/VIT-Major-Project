package com.gopi.work;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListUser extends AppCompatActivity {

    private Toolbar mToolbar;

    private RecyclerView mUsersList;

    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseReference;
    private DatabaseReference mUserDatabase;

    private static String nam;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_user);

        mToolbar = (Toolbar) findViewById(R.id.user_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        mUsersList = (RecyclerView) findViewById(R.id.users_list);
        mUsersList.setHasFixedSize(true);
        mUsersList.setLayoutManager(new LinearLayoutManager(this));

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("User");


        //mUserDatabase = FirebaseDatabase.getInstance().getReference()
        //        .child("User").child(mAuth.getCurrentUser().getUid());

        //mUserDatabase.child("online").setValue(true);
    }

    @Override
    protected void onStart() {
        super.onStart();


        FirebaseRecyclerAdapter<User, UserViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<User, UserViewHolder>(
                User.class,
                R.layout.user_single,
                UserViewHolder.class,
                mDatabaseReference

        ) {
            @Override
            protected void populateViewHolder(UserViewHolder viewHolder, final User model, int position) {

                viewHolder.setName(model.getName());
                viewHolder.setStatus(model.getStatus());
                viewHolder.setThumb_image(model.getThumb_image(),getApplicationContext());

                final String user_id = getRef(position).getKey();

                viewHolder.view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!model.getPh().equals(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber())) {

                            Intent profileIntent = new Intent(ListUser.this, Profile.class);
                            profileIntent.putExtra("user_id", user_id);
                            startActivity(profileIntent);
                        }

                    }
                });

            }
        };

        mUsersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder{

        View view;
        public UserViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }
        public void setName(String name){
            TextView usernameview = (TextView) view.findViewById(R.id.user_nam);
            nam = name;
            usernameview.setText(name);
        }
        public void setStatus(String status){
            TextView userstatusview = (TextView) view.findViewById(R.id.user_stat);
            userstatusview.setText(status);
        }
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

package com.gopi.work;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SingleBlog extends AppCompatActivity {

    private String post_key = null;

    private ImageView imageView;
    private Button delete;

    private DatabaseReference mDatabaseReference;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_blog);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        mAuth = FirebaseAuth.getInstance();

        post_key = getIntent().getExtras().getString("blog_id");

        imageView = (ImageView) findViewById(R.id.imageView);
        delete = (Button) findViewById(R.id.del);

        mDatabaseReference.child(post_key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String post_image = (String) dataSnapshot.child("image").getValue();
                String post_uid = (String) dataSnapshot.child("userId").getValue();

                Picasso.with(SingleBlog.this).load(post_image).into(imageView);

                if(mAuth.getCurrentUser().getUid().equals(post_uid)){
                    delete.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mDatabaseReference.child(post_key).removeValue();
                Intent main = new Intent(SingleBlog.this,MainActivity.class);
                startActivity(main);
                finish();
            }
        });

    }

}

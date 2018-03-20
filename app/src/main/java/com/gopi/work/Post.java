package com.gopi.work;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Post extends AppCompatActivity {

    private ImageButton selectImage;
    private EditText title;
    private EditText des;
    private Button pos;

    private Uri uri = null;

    private StorageReference store;
    private DatabaseReference database;
    private DatabaseReference databaseReferenceuser;
    private ProgressDialog prog;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    private static final int gal_int = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);
        mAuth = FirebaseAuth.getInstance();

        mCurrentUser = mAuth.getCurrentUser();

        store = FirebaseStorage.getInstance().getReference();
        database = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReferenceuser = FirebaseDatabase.getInstance().getReference().child("User").child(mCurrentUser.getUid());

        selectImage = (ImageButton) findViewById(R.id.img);
        title=(EditText) findViewById(R.id.tit);
        des=(EditText) findViewById(R.id.desc);
        pos=(Button) findViewById(R.id.post);

        databaseReferenceuser.child("online").setValue(true);




        prog = new ProgressDialog(this);


        selectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                Intent intent = new Intent(Intent.ACTION_PICK);

                intent.setType("image/*");

                startActivityForResult(intent, gal_int);
            }
        });

        pos.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                posting();
            }
        });
    }

    private void posting() {

        prog.setMessage("Posting to Blog");

        final String tit_val = title.getText().toString().trim();
        final String des_val = des.getText().toString().trim();

        if(!TextUtils.isEmpty(tit_val) && !TextUtils.isEmpty(des_val) && uri != null){
            prog.setCanceledOnTouchOutside(false);
            prog.show();

            StorageReference filepath = store.child("image").child(uri.getLastPathSegment());
            filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    final Uri downuri = taskSnapshot.getDownloadUrl();
                    final DatabaseReference newpost = database.push();
                    databaseReferenceuser.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            newpost.child("title").setValue(tit_val);
                            newpost.child("Desc").setValue(des_val);
                            newpost.child("image").setValue(downuri.toString());
                            newpost.child("userId").setValue(mCurrentUser.getUid());
                            newpost.child("username").setValue(dataSnapshot.child("name").getValue()).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        //Toast.makeText(Post.this, "Upload Done", Toast.LENGTH_LONG).show();
                                        finish();
                                    }

                                }
                            });

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    prog.dismiss();
                    finish();


                }
            });

        }
        else {
            Toast.makeText(Post.this, "Fields are missing", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gal_int && resultCode == RESULT_OK){

            uri = data.getData();

            selectImage.setImageURI(uri);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}

package com.gopi.work;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class AccountSetup extends AppCompatActivity {

    private CircleImageView userAvatar;

    private AutoCompleteTextView userName;
    private AutoCompleteTextView userStatus;

    private Button updateProfile;
    private Button updateDP;

    private ProgressDialog progressDialog;

    private Toolbar mToolbar;

    private static final int gallery_int = 1;

    private FirebaseAuth mAuth;
    private FirebaseUser mfirebaseUser;
    private DatabaseReference mdatabaseReference;
    private DatabaseReference mdatabaseUser;
    private StorageReference store;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_setup);
        getWindow().setBackgroundDrawableResource(R.color.colorPrimary);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mToolbar = (Toolbar) findViewById(R.id.account_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Account Setup");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        userAvatar = (CircleImageView) findViewById(R.id.user_dp);

        userName = (AutoCompleteTextView) findViewById(R.id.Name);
        userStatus = (AutoCompleteTextView) findViewById(R.id.Status);

        updateProfile = (Button) findViewById(R.id.update);
        updateDP = (Button) findViewById(R.id.update_dp);

        progressDialog = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();
        mfirebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        final String userid = mfirebaseUser.getUid();

        mdatabaseUser = FirebaseDatabase.getInstance().getReference().child("User");
        store = FirebaseStorage.getInstance().getReference().child("profilePicture");

        mdatabaseUser.keepSynced(true);

        mdatabaseUser.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                if (dataSnapshot.hasChild(userid)) {

                    mdatabaseReference = FirebaseDatabase.getInstance().getReference().child("User").child(userid);

                    mdatabaseReference.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {

                            if (dataSnapshot.hasChild("name")){
                                String name = dataSnapshot.child("name").getValue().toString();
                                userName.setText(name);
                            }
                            if (dataSnapshot.hasChild("status")){
                                String status = dataSnapshot.child("status").getValue().toString();
                                userStatus.setText(status);
                            }
                            if (dataSnapshot.hasChild("image")) {
                               // String image = dataSnapshot.child("image").getValue().toString();
                                final String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();
                                Picasso.with(AccountSetup.this).load(thumb_image).networkPolicy(NetworkPolicy.OFFLINE)
                                        .placeholder(R.drawable.if_user_925901).into(userAvatar, new Callback() {
                                    @Override
                                    public void onSuccess() {

                                    }

                                    @Override
                                    public void onError() {

                                        Picasso.with(AccountSetup.this).load(thumb_image).placeholder(R.drawable.if_user_925901).into(userAvatar);

                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        updateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSetupAccount();
            }
        });

        updateDP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(Intent.createChooser(galleryIntent,"Select Image"), gallery_int);
            }
        });


    }

    private void startSetupAccount() {

        final String nam = userName.getText().toString().trim();
        final String stat = userStatus.getText().toString().trim();

        if(!TextUtils.isEmpty(nam) && !TextUtils.isEmpty(stat)){

            progressDialog.setMessage("Uploading Name and Status..");
            progressDialog.setCanceledOnTouchOutside(false);
            progressDialog.show();

            final String userid = mAuth.getCurrentUser().getUid();

            String deviceToken = FirebaseInstanceId.getInstance().getToken();
            mdatabaseUser.child(userid).child("token").setValue(deviceToken);

            mdatabaseUser.child(userid).child("name").setValue(nam);
            mdatabaseUser.child(userid).child("status").setValue(stat);
            mdatabaseUser.child(userid).child("ph").setValue(FirebaseAuth.getInstance().getCurrentUser().getPhoneNumber());
        }
        progressDialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == gallery_int && resultCode == RESULT_OK){

            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropWindowSize(500, 500)
                    .setAspectRatio(1,1)
                    .start(AccountSetup.this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                progressDialog.setMessage("Uploading Image..");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final String userid = mAuth.getCurrentUser().getUid();

                final Uri imageUri= result.getUri();

                File actualImageFile = new File(imageUri.getPath());

                Bitmap compressedImageBitmap = null;
                try {
                    compressedImageBitmap = new Compressor(this)
                            .setMaxHeight(200)
                            .setMaxWidth(200)
                            .setQuality(75)
                            .compressToBitmap(actualImageFile);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    compressedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    final byte[] thumb_byte = baos.toByteArray();

                StorageReference filepath = store.child(userid).child(userid+".jpg");
                final StorageReference thumb_filepath = store.child(userid).child("thumbs").child(userid+".jpg");
                //mdatabaseUser.child(userid).child("thumb_image").setValue("default");
                filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                        if (task.isSuccessful()){

                            final String downuri = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                    if (task.isSuccessful()){

                                        String thumb_downloadurl = task.getResult().getDownloadUrl().toString();

                                        Map updateMap = new HashMap();
                                        updateMap.put("image",downuri);
                                        updateMap.put("thumb_image",thumb_downloadurl);

                                        mdatabaseUser.child(userid).updateChildren(updateMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {

                                                if (task.isSuccessful()){
                                                    progressDialog.dismiss();
                                                }

                                            }
                                        });

                                    }

                                }
                            });

                        }

                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}

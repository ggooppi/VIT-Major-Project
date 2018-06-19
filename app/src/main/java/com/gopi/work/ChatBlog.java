package com.gopi.work;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class ChatBlog extends AppCompatActivity {

    private static final String TAG = ChatBlog.class.getSimpleName();

    @BindView(R.id.chatList)
    RecyclerView mChatRecyclerView;
    @BindView(R.id.uMessage)
    EditText mUserMessageChatText;

    private Toolbar mChatToolbar;

    private TextView displayName;
    private TextView lastSceen;

    private CircleImageView userProfile;

    private String mChatUser;
    String mChatUserName;

    private static final int GALLERY_PICK = 1;

    private ProgressDialog progressDialog;


    private String mRecipientId;
    private String mCurrentUserId;
    private MessageChatAdapter messageChatAdapter;
    private DatabaseReference messageChatDatabase;
    private DatabaseReference mRootRef;
    private StorageReference mImageStorage;
    private FirebaseAuth mAuth;
    private ChildEventListener messageChatListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_blog);

        mChatToolbar = (Toolbar) findViewById(R.id.chatBar);
        setSupportActionBar(mChatToolbar);

        ActionBar actionBar = getSupportActionBar();
        //actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        mChatUser = getIntent().getStringExtra("user_id");
        mChatUserName = getIntent().getStringExtra("name");

        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = inflater.inflate(R.layout.chat_custom_bar, null);

        actionBar.setCustomView(actionBarView);

        progressDialog = new ProgressDialog(this);

        displayName = (TextView) findViewById(R.id.uTitlename);
        lastSceen = (TextView) findViewById(R.id.userLastseen);
        userProfile = (CircleImageView) findViewById(R.id.user_image);

        mRootRef = FirebaseDatabase.getInstance().getReference();
        mImageStorage = FirebaseStorage.getInstance().getReference();

        bindButterKnife();
        setDatabaseInstance();
        setUsersId();
        setChatRecyclerView();
        mAuth = FirebaseAuth.getInstance();

        mRootRef.child("User").child(mAuth.getCurrentUser().getUid()).child("online").setValue(true);

        mRootRef.child("User").child(mChatUser).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                displayName.setText(dataSnapshot.child("name").getValue().toString());

                String online = dataSnapshot.child("online").getValue().toString();
                final String image = dataSnapshot.child("thumb_image").getValue().toString();


                if (online.equals("true")) {

                    lastSceen.setText("Online");
                } else if (online.equals("Running Background")) {

                    lastSceen.setText("Running Background");
                }else {

                    GetTimeAgo getTime = new GetTimeAgo();

                    lastSceen.setText(getTime.getTimeAgo(Long.parseLong(online), getApplicationContext()));
                }

                Picasso.with(getApplicationContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.if_user_925901).into(userProfile, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                        Picasso.with(getApplicationContext()).load(image).placeholder(R.drawable.if_user_925901).into(userProfile);

                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void setChatRecyclerView() {
        mChatRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mChatRecyclerView.setHasFixedSize(true);
        messageChatAdapter = new MessageChatAdapter(new ArrayList<ChatMessage>());
        mChatRecyclerView.setAdapter(messageChatAdapter);
    }

    private void setUsersId() {
        mRecipientId = getIntent().getStringExtra(ExtraIntent.EXTRA_RECIPIENT_ID);
        mCurrentUserId = getIntent().getStringExtra(ExtraIntent.EXTRA_CURRENT_USER_ID);
    }

    private void setDatabaseInstance() {
        String chatRef = getIntent().getStringExtra(ExtraIntent.EXTRA_CHAT_REF);
        messageChatDatabase = FirebaseDatabase.getInstance().getReference().child("Messages").child(chatRef);

    }

    private void bindButterKnife() {
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        messageChatListener = messageChatDatabase.limitToFirst(100).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

                if(dataSnapshot.exists()){
                    ChatMessage newMessage = dataSnapshot.getValue(ChatMessage.class);
                    if(newMessage.getSender().equals(mCurrentUserId)){
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.SENDER);
                    }else{
                        newMessage.setRecipientOrSenderStatus(MessageChatAdapter.RECIPIENT);
                    }
                    messageChatAdapter.refillAdapter(newMessage);
                    mChatRecyclerView.scrollToPosition(messageChatAdapter.getItemCount()-1);
                }

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(messageChatListener != null) {
            messageChatDatabase.removeEventListener(messageChatListener);
        }
        messageChatAdapter.cleanUp();
    }

    @OnClick(R.id.uAttach)
    public void btnSendImgListener(View attachButton){

        Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

    }

    @OnClick(R.id.uSend)
    public void btnSendMsgListener(View sendButton){

        String senderMessage = mUserMessageChatText.getText().toString().trim();

        if(!senderMessage.isEmpty()){

            ChatMessage newMessage = new ChatMessage(senderMessage,mCurrentUserId,mRecipientId,"text");
            messageChatDatabase.push().setValue(newMessage);

            mUserMessageChatText.setText("");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropWindowSize(500, 500)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

                CropImage.ActivityResult result = CropImage.getActivityResult(data);

                if (resultCode == RESULT_OK) {

                    progressDialog.setMessage("Uploading Image..");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    final Uri imageUri = result.getUri();

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

                    StorageReference filepath = mImageStorage.child("message_images").child(imageUri.getLastPathSegment());
                    final StorageReference thumb_filepath = mImageStorage.child("message_images").child("thumbs").child(imageUri.getLastPathSegment());

                    filepath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                            if (task.isSuccessful()) {

                                String download_url = task.getResult().getDownloadUrl().toString();

                                UploadTask uploadTask = thumb_filepath.putBytes(thumb_byte);
                                uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                                        if (task.isSuccessful()) {

                                            String thumb_downloadurl = task.getResult().getDownloadUrl().toString();

                                            ChatMessage newMessage = new ChatMessage(thumb_downloadurl, mCurrentUserId, mRecipientId, "image");
                                            messageChatDatabase.push().setValue(newMessage);

                                            mUserMessageChatText.setText("");

                                            progressDialog.dismiss();

                                        }
                                    }
                                });
                            }
                        }
                    });

                }
            }

    }
}

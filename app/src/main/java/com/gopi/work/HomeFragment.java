package com.gopi.work;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static android.content.ContentValues.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class
HomeFragment extends Fragment {

    private RecyclerView mBloglist;
    private LinearLayoutManager mLayoutManager;

    private DatabaseReference databaseReference;
    private DatabaseReference databaseusers;
    private DatabaseReference databaseFriends;

    private FirebaseAuth mAuth;

    private FloatingActionButton floatingActionButton;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_home, container, false);

        floatingActionButton = (FloatingActionButton) v.findViewById(R.id.myFAB);

        mBloglist = (RecyclerView) v.findViewById(R.id.blog_list);
        mBloglist.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(getContext());
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
        databaseReference.keepSynced(true);
        databaseusers = FirebaseDatabase.getInstance().getReference().child("User");
        databaseusers.keepSynced(true);
        databaseFriends = FirebaseDatabase.getInstance().getReference().child("Friends");
        databaseFriends.keepSynced(true);
        mBloglist.setLayoutManager(new LinearLayoutManager(getContext()));

        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                startActivity(new Intent(getContext(), Post.class));
            }
        });

        return v;

    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Blog, BlogViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Blog, BlogViewHolder>(
                Blog.class,
                R.layout.blog_view,
                BlogViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final BlogViewHolder viewHolder, final Blog model, final int position) {

                final String post_key = getRef(position).getKey();

                databaseFriends.child(mAuth.getCurrentUser().getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(model.getUserId()) || (model.getUserId().equals(mAuth.getCurrentUser().getUid()))){
                            viewHolder.setTitle(model.getTitle());
                            viewHolder.setDesc(model.getDesc());
                            viewHolder.setImage(getContext(), model.getImage());
                            viewHolder.setVisibility();
                            databaseReference.child(post_key).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    viewHolder.setShowComments(getContext(),dataSnapshot.hasChild("comment"),post_key);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            databaseusers.child(model.getUserId()).addValueEventListener(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {

                                    String name = dataSnapshot.child("name").getValue().toString();
                                    String image = dataSnapshot.child("thumb_image").getValue().toString();

                                    viewHolder.setUserImage(getContext(),image);
                                    viewHolder.setUsername(name);
                                    viewHolder.setSendComments(getContext(),post_key);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });

                            viewHolder.showDelete(model.getUserId(),post_key);
                        }else {
                            viewHolder.hide();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });






            }
        };

        mBloglist.setAdapter(firebaseRecyclerAdapter);
        mBloglist.setLayoutManager(mLayoutManager);
    }

    public static class BlogViewHolder extends RecyclerView.ViewHolder{

        private ProgressDialog prog;

        View view;

        public BlogViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setTitle(String title){

            TextView post_title = (TextView) view.findViewById(R.id.post_Title);
            post_title.setText(title);

        }

        public void setDesc(String desc){

            TextView post_desc = (TextView) view.findViewById(R.id.post_Desc);
            post_desc.setText(desc);
        }

        public void setImage(final Context ctx, final String img){

            prog = new ProgressDialog(ctx);

            final ImageView downloadImage = (ImageView) view.findViewById(R.id.downloadBlog);

            final ImageView post_img = (ImageView) view.findViewById(R.id.post_Img);
            Picasso.with(ctx). load(img).networkPolicy(NetworkPolicy.OFFLINE).into(post_img, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {

                            Picasso.with(ctx). load(img).into(post_img);

                        }
                    }
            );

            downloadImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    prog.setMessage("Downloading");
                    prog.setCanceledOnTouchOutside(false);
                    prog.show();
                    Retrofit.Builder builder = new Retrofit.Builder().baseUrl("https://www.google.co.in/");
                    Retrofit retrofit = builder.build();
                    ImageDownloadClient imageDownloadClient =retrofit.create(ImageDownloadClient.class);
                    Call<ResponseBody> call = imageDownloadClient.downloadFile(img);
                    call.enqueue(new retrofit2.Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                            Boolean status = writeResponseBodyToDisk(ctx,response.body());
                            prog.dismiss();
                            Toast.makeText(ctx,"Download" + status,Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            prog.dismiss();
                            Toast.makeText(ctx,"Download Failed",Toast.LENGTH_SHORT).show();
                        }
                    });

                }
            });
        }

        private Boolean writeResponseBodyToDisk(Context ctx, ResponseBody body) {

            try {
                final String currentDate = DateFormat.getDateTimeInstance().format(new Date());
                // todo change the file location/name according to your needs
                File blogImageFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)+ File.separator + "Tan Tana Tan_"+currentDate+".png");

                InputStream inputStream = null;
                OutputStream outputStream = null;

                try {
                    byte[] fileReader = new byte[4096];

                    long fileSize = body.contentLength();
                    long fileSizeDownloaded = 0;

                    inputStream = body.byteStream();
                    outputStream = new FileOutputStream(blogImageFile);

                    while (true) {
                        int read = inputStream.read(fileReader);

                        if (read == -1) {
                            break;
                        }

                        outputStream.write(fileReader, 0, read);

                        fileSizeDownloaded += read;

                        Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                    }

                    outputStream.flush();

                    return true;
                } catch (IOException e) {
                    return false;
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }
                }
            } catch (IOException e) {
                return false;
            }
        }

        public void setUsername(String username){
            TextView post_username = (TextView) view.findViewById(R.id.uname);
            post_username.setText(username);
        }

        public void setUserImage(final Context context, final String s){

            final ImageView userImage = (ImageView) view.findViewById(R.id.user_image_blog);

            Picasso.with(context).load(s).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.if_user_925901).into(userImage, new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(context).load(s).placeholder(R.drawable.if_user_925901).into(userImage);

                }
            });
        }

        public void setShowComments(final Context context, boolean showComments, final String post_key) {

            if (showComments){
                final TextView showComment = (TextView) view.findViewById(R.id.showComments);
                showComment.setVisibility(View.VISIBLE);

                showComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        callBottomSheet(showComment,context,R.layout.layout_comments,post_key,"Comments");
                    }
                });
            }

        }

        private void callBottomSheet(TextView showComment, Context context, int layout_comments, String postkey, String title) {
           BottomViewForComments.getInstance().getBottomSheet(showComment, context, layout_comments,postkey, title);
        }

        public void setSendComments(final Context sendComments, final String post_key) {

            final MultiAutoCompleteTextView mComments = (MultiAutoCompleteTextView) view.findViewById(R.id.comment);
            ImageView mPostComment = (ImageView) view.findViewById(R.id.sendComment);

            mPostComment.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    String comments = mComments.getText().toString().trim();

                    if (!TextUtils.isEmpty(comments)){

                        final String userid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");

                        Map commentMap = new HashMap();
                        commentMap.put("user_id", userid);
                        commentMap.put("commentText", comments);

                        databaseReference.child(post_key).child("comment").push().setValue(commentMap);
                    }
                }
            });
        }

        public void showDelete(String userId, final String post_key) {

            ImageView delete = (ImageView) view.findViewById(R.id.deleteBlog);

            if(FirebaseAuth.getInstance().getCurrentUser().getUid().equals(userId)){
                delete.setVisibility(View.VISIBLE);

                delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Blog");
                        mDatabaseReference.child(post_key).removeValue();
                    }
                });
            }else {
                delete.setVisibility(View.GONE);
            }
        }

        public void setVisibility(){

            RelativeLayout cardlayout = (RelativeLayout) view.findViewById(R.id.layoutForCardView);
            cardlayout.setVisibility(View.VISIBLE);

            CardView mCardView = (CardView) view.findViewById(R.id.cardView);
            mCardView.setVisibility(View.VISIBLE);
        }

        public void hide() {

            // Gets linearlayout
            RelativeLayout cardlayout = (RelativeLayout) view.findViewById(R.id.layoutForCardView);
            cardlayout.setVisibility(View.GONE);
            setMargins(view,0,0,0,0);
            CardView layout = (CardView) view.findViewById(R.id.cardView);
            // Gets the layout params that will allow you to resize the layout
            ViewGroup.LayoutParams params = layout.getLayoutParams();
            // Changes the height and width to the specified *pixels*
            params.height = 0;
            layout.setLayoutParams(params);
        }

        public static void setMargins (View v, int l, int t, int r, int b) {
            if (v.getLayoutParams() instanceof ViewGroup.MarginLayoutParams) {
                ViewGroup.MarginLayoutParams p = (ViewGroup.MarginLayoutParams) v.getLayoutParams();
                p.setMargins(l, t, r, b);
                v.requestLayout();
            }
        }
    }
}

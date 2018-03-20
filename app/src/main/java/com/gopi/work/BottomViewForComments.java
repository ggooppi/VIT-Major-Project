package com.gopi.work;

import android.content.Context;
import android.support.design.widget.BottomSheetDialog;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by gopinath.a on 20/3/18.
 */

public class BottomViewForComments {

    public static BottomViewForComments bottomViewForComments = null;

    public static BottomViewForComments getInstance(){
        if(bottomViewForComments==null){
            bottomViewForComments=new BottomViewForComments();
        }
        return bottomViewForComments;
    }

    public void getBottomSheet(TextView showComment, final Context context, final int layout_comments, final String blogKey, String title) {

        final DatabaseReference databaseusers = FirebaseDatabase.getInstance().getReference().child("User");

        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Blog").child(blogKey).child("comment");
        databaseReference.keepSynced(true);

        BottomSheetDialog bottomSheetDialog=new BottomSheetDialog(context);
        bottomSheetDialog.setContentView(layout_comments);

        RecyclerView barRecycler= (RecyclerView) bottomSheetDialog.findViewById(R.id.bar_details_recycler_view);

       // barRecycler.setLayoutManager(new LinearLayoutManager(context));

        AppCompatTextView detailsTitle= (AppCompatTextView) bottomSheetDialog.findViewById(R.id.bar_details_title);

        final MultiAutoCompleteTextView mComments = (MultiAutoCompleteTextView) bottomSheetDialog.findViewById(R.id.commentBottom);
        ImageView mPostComment = (ImageView) bottomSheetDialog.findViewById(R.id.sendCommentBottom);

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

                    databaseReference.child(blogKey).child("comment").push().setValue(commentMap);
                }
            }
        });

        if(title==null){
            detailsTitle.setVisibility(View.GONE);
        }else{
            detailsTitle.setText(title);
        }

        LinearLayoutManager horizontalLayoutManagaer = new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false);
        barRecycler.setLayoutManager(horizontalLayoutManagaer);

        FirebaseRecyclerAdapter<Comments, CommentViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Comments, CommentViewHolder>(
                Comments.class,
                R.layout.comment_view,
                CommentViewHolder.class,
                databaseReference
        ) {
            @Override
            protected void populateViewHolder(final CommentViewHolder viewHolder, final Comments model, int position) {

                final String post_key = getRef(position).getKey();

                databaseusers.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String name = dataSnapshot.child(model.getUser_id()).child("name").getValue().toString();
                        viewHolder.setName(name);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                if (model.getCommentText()!=null){
                    viewHolder.setComment(model.getCommentText());
                }
            }
        };

        barRecycler.setAdapter(firebaseRecyclerAdapter);

        bottomSheetDialog.show();

        bottomSheetDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
    }

    public static class CommentViewHolder extends RecyclerView.ViewHolder{

        View view;

        public CommentViewHolder(View itemView) {
            super(itemView);

            view = itemView;
        }

        public void setName(String name) {
            TextView uName = (TextView) view.findViewById(R.id.userName);
            uName.setText(name);
        }

        public void setComment(String comment) {

            TextView uComment = (TextView) view.findViewById(R.id.userComment);
            uComment.setText(comment);
        }

    }
}

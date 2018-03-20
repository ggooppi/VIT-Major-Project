package com.gopi.work;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gopinath.a on 28/2/18.
 */

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Messages> mMessageList;

    private FirebaseAuth mAuth;


    public MessageAdapter(List<Messages> mMessageList){
        this.mMessageList = mMessageList;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

         View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.messgae_receive, parent,false);

        return new MessageViewHolder(v);

    }



    @Override
    public void onBindViewHolder(MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();

        Messages c = mMessageList.get(position);
        String message_type = c.getType();

        String current_user_id = mAuth.getCurrentUser().getUid();

        String from_user = c.getFrom();

        if(message_type.equals("text")) {

            if (from_user.equals(current_user_id)){

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(60, 10, 10, 2);
                params.addRule(RelativeLayout.ALIGN_PARENT_END);

                holder.messageText.setBackgroundResource(R.drawable.send);
                holder.messageText.setLayoutParams(params);
                holder.messageText.setTextColor(Color.WHITE);
                holder.messageText.setGravity(View.FOCUS_RIGHT);
            }

            holder.messageText.setText(c.getMessage());
            holder.messageImage.setVisibility(View.GONE);


        } else {

            if (from_user.equals(current_user_id)){

                RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(60, 10, 10, 2);
                holder.messageImage.setLayoutParams(params);
            }

            holder.messageText.setVisibility(View.GONE);
            Picasso.with(holder.messageText.getContext()).load(c.getMessage())
                    .placeholder(R.drawable.if_user_925901).into(holder.messageImage);

        }




    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder{

        public TextView messageText;
        public ImageView messageImage;


        public MessageViewHolder(View v) {
            super(v);

            messageText = (TextView) v.findViewById(R.id.messageTextLayout);
            messageImage = (ImageView) v.findViewById(R.id.message_image_layout);
        }
    }
}

package com.gopi.work;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by gopinath.a on 23/3/18.
 */

public class MessageChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private List<ChatMessage> mChatList;
    public static final int SENDER = 0;
    public static final int RECIPIENT = 1;

    public MessageChatAdapter(List<ChatMessage> listOfFireChats) {
        mChatList = listOfFireChats;
    }

    @Override
    public int getItemViewType(int position) {
        if (mChatList.get(position).getRecipientOrSenderStatus() == SENDER) {
            if (mChatList.get(position).getType() == "image"){
                return 100;
            }else return SENDER;
        } else {

            if (mChatList.get(position).getType() == "image"){
                return 200;
            }else return RECIPIENT;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());

        switch (viewType) {
            case SENDER:
                View viewSender = inflater.inflate(R.layout.messagesent, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSender);
                break;
            case RECIPIENT:
                View viewRecipient = inflater.inflate(R.layout.messgae_receive, viewGroup, false);
                viewHolder = new ViewHolderRecipient(viewRecipient);
                break;
            case 100:
                View viewImageSender = inflater.inflate(R.layout.layout_sender_image, viewGroup, false);
                viewHolder = new ViewHolderRecipient(viewImageSender);
                break;
            case 200:
                View viewImageRecipient = inflater.inflate(R.layout.layout_recipient_image, viewGroup, false);
                viewHolder = new ViewHolderRecipient(viewImageRecipient);
                break;
            default:
                View viewSenderDefault = inflater.inflate(R.layout.messagesent, viewGroup, false);
                viewHolder = new ViewHolderSender(viewSenderDefault);
                break;
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int position) {

        ChatMessage c = mChatList.get(position);
        String message_type = c.getType();

        switch (viewHolder.getItemViewType()) {
            case SENDER:
                ViewHolderSender viewHolderSender = (ViewHolderSender) viewHolder;
                configureSenderView(viewHolderSender, message_type, position);
                break;
            case RECIPIENT:
                ViewHolderRecipient viewHolderRecipient = (ViewHolderRecipient) viewHolder;
                configureRecipientView(viewHolderRecipient, message_type, position);
                break;
        }


    }

    private void configureSenderView(final ViewHolderSender viewHolderSender, String type, int position) {
        if (type.equals("image")){
            final ChatMessage senderImageMessage = mChatList.get(position);
            Picasso.with(viewHolderSender.getSenderMessageImageView().getContext()).load(senderImageMessage.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.if_user_925901).into(viewHolderSender.getSenderMessageImageView(), new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(viewHolderSender.getSenderMessageImageView().getContext()).load(senderImageMessage.getMessage()).placeholder(R.drawable.if_user_925901).into(viewHolderSender.getSenderMessageImageView());

                }
            });
        }else {
            ChatMessage senderFireMessage = mChatList.get(position);
            viewHolderSender.getSenderMessageTextView().setText(senderFireMessage.getMessage());
        }
    }

    private void configureRecipientView(final ViewHolderRecipient viewHolderRecipient, String type, int position) {
        if (type.equals("image")){
            final ChatMessage recipientFireMessage = mChatList.get(position);
            Picasso.with(viewHolderRecipient.getRecipientMessageImageView().getContext()).load(recipientFireMessage.getMessage()).networkPolicy(NetworkPolicy.OFFLINE)
                    .placeholder(R.drawable.if_user_925901).into(viewHolderRecipient.getRecipientMessageImageView(), new Callback() {
                @Override
                public void onSuccess() {

                }

                @Override
                public void onError() {

                    Picasso.with(viewHolderRecipient.getRecipientMessageImageView().getContext()).load(recipientFireMessage.getMessage()).placeholder(R.drawable.if_user_925901).into(viewHolderRecipient.getRecipientMessageImageView());

                }
            });

        }else {
            ChatMessage recipientFireMessage = mChatList.get(position);
            viewHolderRecipient.getRecipientMessageTextView().setText(recipientFireMessage.getMessage());
        }

    }

    @Override
    public int getItemCount() {
        return mChatList.size();
    }


    public void refillAdapter(ChatMessage newFireChatMessage) {

        /*add new message chat to list*/
        mChatList.add(newFireChatMessage);

        /*refresh view*/
       // notifyItemInserted(getItemCount()-1);
    }


    public void cleanUp() {
        mChatList.clear();
    }


    /*==============ViewHolder===========*/


    /*ViewHolder for Sender*/

    public class ViewHolderSender extends RecyclerView.ViewHolder {

        private TextView mSenderMessageTextView;
        private ImageView mSenderImageView;

        public ViewHolderSender(View itemView) {
            super(itemView);
            mSenderMessageTextView = (TextView) itemView.findViewById(R.id.messageTextLayout);
            mSenderImageView = (ImageView) itemView.findViewById(R.id.senderImage);
        }

        public ImageView getSenderMessageImageView() {
            mSenderImageView.setVisibility(View.VISIBLE);
            return mSenderImageView;
        }

        public TextView getSenderMessageTextView() {
            mSenderMessageTextView.setVisibility(View.VISIBLE);
            return mSenderMessageTextView;
        }

    }


    /*ViewHolder for Recipient*/
    public class ViewHolderRecipient extends RecyclerView.ViewHolder {

        private TextView mRecipientMessageTextView;
        private ImageView mRecipientImage;

        public ViewHolderRecipient(View itemView) {
            super(itemView);
            mRecipientMessageTextView = (TextView) itemView.findViewById(R.id.messageReceiveTextLayout);
            mRecipientImage = (ImageView) itemView.findViewById(R.id.recipientImage);
        }

        public ImageView getRecipientMessageImageView() {
            mRecipientImage.setVisibility(View.VISIBLE);
            return mRecipientImage;
        }

        public TextView getRecipientMessageTextView() {
            mRecipientMessageTextView.setVisibility(View.VISIBLE);
            return mRecipientMessageTextView;
        }

    }
}

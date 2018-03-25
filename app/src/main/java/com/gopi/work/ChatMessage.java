package com.gopi.work;

import com.google.firebase.database.Exclude;

/**
 * Created by gopinath.a on 23/3/18.
 */

public class ChatMessage {
    private String message;
    private String sender;
    private String recipient;
    private String type;

    private int mRecipientOrSenderStatus;

    public ChatMessage() {
    }

    public ChatMessage(String message, String sender, String recipient, String type) {
        this.message = message;
        this.recipient = recipient;
        this.sender = sender;
        this.type = type;
    }


    public void setRecipientOrSenderStatus(int recipientOrSenderStatus) {
        this.mRecipientOrSenderStatus = recipientOrSenderStatus;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public String getMessage() {
        return message;
    }

    public String getRecipient(){
        return recipient;
    }

    public String getSender(){
        return sender;
    }

    @Exclude
    public int getRecipientOrSenderStatus() {
        return mRecipientOrSenderStatus;
    }

}

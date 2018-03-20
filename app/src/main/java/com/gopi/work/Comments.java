package com.gopi.work;

/**
 * Created by gopinath.a on 20/3/18.
 */

public class Comments {

    String user_id;
    String commentText;

    public Comments() {
    }

    public Comments(String user_id, String commentText) {
        this.user_id = user_id;
        this.commentText = commentText;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getCommentText() {
        return commentText;
    }

    public void setCommentText(String commentText) {
        this.commentText = commentText;
    }
}

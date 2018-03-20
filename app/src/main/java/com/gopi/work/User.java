package com.gopi.work;

/**
 * Created by Gopi on 28-12-2017.
 */

public class User {

    public String image;
    public String status;
    public String name;
    public String thumb_image;
    public String ph;

    public User(){

    }

    public User(String image, String status, String name, String thumb_image, String ph) {
        this.image = image;
        this.status = status;
        this.name = name;
        this.thumb_image = thumb_image;
    }

    public String getThumb_image() {
        return thumb_image;
    }

    public void setThumb_image(String thumb_image) {
        this.thumb_image = thumb_image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPh() {
        return ph;
    }

    public void setPh(String ph) {
        this.ph = ph;
    }
}

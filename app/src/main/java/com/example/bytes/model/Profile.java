package com.example.bytes.model;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;
import androidx.room.TypeConverters;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;
import com.google.firebase.database.ServerValue;

import java.io.Serializable;

@IgnoreExtraProperties
@Entity
public class Profile implements Comparable<Profile>, Serializable {
    @PrimaryKey
    @NonNull
    private String uid;
    @NonNull
    private String name;
    //private String contactName;
    private String number;
    private String thumbnailURL;
    private String thumbnailURI;
    private String profileImageURL;
    private String profileImageURI;
    private String bio;
    @NonNull
    private Object lastUpdated = ServerValue.TIMESTAMP;
    @NonNull
    private Object lastOnline = ServerValue.TIMESTAMP;

    public enum Variable {
        UID("uid"),
        NAME("name"),
        CONTACT_NAME("contactName"),
        NUMBER("number"),
        THUMBNAIL_URL("thumbnailURL"),
        THUMBNAIL_URI("thumbnailURI"),
        PROFILE_IMAGE_URL("profileImageURL"),
        PROFILE_IMAGE_URI("profileImageURI"),
        BIO("bio"),
        LAST_UPDATED("lastUpdated"),
        LAST_ONLINE("lastOnline");

        private String value;

        Variable(@NonNull String value){
            this.value = value;
        }

        @NonNull
        @Override
        public String toString() {
            return this.value;
        }
    }

    public Profile(@NonNull String uid, @NonNull String name){
        this.uid = uid;
        this.name = name;
    }

    @Ignore
    public Profile(){}

    @NonNull
    public String getUid() {
        return uid;
    }

    @NonNull
    public String getName() {
        return name;
    }

//    @Exclude
//    public String getContactName() {
//        return contactName;
//    }

    public String getNumber() {
        return number;
    }

    public String getThumbnailURL() {
        return thumbnailURL;
    }

    @Exclude
    public String getThumbnailURI() {
        return thumbnailURI;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    @Exclude
    public String getProfileImageURI() {
        return profileImageURI;
    }

    public String getBio() {
        return bio;
    }

    @NonNull
    public Object getLastUpdated() throws ClassCastException{
        return lastUpdated;
    }

    @NonNull
    public Object getLastOnline() throws ClassCastException{
        return lastOnline;
    }

    public void setUid(@NonNull String uid) {
        this.uid = uid;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

//    public void setContactName(String contactName) {
//        this.contactName = contactName;
//    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setThumbnailURL(String thumbnailURL) {
        this.thumbnailURL = thumbnailURL;
    }

    public void setThumbnailURI(String thumbnailURI) {
        this.thumbnailURI = thumbnailURI;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public void setProfileImageURI(String profileImageURI) {
        this.profileImageURI = profileImageURI;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setLastUpdated(@NonNull Object lastUpdated){
        this.lastUpdated = lastUpdated;
    }

    public void setLastOnline(@NonNull Object lastOnline){
        this.lastOnline = lastOnline;
    }

    @Override
    public int compareTo(@NonNull Profile o) {
        if(!uid.equals(o.uid))
            return 0;
        else if(!name.equals(o.name))
            return 0;
        else if(number == null || !number.equals(o.number))
            return 0;
//        else if(contactName == null || !contactName.equals(o.contactName))
//            return 0;
        else if(thumbnailURL == null || !thumbnailURL.equals(o.thumbnailURL))
            return 0;
        else if(thumbnailURI == null || !thumbnailURI.equals(o.thumbnailURI))
            return 0;
        else if(profileImageURL == null || !profileImageURL.equals(o.profileImageURL))
            return 0;
        else if(profileImageURI == null || !profileImageURI.equals(o.profileImageURI))
            return 0;
        else if(bio == null || !bio.equals(o.bio))
            return 0;
        else if(!lastUpdated.equals(o.getLastUpdated()))
            return 0;
        else
            return 1;
    }

    public static class Builder{
        private String uid;
        private String name;
        private String number;
        private String thumbnailURL;
        private String profileImageURL;
        private String bio;

        public Builder(@NonNull String uid, @NonNull String name){
            this.uid = uid;
            this.name = name;
        }

        public Builder setNumber(@NonNull String number){
            this.number = number;
            return this;
        }

        public Builder setThumbnailURL(@NonNull String thumbnailURL){
            this.thumbnailURL = thumbnailURL;
            return this;
        }

        public Builder setProfileImageURL(@NonNull String profileImageURL){
            this.profileImageURL = profileImageURL;
            return this;
        }

        public Builder setBio(@NonNull String bio){
            this.bio = bio;
            return this;
        }

        public Profile build(){
            Profile profile = new Profile(uid, name);
            profile.number = number;
            profile.thumbnailURL = thumbnailURL;
            profile.profileImageURL = profileImageURL;
            profile.bio = bio;

            return profile;
        }
    }

    public static class Converter{
        @TypeConverter
        public Long objectToLong(Object lastUpdated){
            return (long) lastUpdated;
        }

        @TypeConverter
        public Object longToObject(Long lastUpdated){
            return lastUpdated;
        }
    }
}

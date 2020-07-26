package com.example.bytes.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.ServerValue;

@Entity
public class Message {
    @PrimaryKey
    @NonNull
    private String messageId;
    private String chatId;
    private String senderUid;
    private String receiverUid;
    private String data;
    private Object time = ServerValue.TIMESTAMP;
    private boolean seen = false;

    public enum Variable{
        MESSAGE_ID("messageId"),
        CHAT_ID("chatId"),
        SENDER_UID("senderUid"),
        RECEIVER_UID("receiverUid"),
        DATA("data"),
        TIME("time"),
        SEEN("seen");

        String value;
        Variable(String value){
            this.value = value;
        }


        @NonNull
        @Override
        public String toString() {
            return this.value;
        }
    }

    public Message(@NonNull String messageId, @NonNull String chatId, @NonNull String senderUid, @NonNull String receiverUid, @NonNull String data){
        this.messageId = messageId;
        this.chatId = chatId;
        this.senderUid = senderUid;
        this.receiverUid = receiverUid;
        this.data = data;
    }

    public Message(){}

    @NonNull
    public String getMessageId() {
        return messageId;
    }

    public String getChatId() {
        return chatId;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getReceiverUid() {
        return receiverUid;
    }

    public String getData() {
        return data;
    }

    public long getTime() throws ClassCastException{
        return (long)time;
    }

    @Exclude
    public boolean getSeen(){
        return seen;
    }

    public void setMessageId(@NonNull String messageId) {
        this.messageId = messageId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setSenderUid(String senderUid) {
        this.senderUid = senderUid;
    }

    public void setReceiverUid(String receiverUid) {
        this.receiverUid = receiverUid;
    }

    public void setData(String data) {
        this.data = data;
    }

    public void setTime(Object time) {
        this.time = time;
    }

    public void setSeen(boolean seen){
        this.seen = seen;
    }

    public static class Converter{
        @TypeConverter
        public static long objectToLong(Object lastUpdated){
            return (long)lastUpdated;
        }

        @TypeConverter
        public static Object longToObject(long lastUpdated){
            return lastUpdated;
        }
    }
}

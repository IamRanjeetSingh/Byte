package com.example.bytes.model.handler;

import android.app.Application;
import android.database.Cursor;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.bytes.model.Message;
import com.example.bytes.model.MessageDao;
import com.example.bytes.model.ProfileDao;
import com.example.bytes.model.CacheDatabase;
import com.example.bytes.model.Contact;
import com.example.bytes.model.Profile;

import java.util.Currency;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CacheHandler {
    private static final String TAG = "CacheHandler";

    private static CacheHandler instance;

    private ProfileDao profileDao;
    private MessageDao messageDao;
    private Executor executor;

    private CacheHandler(@NonNull Application application){
        profileDao = CacheDatabase.getInstance(application).getProfileDao();
        messageDao = CacheDatabase.getInstance(application).getMessageDao();
        executor = Executors.newSingleThreadExecutor();
    }

    public static CacheHandler getInstance(@NonNull Application application){
        if(instance == null)
            instance = new CacheHandler(application);
        return instance;
    }

    public void hasProfile(@NonNull final String uid, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Cursor result = profileDao.hasProfile(uid);
                result.moveToFirst();
                boolean hasProfile = result.getInt(result.getColumnIndex(result.getColumnNames()[0])) == 1;
                sendToMainThread(hasProfile, listener);
            }
        });
    }

    public void addProfile(@NonNull final Profile profile, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccessful = profileDao.insertProfile(profile) != -1;
                sendToMainThread(isSuccessful, listener);
            }
        });
    }

    public void deleteProfile(@NonNull final Profile profile, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccessful = profileDao.deleteProfile(profile) != 0;
                if(!isSuccessful) Log.e(TAG, "deleteProfile: Error Occurred while deleting profile from local cache", new RuntimeException());
                sendToMainThread(isSuccessful, listener);
            }
        });
    }

    public void getProfile(@NonNull final Contact contact, @NonNull final OnHandlerResponseListener<Profile> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Profile profile = profileDao.getProfileFromNumber(contact.getNumber());
                sendToMainThread(profile, listener);
            }
        });
    }

    public void getChats(@NonNull final OnHandlerResponseListener<Cursor> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                sendToMainThread(messageDao.getChats(), listener);
            }
        });
    }

    public void readMessages(@NonNull final String uid, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                messageDao.readMessages(uid);
                sendToMainThread(true, listener);
            }
        });
    }

    public void addMessage(@NonNull final Message message, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean isSuccessful = messageDao.addMessage(message) != -1;
                sendToMainThread(isSuccessful, listener);
            }
        });
    }

    public void getMessages(@NonNull final String chatId, @NonNull final OnHandlerResponseListener<Cursor> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                Cursor messages = messageDao.getMessages(chatId);
                sendToMainThread(messages, listener);
            }
        });
    }

    private <T> void sendToMainThread(@Nullable final T result, @NonNull final OnHandlerResponseListener<T> listener){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(result);
            }
        });
    }
}

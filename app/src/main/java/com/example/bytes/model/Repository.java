package com.example.bytes.model;

import android.app.Application;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bytes.model.handler.CacheHandler;
import com.example.bytes.model.handler.NetworkHandler;
import com.example.bytes.model.handler.OnHandlerResponseListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class Repository {
    private static final String TAG = "Repository";

    public static final String NEW_MESSAGE_ACTION = Repository.class.getCanonicalName()+"_newMessage";
    public static final String NEW_UID_MESSAGE_ACTION = Repository.class.getCanonicalName()+"_newUser_Message_";
    
    private static Repository instance;

    private Application application;
    private LocalBroadcastManager localBroadcastManager;
    private NetworkHandler networkHandler;
    private CacheHandler cacheHandler;
    private ChildEventListener newMessageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            final Message message = snapshot.getValue(Message.class);
            if(message == null){
                Log.e(TAG, "onChildAdded: Couldn't get message from snapshot", new NullPointerException());
                return;
            }

            addReceivedMessage(message, new OnRepositoryResponseListener<Boolean>() {
                @Override
                public void onResponse(Boolean isSuccessful) {
                    if(!isSuccessful){
                        Log.d(TAG, "onResponse: Error occurred while handling newly received message");
                        return;
                    }
                    localBroadcastManager.sendBroadcast(new Intent(NEW_MESSAGE_ACTION));
                    Log.d(TAG, "onResponse: "+NEW_UID_MESSAGE_ACTION+message.getChatId());
                    localBroadcastManager.sendBroadcast(new Intent(NEW_UID_MESSAGE_ACTION+message.getChatId()));
                }
            });
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {}
        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {}
        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e(TAG, "onCancelled: Error occurred while getting new messages from firebase", error.toException());
        }
    };
    private boolean isGettingNewMessages = false;

    public interface OnRepositoryResponseListener<T> {
        void onResponse(T response);
    }

    private Repository(@NonNull Application application) {
        this.application = application;
        localBroadcastManager = LocalBroadcastManager.getInstance(application);
        networkHandler = NetworkHandler.getInstance();
        cacheHandler = CacheHandler.getInstance(application);
    }

    public static Repository getInstance(@NonNull Application application) {
        if (instance == null)
            instance = new Repository(application);
        return instance;
    }

    public void init(){
        if(!isGettingNewMessages) {
            final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Log.d(TAG, "init: No current Firebase user found");
                return;
            }
            isGettingNewMessages = true;
            networkHandler.getLastOnline(user.getUid(), new OnHandlerResponseListener<Long>() {
                @Override
                public void onResponse(Long lastOnline) {
                    if (lastOnline == null) {
                        Log.d(TAG, "onResponse: Error occurred while getting last online from firebase");
                        return;
                    }
                    networkHandler.setLastOnline(user.getUid());
                    networkHandler.getMessagesAfter(user.getUid(), lastOnline, newMessageListener);
                }
            });
        }
    }

    public String getMyUid(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user == null){
            Log.e(TAG, "getMyUid: No currently signed in firebase user", new NullPointerException());
            return null;
        }
        return user.getUid();
    }

    public void hasCurrentUser(@NonNull OnRepositoryResponseListener<Boolean> listener){
        listener.onResponse(FirebaseAuth.getInstance().getCurrentUser() != null);
    }

    public void createNewProfile(@NonNull Profile profile, @NonNull final OnRepositoryResponseListener<Boolean> listener){
        networkHandler.createNewProfile(profile, new OnHandlerResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                listener.onResponse(response);
            }
        });
    }

    public void getContacts(@NonNull OnRepositoryResponseListener<List<Contact>> listener){
        List<Contact> phoneContacts = new ArrayList<>();

        Uri contentUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String displayNameCol = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, numberCol = ContactsContract.CommonDataKinds.Phone.NUMBER;
        String[] projections = {displayNameCol, numberCol};
        String sortOrder = ContactsContract.CommonDataKinds.Phone.SORT_KEY_PRIMARY;

        Cursor phoneContactsCursor = application.getContentResolver().query(contentUri, projections, null, null, sortOrder);
        if(phoneContactsCursor == null){
            Log.e(TAG, "getPhoneContacts: ContactsContract return Null contacts", new NullPointerException());
            listener.onResponse(phoneContacts);
            return;
        }

        while(phoneContactsCursor.moveToNext()){
            String name = phoneContactsCursor.getString(phoneContactsCursor.getColumnIndex(displayNameCol));
            String number = phoneContactsCursor.getString(phoneContactsCursor.getColumnIndex(numberCol));
            phoneContacts.add(new Contact(name, number));
        }

        phoneContactsCursor.close();
        listener.onResponse(phoneContacts);
    }

    public void getProfileFromCache(@NonNull Contact contact, @NonNull final OnRepositoryResponseListener<Profile> listener){
        cacheHandler.getProfile(contact, new OnHandlerResponseListener<Profile>(){
            @Override
            public void onResponse(Profile profile) {
                listener.onResponse(profile);
            }
        });
    }

    public void getProfileFromFirebase(@NonNull final String field, @NonNull final String value, @NonNull final OnRepositoryResponseListener<Profile> listener){
        networkHandler.getProfile(field, value, new OnHandlerResponseListener<Profile>() {
            @Override
            public void onResponse(Profile profile) {
                if(profile != null){
                    addProfileToCache(profile);
                }
                listener.onResponse(profile);
            }

            private void addProfileToCache(@NonNull Profile profile){
                cacheHandler.addProfile(profile, new OnHandlerResponseListener<Boolean>() {
                    @Override
                    public void onResponse(Boolean isSuccessful) {
                        if(!isSuccessful) Log.d(TAG, "onResponse: Error occurred while saving profile in local cache");
                    }
                });
            }
        });
    }

    public void updateIfNeeded(@NonNull final Profile profile, @NonNull final OnRepositoryResponseListener<Profile> listener){
        networkHandler.requireUpdate(profile, new OnHandlerResponseListener<Boolean>() {
            @Override
            public void onResponse(Boolean requireUpdate) {
                if (requireUpdate) {
                    updateProfile(profile);
                } else {
                    listener.onResponse(profile);
                }
            }

            private void updateProfile(@NonNull final Profile profile){
                getProfileFromFirebase(Profile.Variable.UID.toString(), profile.getUid(), new OnRepositoryResponseListener<Profile>() {
                    @Override
                    public void onResponse(Profile profile) {
                        listener.onResponse(profile);
                    }
                });
            }
        });
    }

    public void getChats(@NonNull final OnRepositoryResponseListener<Cursor> listener){
        cacheHandler.getChats(new OnHandlerResponseListener<Cursor>() {
            @Override
            public void onResponse(Cursor response) {
                listener.onResponse(response);
            }
        });
    }

    public void addReceivedMessage(@NonNull final Message message, @NonNull final OnRepositoryResponseListener<Boolean> listener){
        cacheHandler.hasProfile(message.getChatId(), new OnHandlerResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                if (!response) {
                    getProfileFromFirebase(Profile.Variable.UID.toString(), message.getChatId(), new OnRepositoryResponseListener<Profile>() {
                        @Override
                        public void onResponse(Profile profile) {
                            if(profile == null) {
                                Log.d(TAG, "onResponse: Profile for this message's sender not found on firebase, ignoring message");
                                listener.onResponse(false);
                                return;
                            }
                            addMessage(message);
                        }
                    });
                    return;
                }
                addMessage(message);
            }

            private void addMessage(@NonNull Message message){
                cacheHandler.addMessage(message, new OnHandlerResponseListener<Boolean>() {
                    @Override
                    public void onResponse(Boolean response) {
                        if (!response) Log.d(TAG, "onResponse: Error occurred while storing message in cache");
                        listener.onResponse(response);
                    }
                });
            }

        });
    }

    public void readMessages(@NonNull final String uid, @NonNull final OnRepositoryResponseListener<Boolean> listener){
        cacheHandler.readMessages(uid, new OnHandlerResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                listener.onResponse(response);
            }
        });
    }

    public void getMessages(@NonNull String chatId, @NonNull final OnRepositoryResponseListener<Cursor> listener){
        cacheHandler.getMessages(chatId, new OnHandlerResponseListener<Cursor>(){
            @Override
            public void onResponse(Cursor messages) {
                listener.onResponse(messages);
            }
        });
    }

    public void sendMessage(@NonNull String receiverUid, @NonNull String messageData, @NonNull final OnRepositoryResponseListener<Boolean> listener){
        if(FirebaseAuth.getInstance().getCurrentUser() == null) throw new RuntimeException("No Current Firebase User");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String messageId = networkHandler.getNewMessageId(user.getUid());
        Message senderMessage = new Message(messageId, receiverUid, user.getUid(), receiverUid, messageData);
        senderMessage.setTime(System.currentTimeMillis());
        Message receiverMessage = new Message(messageId, user.getUid(), user.getUid(), receiverUid, messageData);
        receiverMessage.setTime(senderMessage.getTime());

        cacheHandler.addMessage(senderMessage, new OnHandlerResponseListener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(!response) Log.d(TAG, "onResponse: Error occurred while adding message to cache");
                listener.onResponse(response);
            }
        });

        networkHandler.sendMessage(user.getUid(), senderMessage, new OnHandlerResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                if(!response) Log.d(TAG, "onResponse: Error Occurred while adding message to my database on Firebase");
            }
        });

        networkHandler.sendMessage(receiverUid, receiverMessage, new OnHandlerResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                if(!response) Log.d(TAG, "onResponse: Error Occurred while adding message to receiver's database on Firebase");
            }
        });
    }

    //this method is broken don't use it
    public void getMessagesFromFirebase(@NonNull String uid, @NonNull final OnRepositoryResponseListener<Boolean> listener){
        networkHandler.getMessages(uid, new OnHandlerResponseListener<DataSnapshot>(){
            @Override
            public void onResponse(DataSnapshot allMessages) {
                if(allMessages != null) {
                    final long[] messageAck = {allMessages.getChildrenCount()};
                    for (DataSnapshot messageSnapshot : allMessages.getChildren()) {
                        final Message message = messageSnapshot.getValue(Message.class);
                        if(message == null) {
                            Log.e(TAG, "onResponse: Message snapshot from all messages snapshot is Null", new NullPointerException());
                            continue;
                        }


                    }
                } else{
                    Log.d(TAG, "onResponse: Error occurred while getting messages from firebase");
                    listener.onResponse(false);
                }
            }
        });
    }
}
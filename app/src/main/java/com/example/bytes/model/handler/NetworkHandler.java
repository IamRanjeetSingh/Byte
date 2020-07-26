package com.example.bytes.model.handler;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;

import com.example.bytes.model.Contact;
import com.example.bytes.model.Message;
import com.example.bytes.model.Profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NetworkHandler {
    private static final String TAG = "NetworkHandler";

    private static final String FIREBASE_PROFILES_BRANCH = "Profiles";
    private static final String FIREBASE_CHATS_BRANCH = "Chats";
    private static final String FIREBASE_GROUPS_BRANCH = "Groups";

    private static NetworkHandler instance;

    private Executor executor;

    private NetworkHandler(){
        executor = Executors.newSingleThreadExecutor();
    }

    public static NetworkHandler getInstance(){
        if(instance == null)
            instance = new NetworkHandler();
        return instance;
    }

    public void createNewProfile(@NonNull final Profile profile, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference(FIREBASE_PROFILES_BRANCH).child(profile.getUid()).setValue(profile)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Log.e(TAG, "onComplete: Error occurred while creating new Profile on Firebase Database", new RuntimeException());
                                    sendToMainThread(false, listener);
                                    return;
                                }
                                sendToMainThread(true, listener);
                            }
                        });
            }
        });

    }

    public void getLastOnline(@NonNull String uid, final OnHandlerResponseListener<Long> listener){
        FirebaseDatabase.getInstance().getReference(FIREBASE_PROFILES_BRANCH).child(uid).child(Profile.Variable.LAST_ONLINE.toString())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(!snapshot.exists()){
                            Log.e(TAG, "onDataChange: Last online snapshot doesn't exists for the given uid", new NullPointerException());
                            sendToMainThread(null, listener);
                            return;
                        }
                        sendToMainThread(snapshot.getValue(Long.class), listener);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: Error occurred while getting last online from Firebase", error.toException());
                        sendToMainThread(null, listener);
                    }
                });
    }

    public void setLastOnline(@NonNull String uid){
        FirebaseDatabase.getInstance().getReference(FIREBASE_PROFILES_BRANCH).child(uid).child(Profile.Variable.LAST_ONLINE.toString()).setValue(ServerValue.TIMESTAMP)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(!task.isSuccessful()){
                            Log.e(TAG, "onComplete: Error occurred while setting last online for given uid", task.getException());
                        }
                    }
                });
    }

    /**
     * Download profile from firebase database
     * @param field Field can either be 'number' or 'uid'
     * @param value Value should be either contact number or profile uid
     */
    public void getProfile(@NonNull final String field, @NonNull final String value, @NonNull final OnHandlerResponseListener<Profile> listener){
        Query query = FirebaseDatabase.getInstance().getReference(FIREBASE_PROFILES_BRANCH).orderByChild(field).equalTo(value);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.getChildrenCount() > 0) {
                    Profile profile = snapshot.getChildren().iterator().next().getValue(Profile.class);
                    sendToMainThread(profile, listener);
                } else{
                    sendToMainThread(null, listener);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: while getting profile from Firebase Database", error.toException());
                sendToMainThread(null, listener);
            }
        });
    }

    public void requireUpdate(@NonNull final Profile profile, @NonNull final OnHandlerResponseListener<Boolean> listener) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference(FIREBASE_PROFILES_BRANCH).child(profile.getUid()).child(Profile.Variable.LAST_UPDATED.toString())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    Long lastUpdated = snapshot.getValue(Long.class);
                                    if(lastUpdated == null){
                                        Log.e(TAG, "onDataChange: snapshot value for last updated is null", new NullPointerException());
                                        sendToMainThread(false, listener);
                                        return;
                                    }
                                    boolean result = !lastUpdated.equals(profile.getLastUpdated());
                                    sendToMainThread(result, listener);
                                } else{
                                    Log.e(TAG, "onDataChange: No last updated value exist for the given profile", new NullPointerException());
                                    sendToMainThread(false, listener);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: Occurred while getting last updated value for profile", error.toException());
                                sendToMainThread(false, listener);
                            }
                        });
            }
        });
    }

    public void getMessages(@NonNull final String uid, @NonNull final OnHandlerResponseListener<DataSnapshot> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference(FIREBASE_CHATS_BRANCH).child(uid)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    sendToMainThread(snapshot, listener);
                                } else{
                                    sendToMainThread(null, listener);
                                    Log.e(TAG, "onDataChange: Message snapshot doesn't exists on Firebase for uid: "+uid, new NullPointerException());
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "onCancelled: Error occurred while getting messages from firebase", error.toException());
                                sendToMainThread(null, listener);
                            }
                        });
            }
        });
    }

    public void getMessagesAfter(@NonNull String uid, @NonNull Long after, @NonNull ChildEventListener newMessageListener){
        FirebaseDatabase.getInstance().getReference(FIREBASE_CHATS_BRANCH).child(uid).orderByChild(Message.Variable.TIME.toString()).startAt(after)
                .addChildEventListener(newMessageListener);
    }

    @NonNull
    public String getNewMessageId(@NonNull String senderUid){
        String messageId = FirebaseDatabase.getInstance().getReference(FIREBASE_CHATS_BRANCH).child(senderUid).push().getKey();
        if(messageId == null) throw new RuntimeException("Couldn't get messageId for new message");
        return messageId;
    }

    public void sendMessage(@NonNull final String uid, @NonNull final Message message, @NonNull final OnHandlerResponseListener<Boolean> listener){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                FirebaseDatabase.getInstance().getReference(FIREBASE_CHATS_BRANCH).child(uid).child(message.getMessageId()).setValue(message)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(!task.isSuccessful()){
                                    Log.e(TAG, "onComplete: Error occurred while adding message to "+uid+" Uid on Firebase Database", task.getException());
                                    sendToMainThread(false, listener);
                                    return;
                                }
                                sendToMainThread(true, listener);
                            }
                        });
            }
        });
    }

    private <T> void sendToMainThread(final T result, @NonNull final OnHandlerResponseListener<T> listener){
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                listener.onResponse(result);
            }
        });
    }
}

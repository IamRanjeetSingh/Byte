package com.example.bytes.viewModel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bytes.model.Profile;
import com.example.bytes.model.Repository;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

public class ChatViewModel extends AndroidViewModel {
    private static final String TAG = "ChatViewModel";

    private Repository repository;
    private Presenter presenter;
    private NewMessageReceiver newMessageReceiver;

    private String uid, name, number, thumbnailURI;

    public interface Presenter{
        void setProfileData(@NonNull String uid, @NonNull String name,@Nullable String thumbnailURI);
        void showMessages(@NonNull Cursor messages);
    }

    public ChatViewModel(Application application){
        super(application);
        this.repository = Repository.getInstance(application);
        newMessageReceiver = new NewMessageReceiver();
    }

    public void setPresenter(@NonNull Presenter presenter){
        this.presenter = presenter;
    }

    public void getProfileData(@NonNull Intent intent){
        uid = intent.getStringExtra(Profile.Variable.UID.toString());
        name = intent.getStringExtra(Profile.Variable.NAME.toString());
        number = intent.getStringExtra(Profile.Variable.NUMBER.toString());
        thumbnailURI = intent.getStringExtra(Profile.Variable.THUMBNAIL_URI.toString());
        presenter.setProfileData(uid, name, thumbnailURI);

        LocalBroadcastManager.getInstance(getApplication()).registerReceiver(newMessageReceiver, new IntentFilter(Repository.NEW_UID_MESSAGE_ACTION+uid));
    }

    public void getMessages(){
        repository.readMessages(uid, new Repository.OnRepositoryResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                getMessagesFromCache();
            }

            private void getMessagesFromCache(){
                repository.getMessages(uid, new Repository.OnRepositoryResponseListener<Cursor>(){
                    @Override
                    public void onResponse(Cursor messages) {
                        presenter.showMessages(messages);
                    }
                });
            }
        });
    }

    public void sendMessage(@NonNull String message){
        repository.sendMessage(uid, message, new Repository.OnRepositoryResponseListener<Boolean>(){
            @Override
            public void onResponse(Boolean response) {
                Log.d(TAG, "onResponse: Message result: "+response);
                getMessages();
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        LocalBroadcastManager.getInstance(getApplication()).unregisterReceiver(newMessageReceiver);
    }

    private class NewMessageReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "onReceive: ");
            getMessages();
        }
    }
}

package com.example.bytes.viewModel;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.bytes.model.Repository;

public class ChatListViewModel extends AndroidViewModel {
    private static final String TAG = "ChatListViewModel";
    private Repository repository;
    private Presenter presenter;
    private NewMessageReceiver newMessageReceiver;

    public interface Presenter{
        void showChats(Cursor chats);
    }

    public ChatListViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
        repository.init();
        newMessageReceiver = new NewMessageReceiver();
        LocalBroadcastManager.getInstance(application).registerReceiver(newMessageReceiver, new IntentFilter(Repository.NEW_MESSAGE_ACTION));
    }

    public void setPresenter(@NonNull Presenter presenter){
        this.presenter = presenter;
    }

    public String getMyUid(){
        return repository.getMyUid();
    }

    public void getChats(){
        Log.d(TAG, "getChats: ");
        repository.getChats(new Repository.OnRepositoryResponseListener<Cursor>() {
            @Override
            public void onResponse(Cursor chats) {
                presenter.showChats(chats);
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
            getChats();
        }
    }
}

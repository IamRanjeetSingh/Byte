package com.example.bytes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.bytes.R;
import com.example.bytes.model.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class SplashScreen extends AppCompatActivity {
    private long after = 2000;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

//        deleteDatabase("CacheDatabase");

        new CountDownTimer(after, 1000){
            @Override
            public void onTick(long millisUntilFinished) {
                after = millisUntilFinished;
            }

            @Override
            public void onFinish() {}
        }.start();

        Repository.getInstance(getApplication()).hasCurrentUser(new Repository.OnRepositoryResponseListener<Boolean>() {
            @Override
            public void onResponse(Boolean response) {
                if(response) sendToChatListActivity(after);
                else sendToLoginActivity(after);
            }
        });
    }

    private void sendToChatListActivity(long delay){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent chatListActivityIntent = new Intent(SplashScreen.this, ChatListActivity.class);
                startActivity(chatListActivityIntent);
                finish();
            }
        }, delay);
    }

    private void sendToLoginActivity(long delay){
        new Handler(Looper.getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent loginActivityIntent = new Intent(SplashScreen.this, LoginActivity.class);
                startActivity(loginActivityIntent);
                finish();
            }
        }, delay);
    }
}

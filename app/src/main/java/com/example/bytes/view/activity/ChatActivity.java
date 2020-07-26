package com.example.bytes.view.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bytes.R;
import com.example.bytes.databinding.ChatActivityBinding;
import com.example.bytes.view.adapter.MessageListAdapter;
import com.example.bytes.viewModel.ChatViewModel;

public class ChatActivity extends AppCompatActivity implements ChatViewModel.Presenter{
    private static final String TAG = "ChatActivity";

    private ChatActivityBinding binding;
    private ChatViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.chat_activity);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ChatViewModel.class);
        viewModel.setPresenter(this);

        viewModel.getProfileData(getIntent());

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        //linearLayoutManager.setReverseLayout(true);
        binding.messageList.setLayoutManager(linearLayoutManager);

        binding.send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.messageBox.getText() != null && binding.messageBox.getText().toString().trim().length() > 0) {
                    viewModel.sendMessage(binding.messageBox.getText().toString().trim());
                    binding.messageBox.setText("");
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.getMessages();
    }

    @Override
    public void setProfileData(@NonNull String uid, @NonNull String name, @Nullable String thumbnailURI) {
        binding.name.setText(name);
        if(thumbnailURI != null) binding.profileImage.setImageURI(Uri.parse(thumbnailURI));
        binding.messageList.setAdapter(new MessageListAdapter(uid));
    }

    @Override
    public void showMessages(@NonNull Cursor messages) {
        if(binding.messageList.getAdapter() == null){
            Log.e(TAG, "showMessages: Message list recycler view has no adapter", new NullPointerException());
            return;
        }
        ((MessageListAdapter)binding.messageList.getAdapter()).setMessages(messages);
        binding.messageList.scrollToPosition(binding.messageList.getAdapter().getItemCount()-1);
    }
}

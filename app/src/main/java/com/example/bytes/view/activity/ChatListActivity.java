package com.example.bytes.view.activity;

import android.content.Intent;
import android.database.Cursor;
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
import com.example.bytes.databinding.ChatListActivityBinding;
import com.example.bytes.model.Profile;
import com.example.bytes.model.Repository;
import com.example.bytes.view.adapter.ChatListAdapter;
import com.example.bytes.view.adapter.OnViewHolderClickListener;
import com.example.bytes.viewModel.ChatListViewModel;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Arrays;

public class ChatListActivity extends AppCompatActivity implements ChatListViewModel.Presenter, OnViewHolderClickListener<ChatListAdapter.ViewHolder> {
    private static final String TAG = "ChatListActivity";

    private ChatListActivityBinding binding;
    private ChatListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.chat_list_activity);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ChatListViewModel.class);
        viewModel.setPresenter(this);

        binding.addChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent profileListActivityIntent = new Intent(ChatListActivity.this, ProfileListActivity.class);
                startActivity(profileListActivityIntent);
            }
        });

        binding.chatList.setLayoutManager(new LinearLayoutManager(this));
        ChatListAdapter adapter = new ChatListAdapter(viewModel.getMyUid());
        adapter.setOnViewHolderClickListener(this);
        binding.chatList.setAdapter(adapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        viewModel.getChats();
    }

    @Override
    public void showChats(@NonNull Cursor chats) {
        Log.d(TAG, "showChats: "+Arrays.toString(chats.getColumnNames()));
        if(binding.chatList.getAdapter() == null){
            Log.e(TAG, "showChats: Chat List recycler view has no adapter", new NullPointerException());
            return;
        }
        ((ChatListAdapter)binding.chatList.getAdapter()).setChatCursor(chats);
    }

    @Override
    public void onViewHolderClick(ChatListAdapter.ViewHolder viewHolder) {
        Intent chatActivityIntent = new Intent(this, ChatActivity.class);

        chatActivityIntent.putExtra(Profile.Variable.UID.toString(), viewHolder.uid);
        chatActivityIntent.putExtra(Profile.Variable.NAME.toString(), viewHolder.name);
        chatActivityIntent.putExtra(Profile.Variable.NUMBER.toString(), viewHolder.number);
        chatActivityIntent.putExtra(Profile.Variable.THUMBNAIL_URI.toString(), viewHolder.thumbnailURI);

        startActivity(chatActivityIntent);
    }
}

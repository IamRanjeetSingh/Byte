package com.example.bytes.view.adapter;

import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytes.R;
import com.example.bytes.databinding.ChatListItemBinding;
import com.example.bytes.model.Message;
import com.example.bytes.model.Profile;

import java.text.DateFormat;
import java.util.Date;

public class ChatListAdapter extends RecyclerView.Adapter<ChatListAdapter.ViewHolder> {
    private static final String TAG = "ChatListAdapter";
    @Nullable private OnViewHolderClickListener<ChatListAdapter.ViewHolder> holderClickListener;

    private String myUid;
    private Cursor chatCursor;
    private DateFormat dateFormat = DateFormat.getDateInstance();

    public class ViewHolder extends RecyclerView.ViewHolder {
        private ChatListItemBinding binding;
        public String uid, name, number, thumbnailURI;

        private ViewHolder(@NonNull ChatListItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    if(holderClickListener != null)
                        holderClickListener.onViewHolderClick(ChatListAdapter.ViewHolder.this);
                }
            });
        }

        private void setChat(){
            uid = chatCursor.getString(chatCursor.getColumnIndex(Profile.Variable.UID.toString()));
            name = chatCursor.getString(chatCursor.getColumnIndex(Profile.Variable.NAME.toString()));
            number = chatCursor.getString(chatCursor.getColumnIndex(Profile.Variable.NUMBER.toString()));
            thumbnailURI = chatCursor.getString(chatCursor.getColumnIndex(Profile.Variable.THUMBNAIL_URI.toString()));

            String lastMessage = chatCursor.getString(chatCursor.getColumnIndex(Message.Variable.DATA.toString()));
            if(!myUid.equals(chatCursor.getString(chatCursor.getColumnIndex(Message.Variable.SENDER_UID.toString()))))
                binding.lastMessage.setText(lastMessage);
            else
                binding.lastMessage.setText(binding.getRoot().getResources().getString(R.string.myMessage_PlaceHolder, lastMessage));
            binding.lastMessageTime.setText(dateFormat.format(new Date(chatCursor.getLong(chatCursor.getColumnIndex(Message.Variable.TIME.toString())))));
            binding.name.setText(chatCursor.getString(chatCursor.getColumnIndex(Profile.Variable.NAME.toString())));
        }
    }

    public ChatListAdapter(@NonNull String myUid){
        this.myUid = myUid;
    }

    public void setChatCursor(@NonNull Cursor chats){
        chatCursor = chats;
        notifyDataSetChanged();
    }

    public void setOnViewHolderClickListener(@NonNull OnViewHolderClickListener<ChatListAdapter.ViewHolder> holderClickListener){
        this.holderClickListener = holderClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ChatListAdapter.ViewHolder((ChatListItemBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.chat_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        chatCursor.moveToPosition(position);
        holder.setChat();
    }

    @Override
    public int getItemCount() {
        return chatCursor != null ? chatCursor.getCount() : 0;
    }
}

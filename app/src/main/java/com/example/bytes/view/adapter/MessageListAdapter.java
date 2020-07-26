package com.example.bytes.view.adapter;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytes.R;
import com.example.bytes.databinding.MessageListItemBinding;
import com.example.bytes.model.Message;

import java.text.DateFormat;
import java.util.Date;

public class MessageListAdapter extends RecyclerView.Adapter<MessageListAdapter.ViewHolder> {
    @Nullable private OnViewHolderClickListener<MessageListAdapter.ViewHolder> holderClickListener;

    private Cursor messageCursor;
    private String userUid;
    private DateFormat dateFormat = DateFormat.getDateInstance();

    public class ViewHolder extends RecyclerView.ViewHolder{
        private MessageListItemBinding binding;
        private Message message;

        private ViewHolder(@NonNull MessageListItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;
            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holderClickListener != null)
                        holderClickListener.onViewHolderClick(MessageListAdapter.ViewHolder.this);
                }
            });
        }

        private void setMessage(@NonNull Message message){
            this.message = message;
            if(userUid.equals(message.getSenderUid())){
                binding.receivedMessageLayout.setVisibility(View.VISIBLE);
                binding.sentMessageLayout.setVisibility(View.GONE);
                binding.receivedMessage.setText(message.getData());
                binding.receivedTime.setText(dateFormat.format(new Date(message.getTime())));
            } else{
                binding.receivedMessageLayout.setVisibility(View.GONE);
                binding.sentMessageLayout.setVisibility(View.VISIBLE);
                binding.sentMessage.setText(message.getData());
                binding.sentTime.setText(dateFormat.format(new Date(message.getTime())));
            }
        }
    }

    public MessageListAdapter(@NonNull String userUid){
        this.userUid = userUid;
    }

    public void setMessages(@NonNull Cursor messages){
        this.messageCursor = messages;
        notifyDataSetChanged();
    }

    public void setOnViewHolderClickListener(@NonNull OnViewHolderClickListener<MessageListAdapter.ViewHolder> holderClickListener){
        this.holderClickListener = holderClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MessageListAdapter.ViewHolder((MessageListItemBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.message_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        messageCursor.moveToPosition(position);
        String messageId = messageCursor.getString(messageCursor.getColumnIndex(Message.Variable.MESSAGE_ID.toString()));
        String chatId = messageCursor.getString(messageCursor.getColumnIndex(Message.Variable.CHAT_ID.toString()));
        String senderUid = messageCursor.getString(messageCursor.getColumnIndex(Message.Variable.SENDER_UID.toString()));
        String receiverUid = messageCursor.getString(messageCursor.getColumnIndex(Message.Variable.RECEIVER_UID.toString()));
        String data = messageCursor.getString(messageCursor.getColumnIndex(Message.Variable.DATA.toString()));
        Long time = messageCursor.getLong(messageCursor.getColumnIndex(Message.Variable.TIME.toString()));
        Message message = new Message(messageId, chatId, senderUid, receiverUid, data);
        message.setTime(time);
        holder.setMessage(message);
    }

    @Override
    public int getItemCount() {
        return messageCursor != null ? messageCursor.getCount() : 0;
    }
}

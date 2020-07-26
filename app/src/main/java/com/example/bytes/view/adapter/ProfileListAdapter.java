package com.example.bytes.view.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.bytes.R;
import com.example.bytes.databinding.ProfileListItemBinding;
import com.example.bytes.model.Profile;

import java.util.ArrayList;
import java.util.List;

public class ProfileListAdapter extends RecyclerView.Adapter<ProfileListAdapter.ViewHolder> {
    private List<Profile> profileList;
    @Nullable private OnViewHolderClickListener<ProfileListAdapter.ViewHolder> holderClickListener;

    public class ViewHolder extends RecyclerView.ViewHolder{
        private ProfileListItemBinding binding;
        public Profile profile;

        private ViewHolder(@NonNull ProfileListItemBinding binding){
            super(binding.getRoot());
            this.binding = binding;

            binding.getRoot().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(holderClickListener != null)
                        holderClickListener.onViewHolderClick(ProfileListAdapter.ViewHolder.this);
                }
            });
        }

        private void setProfile(@NonNull Profile profile){
            this.profile = profile;
            binding.name.setText(profile.getName());
            //set profile image
        }
    }

    public void setOnViewHolderClickListener(@NonNull OnViewHolderClickListener<ProfileListAdapter.ViewHolder> holderClickListener){
        this.holderClickListener = holderClickListener;
    }

    public void addProfileList(@NonNull List<Profile> profileList){
        if(this.profileList != null){
            DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtilCallback(this.profileList, profileList));
            this.profileList = profileList;
            diffResult.dispatchUpdatesTo(this);
        } else{
            this.profileList = profileList;
            notifyDataSetChanged();
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProfileListAdapter.ViewHolder((ProfileListItemBinding) DataBindingUtil.inflate(LayoutInflater.from(parent.getContext()), R.layout.profile_list_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.setProfile(profileList.get(position));
    }

    @Override
    public int getItemCount() {
        return profileList != null ? profileList.size() : 0;
    }


    private static class DiffUtilCallback extends DiffUtil.Callback{
        private List<Profile> oldList, newList;

        private DiffUtilCallback(@Nullable List<Profile> oldList, @Nullable List<Profile> newList){
            this.oldList = oldList;
            this.newList = newList;
        }

        @Override
        public int getOldListSize() {
            return oldList != null ? oldList.size() : 0;
        }

        @Override
        public int getNewListSize() {
            return newList != null ? newList.size() : 0;
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            if(oldList == null || newList == null){
                return false;
            }
            return oldList.get(oldItemPosition).getUid().equals(newList.get(newItemPosition).getUid());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            if(oldList == null || newList == null){
                return false;
            }
            return oldList.get(oldItemPosition).compareTo(newList.get(newItemPosition)) > 0;
        }
    }
}

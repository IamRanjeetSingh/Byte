package com.example.bytes.view.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.bytes.R;
import com.example.bytes.databinding.ProfileListActivityBinding;
import com.example.bytes.model.Profile;
import com.example.bytes.view.adapter.OnViewHolderClickListener;
import com.example.bytes.view.adapter.ProfileListAdapter;
import com.example.bytes.viewModel.ProfileListViewModel;

import java.util.List;

public class ProfileListActivity extends AppCompatActivity implements ProfileListViewModel.Presenter, OnViewHolderClickListener<ProfileListAdapter.ViewHolder> {
    private static final String TAG = "ProfileListActivity";

    private ProfileListActivityBinding binding;
    private ProfileListViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.profile_list_activity);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(ProfileListViewModel.class);
        viewModel.setPresenter(this);

        binding.contactList.setLayoutManager(new LinearLayoutManager(this));
        ProfileListAdapter adapter = new ProfileListAdapter();
        adapter.setOnViewHolderClickListener(this);
        binding.contactList.setAdapter(adapter);
        binding.contactList.setHasFixedSize(true);

        binding.progressBar.setVisibility(View.VISIBLE);
        viewModel.getProfiles();
    }

    @Override
    public void showProfiles(List<Profile> profiles, boolean stopLoading) {
        if(binding.contactList.getAdapter() == null) {
            Log.e(TAG, "showContacts: ContactList has no Adapter", new NullPointerException());
            return;
        }

        if(profiles.size() != 0)((ProfileListAdapter) binding.contactList.getAdapter()).addProfileList(profiles);
        if(stopLoading) binding.progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onViewHolderClick(@NonNull ProfileListAdapter.ViewHolder viewHolder) {
        Intent chatActivityIntent = new Intent(this, ChatActivity.class);

        chatActivityIntent.putExtra(Profile.Variable.UID.toString(), viewHolder.profile.getUid());
        chatActivityIntent.putExtra(Profile.Variable.NAME.toString(), viewHolder.profile.getName());
        chatActivityIntent.putExtra(Profile.Variable.NUMBER.toString(), viewHolder.profile.getNumber());
        chatActivityIntent.putExtra(Profile.Variable.THUMBNAIL_URI.toString(), viewHolder.profile.getThumbnailURI());

        startActivity(chatActivityIntent);
        finish();
    }
}

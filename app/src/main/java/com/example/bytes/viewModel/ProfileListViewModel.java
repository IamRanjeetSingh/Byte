package com.example.bytes.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import com.example.bytes.model.Contact;
import com.example.bytes.model.Profile;
import com.example.bytes.model.Repository;

import java.util.ArrayList;
import java.util.List;

public class ProfileListViewModel extends AndroidViewModel {
    private Repository repository;
    private Presenter presenter;

    public interface Presenter{
        void showProfiles(List<Profile> profiles, boolean stopLoading);
    }

    public ProfileListViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    public void setPresenter(@NonNull Presenter presenter){
        this.presenter = presenter;
    }

//    public void getProfiles(){
//        final List<Profile> profiles = new ArrayList<>();
//        repository.getContacts(new Repository.OnRepositoryResponseListener<List<Contact>>() {
//            @Override
//            public void onResponse(List<Contact> contacts) {
//                checkEachContact(contacts);
//            }
//
//            private void checkEachContact(@NonNull final List<Contact> contacts){
//                final int[] count = {0};
//                for(final Contact contact : contacts){
//                    repository.getProfile(contact, new Repository.OnRepositoryResponseListener<Profile>() {
//                        @Override
//                        public void onResponse(Profile profile) {
//                            if(profile != null) profiles.add(profile);
//                            count[0]++;
//                            if(count[0] == contacts.size())
//                                presenter.showProfiles(profiles);
//                        }
//                    });
//                }
//            }
//        });
//    }

    public void getProfiles(){
        final int[] totalAck = {0};
        final int[] downloadAck = {0};
        final int[] updateAck = {0};

        final List<Profile> profileList = new ArrayList<>();
        final List<Profile> newProfileList = new ArrayList<>();

        repository.getContacts(new Repository.OnRepositoryResponseListener<List<Contact>>() {
            @Override
            public void onResponse(List<Contact> contactList) {
                totalAck[0] = contactList.size();

                for(Contact contact : contactList)
                    getProfileFromCache(contact);
            }

            private void getProfileFromCache(@NonNull final Contact contact){
                repository.getProfileFromCache(contact, new Repository.OnRepositoryResponseListener<Profile>(){
                    @Override
                    public void onResponse(Profile profile) {
                        if(profile == null){
                            downloadAck[0]++;
                            getProfileFromFirebase(contact);
                        } else {
                            updateAck[0]++;
                            profileList.add(profile);
                            updateProfileIfNeeded(profile);
                        }
                        if(--totalAck[0] == 0)
                            sendProfiles(profileList, downloadAck[0] == 0);
                    }
                });
            }

            private void getProfileFromFirebase(@NonNull Contact contact){
                repository.getProfileFromFirebase(Contact.Variable.NUMBER.toString(), contact.getNumber(), new Repository.OnRepositoryResponseListener<Profile>(){
                    @Override
                    public void onResponse(Profile profile) {
                        if(profile != null){
                            newProfileList.add(profile);
                        }

                        if(--downloadAck[0] == 0 && updateAck[0] == 0)
                            sendProfiles(newProfileList, true);
                    }
                });
            }

            private void updateProfileIfNeeded(@NonNull final Profile profile){
                repository.updateIfNeeded(profile, new Repository.OnRepositoryResponseListener<Profile>() {
                    @Override
                    public void onResponse(Profile newProfile) {
                        newProfileList.add(newProfile);

                        if(--updateAck[0] == 0 && downloadAck[0] == 0)
                            sendProfiles(newProfileList, true);
                    }
                });
            }

            private void sendProfiles(List<Profile> profiles, boolean stopLoading){
                presenter.showProfiles(profiles, stopLoading);
            }
        });
    }
}

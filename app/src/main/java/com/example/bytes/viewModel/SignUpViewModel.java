package com.example.bytes.viewModel;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.example.bytes.model.Profile;
import com.example.bytes.model.Repository;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class SignUpViewModel extends AndroidViewModel {
    private static final String TAG = "SignUpViewModel";

    private Presenter presenter;
    private Repository repository;

    private String mobile;
    @Nullable private String verificationId;
    @Nullable private PhoneAuthCredential credential;

    public interface Presenter{
        void loginUser();
        void showVerificationFailed();
        void requestVerificationCode();
        void requestSmsCode();
        void showSignUpError();
        void codeSent();
    }

    public SignUpViewModel(Application application){
        super(application);
        repository = Repository.getInstance(application);
    }

    public void setPresenter(@NonNull Presenter presenter){
        this.presenter = presenter;
    }

    public void getVerificationCode(@NonNull String mobile, @NonNull Activity activity){
        this.mobile = mobile;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(this.mobile, 120, TimeUnit.SECONDS, activity, verificationCallbacks);
    }

    public void signUp(@NonNull final String name, @Nullable String smsCode) {
        if (credential == null) {
            if (smsCode != null && verificationId != null)
                credential = PhoneAuthProvider.getCredential(verificationId, smsCode);
            else if (verificationId == null) {
                presenter.requestVerificationCode();
                return;
            } else {
                presenter.requestSmsCode();
                return;
            }
        }

        FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful() && task.getResult() != null && task.getResult().getUser() != null) {
                            Log.d(TAG, "onComplete: Sign up successful");
                            String uid = task.getResult().getUser().getUid();
                            final Profile profile = new Profile.Builder(uid, name).setNumber(mobile).build();
                            repository.createNewProfile(profile, new Repository.OnRepositoryResponseListener<Boolean>(){
                                @Override
                                public void onResponse(Boolean response) {
                                    if(!response){
                                        Log.d(TAG, "onResponse: Error occurred while creating new user");
                                        presenter.showSignUpError();
                                        return;
                                    }

                                    //repository.setUserProfile(profile);
                                    presenter.loginUser();
                                }
                            });

                        } else{
                            Log.e(TAG, "onComplete: Sign up failed", task.getException());
                            if(task.getResult() == null) Log.e(TAG, "onComplete: Task Result is Null", new NullPointerException());
                            if(task.getResult().getUser() == null) Log.e(TAG, "onComplete: Task Result's User is Null", new NullPointerException());
                            presenter.showSignUpError();
                        }
                    }
                });
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            verificationId = s;
            presenter.codeSent();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            credential = phoneAuthCredential;
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            Log.e(TAG, "onVerificationFailed: Error occurred while verifying phone number", e);
            presenter.showVerificationFailed();
        }
    };
}

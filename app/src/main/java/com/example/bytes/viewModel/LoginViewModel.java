package com.example.bytes.viewModel;

import android.app.Activity;
import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.example.bytes.R;
import com.example.bytes.model.Contact;
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

public class LoginViewModel extends AndroidViewModel {
    private static final String TAG = "LoginViewModel";

    private Presenter presenter;

    private static final boolean DEBUG = true;

    @Nullable private String verificationId;
    private String mobile;
    @Nullable private PhoneAuthCredential credential;

    public interface Presenter
    {
        void showVerificationFailed();
        void requestVerificationCode();
        void requestSmsCode();
        void codeSent();
        void loginUser();
        void showLoginError(@NonNull String error);
    }

    public LoginViewModel(Application application){
        super(application);
    }

    public void setPresenter(@NonNull Presenter presenter){
        this.presenter = presenter;
    }

    public void getVerificationCode(@NonNull String mobile, @NonNull Activity activity){
        if(DEBUG) Log.d(TAG, "getVerificationCode: ");
        this.mobile = mobile;
        PhoneAuthProvider.getInstance().verifyPhoneNumber(mobile, 60, TimeUnit.SECONDS, activity, verificationCallbacks);
    }

    public void login(@Nullable String smsCode){
        if(DEBUG) Log.d(TAG, "login: ");
        if(credential == null){
            if(verificationId != null && smsCode != null){
                credential = PhoneAuthProvider.getCredential(verificationId, smsCode);
            } else if (verificationId == null) {
                presenter.requestVerificationCode();
                return;
            } else {
                presenter.requestSmsCode();
                return;
            }
        }

        Repository.getInstance(getApplication()).getProfileFromFirebase(Contact.Variable.NUMBER.toString(), mobile, new Repository.OnRepositoryResponseListener<Profile>() {
            @Override
            public void onResponse(Profile profile) {
                if (profile == null) {
                    presenter.showLoginError(getApplication().getResources().getString(R.string.NoAccountFoundError));
                    return;
                }
                FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    Log.d(TAG, "onComplete: Login successful");
                                    presenter.loginUser();
                                } else {
                                    Log.d(TAG, "onComplete: Login failed" + task.getException());
                                    presenter.showLoginError(getApplication().getResources().getString(R.string.LoginError));
                                }
                            }
                        });
            }
        });



    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
        @Override
        public void onCodeSent(@NonNull String s, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
            if(DEBUG) Log.d(TAG, "onCodeSent: ");
            verificationId = s;
            presenter.codeSent();
        }

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
            if(DEBUG) Log.d(TAG, "onVerificationCompleted: ");
            credential = phoneAuthCredential;
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {
            if(DEBUG) Log.d(TAG, "onVerificationFailed: ");
            Log.d(TAG, "onVerificationFailed: Verification failed. Exception: "+e.toString());
            presenter.showVerificationFailed();
        }
    };
}

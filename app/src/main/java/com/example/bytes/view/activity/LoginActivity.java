package com.example.bytes.view.activity;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.bytes.R;
import com.example.bytes.databinding.LoginActivityBinding;
import com.example.bytes.viewModel.LoginViewModel;

public class LoginActivity extends AppCompatActivity implements LoginViewModel.Presenter{
    private static final String TAG = "LoginActivity";

    private static final boolean DEBUG = true;

    private LoginActivityBinding binding;
    private LoginViewModel viewModel;

    private String countryCode;
    private boolean isLoggingIn = false, isGettingCode = false, hasWaitFinished = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.login_activity);
        countryCode = getResources().getString(R.string.countryCode);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(LoginViewModel.class);
        viewModel.setPresenter(this);

        binding.getCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCode();
            }
        });
        binding.loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                login();
            }
        });

        binding.signUpLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent signUpIntent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signUpIntent);
            }
        });


        binding.mobile.setSelection(countryCode.length());

        final TextWatcher errorRemover = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.mobileInputLayout.setError(null);
                binding.verificationCodeInputLayout.setError(null);
            }
        };

        final TextWatcher mobileTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                errorRemover.afterTextChanged(s);
                if(!s.toString().startsWith(countryCode)){
                    binding.mobile.setText(countryCode);
                    Selection.setSelection(binding.mobile.getText(), countryCode.length());
                }
            }
        };

        binding.mobile.addTextChangedListener(mobileTextWatcher);
        binding.verificationCode.addTextChangedListener(errorRemover);
    }

    private void getCode(){
        if(DEBUG) Log.d(TAG, "getCode: ");
        if(hasWaitFinished) {
            if (binding.mobile.getText() != null && !isGettingCode) {
                if (binding.mobile.getText().toString().trim().length() > 0) {
                    String mobile = binding.mobile.getText().toString().trim();

                    if (Patterns.PHONE.matcher(mobile).matches()) {
                        startGettingCode();
                        viewModel.getVerificationCode(mobile, this);
                    } else {
                        binding.mobile.setError(getResources().getString(R.string.InvalidMobileError));
                        binding.mobile.requestFocus();
                    }

                } else {
                    binding.mobileInputLayout.setError(getResources().getString(R.string.NoMobileError));
                    binding.mobile.requestFocus();
                }
            } else {
                if (DEBUG) Log.d(TAG, "getCode: ignoring getCode");
            }
        } else{
            showVibrationAnim(binding.getCodeTimer);
        }
    }

    private void login(){
        if(DEBUG) Log.d(TAG, "login: ");
        if(binding.mobile.getText() != null && !isLoggingIn){
            String mobile = binding.mobile.getText().toString().trim();
            String smsCode = binding.verificationCode.getText() != null ? binding.verificationCode.getText().toString().trim() : null;

            if(mobile.length() != 0 && Patterns.PHONE.matcher(mobile).matches()){
                startLogin();
                viewModel.login(smsCode);
            } else{
                if(mobile.length() == 0)
                    binding.mobileInputLayout.setError(getResources().getString(R.string.NoMobileError));
                else
                    binding.mobileInputLayout.setError(getResources().getString(R.string.InvalidMobileError));
            }
        } else{
            if(DEBUG) Log.d(TAG, "login: ignoring login");
        }
    }

    private void startLogin() {
        if (!isLoggingIn) {
            if (DEBUG) Log.d(TAG, "startLogin: ");
            isLoggingIn = true;
            binding.loginButtonText.setVisibility(View.GONE);
            binding.loginButtonProgress.setVisibility(View.VISIBLE);
        }
    }

    private void stopLogin() {
        if (isLoggingIn) {
            if (DEBUG) Log.d(TAG, "stopLogin: ");
            isLoggingIn = false;
            binding.loginButtonText.setVisibility(View.VISIBLE);
            binding.loginButtonProgress.setVisibility(View.GONE);
        }
    }

    private void startGettingCode() {
        if (!isGettingCode) {
            if (DEBUG) Log.d(TAG, "startGettingCode: ");
            isGettingCode = true;
            binding.getCodeButtonText.setVisibility(View.GONE);
            binding.getCodeButtonProgress.setVisibility(View.VISIBLE);

            binding.getCodeTimer.setVisibility(View.VISIBLE);

            hasWaitFinished = false;
            new CountDownTimer(75 * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    binding.getCodeTimer.setText(getResources().getString(R.string.codeWaitPlaceholder, (millisUntilFinished / 1000)));
                }

                @Override
                public void onFinish() {
                    binding.getCodeTimer.setVisibility(View.GONE);
                    hasWaitFinished = true;
                }
            }.start();
        }
    }

    private void stopGettingCode() {
        if (isGettingCode) {
            if (DEBUG) Log.d(TAG, "stopGettingCode: ");
            isGettingCode = false;
            binding.getCodeButtonText.setVisibility(View.VISIBLE);
            binding.getCodeButtonProgress.setVisibility(View.GONE);
        }
    }

    private void showVibrationAnim(@NonNull View target){
        if(DEBUG) Log.d(TAG, "showVibrationAnim: ");
        float baseX = target.getX();
        ObjectAnimator vibration = ObjectAnimator.ofFloat(target, "x", baseX+5, baseX-5, baseX);
        vibration.setRepeatCount(3);
        vibration.start();
    }


    //Presenter

    @Override
    public void showVerificationFailed() {
        if(DEBUG) Log.d(TAG, "showVerificationFailed: ");
        stopLogin();
        stopGettingCode();

        Toast.makeText(this, getResources().getString(R.string.Verification_Failed), Toast.LENGTH_LONG).show();
    }

    @Override
    public void requestVerificationCode() {
        if(DEBUG) Log.d(TAG, "requestVerificationId: ");
        stopLogin();
        stopGettingCode();

        binding.verificationCodeInputLayout.setError(getResources().getString(R.string.NoVerificationCodeRequestedError));
        binding.verificationCode.requestFocus();

        showVibrationAnim(binding.getCodeButton);
    }

    @Override
    public void requestSmsCode() {
        if(DEBUG) Log.d(TAG, "requestSmsCode: ");
        stopLogin();
        stopGettingCode();

        binding.verificationCodeInputLayout.setError(getResources().getString(R.string.NoVerificationCodeEnteredError));
        binding.getCodeButton.requestFocus();
    }

    @Override
    public void codeSent() {
        if(DEBUG) Log.d(TAG, "codeSent: ");
        stopLogin();
        stopGettingCode();

        Toast.makeText(this, getResources().getString(R.string.code_sent), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void loginUser() {
        if(DEBUG) Log.d(TAG, "loginUser: ");
        stopLogin();
        stopGettingCode();

        Intent chatListActivityIntent = new Intent(this, ChatListActivity.class);
        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(chatListActivityIntent);
    }

    @Override
    public void showLoginError(@NonNull String error) {
        if(DEBUG) Log.d(TAG, "showLoginError: ");
        stopLogin();
        stopGettingCode();

        Toast.makeText(this, error, Toast.LENGTH_LONG).show();
        if(error.equals(getResources().getString(R.string.NoAccountFoundError)))
            showVibrationAnim(binding.signUpLink);
    }
}

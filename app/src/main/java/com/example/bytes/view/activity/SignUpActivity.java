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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;

import com.example.bytes.R;
import com.example.bytes.databinding.SignupActivityBinding;
import com.example.bytes.viewModel.SignUpViewModel;

public class SignUpActivity extends AppCompatActivity implements SignUpViewModel.Presenter {
    private static final String TAG = "SignUpActivity";

    private static final boolean DEBUG = true;

    private SignupActivityBinding binding;
    private SignUpViewModel viewModel;

    private String countryCode;
    private boolean isGettingCode = false, isSigningUp = false, hasWaitFinished = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.signup_activity);
        countryCode = getResources().getString(R.string.countryCode);
        viewModel = new ViewModelProvider(this, new ViewModelProvider.AndroidViewModelFactory(getApplication())).get(SignUpViewModel.class);
        viewModel.setPresenter(this);

        binding.getCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getCode();
            }
        });
        binding.signUpButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                signUp();
            }
        });

        binding.loginLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent loginIntent = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginIntent);
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
                binding.nameInputLayout.setError(null);
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

        binding.name.addTextChangedListener(errorRemover);
        binding.mobile.addTextChangedListener(mobileTextWatcher);
        binding.verificationCode.addTextChangedListener(errorRemover);
    }

    private void signUp(){
        if(DEBUG) Log.d(TAG, "signUp:");
        if(binding.name.getText() != null && binding.mobile.getText() != null && !isSigningUp){
            String name = binding.name.getText().toString().trim();
            String mobile = binding.mobile.getText().toString().trim();
            String smsCode = binding.verificationCode.getText() != null ? binding.verificationCode.getText().toString().trim() : null;

            if(name.length() > 0 && mobile.length() > 0 && Patterns.PHONE.matcher(mobile).matches()){

                startSigningUp();
                viewModel.signUp(name, smsCode);
            } else{
                if(name.length() == 0)
                    binding.nameInputLayout.setError(getResources().getString(R.string.NoNameError));

                if(mobile.length() == 0)
                    binding.mobileInputLayout.setError(getResources().getString(R.string.NoMobileError));
                else if(!Patterns.PHONE.matcher(mobile).matches())
                    binding.mobileInputLayout.setError(getResources().getString(R.string.InvalidMobileError));
            }
        } else{
            if(DEBUG) Log.d(TAG, "signUp: ignoring signUp");
        }
    }

    private void getCode() {
        if (DEBUG) Log.d(TAG, "getCode: ");
        if (hasWaitFinished) {

            if (binding.mobile.getText() != null && !isGettingCode) {
                if (binding.mobile.getText().toString().trim().length() > 0) {
                    String mobile = binding.mobile.getText().toString().trim();

                    if (Patterns.PHONE.matcher(mobile).matches()) {
                        startGettingCode();
                        viewModel.getVerificationCode(mobile, this);
                    } else {
                        binding.mobile.requestFocus();
                        binding.mobileInputLayout.setError(getResources().getString(R.string.InvalidMobileError));
                    }

                } else {
                    binding.mobile.requestFocus();
                    binding.mobileInputLayout.setError(getResources().getString(R.string.NoMobileError));
                }
            } else {
                if (DEBUG) Log.d(TAG, "getCode: ignoring getCode");
            }
        } else{
            showVibrationAnim(binding.getCodeTimer);
        }
    }


    private void startSigningUp() {
        if (!isSigningUp) {
            if (DEBUG) Log.d(TAG, "startSigningUp: ");
            isSigningUp = true;
            binding.signUpButtonText.setVisibility(View.GONE);
            binding.signUpButtonProgress.setVisibility(View.VISIBLE);
        }
    }

    private void stopSigningUp() {
        if (isSigningUp) {
            if (DEBUG) Log.d(TAG, "stopSigningUp: ");
            isSigningUp = false;
            binding.signUpButtonText.setVisibility(View.VISIBLE);
            binding.signUpButtonProgress.setVisibility(View.GONE);
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
                    binding.getCodeTimer.setText(getResources().getString(R.string.codeWaitPlaceholder, millisUntilFinished / 1000));
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
        float baseX = target.getX();
        ObjectAnimator vibration = ObjectAnimator.ofFloat(target, "x", baseX+5, baseX-5, baseX);
        vibration.setRepeatCount(3);
        vibration.start();
    }

    //Presenter

    @Override
    public void loginUser() {
        if(DEBUG) Log.d(TAG, "signInUser: ");
        stopSigningUp();
        stopGettingCode();

        Intent chatListActivityIntent = new Intent(this, ChatListActivity.class);
        chatListActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(chatListActivityIntent);
    }

    @Override
    public void showVerificationFailed() {
        if(DEBUG) Log.d(TAG, "showVerificationFailed: ");
        stopSigningUp();
        stopGettingCode();

        Toast.makeText(this, getResources().getString(R.string.Verification_Failed), Toast.LENGTH_LONG).show();

    }

    @Override
    public void requestVerificationCode() {
        if(DEBUG) Log.d(TAG, "requestVerificationCode: ");
        stopSigningUp();
        stopGettingCode();

        binding.verificationCodeInputLayout.setError(getResources().getString(R.string.NoVerificationCodeRequestedError));
        binding.verificationCode.requestFocus();

        showVibrationAnim(binding.getCodeButton);
    }

    @Override
    public void requestSmsCode() {
        if(DEBUG) Log.d(TAG, "requestSmsCode: ");
        stopSigningUp();
        stopGettingCode();

        binding.verificationCodeInputLayout.setError(getResources().getString(R.string.NoVerificationCodeEnteredError));
        binding.getCodeButton.requestFocus();
    }

    @Override
    public void showSignUpError() {
        if(DEBUG) Log.d(TAG, "showSignUpError: ");
        stopSigningUp();
        stopGettingCode();

        Toast.makeText(this, getResources().getString(R.string.SignUpError), Toast.LENGTH_LONG).show();
    }

    @Override
    public void codeSent() {
        if(DEBUG) Log.d(TAG, "codeSent: ");
        stopSigningUp();
        stopGettingCode();

        Toast.makeText(this, getResources().getString(R.string.code_sent), Toast.LENGTH_SHORT).show();
    }
}

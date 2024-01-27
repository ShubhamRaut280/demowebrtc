package com.shubham.backspace;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthMissingActivityForRecaptchaException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthOptions;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class phoneAuthentication extends AppCompatActivity {
    EditText phoneNoInput , otpInput;
    String verificationId;

    Button  getOtp, submitOtp;

    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    TextView otptext;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_authentication);

        phoneNoInput = findViewById(R.id.inputMobileno);
        getOtp = findViewById(R.id.getotp);
        otptext = findViewById(R.id.Otptext);
        submitOtp = findViewById(R.id.submitOTP);
        otpInput = findViewById(R.id.inputOTP);
        getOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               String  phonenumber = phoneNoInput.getText().toString();
                if (phonenumber == null || phonenumber.length() != 10) {
                    Toast.makeText(phoneAuthentication.this, "Please enter correct number", Toast.LENGTH_SHORT).show();
                } else {
                    sendVerificationCode(phonenumber);
                    Toast.makeText(phoneAuthentication.this, "Sending OTP", Toast.LENGTH_SHORT).show();
                    otptext.setVisibility(View.VISIBLE);
                    otpInput.setVisibility(View.VISIBLE);
                    submitOtp.setVisibility(View.VISIBLE);

                }
            }
        });

        submitOtp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String otp = otpInput.getText().toString();
                if (otp == null ) {
                    Toast.makeText(phoneAuthentication.this, "Please enter OTP", Toast.LENGTH_SHORT).show();
                } else{
                    verifyCode(otp);
                }
                }
        });


    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(@NonNull PhoneAuthCredential credential) {

            String code = credential.getSmsCode();
            if(code!=null)
            {
                verifyCode(code);
            }
        }

        @Override
        public void onVerificationFailed(@NonNull FirebaseException e) {

            Toast.makeText(phoneAuthentication.this, "Verification Failed : "+e, Toast.LENGTH_LONG).show();
        }

        @Override
        public void onCodeSent(@NonNull String vId,
                @NonNull PhoneAuthProvider.ForceResendingToken token) {
            super.onCodeSent(vId, token);
            verificationId = vId;

        }
    };



    public void sendVerificationCode(String number)
    {
        PhoneAuthOptions options =
                PhoneAuthOptions.newBuilder(mAuth)
                        .setPhoneNumber("+91"+number)       // Phone number to verify
                        .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
                        .setActivity(this)                 // (optional) Activity for callback binding
                        .setCallbacks(mCallbacks)          // OnVerificationStateChangedCallbacks
                        .build();
        PhoneAuthProvider.verifyPhoneNumber(options);
    }


    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information

                            Toast.makeText(phoneAuthentication.this, "Sign in successfull", Toast.LENGTH_SHORT).show();
                            FirebaseUser user = task.getResult().getUser();

                            // Update UI
                            startActivity(new Intent(phoneAuthentication.this, MainActivity.class));
                        } else {
                            // Sign in failed, display a message and update the UI
                            Toast.makeText(phoneAuthentication.this, "Sign in Failed ! please try again.", Toast.LENGTH_SHORT).show();
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void verifyCode (String code)
    {
        // user entered code and orginal sent code verification
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, code);
        signInWithPhoneAuthCredential(credential);
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentuser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentuser != null)
        {
            startActivity(new Intent(phoneAuthentication.this, MainActivity.class));
            finish();
        }
    }
}
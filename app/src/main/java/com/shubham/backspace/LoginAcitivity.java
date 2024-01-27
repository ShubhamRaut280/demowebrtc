package com.shubham.backspace;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.api.identity.BeginSignInRequest;
import com.google.android.gms.auth.api.identity.SignInCredential;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.FirebaseDatabase;

public class LoginAcitivity extends AppCompatActivity {
    ImageView facebook , google, phone;
    EditText username, email, password;
    Button register;
    TextView login;
    FirebaseAuth auth;
    FirebaseDatabase database;
    GoogleSignInClient googleSignInClient;
    int RC_SIGN_IN = 20;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_acitivity);

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        google = findViewById(R.id.google);
        phone = findViewById(R.id.phone);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginAcitivity.this, phoneAuthentication.class));
            }
        });

        GoogleSignInOptions gso  = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                 .requestEmail().build();

        googleSignInClient = GoogleSignIn.getClient(this, gso);



        google.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleSignin();
            }
            private void googleSignin() {
                Intent intent = googleSignInClient.getSignInIntent();
                startActivityForResult(intent,RC_SIGN_IN);
            }



        });





    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode==RC_SIGN_IN)
        {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);


            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuth(account.getIdToken());
            } catch (ApiException e) {
                int statusCode = e.getStatusCode();
                Toast.makeText(this, "Error code: " + statusCode, Toast.LENGTH_SHORT).show();
            }

        }

    }


    private void firebaseAuth(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken,null);
        auth.signInWithCredential(credential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success
                            FirebaseUser user = auth.getCurrentUser();
                            if (user != null) {
                                Log.d(TAG, "onComplete: username is : "+ user.getDisplayName()+ " email for user is : "+ user.getEmail()
                                +" user : "+ user.getPhoneNumber());
                                Toast.makeText(LoginAcitivity.this, "sign in successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(LoginAcitivity.this, MainActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginAcitivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        FirebaseUser user = auth.getCurrentUser();
        if (user != null) {
            startActivity(new Intent(LoginAcitivity.this, MainActivity.class));
            finish();
        }

    }
}


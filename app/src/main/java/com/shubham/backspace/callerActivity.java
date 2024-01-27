package com.shubham.backspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.webkit.PermissionRequest;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;
import com.shubham.backspace.databinding.ActivityCallerBinding;
import com.shubham.backspace.models.InterfaceJava;
import com.shubham.backspace.models.User;

import java.util.UUID;

public class callerActivity extends AppCompatActivity {

    FirebaseAuth auth;
    String uniqueId = "";
    ActivityCallerBinding binding;
    String username = "";
    String friendsUsername = "";

    static boolean isPeerConnected = false;

    DatabaseReference dbref;

    boolean isAudio = true;
    boolean isVideo = true;
    String createdBy;
    boolean pageExit = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         binding = ActivityCallerBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        dbref = FirebaseDatabase.getInstance().getReference().child("users");

        username = getIntent().getStringExtra("username");
        String incoming = getIntent().getStringExtra("incoming");
        createdBy = getIntent().getStringExtra("createdBy");

        friendsUsername = incoming;

        setupWebView();

        binding.micBtn.setOnClickListener(view -> {
            isAudio = !isAudio;
            callJavaScriptFunction("javascript:toggleAudio(\""+isAudio+"\")");
            if(isAudio){
                binding.micBtn.setImageResource(R.drawable.btn_unmute_normal);
            } else {
                binding.micBtn.setImageResource(R.drawable.btn_mute_normal);
            }
        });

        binding.videoBtn.setOnClickListener(view -> {
            isVideo = !isVideo;
            callJavaScriptFunction("javascript:toggleVideo(\""+isVideo+"\")");
            if(isVideo){
                binding.videoBtn.setImageResource(R.drawable.btn_video_normal);
            } else {
                binding.videoBtn.setImageResource(R.drawable.btn_video_muted);
            }
        });

        binding.endCall.setOnClickListener(view -> finish());
    }


    // setting up webview to use the html
    private void setupWebView() {
        binding.webView.setWebChromeClient(new WebChromeClient(){
            @Override
            public void onPermissionRequest(PermissionRequest request) {
                request.grant(request.getResources());
            }
        });

        binding.webView.getSettings().setJavaScriptEnabled(true);
        binding.webView.getSettings().setMediaPlaybackRequiresUserGesture(false);
        binding.webView.addJavascriptInterface(new InterfaceJava(this), "Android");

        loadVideoCall();
    }


    // loads the video call on webview
    public void loadVideoCall() {
        String filePath = "file:android_asset/call.html";
        binding.webView.loadUrl(filePath);

        binding.webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                initializePeer();
            }
        });
    }
    public static void onPeerConnected(){
        isPeerConnected = true;
    }

    private void initializePeer() {
        uniqueId = getUniqueId();

        callJavaScriptFunction("javascript:init(\"" + uniqueId + "\")");

        /// now setting connection id into db if room creator is sender he will generate connection id

        if(createdBy.equalsIgnoreCase(username)) {
            if(pageExit)
                return;
            dbref.child(username).child("connId").setValue(uniqueId);
            dbref.child(username).child("isAvailable").setValue(true);


        } else {

            // check for another user if he shared the connection id to database
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    friendsUsername = createdBy;

                    FirebaseDatabase.getInstance().getReference()
                            .child("users")
                            .child(friendsUsername)
                            .child("connId")
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                                    if(snapshot.getValue() != null) {
                                        sendCallRequest();
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                }
                            });
                }
            }, 3000);
        }

    }
    void sendCallRequest(){
        if(!isPeerConnected) {
            Toast.makeText(this, "You are not connected. Please check your internet.", Toast.LENGTH_SHORT).show();
            return;
        }

        listenConnId();
    }

    void listenConnId() {
        dbref.child(friendsUsername).child("connId").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull @NotNull DataSnapshot snapshot) {
                if(snapshot.getValue() == null)
                    return;

                String connId = snapshot.getValue(String.class);
                callJavaScriptFunction("javascript:startCall(\""+connId+"\")");
            }

            @Override
            public void onCancelled(@NonNull @NotNull DatabaseError error) {

            }
        });
    }
    void callJavaScriptFunction(String function){
        binding.webView.post(new Runnable() {
            @Override
            public void run() {
                binding.webView.evaluateJavascript(function, null);
            }
        });
    }

    String getUniqueId(){
        return UUID.randomUUID().toString();
    }

}
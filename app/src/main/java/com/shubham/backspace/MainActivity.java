package com.shubham.backspace;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.NotNull;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseDatabase db;
    boolean isOkay = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button button = findViewById(R.id.button);

        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();

        String curruserid = auth.getUid();

     /*  this is for finding the user to connect */

            // take first user who is available
            db.getReference().child("users")
                    .orderByChild("status")
                    .equalTo(0).limitToFirst(1)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getChildrenCount() > 0) {
                                // room available
                                isOkay = true;
                                for (DataSnapshot childsnap : snapshot.getChildren()) {
                                    // set our uid as incoming in another users room
                                    db.getReference()
                                            .child("users")
                                            .child(childsnap.getKey())
                                            .child("incoming")
                                            .setValue(curruserid);
                                    db.getReference()
                                            .child("users")
                                            .child(childsnap.getKey())
                                            .child("status")
                                            .setValue(1);

                                    Intent intent = new Intent(MainActivity.this, callerActivity.class);
                                    String incoming = childsnap.child("incoming").getValue(String.class);
                                    String createdBy = childsnap.child("createdBy").getValue(String.class);
                                    boolean isAvailable = childsnap.child("isAvailable").getValue(Boolean.class);
                                    intent.putExtra("username", curruserid);
                                    intent.putExtra("incoming", incoming);
                                    intent.putExtra("createdBy", createdBy);
                                    intent.putExtra("isAvailable", isAvailable);

                                    // sharing sender details further
                                    startActivity(intent);
                                    finish();
                                }
                            } else {
                                // room Not Available

                                HashMap<String, Object> room = new HashMap<>();
                                room.put("incoming", curruserid);
                                room.put("createdBy", curruserid);
                                room.put("isAvailable", true);
                                room.put("status", 0);


                                // set this room description and check until anyone wans to join until changes
                                db.getReference()
                                        .child("users")
                                        .child(curruserid)
                                        .setValue(room).addOnSuccessListener(unused -> db.getReference()
                                                .child("users")
                                                .child(curruserid).addValueEventListener(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull @NotNull DataSnapshot snapshot1) {
                                                        if (snapshot1.child("status").exists()) {
                                                            if (snapshot1.child("status").getValue(Integer.class) == 1) {

                                                                if (isOkay)
                                                                    return;

                                                                isOkay = true;
                                                                Intent intent = new Intent(MainActivity.this, callerActivity.class);
                                                                String incoming = snapshot1.child("incoming").getValue(String.class);
                                                                String createdBy = snapshot1.child("createdBy").getValue(String.class);
                                                                boolean isAvailable = snapshot1.child("isAvailable").getValue(Boolean.class);
                                                                intent.putExtra("username", curruserid);
                                                                intent.putExtra("incoming", incoming);
                                                                intent.putExtra("createdBy", createdBy);
                                                                intent.putExtra("isAvailable", isAvailable);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull @NotNull DatabaseError error) {

                                                    }
                                                }));
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


    }

}
package com.pratiksymz.android.instapics;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private EditText nameField, eMailField, passField;
    private Button registerUserButton;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        registerUserButton = findViewById(R.id.register_user_button);
        nameField = findViewById(R.id.name_field);
        eMailField = findViewById(R.id.email_field);
        passField = findViewById(R.id.password_field);

        mFirebaseAuth = FirebaseAuth.getInstance();
        // To store the reference to the picture data stored by the user
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("users");

        registerUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerUser();
            }
        });
    }

    public void registerUser() {
        final String userName = nameField.getText().toString().trim();
        String userEmail = eMailField.getText().toString().trim();
        String userPass = passField.getText().toString().trim();

        if (!TextUtils.isEmpty(userName) && !TextUtils.isEmpty(userEmail) && !TextUtils.isEmpty(userPass)) {
            mFirebaseAuth.createUserWithEmailAndPassword(userEmail, userPass)
                    .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // Get user ID
                                String userID = mFirebaseAuth.getCurrentUser().getUid();
                                // Get user's database reference
                                DatabaseReference currentUserDB = mDatabaseReference.child(userID);
                                // Set the name & image for the user
                                currentUserDB.child("name").setValue(userName);
                                currentUserDB.child("image").setValue("default");

                                // Redirect user to Profile Setup Activity
                                Intent mainIntent = new Intent(RegisterActivity.this,
                                        SetupActivity.class);
                                mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(mainIntent);
                            }
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Authentication", e.getMessage());
                        }
                    });
        }
    }
}

package com.pratiksymz.android.instapics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class PostActivity extends AppCompatActivity {

    private final static int GALLERY_REQUEST = 1;
    private Uri uri = null;
    private ImageButton addPictureFromGallery;
    private EditText nameEditText, descEditText;
    private FloatingActionButton submitFab;

    // Reference to Firebase Authentication & Database for "users"
    FirebaseAuth mFirebaseAuth;
    FirebaseUser mCurrentUser;
    DatabaseReference mFirebaseUserDatabase;

    // Reference to Firebase Storage
    private StorageReference storageReference;

    // Reference to Firebase Database
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        addPictureFromGallery = findViewById(R.id.add_picture_from_gallery);
        nameEditText = findViewById(R.id.picture_name_input);
        descEditText = findViewById(R.id.picture_desc_input);
        submitFab = findViewById(R.id.submit_picture_fab);

        addPictureFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addPictureFromGallery(view);
            }
        });

        submitFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitDataToStorage(view);
            }
        });

        storageReference = FirebaseStorage.getInstance().getReference();
        databaseReference = FirebaseDatabase.getInstance().getReference().child("InstaPics");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFirebaseAuth.getCurrentUser();
        mFirebaseUserDatabase = FirebaseDatabase.getInstance().getReference()
                .child("users")
                .child(mCurrentUser.getUid());
    }

    public void addPictureFromGallery(View view) {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    public void submitDataToStorage(View view) {
        final String nameInput = nameEditText.getText().toString().trim();
        final String descInput = descEditText.getText().toString().trim();

        if (!TextUtils.isEmpty(nameInput) && !TextUtils.isEmpty(descInput)) {
            // Add image to Storage with name as the last path segment --> "info.jpg"
            StorageReference filePath = storageReference
                    .child("PostImage")
                    .child(uri.getLastPathSegment());

            // Add file to Firebase Storage and check if it was successfully uploaded
            filePath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    // Store the info about image as well as the image in the Database
                    final DatabaseReference newPost = databaseReference.push();

                    // Post new Picture values when new Picture data is added
                    mFirebaseUserDatabase.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            newPost.child("userId").setValue(mCurrentUser.getUid());
                            newPost.child("username").setValue(dataSnapshot.child("name").getValue());
                            newPost.child("title").setValue(nameInput);
                            newPost.child("description").setValue(descInput);
                            newPost.child("image").setValue(
                                    downloadUrl != null ? downloadUrl.toString() : null

                            ).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        // Redirect user back to MainActivity
                                        Intent mainIntent = new Intent(
                                                PostActivity.this, MainActivity.class
                                        );
                                        startActivity(mainIntent);
                                    }
                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                            Log.e("Post Error!", databaseError.getMessage());
                        }
                    });

                    Toast.makeText(
                            PostActivity.this,
                            "Upload Complete!",
                            Toast.LENGTH_LONG
                    ).show();
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            uri = data.getData();
            addPictureFromGallery.setImageURI(uri);
        }
    }
}

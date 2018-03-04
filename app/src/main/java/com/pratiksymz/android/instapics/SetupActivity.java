package com.pratiksymz.android.instapics;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Set;

public class SetupActivity extends AppCompatActivity {

    private ImageButton mProfileImageSelector;
    private EditText mProfileUsername;
    private Button mProfileSubmitButton;
    private static final int GALLERY_REQUEST = 2;
    private Uri mProfileImageUri = null;
    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseUserReference;
    private StorageReference mStorageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setup);

        mProfileImageSelector = findViewById(R.id.profile_picture_button);
        mProfileUsername = findViewById(R.id.profile_username);
        mProfileSubmitButton = findViewById(R.id.profile_submit_button);

        mProfileImageSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addProfileImage();
            }
        });

        mProfileSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitProfile();
            }
        });

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseUserReference = FirebaseDatabase.getInstance().getReference().child("users");
        mStorageReference = FirebaseStorage.getInstance().getReference().child("ProfileImage");
    }

    private void addProfileImage() {
        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST);
    }

    private void submitProfile() {
        final String username = mProfileUsername.getText().toString().trim();
        final String userID = mFirebaseAuth.getCurrentUser().getUid();
        if (!TextUtils.isEmpty(username) && mProfileImageUri != null) {
            // Adds imageUri to the defined path in Firebase Strorage defined by its lastPathSegment()
            StorageReference filePath = mStorageReference.child(mProfileImageUri.getLastPathSegment());
            filePath.putFile(mProfileImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // Reference to the name of a particular user through userID
                    mDatabaseUserReference.child(userID).child("name").setValue(username);
                    // Reference to the IMAGE of a particular user through userID
                    // Can't pass imageUri
                    // Get reference to the image stored in Firebase
                    String downloadUrl = taskSnapshot.getDownloadUrl().toString();
                    mDatabaseUserReference.child(userID).child("image").setValue(downloadUrl);

                    // Redirect user back to MainActivity
                    Intent mainIntent = new Intent(
                            SetupActivity.this, MainActivity.class
                    );
                    startActivity(mainIntent);
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            // Instead of making a URI, allow user to "CROP THE IMAGE"
            // Use "Android Image Cropper"
            Uri selectedImageUri = data.getData();
            CropImage.activity(selectedImageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);

        }

        // Get the cropped image
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {    // Successfully Cropped
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mProfileImageUri = result.getUri();
                mProfileImageSelector.setImageURI(mProfileImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e("Profile Error!", error.getMessage());
            }
        }
    }
}

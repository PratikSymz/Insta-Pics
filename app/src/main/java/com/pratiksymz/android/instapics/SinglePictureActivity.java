package com.pratiksymz.android.instapics;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SinglePictureActivity extends AppCompatActivity {

    private String postKey = null;
    private DatabaseReference mDatabaseReference;
    private ImageView fullscreenPictureView;
    private TextView fullscreenTitleView, fullscreenDescView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_picture);

        postKey = getIntent().getExtras().getString("postId");
        // Reference to the photos uploaded
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("InstaPics");
        fullscreenPictureView = findViewById(R.id.fullscreen_picture_view);
        fullscreenTitleView = findViewById(R.id.fullscreen_picture_title);
        fullscreenDescView = findViewById(R.id.fullscreen_picture_desc);

        mDatabaseReference.child(postKey).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                String postTitle = dataSnapshot.child("title").getValue().toString();
                String postDesc = dataSnapshot.child("description").getValue().toString();
                String postImageUrl = dataSnapshot.child("image").getValue().toString();
                // String postUserId = dataSnapshot.child("userId").getValue().toString();

                Picasso.with(SinglePictureActivity.this)
                        .load(postImageUrl)
                        .into(fullscreenPictureView);

                fullscreenTitleView.setText(postTitle);
                fullscreenDescView.setText(postDesc);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}

package com.pratiksymz.android.instapics;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mPicturesList;

    // Reference to Firebase Database for displaying data
    private DatabaseReference mDatabaseReference;

    // Reference to Firebase Authentication for user authentication
    private FirebaseAuth mFirebaseAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPicturesList = findViewById(R.id.picture_list);
        mPicturesList.setHasFixedSize(true);
        mPicturesList.setLayoutManager(new LinearLayoutManager(this));
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("InstaPics");

        mFirebaseAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                // If the current user is not registered
                if (firebaseAuth.getCurrentUser() == null) {
                    Intent loginIntent = new Intent(
                            MainActivity.this, RegisterActivity.class
                    );
                    // To not enable the user to go back to previous activity by pressing BACK button
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(loginIntent);
                }
            }
        };
    }

    public static class PictureViewHolder extends RecyclerView.ViewHolder {
        private View mItemView;

        public PictureViewHolder(View itemView) {
            super(itemView);
            mItemView = itemView;
        }

        public void setUsername(String username) {
            TextView postUsernameView = mItemView.findViewById(R.id.picture_user_view);
            postUsernameView.setText("@" + username);
        }

        public void setTitle(String title) {
            TextView postTitleView = mItemView.findViewById(R.id.picture_title_view);
            postTitleView.setText(title);
        }

        public void setDesc(String description) {
            TextView postDescView = mItemView.findViewById(R.id.picture_desc_view);
            postDescView.setText(description);
        }

        public void setImage(Context context, String imageUrl) {
            ImageView postPictureView = mItemView.findViewById(R.id.picture_image_view);
            Picasso.with(context).load(imageUrl).into(postPictureView);
        }

        public CardView getCardView() {
            CardView pictureCard = mItemView.findViewById(R.id.picture_card_view);
            return pictureCard;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == R.id.action_add_picture) {
            Intent intent = new Intent(this, PostActivity.class);
            startActivity(intent);
        } else if (id == R.id.action_logout) {
            mFirebaseAuth.signOut();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // OnStart of app --> Register the user
        mFirebaseAuth.addAuthStateListener(mAuthStateListener);
        FirebaseRecyclerAdapter<Picture, PictureViewHolder> firebaseRecyclerAdapter =
                new FirebaseRecyclerAdapter<Picture, PictureViewHolder>(
                        Picture.class,
                        R.layout.picture_item,
                        PictureViewHolder.class,
                        mDatabaseReference
                ) {
                    @Override
                    protected void populateViewHolder(final PictureViewHolder viewHolder, Picture model, int position) {
                        viewHolder.setUsername(model.getUsername());
                        viewHolder.setTitle(model.getTitle());
                        viewHolder.setDesc(model.getDescription());
                        viewHolder.setImage(getApplicationContext(), model.getImage());

                        final String postKey = getRef(position).getKey();
                        /*viewHolder.mItemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Open the image in a new activity
                                Intent singlePictureIntent = new Intent(
                                        MainActivity.this, SinglePictureActivity.class
                                );
                                singlePictureIntent.putExtra("postId", postKey);
                            }
                        });*/

                        // Apply animation to the cardview on press
                        final CardView pictureCard = viewHolder.getCardView();

                        // Create a system to run the physics loop for a set of springs.
                        SpringSystem springSystem = SpringSystem.create();

                        // Add a spring to the system.
                        final Spring spring = springSystem.createSpring();

                        // Add a listener to observe the motion of the spring.
                        spring.addListener(new SimpleSpringListener() {
                            @Override
                            public void onSpringUpdate(Spring spring) {
                                // You can observe the updates in the spring
                                // state by asking its current value in onSpringUpdate.
                                float value = (float) spring.getCurrentValue();
                                float scale = 1f - (value * 0.1f);
                                pictureCard.setScaleX(scale);
                                pictureCard.setScaleY(scale);
                            }
                        });

                        pictureCard.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (event.getAction() == MotionEvent.AXIS_PRESSURE) {
                                    spring.setEndValue(1);
                                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                                    Intent singlePictureIntent = new Intent(
                                            MainActivity.this, SinglePictureActivity.class
                                    );
                                    singlePictureIntent.putExtra("postId", postKey);
                                    startActivity(singlePictureIntent);
                                } else {
                                    spring.setEndValue(0);
                                }

                                return true;
                            }
                        });
                    }
                };

        mPicturesList.setAdapter(firebaseRecyclerAdapter);
    }
}

package com.example.endpoverty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import static com.example.endpoverty.SetupActivity.Gallery_Pick;

public class PostsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private ImageButton newPostImage;
    private Button newPostButton;
    private EditText PostDescription;

    private Uri ImageUri;
    private String Description;
    private String mCurrentDate;
    private String mCurrentTime;
    private String RandomPostName;
    private String downloadUrl;
    private String Current_user_id;

    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;
    private StorageReference PostsImagesRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_posts);

        mAuth = FirebaseAuth.getInstance();
        Current_user_id = mAuth.getCurrentUser().getUid();
        PostsImagesRef = FirebaseStorage.getInstance().getReference();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        newPostImage = (ImageButton) findViewById(R.id.new_post_image);
        newPostButton = (Button) findViewById(R.id.new_post_button);
        PostDescription = (EditText) findViewById(R.id.post_description);

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Create Post");


        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();

            }
        });

        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PostInfoChecker();
            }
        });

    }

    private void PostInfoChecker() {
        Description = PostDescription.getText().toString();
        //Reminding user to enter their image
        if(ImageUri == null){
            Toast.makeText(this, "Please select Image", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(Description)){
            Toast.makeText(this, "Enter Post Description", Toast.LENGTH_SHORT).show();
        }
        else{
            ImageToFirebaseStorage();
        }

    }

    private void ImageToFirebaseStorage() {

        //Save date post is created
        Calendar callDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("dd-MMMM-yyyy");
        mCurrentDate = currentDate.format(callDate.getTime());

        //Save time post is created
        Calendar callTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("HH:mm");
        mCurrentTime = currentTime.format(callTime.getTime());

        RandomPostName = mCurrentDate + mCurrentTime;

        StorageReference filePath = PostsImagesRef.child("Posts Images").child(ImageUri.getLastPathSegment() + RandomPostName + ".jpg");
        filePath.putFile(ImageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if(task.isSuccessful()){
                    downloadUrl = task.getResult().getMetadata().getReference().getDownloadUrl().toString();
                    Toast.makeText(PostsActivity.this, "Image Uploaded Sucessfully", Toast.LENGTH_SHORT).show();

                    StorePostInfoDatabase();
                }
                else{
                    String message = task.getException().toString();
                    Toast.makeText(PostsActivity.this, "Error: "+ message, Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    private void StorePostInfoDatabase() {
        UsersRef.child(Current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    String userFullName = dataSnapshot.child("fullname").getValue().toString();
                    String userProfileImage = dataSnapshot.child("ProfileImage").getValue().toString();

                    HashMap postsMap = new HashMap();
                    postsMap.put("uid", Current_user_id);
                    postsMap.put("date", mCurrentDate);
                    postsMap.put("time", mCurrentTime);
                    postsMap.put("description", Description);
                    postsMap.put("postImage", downloadUrl);
                    postsMap.put("ProfileImage", userProfileImage);
                    postsMap.put("fullname", userFullName);


                    PostsRef.child(Current_user_id + RandomPostName).updateChildren(postsMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful()){
                                GoToMainActivity();
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }

    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, Gallery_Pick);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null){
            ImageUri = data.getData();
            newPostImage.setImageURI(ImageUri);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home){
            GoToMainActivity();
        }

        return super.onOptionsItemSelected(item);
    }

    private void GoToMainActivity() {
        Intent mainIntent = new Intent(PostsActivity.this, MainActivity.class);
        startActivity(mainIntent);
    }



}
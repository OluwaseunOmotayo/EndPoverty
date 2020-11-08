package com.example.endpoverty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {

    private NavigationView nav_View;
    private DrawerLayout drawer_Layout;
    private RecyclerView posts_List;
    private Toolbar mToolbar;
    private ActionBarDrawerToggle toolbar_Toggle;
    private CircleImageView navProfileImage;
    private TextView navProfileName;
    private ImageButton AddPost;


    private FirebaseAuth mAuth;
    private DatabaseReference UsersRef, PostsRef;

    String Current_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mAuth = FirebaseAuth.getInstance();
        Current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        PostsRef = FirebaseDatabase.getInstance().getReference().child("Posts");

        mToolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Home");

        AddPost = (ImageButton) findViewById(R.id.add_post);

        drawer_Layout = (DrawerLayout) findViewById(R.id.Drawer_Layout);
        toolbar_Toggle =new ActionBarDrawerToggle(MainActivity.this, drawer_Layout, R.string.drawer_open, R.string.drawer_close);
        drawer_Layout.addDrawerListener(toolbar_Toggle);
        toolbar_Toggle.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nav_View = (NavigationView) findViewById(R.id.navigation_view);
        View NavView = nav_View.inflateHeaderView(R.layout.nav_header);
        navProfileImage = (CircleImageView) NavView.findViewById(R.id.nav_profile_image) ;
        navProfileName = (TextView) NavView.findViewById(R.id.nav_full_name);

        posts_List = (RecyclerView) findViewById(R.id.users_post_list);
        posts_List.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true); //to make sure new posts appear on top
        linearLayoutManager.setStackFromEnd(true);
        posts_List.setLayoutManager(linearLayoutManager);



        UsersRef.child(Current_user_id).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){

                    if(dataSnapshot.hasChild("fullname")){
                        String full_name = dataSnapshot.child("fullname").getValue().toString();
                        navProfileName.setText(full_name);
                    }

                    if(dataSnapshot.hasChild("ProfileImage")){
                        String nav_image = dataSnapshot.child("ProfileImage").getValue().toString();
                        Picasso.get().load(nav_image).placeholder(R.drawable.profile).into(navProfileImage);

                    }
                    else{
                        Toast.makeText(MainActivity.this, "User Profile has not been set...", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        nav_View.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                UserMenuSelector(menuItem);
                return false;
            }
        });

        AddPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoToPostsActivity();
            }
        });

        DisplayUsersPosts();
    }

    private void DisplayUsersPosts() {
        FirebaseRecyclerOptions<Posts> firebaseRecyclerOptions =
                new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostsRef, Posts.class).build();
        FirebaseRecyclerAdapter firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Posts, PostsHolder>(firebaseRecyclerOptions) {
            @Override
            protected void onBindViewHolder(@NonNull    PostsHolder holder, int position, @NonNull Posts model) {
                holder.username.setText(model.getFullname());
                holder.postTime.setText(" "+model.getTime());
                holder.postDate.setText(" "+model.getDate());
                holder.postDescription.setText(model.getDescription());
                Picasso.get().load(model.getPostImage()).into(holder.post_image);
                Picasso.get().load(model.getprofile_Image()).into(holder.profile_image);
            }

            @NonNull
            @Override
            public PostsHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.all_posts_layout, parent, false);
                PostsHolder viewHolder = new PostsHolder(view);
                return viewHolder;
            }
        };
        posts_List.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }
    public static class PostsHolder extends RecyclerView.ViewHolder {
        TextView username, postTime, postDate, postDescription;
        CircleImageView profile_image;
        ImageView post_image;

        public PostsHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.post_user_name);
            postTime = itemView.findViewById(R.id.post_time);
            postDate = itemView.findViewById(R.id.post_date);
            postDescription = itemView.findViewById(R.id.post_Description);
            profile_image = itemView.findViewById(R.id.post_profile_image);
            post_image = itemView.findViewById(R.id.post_Image);

        }
    }

    private void GoToPostsActivity() {
        Intent postIntent = new Intent(MainActivity.this, PostsActivity.class);
        startActivity(postIntent);
    }

    protected void onStart() {
        super.onStart();

        //Check to see if user exists
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            GoToLoginActivity();
        }
        else{
            CheckIfUserExists();
        }
    }

    private void CheckIfUserExists() {
        final String current_user_id = mAuth.getCurrentUser().getUid();
        UsersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(current_user_id)){
                    GoToSetupActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void GoToSetupActivity() {
        Intent setupIntent = new Intent(MainActivity.this, SetupActivity.class);
        setupIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(setupIntent);
        finish();
    }



    public boolean onOptionsItemSelected(MenuItem item) {
        if(toolbar_Toggle.onOptionsItemSelected(item)){
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

//activate menu options
    private void UserMenuSelector(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.nav_post:
                GoToPostsActivity();
                break;
            case R.id.nav_profile:
                Toast.makeText(this, "Profile", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_home:
                Toast.makeText(this, "Home", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_organizations:
                Toast.makeText(this, "Organizations", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_monthly_spotlight:
                Toast.makeText(this, "Monthly Spotlight", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_reminder:
                Toast.makeText(this, "Giving Reminder", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_settings:
               // SendUserToSettingsActivity();
                break;
            case R.id.nav_logout:
                mAuth.signOut();
               GoToLoginActivity();
                break;
        }
    }

    private void GoToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }
}
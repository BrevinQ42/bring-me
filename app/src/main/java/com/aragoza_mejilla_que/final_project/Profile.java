package com.aragoza_mejilla_que.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class Profile extends AppCompatActivity {

    private Realm realm;
    private String currentUserID;
    private User currentUser;

    private ImageView backButton;
    private ImageView profilePic;
    private TextView username;
    private TextView photosPosted;
    private Button editProfileButton;
    private Button logoutButton;
    private RecyclerView photoGrid;

    private PhotoGridAdapter photoGridAdapter;
    private RealmResults<Photo> userPhotos;

    SharedPreferences prefs;

    public static final int REQUEST_CODE_EDIT_PROFILE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRealm();
        initViews();
        loadUserProfile();
        setupRecyclerView();
        setupButtons();
    }

    private void initRealm() {
        realm = Realm.getDefaultInstance();
        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);
    }

    private void initViews() {
        backButton = findViewById(R.id.backButton);
        profilePic = findViewById(R.id.profilePic);
        username = findViewById(R.id.username);
        photosPosted = findViewById(R.id.photosPosted);
        editProfileButton = findViewById(R.id.editProfile);
        logoutButton = findViewById(R.id.logout);
        photoGrid = findViewById(R.id.photoGrid);
    }

    private void loadUserProfile() {
        currentUserID = getIntent().getStringExtra("userID");

        if (currentUserID == null) {
            Toast.makeText(this, "User ID not provided.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        currentUser = realm.where(User.class).equalTo("userID", currentUserID).findFirst();

        if (currentUser == null) {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        username.setText("@" + currentUser.getName());
        loadProfilePicture(currentUser);

        long photoCount = realm.where(Photo.class)
                .equalTo("userID", currentUser.getUserID())
                .count();
        photosPosted.setText("Photos Posted: " + photoCount);

        String loggedInUserID = prefs.getString("userID", null);
        if (loggedInUserID != null && loggedInUserID.equals(currentUserID)) {
            editProfileButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.VISIBLE);
        } else {
            editProfileButton.setVisibility(View.GONE);
            logoutButton.setVisibility(View.GONE);
        }
    }

    private void loadProfilePicture(User user) {
        File cacheDir = getExternalCacheDir();
        File userPhoto = new File(cacheDir, user.getUserID() + ".jpeg");

        if (userPhoto.exists())
        {
            Picasso.get()
                    .load(userPhoto)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(profilePic);
        }
        else
        {
            profilePic.setImageResource(R.mipmap.ic_launcher); // insert default photo
        }
    }

    private void setupRecyclerView() {
        userPhotos = realm.where(Photo.class)
                .equalTo("userID", currentUserID)
                .findAll()
                .sort("photoID", Sort.DESCENDING);

        photoGrid.setLayoutManager(new GridLayoutManager(this, 3));
        photoGridAdapter = new PhotoGridAdapter(this, userPhotos);
        photoGrid.setAdapter(photoGridAdapter);

        userPhotos.addChangeListener(photos -> photoGridAdapter.notifyDataSetChanged());
    }

    private void setupButtons() {
        backButton.setOnClickListener(v -> finish());

        editProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(Profile.this, EditScreen.class);
            intent.putExtra("userID", currentUserID);
            startActivityForResult(intent, REQUEST_CODE_EDIT_PROFILE);
        });

        logoutButton.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.remove("userID");
            editor.apply();

            Intent intent = new Intent(Profile.this, LoginScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
            Toast.makeText(this, "Logged out.", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        loadUserProfile();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null && !realm.isClosed()) {
            realm.close();
        }
        if (userPhotos != null) {
            userPhotos.removeAllChangeListeners();
        }
    }
}
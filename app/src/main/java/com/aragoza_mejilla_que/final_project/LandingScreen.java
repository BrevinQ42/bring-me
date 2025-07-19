package com.aragoza_mejilla_que.final_project;

import static com.aragoza_mejilla_que.final_project.PromptScreen.REQUEST_CODE_IMAGE_SCREEN;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class LandingScreen extends AppCompatActivity {
    private RecyclerView postsList;
    private PhotoAdapter photoAdapter;
    private Realm realm;
    private RealmResults<Photo> photoResults;
    SharedPreferences prefs;

    ImageView btArchive;
    ImageView btCamera;
    ImageView btUsers;
    ImageView profilePicture;
    Prompt currentPrompt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_landing_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        btArchive = findViewById(R.id.btArchive);
        btCamera = findViewById(R.id.btCamera);
        btUsers = findViewById(R.id.btUsers);
        profilePicture = findViewById(R.id.profilePicture);

        realm = Realm.getDefaultInstance();
        postsList = findViewById(R.id.postsList);
        postsList.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        currentPrompt = realm.where(Prompt.class)
                .equalTo("isActive", true)
                .findFirst();

        if (currentPrompt != null) {
            photoResults = realm.where(Photo.class)
                    .equalTo("promptID", currentPrompt.getPromptID())
                    .sort("photoID", Sort.DESCENDING)
                    .findAll();
        } else {

            photoResults = realm.where(Photo.class).equalTo("promptID", "no_active_prompt_id").findAll();
            Toast.makeText(this, "No active prompt found.", Toast.LENGTH_LONG).show();
        }

        photoAdapter = new PhotoAdapter(this, photoResults);
        postsList.setAdapter(photoAdapter);

        photoResults.addChangeListener(photos -> photoAdapter.notifyDataSetChanged());

        currentPrompt = realm.where(Prompt.class)
                .equalTo("isActive", true)
                .findFirst();

        // button logic
        btArchive.setOnClickListener(v -> {
            Intent i = new Intent(this, Archives.class);
            startActivity(i);
        });

        btCamera.setOnClickListener(v -> takePicture());

        btUsers.setOnClickListener(v -> {
            Intent i = new Intent(this, UsersScreen.class);
            startActivity(i);
        });

        profilePicture.setOnClickListener(v -> {
            String loggedInUserID = prefs.getString("userID", null);

            Intent intent = new Intent(this, Profile.class);
            intent.putExtra("userID", loggedInUserID);
            startActivity(intent);
        });

        // profile picture
        String loggedInUserID = prefs.getString("userID", null);
        File cacheDir = getExternalCacheDir();
        File userPhoto = new File(cacheDir, loggedInUserID + ".jpeg");

        if (userPhoto.exists())
        {
            Picasso.get()
                    .load(userPhoto)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(profilePicture);
        }
        else
        {
            profilePicture.setImageResource(R.mipmap.ic_launcher); // insert default photo
        }
    }

    public void takePicture()
    {
        if (currentPrompt == null)
        {
            Toast t = Toast.makeText(this, "No prompt active", Toast.LENGTH_LONG);
            t.show();
            return;
        }

        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }

    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode==REQUEST_CODE_IMAGE_SCREEN)
        {
            if (responseCode==ImageActivity.RESULT_CODE_IMAGE_TAKEN)
            {
                String userID = prefs.getString("userID", "null");

                // to create unique filenames, use uuid
                String photoId = UUID.randomUUID().toString();
                String fileName = photoId + ".jpeg";

                // receive the raw JPEG data from ImageActivity
                // this can be saved to a file or save elsewhere like Realm or online
                byte[] rawImage = data.getByteArrayExtra("rawJpeg");

                try
                {
                    // save rawImage
                    saveFile(rawImage, fileName);

                    Photo pic = new Photo();
                    pic.setImagePath(fileName);
                    pic.setUserID(userID);
                    pic.setPromptID(currentPrompt.getPromptID());

                    String caption = data.getStringExtra("caption");
                    pic.setCaption(caption);

                    pic.setLikeCount(0);

                    realm.beginTransaction();
                    realm.copyToRealmOrUpdate(pic);
                    realm.commitTransaction();

                    Toast t = Toast.makeText(this, fileName + " is saved", Toast.LENGTH_LONG);
                    t.show();

                    Intent viewImageIntent = new Intent(this, ViewImageScreen.class);
                    viewImageIntent.putExtra("photoID", pic.getPhotoID());
                    startActivity(viewImageIntent);
                }
                catch (Exception e)
                {
                    Toast t = Toast.makeText(this, "Error saving new pic", Toast.LENGTH_LONG);
                    t.show();
                }
            }
        }
    }

    private File saveFile(byte[] jpeg, String filename) throws IOException
    {
        // this is the root directory for the images
        File getImageDir = getExternalCacheDir();

        // just a sample, normally you have a diff image name each time
        File savedImage = new File(getImageDir, filename);


        FileOutputStream fos = new FileOutputStream(savedImage);
        fos.write(jpeg);
        fos.close();
        return savedImage;
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
    }
}
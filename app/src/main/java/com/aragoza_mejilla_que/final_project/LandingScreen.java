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

        realm = Realm.getDefaultInstance();
        postsList = findViewById(R.id.postsList);
        postsList.setLayoutManager(new LinearLayoutManager(this));

        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        photoResults = realm.where(Photo.class).findAll().sort("photoID", Sort.DESCENDING);
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
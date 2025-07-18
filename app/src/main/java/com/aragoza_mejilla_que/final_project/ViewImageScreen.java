package com.aragoza_mejilla_que.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Date;

import io.realm.Realm;

public class ViewImageScreen extends AppCompatActivity {

    Realm realm;
    SharedPreferences prefs;
    TextView currentPromptText;
    Prompt prompt;
    String[] month =
    {
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };
    ImageView currentPhoto;
    TextView photoLikeCount;
    TextView photoCaption;
    Photo pic;
    ImageView opProfilePic;
    TextView opUsername;
    ImageView userDP;
    User user;
    ImageView backToLandingButton;
    ImageView heart;
    Boolean isPhotoLiked;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_view_image_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRealm();
        initViews();
    }

    void initRealm()
    {
        realm = Realm.getDefaultInstance();
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
    }

    void initViews()
    {
        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        currentPromptText = findViewById(R.id.currentPromptText);
        currentPhoto = findViewById(R.id.currentPhoto);
        photoLikeCount = findViewById(R.id.photoLikeCount);
        photoCaption = findViewById(R.id.photoCaption);
        opProfilePic = findViewById(R.id.opProfilePic);
        opUsername = findViewById(R.id.opUsername);
        userDP = findViewById(R.id.userDP);
        heart = findViewById(R.id.heart);
        backToLandingButton = findViewById(R.id.backtoLandingButton);

        setupCurrentPromptText();
        setupCurrentPhoto();
        setupOPinfo();
        setupUserDP();
        setupLikeButton();

        backToLandingButton.setOnClickListener(v -> goToLandingScreen());
    }

    void setupCurrentPromptText()
    {
        prompt = realm.where(Prompt.class)
                        .equalTo("isActive", true)
                        .findFirst();

        Date currentTime = new Date(System.currentTimeMillis());
        int hoursLeft = 24 - currentTime.getHours() - 1;
        int minutesLeft = 60 - currentTime.getMinutes() - 1;
        int secondsLeft = 60 - Math.min(currentTime.getSeconds(), 59);

        if (secondsLeft == 60)
        {
            secondsLeft = 0;
            minutesLeft++;
        }

        if (minutesLeft == 60)
        {
            minutesLeft = 0;
            hoursLeft++;
        }

        String timeLeft = hoursLeft + ":" + minutesLeft + ":" + secondsLeft;

        currentPromptText.setText(
                month[prompt.getDate().getMonth()] + " " + prompt.getDate().getDate() + ", " +
                        prompt.getText() + "\n" + timeLeft
        );
    }

    void setupCurrentPhoto()
    {
        Intent i = getIntent();
        String photoID = i.getStringExtra("photoID");

        pic = realm.where(Photo.class)
                            .equalTo("photoID", photoID)
                            .findFirst();

        if (pic == null)
            return;

        // get cache directory
        File cacheDir = getExternalCacheDir();
        File photo = new File(cacheDir, pic.getImagePath());

        if (photo.exists())
        {
            Picasso.get()
                    .load(photo)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(currentPhoto);
        }
        else
        {
            currentPhoto.setImageResource(R.mipmap.ic_launcher); // insert default photo
        }

        photoLikeCount.setText(String.valueOf(pic.getLikeCount()));

        if (pic.getCaption().equals("null"))
            photoCaption.setText("");
        else
            photoCaption.setText(pic.getCaption());
    }

    void setupOPinfo()
    {
        User op = realm.where(User.class)
                        .equalTo("userID", pic.getUserID())
                        .findFirst();

        String profilePicPath;
        try
        {
            profilePicPath = op.getProfilePicturePath();

            if (profilePicPath.isEmpty())
            {
                opProfilePic.setImageResource(R.mipmap.ic_launcher);
            }
            else
            {
                // get cache directory
                File cacheDir = getExternalCacheDir();
                File profilePic = new File(cacheDir, profilePicPath);

                if (profilePic.exists())
                {
                    Picasso.get()
                            .load(profilePic)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(opProfilePic);
                }
                else
                {
                    opProfilePic.setImageResource(R.mipmap.ic_launcher); // insert default photo
                }
            }
        }
        catch (Exception e)
        {
            opProfilePic.setImageResource(R.mipmap.ic_launcher);
        }

        opUsername.setText(op.getName());
    }

    void setupUserDP()
    {
        String userID = prefs.getString("userID", "null");

        user = realm.where(User.class)
                    .equalTo("userID", userID)
                    .findFirst();

        String profilePicPath;
        try
        {
            profilePicPath = user.getProfilePicturePath();

            if (profilePicPath.isEmpty())
            {
                userDP.setImageResource(R.mipmap.ic_launcher);
            }
            else
            {
                // get cache directory
                File cacheDir = getExternalCacheDir();
                File profilePic = new File(cacheDir, profilePicPath);

                if (profilePic.exists())
                {
                    Picasso.get()
                            .load(profilePic)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(userDP);
                }
                else
                {
                    userDP.setImageResource(R.mipmap.ic_launcher); // insert default photo
                }
            }
        }
        catch (Exception e)
        {
            userDP.setImageResource(R.mipmap.ic_launcher);
        }
    }

    void goToLandingScreen()
    {
        saveLikeStatus();

        // go to landing screen
    }

    void setupLikeButton()
    {
        // if this user is the same user as the owner of the photo
        if (user.getUserID().equals(pic.getUserID()))
            return; // don't setup like button

        isPhotoLiked = pic.isUserLiker(user);

        heart.setOnClickListener(v -> likePhoto());
    }

    void likePhoto()
    {
        if (isPhotoLiked)
        {
            Toast t = Toast.makeText(this, "You have UNLIKED this post", Toast.LENGTH_LONG);
            t.show();
        }
        else
        {
            Toast t = Toast.makeText(this, "You have LIKED this post", Toast.LENGTH_LONG);
            t.show();
        }

        isPhotoLiked = !isPhotoLiked;
    }

    void saveLikeStatus()
    {
        // if the photo is liked and the user liked it before going to this screen, or
        // the photo is unliked and the user has it unliked before going to this screen,
        if (isPhotoLiked == pic.isUserLiker(user))
            return; // disregard

        // else
        try
        {
            realm.beginTransaction();

            // if the photo is now liked
            if (isPhotoLiked)
                pic.addUserToLikers(user);

            // else, if the photo is now unliked
            else
                pic.removeUserFromLikers(user);

            realm.copyToRealmOrUpdate(pic);
            realm.commitTransaction();

            Toast t = Toast.makeText(this, "Like status has been saved", Toast.LENGTH_LONG);
            t.show();
        }
        catch (Exception e)
        {
            Toast t = Toast.makeText(this, "Error saving like status", Toast.LENGTH_LONG);
            t.show();
        }
    }
}
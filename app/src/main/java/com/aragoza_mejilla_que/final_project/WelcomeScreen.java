package com.aragoza_mejilla_que.final_project;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;

public class WelcomeScreen extends AppCompatActivity {
    Realm realm;
    TextView welcomeText;

    ImageView userPhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_welcome_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRealm();
        initViews();
    }

    public void initRealm()
    {
        realm = Realm.getDefaultInstance();
    }

    public void initViews()
    {
        welcomeText = findViewById(R.id.welcomeText);
        userPhoto = findViewById(R.id.userPhoto);

        SharedPreferences prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        // get saved state of rememberMe checkbox
        boolean isRememberMeChecked = prefs.getBoolean("isRememberMeChecked", false);

        // get saved name
        //String name = prefs.getString("name", "");

        // get saved uuid
        String uuid = prefs.getString("uuid", "");

        String name = "";

        if (!uuid.isEmpty())
        {
            User activeUser = realm.where(User.class)
                                .equalTo("uuid", uuid)
                                .findFirst();

            if (activeUser != null)
            {
                // get the user'sname
                name = activeUser.getName();

                // get cache directory
                File cacheDir = getExternalCacheDir();
                File imageFile = new File(cacheDir, activeUser.getUserID() + ".jpeg");

                if (imageFile.exists())
                {
                    Picasso.get()
                            .load(imageFile)
                            .networkPolicy(NetworkPolicy.NO_CACHE)
                            .memoryPolicy(MemoryPolicy.NO_CACHE)
                            .into(userPhoto);
                }
                else
                {
                    userPhoto.setImageResource(R.mipmap.ic_launcher); // insert default photo
                }
            }
        }

        // if rememberMe is checked, put "You will be remembered" after welcome message
        if (isRememberMeChecked)
            welcomeText.setText("Welcome, " + name + "!\nYou will be remembered!");

        // else, just put the welcome message
        else
            welcomeText.setText("Welcome, " + name + "!");
    }
}
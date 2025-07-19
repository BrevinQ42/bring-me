package com.aragoza_mejilla_que.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class EditScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_edit_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    Realm realm;

    EditText newUsername;
    SharedPreferences prefs;
    EditText newPassword;
    EditText confirmNewPassword;
    Button saveButton;
    Button cancelButton;

    ImageView newPhoto;

    byte[] rawImage = {};
    File newImage;

    void init()
    {
        // initialize realm
        realm = Realm.getDefaultInstance();

        // initialize prefs
        prefs = getSharedPreferences("userToBeEdited", MODE_PRIVATE);

        // get uuid of user to be edited
        // String userID = prefs.getString("userID", "null");
        String userID = getIntent().getStringExtra("userID");

        // initialize views
        newUsername = findViewById(R.id.newUsername);
        newPassword = findViewById(R.id.newPassword);
        confirmNewPassword = findViewById(R.id.confirmNewPassword);

        // get user to be edited
        User u = realm.where(User.class)
                    .equalTo("userID", userID)
                    .findFirst();

        // fill in username and passwords as necessary
        newUsername.setText(u.getName());
        newPassword.setText(u.getPassword());
        confirmNewPassword.setText(u.getPassword());

        // initialize buttons
        saveButton = findViewById(R.id.saveButton);
        cancelButton = findViewById(R.id.cancelButton);

        saveButton.setOnClickListener(v -> saveChanges(u));
        cancelButton.setOnClickListener(v -> cancelChanges());

        // initialize imageView
        newPhoto = findViewById(R.id.newPhoto);
        newPhoto.setOnClickListener(v -> takeNewPicture());

        // set picture

        // get cache directory
        File cacheDir = getExternalCacheDir();
        File userPhoto = new File(cacheDir, u.getUserID() + ".jpeg");

        if (userPhoto.exists())
        {
            Picasso.get()
                    .load(userPhoto)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(newPhoto);
        }
        else
        {
            newPhoto.setImageResource(R.mipmap.ic_launcher); // insert default photo
        }
    }

    void saveChanges(User u)
    {
        // get new username and password
        String username = newUsername.getText().toString();
        String password = newPassword.getText().toString();
        String confirmPassword = confirmNewPassword.getText().toString();

        // name should not be blank
        if (username.isEmpty())
        {
            Toast t = Toast.makeText(this, "Name must not be blank", Toast.LENGTH_LONG);
            t.show();
        }
        // password should be equal to confirm password
        else if (!password.equals(confirmPassword))
        {
            Toast t = Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_LONG);
            t.show();
        }
        else
        {
            User existingUser = realm.where(User.class)
                                        .equalTo("name", username)
                                        .findFirst();

            // check if user exists and is not the same with the user being edited
            if (existingUser != null && !existingUser.equals(u))
            {
                Toast t = Toast.makeText(this, "User already exists", Toast.LENGTH_LONG);
                t.show();
            }
            // else
            else
            {
                try
                {
                    // save changes
                    realm.beginTransaction();
                    u.setName(username);
                    u.setPassword(password);
                    realm.copyToRealmOrUpdate(u);
                    realm.commitTransaction();

                    if (rawImage.length > 0)
                    {
                        // save rawImage to file
                        File savedImage = saveFile(rawImage, u.getUserID() + ".jpeg");

                        // load file to the image view via picasso
                        refreshImageView(newPhoto, savedImage);

                        // check if newImage variable has something
                        if (newImage.exists())
                            newImage.delete(); // delete it
                    }

                    // send corresponding message through toast
                    Toast t = Toast.makeText(this, "Edited", Toast.LENGTH_LONG);
                    t.show();

                    // go back to admin page
                    setResult(RESULT_OK);
                    finish();
                }
                catch (Exception e)
                {
                    // send corresponding message through toast
                    Toast t = Toast.makeText(this, "Error saving changes", Toast.LENGTH_LONG);
                    t.show();
                }
            }
        }
    }

    void cancelChanges()
    {
        // check if newImage variable has something
        if (newImage != null && newImage.exists())
            newImage.delete(); // delete it

        // go back to admin page
        setResult(RESULT_CANCELED);
        finish();
    }

    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    public void takeNewPicture()
    {
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }


    // SINCE WE USE startForResult(), code will trigger this once the next screen calls finish()
    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode==REQUEST_CODE_IMAGE_SCREEN)
        {
            if (responseCode==ImageActivity.RESULT_CODE_IMAGE_TAKEN)
            {
                // receieve the raw JPEG data from ImageActivity
                // this can be saved to a file or save elsewhere like Realm or online
                rawImage = data.getByteArrayExtra("rawJpeg");
                try
                {
                    // save rawImage to tempfile
                    newImage = saveFile(rawImage, "temp.jpeg");

                    // load file to the image view via picasso
                    refreshImageView(newPhoto, newImage);
                }
                catch (Exception e)
                {
                    Toast t = Toast.makeText(this, "Error showing new pic", Toast.LENGTH_LONG);
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



    private void refreshImageView(ImageView imageView, File savedImage) {


        // this will put the image saved to the file system to the imageview
        Picasso.get()
                .load(savedImage)
                .networkPolicy(NetworkPolicy.NO_CACHE)
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView);
    }
}
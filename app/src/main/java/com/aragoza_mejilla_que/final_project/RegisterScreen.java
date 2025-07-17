package com.aragoza_mejilla_que.final_project;

import android.Manifest;
import android.content.Intent;
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

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;

public class RegisterScreen extends AppCompatActivity {
    Realm realm;

    EditText usernameInput;
    EditText passwordInput;
    EditText confirmPasswordInput;
    Button saveNewUserButton;
    Button cancelRegisButton;
    ImageView photoInput;

    byte[] rawImage = {};
    File newImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissions();
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
    }

    public void checkPermissions()
    {

        // REQUEST PERMISSIONS for Android 6+
        // THESE PERMISSIONS SHOULD MATCH THE ONES IN THE MANIFEST
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA

                )

                .withListener(new BaseMultiplePermissionsListener()
                {
                    public void onPermissionsChecked(MultiplePermissionsReport report)
                    {
                        if (report.areAllPermissionsGranted())
                        {
                            // all permissions accepted proceed
                            initRealm();
                            initViews();
                        }
                        else
                        {
                            // notify about permissions
                            toastRequirePermissions();
                        }
                    }
                })
                .check();

    }


    public void toastRequirePermissions()
    {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }

    void initRealm()
    {
        realm = Realm.getDefaultInstance();
    }

    public void initViews()
    {
        usernameInput = findViewById(R.id.usernameInput);
        passwordInput = findViewById(R.id.passwordInput);
        confirmPasswordInput = findViewById(R.id.confirmPasswordInput);

        saveNewUserButton = findViewById(R.id.saveNewUserButton);
        cancelRegisButton = findViewById(R.id.cancelRegisButton);
        photoInput = findViewById(R.id.photoInput);

        saveNewUserButton.setOnClickListener(v -> saveUser());
        cancelRegisButton.setOnClickListener(v -> cancelRegistration());
        photoInput.setOnClickListener(v -> takePicture());
    }

    void saveUser()
    {
        // checked if username field is empty
        String username = usernameInput.getText().toString();
        if (username.isEmpty())
        {
            // if it is, send error message through toast
            Toast t = Toast.makeText(this, "Name must not be blank", Toast.LENGTH_LONG);
            t.show();
        }
        else
        {
            // else,

            // get entered password and confirm password
            String password = passwordInput.getText().toString();
            String confirmPassword = confirmPasswordInput.getText().toString();

            // check if password matches confirm password
            if (!password.equals(confirmPassword))
            {
                // if they don't match, send error message through toast
                Toast t = Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_LONG);
                t.show();
            }
            else
            {
                // if they match,

                //LAB 1
                // SharedPreferences prefs = getSharedPreferences("savedUser", MODE_PRIVATE);
                // SharedPreferences.Editor edit = prefs.edit();
                //save credentials
                // edit.putString("username", username);
                // edit.putString("password", password);
                // edit.apply();

                //LAB 2
                // check if user exists
                User result = realm.where(User.class)
                                .equalTo("name", username)
                                .findFirst();

                // if there is a match, a user exists
                if (result != null)
                {
                    Toast t = Toast.makeText(this, "User already exists", Toast.LENGTH_LONG);
                    t.show();
                }
                // else, we can save new user to realm
                else
                {
                    // create new user
                    User newUser = new User();
                    newUser.setName(username);
                    newUser.setPassword(password);

                    try
                    {
                        // save new user to realm
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(newUser);
                        realm.commitTransaction();

                        // get number of users in realm
                        long userCount = realm.where(User.class).count();

                        // send corresponding message through toast
                        Toast t = Toast.makeText(this, "New User Saved.\tTotal: " + userCount, Toast.LENGTH_LONG);
                        t.show();

                        // if there was a rawImage generated for this user
                        if (rawImage.length > 0)
                        {
                            // save rawImage to file
                            File savedImage = saveFile(rawImage, newUser.getUuid() + ".jpeg");

                            // load file to the image view via picasso
                            refreshImageView(photoInput, savedImage);
                        }

                        // go back to login page
                        finish();
                    }
                    catch (Exception e)
                    {
                        Toast t = Toast.makeText(this, "Error saving", Toast.LENGTH_LONG);
                        t.show();
                    }
                }
            }
        }
    }

    void cancelRegistration()
    {
        // check if newImage variable has something
        if (newImage != null && newImage.exists())
            newImage.delete(); // delete it

        finish();
    }


    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    public void takePicture()
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
                    refreshImageView(photoInput, newImage);
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
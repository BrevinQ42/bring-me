package com.aragoza_mejilla_que.final_project;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import io.realm.Realm;

public class PromptScreen extends AppCompatActivity {
    Realm realm;
    SharedPreferences prefs;
    Prompt currentPrompt;
    TextView promptText;
    Button takePictureButton;
    Button landingScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_prompt_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissions();
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

    public void initRealm()
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

    public void initViews()
    {
        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        promptText = findViewById(R.id.promptText);

        currentPrompt = realm.where(Prompt.class)
                                    .equalTo("isActive", true)
                                    .findFirst();

        if (currentPrompt != null)
            promptText.setText(currentPrompt.getText());
        else
            promptText.setText("");

        takePictureButton = findViewById(R.id.takePictureButton);
        landingScreenButton = findViewById(R.id.landingScreenButton);

        takePictureButton.setOnClickListener(v -> takePicture());
        landingScreenButton.setOnClickListener(v -> goToLandingScreen());
    }

    void goToLandingScreen()
    {

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
                String userID = prefs.getString("userID", "null");

                Date promptDate = currentPrompt.getDate();
                String fileName = userID+promptDate.getMonth()+promptDate.getDate()+promptDate.getYear()+".jpeg";

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
}
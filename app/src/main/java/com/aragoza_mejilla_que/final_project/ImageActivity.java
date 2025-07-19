package com.aragoza_mejilla_que.final_project;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;

public class ImageActivity extends AppCompatActivity
{
    // additional stuff
    private Realm realm;
    private SharedPreferences prefs;
    private EditText caption;
    private ImageView profilePicture;
    private TextView currentPrompt;
    private String[] month =
    {
        "JAN", "FEB", "MAR", "APR", "MAY", "JUN",
        "JUL", "AUG", "SEP", "OCT", "NOV", "DEC"
    };

    private String fileAuthority;

    public static int RESULT_CODE_IMAGE_TAKEN = 100;
    public static int MAX_WIDTH = 500;
    public static int MAX_HEIGHT = 500;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_capture_image);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.card_view), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

    }


    CropImageView cropImageView;
    private ImageButton capture;
    private ImageButton rotate;
    private ImageButton crop;

    private ImageView cancel;




    public void init()
    {
        // this file authority is in the res/values/strings.xml
        // it MUST match the package name of the app or it will not work
        fileAuthority = getResources().getString(R.string.fileAuthority);

        // initialize crop image view
        cropImageView = findViewById(R.id.cropImageView);

        // initialize all buttons
        capture = findViewById(R.id.btCamera);
        capture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                capture();
            }
        });

        rotate = findViewById(R.id.btUsers);
        rotate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                rotate();
            }
        });

        crop = findViewById(R.id.btArchive);
        crop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                crop();
            }
        });

        cancel = findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        // additional stuff
        realm = Realm.getDefaultInstance();
        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);

        caption = findViewById(R.id.caption);

        profilePicture = findViewById(R.id.profilePicture);
        setupProfilePicture();

        currentPrompt = findViewById(R.id.currentPrompt);
        setupCurrentPrompt();
    }

    void setupProfilePicture()
    {
        String userID = prefs.getString("userID", "null");
        User user = realm.where(User.class)
                .equalTo("userID", userID)
                .findFirst();

        String profilePicPath;
        try
        {
            profilePicPath = user.getProfilePicturePath();

            if (profilePicPath.isEmpty())
            {
                profilePicture.setImageResource(R.mipmap.ic_launcher);
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
                            .into(profilePicture);
                }
                else
                {
                    profilePicture.setImageResource(R.mipmap.ic_launcher); // insert default photo
                }
            }
        }
        catch (Exception e)
        {
            profilePicture.setImageResource(R.mipmap.ic_launcher);
        }
    }

    void setupCurrentPrompt()
    {
        Prompt prompt = realm.where(Prompt.class)
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

        currentPrompt.setText(
                month[prompt.getDate().getMonth()] + " " + prompt.getDate().getDate() + ", " +
                        prompt.getText() + "\n" + timeLeft
        );
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
    }

    public void capture()
    {
        startActivityForResult(getPickImageChooserIntent(), 200);
    }

    public void rotate()
    {
        cropImageView.rotateImage(90);
    }


    public void cancel()
    {
        finish();
    }

    public void crop()
    {
        // extract to size
        Bitmap cropped =  cropImageView.getCroppedImage(MAX_WIDTH, MAX_HEIGHT);

        if (cropped != null)
        {

            System.out.println(cropped.getWidth()+" x "+cropped.getHeight());
            cropImageView.setImageBitmap(cropped);


            // encode image to JPEG format
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            cropped.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            byte[] byteArray = stream.toByteArray();


            // send this path back
            Intent i = new Intent();
            i.putExtra("rawJpeg", byteArray);

            String captionText = caption.getText().toString();
            if (captionText.isEmpty())
                captionText = "null";

            i.putExtra("caption", captionText);

            setResult(RESULT_CODE_IMAGE_TAKEN, i);
            finish();
        }
    }



    // PHONE CAMERA APP will return here once done
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        // do stuff here
        if (requestCode==200) {
            if (resultCode == Activity.RESULT_OK) {

                Uri imageUri = getPickImageResultUri(data);
                cropImageView.setImageUriAsync(imageUri);

                System.out.println("URI: " + imageUri.getPath());

                // update cropImageView
                cropImageView.setImageUriAsync(imageUri);
            }
        }
    }


    //==========================================================================================


    // from sample code
    // https://theartofdev.com/2015/02/15/android-cropping-image-from-camera-or-gallery/


    // NEEDED FOR ANDROID 9
    public Uri createUriFromFile(File file, boolean useFileProvider)
    {
        // may require API check for SDKs prior to N?

        if (!useFileProvider) {
            return Uri.fromFile(file);
        }
        else {
            return FileProvider.getUriForFile(this,
                    fileAuthority,
                    file);
        }
    }


    /**
     * Create a chooser intent to select the  source to get image from.<br/>
     * The source can be camera's  (ACTION_IMAGE_CAPTURE) or gallery's (ACTION_GET_CONTENT).<br/>
     * All possible sources are added to the  intent chooser.
     */
    public Intent getPickImageChooserIntent() {

// Determine Uri of camera image to  save.
        Uri outputFileUri =  getCaptureImageOutputUri();

        List<Intent> allIntents = new ArrayList<>();
        PackageManager packageManager =  getPackageManager();

// collect all camera intents
        Intent captureIntent = new  Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> listCam =  packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            Intent intent = new  Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            if (outputFileUri != null) {
                intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            }
            allIntents.add(intent);
        }

// collect all gallery intents
        Intent galleryIntent = new  Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
        List<ResolveInfo> listGallery =  packageManager.queryIntentActivities(galleryIntent, 0);
        for (ResolveInfo res : listGallery) {
            Intent intent = new  Intent(galleryIntent);
            intent.setComponent(new  ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            intent.setPackage(res.activityInfo.packageName);
            allIntents.add(intent);
        }

// the main intent is the last in the  list (fucking android) so pickup the useless one
        Intent mainIntent =  allIntents.get(allIntents.size() - 1);
        for (Intent intent : allIntents) {
            if  (intent.getComponent().getClassName().equals("com.android.documentsui.DocumentsActivity"))  {
                mainIntent = intent;
                break;
            }
        }
        allIntents.remove(mainIntent);

// Create a chooser from the main  intent
        Intent chooserIntent =  Intent.createChooser(mainIntent, "Select source");

// Add all other intents
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,  allIntents.toArray(new Parcelable[allIntents.size()]));

        return chooserIntent;
    }

    /**
     * Get URI to image received from capture  by camera.
     */
    private Uri getCaptureImageOutputUri() {
        Uri outputFileUri = null;
        File getImageDir = getExternalCacheDir();
        if (getImageDir != null) {
            outputFileUri = createUriFromFile(new  File(getImageDir.getPath(), "pickImageResult.jpeg"), true);
        }
        return outputFileUri;
    }

    /**
     * Get the URI of the selected image from  {@link #getPickImageChooserIntent()}.<br/>
     * Will return the correct URI for camera  and gallery image.
     *
     * @param data the returned data of the  activity result
     */
    public Uri getPickImageResultUri(Intent  data) {
        boolean isCamera = true;
        if (data != null && data.getData() != null) {
            String action = data.getAction();
            isCamera = action != null  && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
        }
        return isCamera ?  getCaptureImageOutputUri() : data.getData();
    }


}

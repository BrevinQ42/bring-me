package com.aragoza_mejilla_que.final_project;

import android.app.Application;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class MyApp extends Application {
    private static final String PREF_NAME = "AppPrefs";
    private static final String PREF_PREPOPULATED_DATA = "prepopulated_data";
    private static final String TAG = "MyApp_MyActivity";

    public void onCreate()
    {
        super.onCreate();

        Realm.init(this);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();
        Realm.setDefaultConfiguration(config);

        prepopulateData();
    }

    private void prepopulateData() {
        boolean dataPrepopulated = getSharedPreferences(PREF_NAME, MODE_PRIVATE)
                .getBoolean(PREF_PREPOPULATED_DATA, false);

        if (!dataPrepopulated) {
            Log.d(TAG, "Prepopulating");
            Realm realm = null;
            try {
                realm = Realm.getDefaultInstance();
                realm.executeTransaction(r -> {
                    // users
                    User user1 = r.createObject(User.class, UUID.randomUUID().toString());
                    user1.setName("kirby_poyo");
                    user1.setPassword("password123");
                    user1.setProfilePicturePath(user1.getUserID() + ".jpg");
                    saveImageToCache(user1.getUserID(), "kirby.jpg");

                    User user2 = r.createObject(User.class, UUID.randomUUID().toString());
                    user2.setName("StevenQuartzUniverse");
                    user2.setPassword("password123");
                    user2.setProfilePicturePath(user2.getUserID() + ".jpg");
                    saveImageToCache(user2.getUserID(), "steven.jpg");

                    User user3 = r.createObject(User.class, UUID.randomUUID().toString());
                    user3.setName("GalacticBaseballer");
                    user3.setPassword("password123");
                    user3.setProfilePicturePath(user3.getUserID() + ".jpg");
                    saveImageToCache(user3.getUserID(), "caelus.jpg");

                    // prompts
                    Prompt promptOld = r.createObject(Prompt.class, UUID.randomUUID().toString());
                    promptOld.setText("OLD! 🏛️");
                    promptOld.setDate(new Date(124, 5, 8));
                    promptOld.setActive(false);
                    promptOld.setImagePath(promptOld.getPromptID() + ".jpeg");
                    saveImageToCache(promptOld.getPromptID(), "old.jpg");

                    Prompt promptShiny = r.createObject(Prompt.class, UUID.randomUUID().toString());
                    promptShiny.setText("SHINY! ✨");
                    promptShiny.setDate(new Date(124, 5, 7));
                    promptShiny.setActive(false);
                    promptShiny.setImagePath(promptShiny.getPromptID() + ".jpeg");
                    saveImageToCache(promptShiny.getPromptID(), "shiny.jpg");

                    Prompt promptRound = r.createObject(Prompt.class, UUID.randomUUID().toString());
                    promptRound.setText("ROUND! ⚪");
                    promptRound.setDate(new Date(124, 5, 9));
                    promptRound.setActive(true);
                    promptRound.setImagePath(promptRound.getPromptID() + ".jpeg");
                    saveImageToCache(promptRound.getPromptID(), "round.jpg");

                    // posts
                    Photo photo1 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo1.setImagePath(photo1.getPhotoID() + ".jpeg");
                    photo1.setUserID(user1.getUserID());
                    photo1.setPromptID(promptOld.getPromptID());
                    photo1.setCaption("poyo poyo");
                    photo1.setLikeCount(1);
                    saveImageToCache(photo1.getPhotoID(), "post1.jpg");

                    Photo photo2 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo2.setImagePath(photo2.getPhotoID() + ".jpeg");
                    photo2.setUserID(user2.getUserID());
                    photo2.setPromptID(promptShiny.getPromptID());
                    photo2.setCaption("The Crystal Gems will always save the day!");
                    photo2.setLikeCount(2);
                    saveImageToCache(photo2.getPhotoID(), "post2.jpg");

                    Photo photo3 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo3.setImagePath(photo3.getPhotoID() + ".jpeg");
                    photo3.setUserID(user3.getUserID());
                    photo3.setPromptID(promptRound.getPromptID());
                    photo3.setCaption("she astral on my express until i trailblaze");
                    photo3.setLikeCount(3);
                    saveImageToCache(photo3.getPhotoID(), "post3.jpg");

                    Photo photo4 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo4.setImagePath(photo4.getPhotoID() + ".jpeg");
                    photo4.setUserID(user1.getUserID());
                    photo4.setPromptID(promptRound.getPromptID());
                    photo4.setCaption("poyo poyo (me)");
                    photo4.setLikeCount(2);
                    saveImageToCache(photo4.getPhotoID(), "post4.jpg");

                    Photo photo5 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo5.setImagePath(photo5.getPhotoID() + ".jpeg");
                    photo5.setUserID(user2.getUserID());
                    photo5.setPromptID(promptRound.getPromptID());
                    photo5.setCaption("Alone together <3");
                    photo5.setLikeCount(2);
                    saveImageToCache(photo5.getPhotoID(), "post5.jpg");

                    Photo photo6 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo6.setImagePath(photo6.getPhotoID() + ".jpeg");
                    photo6.setUserID(user3.getUserID());
                    photo6.setPromptID(promptShiny.getPromptID());
                    photo6.setCaption("sparkles are shiny, right?");
                    photo6.setLikeCount(2);
                    saveImageToCache(photo6.getPhotoID(), "post6.jpg");

                    Photo photo7 = r.createObject(Photo.class, UUID.randomUUID().toString());
                    photo7.setImagePath(photo7.getPhotoID() + ".jpeg");
                    photo7.setUserID(user3.getUserID());
                    photo7.setPromptID(promptOld.getPromptID());
                    photo7.setCaption("What's this, Mr. Yang?");
                    photo7.setLikeCount(3);
                    saveImageToCache(photo7.getPhotoID(), "post7.jpg");
                });

                getSharedPreferences(PREF_NAME, MODE_PRIVATE).edit()
                        .putBoolean(PREF_PREPOPULATED_DATA, true)
                        .apply();

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (realm != null) {
                    realm.close();
                }
            }
        }
    }
    private void saveImageToCache(String id, String assetFileName) {
        InputStream inputStream = null;
        FileOutputStream outputStream = null;
        try {
            inputStream = getAssets().open(assetFileName);
            File cacheDir = getExternalCacheDir();
            File imageFile = new File(cacheDir, id + ".jpeg");
            outputStream = new FileOutputStream(imageFile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                outputStream.write(buffer, 0, length);
            }
            outputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
                if (outputStream != null) outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

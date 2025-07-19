package com.aragoza_mejilla_que.final_project;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;

public class ArchivedPhotos extends AppCompatActivity {

    private static final String TAG = "ArchivedPhotos";
    private Realm realm;
    private RecyclerView photoGridRecyclerView;
    private PhotoGridAdapter photoGridAdapter;
    private TextView currentPromptTextView;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archived_photos);

        Realm.init(this);
        realm = Realm.getDefaultInstance();

        currentPromptTextView = findViewById(R.id.currentPrompt);
        photoGridRecyclerView = findViewById(R.id.photoGrid);
        backButton = findViewById(R.id.cancel);

        backButton.setOnClickListener(v -> finish());

        String promptID = getIntent().getStringExtra("promptID");
        if (promptID == null) {
            Log.e(TAG, "No promptID received from Intent.");
            finish();
            return;
        }

        Prompt prompt = realm.where(Prompt.class).equalTo("promptID", promptID).findFirst();

        if (prompt != null) {
            SimpleDateFormat monthDayFormat = new SimpleDateFormat("MMM d", Locale.getDefault());
            String formattedDate = monthDayFormat.format(prompt.getDate());
            currentPromptTextView.setText(formattedDate + ", " + prompt.getText() + "!");

            RealmResults<Photo> photosForPrompt = realm.where(Photo.class)
                    .equalTo("promptID", promptID)
                    .findAllAsync();

            photoGridRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
            photoGridAdapter = new PhotoGridAdapter(this, photosForPrompt); // Instantiate PhotoGridAdapter
            photoGridRecyclerView.setAdapter(photoGridAdapter);

        } else {
            Log.e(TAG, "Prompt with ID " + promptID + " not found in Realm.");
            currentPromptTextView.setText("Prompt Not Found");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
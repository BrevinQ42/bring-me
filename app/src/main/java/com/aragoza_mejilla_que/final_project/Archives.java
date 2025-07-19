// ArchivesScreen.java
package com.aragoza_mejilla_que.final_project;

import android.os.Bundle;
import android.widget.ImageView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class Archives extends AppCompatActivity {

    private Realm realm;
    private RecyclerView postsList;
    private ImageView backButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_archives);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        realm = Realm.getDefaultInstance();

        postsList = findViewById(R.id.postsList);
        backButton = findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> finish());

        setupRecyclerView();
    }

    private void setupRecyclerView() {
        RealmResults<Prompt> archivedPrompts = realm.where(Prompt.class)
                .sort("date", Sort.DESCENDING)
                .findAll();

        PromptAdapter adapter = new PromptAdapter(this, archivedPrompts);
        postsList.setLayoutManager(new LinearLayoutManager(this));
        postsList.setAdapter(adapter);

        archivedPrompts.addChangeListener(results -> adapter.notifyDataSetChanged());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
package com.aragoza_mejilla_que.final_project;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    SharedPreferences prefs;
    Realm realm;
    SharedPreferences.Editor edit;
    RecyclerView usersList;
    Button addButton;
    Button clearButton;

    void init()
    {
        // initialize realm
        realm = Realm.getDefaultInstance();

        // initialize RecyclerView
        usersList = findViewById(R.id.usersList);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        usersList.setLayoutManager(mLayoutManager);

        // set up adapter
        RealmResults<User> list = realm.where(User.class).findAll();
        UserAdapter adapter = new UserAdapter(this, list, true);
        usersList.setAdapter(adapter);

        // initialize prefs and editor
        prefs = getSharedPreferences("userToBeEdited", MODE_PRIVATE);
        edit = prefs.edit();

        // initialize buttons
        addButton = findViewById(R.id.addButton);
        clearButton = findViewById(R.id.clearButton);

        addButton.setOnClickListener(v -> addUser());
        clearButton.setOnClickListener(v -> clearUsers());
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
    }

    void addUser()
    {
        // go to register page
        Intent registerPageIntent = new Intent(this, RegisterScreen.class);
        startActivity(registerPageIntent);
    }

    void clearUsers()
    {
        // Source: https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/

        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set message
        builder.setMessage("Are you sure you want to delete all users?");

        // Set title
        builder.setTitle("CAUTION");

        // Set Cancelable false for when the user clicks
        // on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Positive Button: Delete User
        builder.setPositiveButton("DELETE ALL USERS", (DialogInterface.OnClickListener) (dialog, which) -> {

            // delete user
            realm.beginTransaction();
            realm.delete(User.class);
            realm.commitTransaction();
        });

        // Negative Button: Cancel Deletion
        builder.setNegativeButton("CANCEL", (DialogInterface.OnClickListener) (dialog, which) -> {

            // cancel dialog box
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }

    public void edit(User u)
    {
        // save uuid of user to be edited
        edit.putString("uuid", u.getUserID());
        edit.apply();

        // go to edit page
        Intent editPageIntent = new Intent(this, EditScreen.class);
        startActivity(editPageIntent);
    }

    public void delete(User u)
    {
        // Source: https://www.geeksforgeeks.org/how-to-create-an-alert-dialog-box-in-android/

        // Create the object of AlertDialog Builder class
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set message
        builder.setMessage("Are you sure you want to delete user " + u.getName() + "?");

        // Set title
        builder.setTitle("CAUTION");

        // Set Cancelable false for when the user clicks
        // on the outside the Dialog Box then it will remain show
        builder.setCancelable(false);

        // Positive Button: Delete User
        builder.setPositiveButton("DELETE USER", (DialogInterface.OnClickListener) (dialog, which) -> {

            // delete user
            realm.beginTransaction();
            u.deleteFromRealm();
            realm.commitTransaction();
        });

        // Negative Button: Cancel Deletion
        builder.setNegativeButton("CANCEL", (DialogInterface.OnClickListener) (dialog, which) -> {

            // cancel dialog box
            dialog.cancel();
        });

        // Create the Alert dialog
        AlertDialog alertDialog = builder.create();

        // Show the Alert Dialog box
        alertDialog.show();
    }
}
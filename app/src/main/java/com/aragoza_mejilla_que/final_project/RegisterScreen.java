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

        initRealm();
        initViews();
    }

    public void onDestroy()
    {
        super.onDestroy();

        if (!realm.isClosed()) {
            realm.close();
        }
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

        saveNewUserButton.setOnClickListener(v -> saveUser());
        cancelRegisButton.setOnClickListener(v -> cancelRegistration());
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
        finish();
    }
}
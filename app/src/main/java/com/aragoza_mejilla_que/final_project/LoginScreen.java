package com.aragoza_mejilla_que.final_project;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Date;

import io.realm.Realm;

public class LoginScreen extends AppCompatActivity {
    Realm realm;
    EditText usernameCred;
    EditText passwordCred;
    CheckBox rememberMe;
    Button loginButton;
    Button registerButton;

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_screen);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRealm();
        initViews();

        Prompt p = new Prompt();
        p.setText("ROUND! ⚪");
        p.setDate(new Date(2024, 5, 9));
        p.setActive(true);

        try {
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(p);
            realm.commitTransaction();
        }
        catch (Exception e){}
    }

    void initRealm()
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

    void initViews()
    {
        usernameCred = findViewById(R.id.usernameCred);
        passwordCred = findViewById(R.id.passwordCred);
        rememberMe = findViewById(R.id.rememberMe);
        loginButton = findViewById(R.id.loginButton);
        registerButton = findViewById(R.id.registerButton);

        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);
        edit = prefs.edit();

        // check if state of rememberMe is remembered
        // if it is, retrieve its state
        // if not, default to false (not toggled)
        boolean isRememberMeChecked = prefs.getBoolean("isRememberMeChecked", false);

        // if it is toggled
        if (isRememberMeChecked)
        {
            String userID = prefs.getString("userID", "");

            if (!userID.isEmpty())
            {
                User rememberedUser = realm.where(User.class)
                        .equalTo("userID", userID)
                        .findFirst();

                if (rememberedUser != null)
                {
                    // pre-fill the credentials saved
                    usernameCred.setText(rememberedUser.getName());
                    passwordCred.setText(rememberedUser.getPassword());

                    // tick the checkbox of rememberMe
                    rememberMe.setChecked(true);

                    // clear shared preferences since user with saved userID is gone
                    edit.clear();
                    edit.apply();
                }
            }
        }
        // else, clear shared preferences in case they exist
        else
        {
            edit.clear();
            edit.apply();
        }

        loginButton.setOnClickListener(v -> login());
        registerButton.setOnClickListener(v -> register());
    }

    void login()
    {
        String enteredUsername = usernameCred.getText().toString();

        User result = realm.where(User.class)
                        .equalTo("name", enteredUsername)
                        .findFirst();

        // check if no user has that username
        if (result == null)
        {
            // send corresponding message through toast
            Toast t = Toast.makeText(this, "No User Found", Toast.LENGTH_LONG);
            t.show();
        }
        // if a user has that username
        else
        {
            String enteredPassword = passwordCred.getText().toString();

            // if the password does not match
            if (!enteredPassword.equals(result.getPassword()))
            {
                // send error message through toast
                Toast t = Toast.makeText(this, "Invalid credentials", Toast.LENGTH_LONG);
                t.show();
            }
            // if they match
            else
            {
                // save userID
                edit.putString("userID", result.getUserID());
                edit.apply();

                // save state of rememberMe checkbox
                edit.putBoolean("isRememberMeChecked", rememberMe.isChecked());
                edit.apply();

                Date today = new Date(System.currentTimeMillis());

                setLastLoginToPast(result);

                // if the user has already logged in today
                if (result.getLastLoginDate().getYear() == today.getYear() &&
                    result.getLastLoginDate().getMonth() == today.getMonth() &&
                    result.getLastLoginDate().getDate() == today.getDate())
                {
                    try
                    {
                        realm.beginTransaction();

                        // set last login date to current
                        result.setLastLoginDate(today);

                        // update user in realm
                        realm.copyToRealmOrUpdate(result);
                        realm.commitTransaction();

                        // go to landing screen

                    }
                    catch (Exception e)
                    {
                        Toast t = Toast.makeText(this, "Error saving", Toast.LENGTH_LONG);
                        t.show();
                    }
                }
                else // this is the first time user is logging in
                {
                    try
                    {
                        realm.beginTransaction();

                        // set last login date to current
                        result.setLastLoginDate(today);

                        // update user in realm
                        realm.copyToRealmOrUpdate(result);
                        realm.commitTransaction();

                        // go to prompt screen
                        Intent promptScreenIntent = new Intent(this, PromptScreen.class);
                        startActivity(promptScreenIntent);
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

    void register()
    {
        // go to register page
        Intent registerPageIntent = new Intent(this, RegisterScreen.class);
        startActivity(registerPageIntent);
    }

    // temp for testing
    void setLastLoginToPast(User result)
    {
        try
        {
            realm.beginTransaction();

            // set last login date to current
            result.setLastLoginDate(new Date(2024, 5,9));

            // update user in realm
            realm.copyToRealmOrUpdate(result);
            realm.commitTransaction();

            // go to landing screen

        }
        catch (Exception e)
        {
            Toast t = Toast.makeText(this, "Error saving", Toast.LENGTH_LONG);
            t.show();
        }
    }
}
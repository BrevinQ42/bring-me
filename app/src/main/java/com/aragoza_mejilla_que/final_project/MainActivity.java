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

import io.realm.Realm;

public class MainActivity extends AppCompatActivity {
    Realm realm;
    EditText usernameCred;
    EditText passwordCred;
    CheckBox rememberMe;
    Button loginButton;
    // Button registerButton;
    Button adminButton;
    Button clearButton;

    SharedPreferences prefs;
    SharedPreferences.Editor edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initRealm();
        initViews();
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
        //registerButton = findViewById(R.id.registerButton);
        adminButton = findViewById(R.id.adminButton);
        clearButton = findViewById(R.id.clearPrefsButton);

        prefs = getSharedPreferences("savedUser", MODE_PRIVATE);
        edit = prefs.edit();

        // check if state of rememberMe is remembered
        // if it is, retrieve its state
        // if not, default to false (not toggled)
        boolean isRememberMeChecked = prefs.getBoolean("isRememberMeChecked", false);

        // if it is toggled
        if (isRememberMeChecked)
        {
            //LAB 1
            //String savedUsername = prefs.getString("username", "null");
            //String savedPassword = prefs.getString("password", "");

            //LAB 2
            String uuid = prefs.getString("uuid", "");

            if (!uuid.isEmpty())
            {
                User rememberedUser = realm.where(User.class)
                        .equalTo("uuid", uuid)
                        .findFirst();

                if (rememberedUser != null)
                {
                    // pre-fill the credentials saved
                    usernameCred.setText(rememberedUser.getName());
                    passwordCred.setText(rememberedUser.getPassword());

                    // tick the checkbox of rememberMe
                    rememberMe.setChecked(true);

                    // clear shared preferences since user with saved uuid is gone
                    edit.clear();
                    edit.apply();
                }
            }
        }
        // else, leave them be

        loginButton.setOnClickListener(v -> login());
        //registerButton.setOnClickListener(v -> register());
        adminButton.setOnClickListener(v -> goToAdminScreen());
        clearButton.setOnClickListener(v -> clearPrefs());
    }

    void login()
    {
        //LAB1
        //String savedUsername = prefs.getString("username", "null");

        // if there is a saved username,
        //if (!savedUsername.equals("null"))
        //{
            // get the saved password
            //String savedPassword = prefs.getString("password", "");

            // check if the saved credentials match those entered by the user
            //if (savedUsername.equals(usernameCred.getText().toString()) &&
                //savedPassword.equals(passwordCred.getText().toString()))
            //{
                // save state of rememberMe checkbox
                //edit.putBoolean("isRememberMeChecked", rememberMe.isChecked());
                //edit.apply();

                // if it does, go to the welcome page
                //Intent welcomePageIntent = new Intent(this, WelcomeScreen.class);
                //startActivity(welcomePageIntent);
            //}
            //else
            //{
                // else, send error message through toast
                //Toast t = Toast.makeText(this, "Invalid Credentials", Toast.LENGTH_LONG);
                //t.show();
            //}
        //}
        //else // if no saved username,
        //{
            // send corresponding message through toast
            //Toast t = Toast.makeText(this, "Nothing saved", Toast.LENGTH_LONG);
            //t.show();
        //}

        //LAB 2
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
                // save uuid
                edit.putString("uuid", result.getUuid());
                edit.apply();

                // save state of rememberMe checkbox
                edit.putBoolean("isRememberMeChecked", rememberMe.isChecked());
                edit.apply();

                // proceed to welcome page
                Intent welcomePageIntent = new Intent(this, WelcomeScreen.class);
                startActivity(welcomePageIntent);
            }
        }
    }

    void register()
    {
        // go to register page
        Intent registerPageIntent = new Intent(this, RegisterScreen.class);
        startActivity(registerPageIntent);
    }

    void clearPrefs()
    {
        // clear shared preferences
        edit.clear();
        edit.apply();

        // send corresponding message through toast
        Toast t = Toast.makeText(this, "Preferences cleared", Toast.LENGTH_LONG);
        t.show();
    }

    void goToAdminScreen()
    {
        // go to admin screen
        Intent adminPageIntent = new Intent(this, AdminScreen.class);
        startActivity(adminPageIntent);
    }
}
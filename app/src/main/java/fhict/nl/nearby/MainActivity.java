package fhict.nl.nearby;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    final int LAUNCH_LOGIN_ACTIVITY = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        SharedPreferences pref = getApplication().getSharedPreferences("myinfo", MODE_PRIVATE);
        String password = pref.getString("password", "");

        if(mAuth.getCurrentUser() == null || password.equals("")){
            //not signed in
            mAuth.signOut();
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivityForResult(intent, LAUNCH_LOGIN_ACTIVITY);
        }else{
            //signed in
            LaunchActivities();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LAUNCH_LOGIN_ACTIVITY) {
            if(resultCode == Activity.RESULT_OK){
                //user is logged in
                LaunchActivities();
            }else{
                //dont think this is ever reached
            }
        }
    }

    private void LaunchActivities(){
        //demo showing login success
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        startActivity(intent);


        //you can get user details.
        //mAuth.getCurrentUser().getEmail() -> email(changeable)
        //mAuth.getCurrentUser().getUid() -> unique ID(static), maybe use this to save data in firebase

        //TODO actually launching  the other main activities from here
    }
}

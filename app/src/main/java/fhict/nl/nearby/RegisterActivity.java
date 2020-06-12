package fhict.nl.nearby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity {

    EditText etEmail;
    EditText etNickname;
    EditText etPassword;
    EditText etRepeatPassword;
    Button btnRegisterEmail;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
        setContentView(R.layout.activity_register);

        //Firebase
        mAuth = FirebaseAuth.getInstance();

        //UI
        etEmail = findViewById(R.id.editTextRegisterEmail);
        etNickname = findViewById(R.id.editTextRegisterNickname);
        etPassword = findViewById(R.id.editTextRegisterPassword);
        etRepeatPassword = findViewById(R.id.editTextRegisterRepeatPassword);
        btnRegisterEmail = findViewById(R.id.btnRegisterEmail);
        btnRegisterEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailRegister();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //go back to login activity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    public void EmailRegister(){
        etEmail.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        if(!ValidForm()){
            return;
        }

        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();


        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    //signup and sign in success
                    Log.i("EmailRegister", "Success");
                    SharedPreferences pref = getApplicationContext().getSharedPreferences("myinfo", MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("password", etPassword.getText().toString().trim());
                    editor.apply();
                    setResult(Activity.RESULT_OK);
                    //user is created and is logged in
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    //create new user object in the database
                    DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users");
                    //the only image that uses extension
                    MyUser newUser = new MyUser(etNickname.getText().toString(), user.getEmail(), 0, 0, new ArrayList<String>(), "default_user.png");
                    userDatabase.child(user.getUid()).setValue(newUser);
                    finish();
                }else{
                    Exception e = task.getException();
                    if(e instanceof FirebaseAuthUserCollisionException){
                        Log.e("EmailRegister", e.getMessage());
                        etEmail.setError("Email already in use by an account");
                    } else {
                        Log.e("EmailRegister", e.getMessage());
                        Toast.makeText(RegisterActivity.this, "Unknown error occurred. Try again later", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private boolean ValidForm(){
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String repeatPassword = etRepeatPassword.getText().toString().trim();

        if(email.isEmpty() || password.isEmpty() || repeatPassword.isEmpty()){
            if(email.isEmpty()){
                etEmail.setError("Required");
            }

            if(password.isEmpty()){
                etPassword.setError("Required");
            }

            if(repeatPassword.isEmpty()){
                etRepeatPassword.setError("Required");
            }
            return false;
        }

        if(!email.matches(emailPattern)){
            etEmail.setError("Email does not contain a valid format.");
            return false;
        }

        if(!password.equals(repeatPassword)){
            etRepeatPassword.setError("Please enter matching password");
            return false;
        }

        if(password.length() < 8){
            etPassword.setError("Password should be 8 characters or more.");
            return false;
        }

        return true;
    }
}

class MyUser{
    public String nickname;
    public String email;
    public double lat;
    public double lng;
    public boolean logged;
    public List<String> friends;
    public String image;

    public MyUser(){} //needed for profile
    public MyUser(String nickname, String email, double lat, double lng, List<String> friends, String image) {
        this.nickname = nickname;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.logged = true;
        this.friends = friends;
        this.image = image;
    }
}

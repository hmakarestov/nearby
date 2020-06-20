package fhict.nl.nearby;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class RegisterActivity extends AppCompatActivity {

    final int GALLERY_REQUEST_CODE = 100;
    Uri selectedImage = null;
    EditText etEmail;
    EditText etNickname;
    EditText etPassword;
    EditText etRepeatPassword;

    ImageView ivProfilePic;
    Button btnChangeProfilePic;
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
        ivProfilePic = findViewById(R.id.ivRegisterPic);
        btnChangeProfilePic = findViewById(R.id.btnRegisterImage);
        btnRegisterEmail = findViewById(R.id.btnRegisterEmail);
        btnRegisterEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EmailRegister();
            }
        });
        btnChangeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickFromGallery();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //go back to login activity
        setResult(Activity.RESULT_CANCELED);
        finish();
    }

    private void pickFromGallery(){
        //Create an Intent with action as ACTION_PICK
        Intent intent=new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES,mimeTypes);
        // Launching the Intent
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    selectedImage = data.getData();
                    String[] filePathColumn = {MediaStore.Images.Media.DATA};
                    // Get the cursor
                    Cursor cursor = getApplicationContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String
                    ivProfilePic.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    break;
            }
    }

    public void EmailRegister(){
        etEmail.setError(null);
        etPassword.setError(null);
        etRepeatPassword.setError(null);

        if(!ValidForm()){
            return;
        }

        final String email = etEmail.getText().toString().trim();
        final String password = etPassword.getText().toString().trim();

        final String imgName = UUID.randomUUID().toString() + Calendar.getInstance().getTimeInMillis();
        if(selectedImage != null){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(imgName);
            ref.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        ContinueProfileRegister(email, password, true, imgName);
                    }else{
                        Toast.makeText(getApplication(), "Upload Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            ContinueProfileRegister(email, password, false, imgName);
        }
    }

    private void ContinueProfileRegister(String emailOf, String password, final Boolean newImage, final String imageName){
        final String email = emailOf;
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

                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference("users");
                    String name = etNickname.getText().toString();
                    if(name == null || name.isEmpty()){
                        name = "";
                    }

                    MyUser newUser;
                    if(newImage){
                        newUser = new MyUser(name, email, 0, 0, imageName);
                    }else{
                        newUser = new MyUser(name, email, 0, 0, "default_user.png");
                    }
                    userDatabase.child(user.getUid()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                setResult(Activity.RESULT_OK);
                                finish();
                            }else{
                                Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
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

class MyUser {
    public String nickname;
    public String email;
    public double lat;
    public double lng;
    public boolean logged;
    public boolean showLocation;
    public HashMap<String, String> friends;
    public String image;

    public MyUser(){} //needed for profile
    public MyUser(String nickname, String email, double lat, double lng, String image) {
        this.nickname = nickname;
        this.email = email;
        this.lat = lat;
        this.lng = lng;
        this.logged = true;
        this.showLocation = true;
        this.friends = new HashMap<>();
        this.image = image;
    }
}

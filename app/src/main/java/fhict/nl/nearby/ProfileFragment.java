package fhict.nl.nearby;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.UUID;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    final int GALLERY_REQUEST_CODE = 100;
    final int STORAGE_REQUEST_CODE = 101;
    View view;
    EditText etName;
    EditText etEmail;
    EditText etCurrentPassword;
    EditText etNewPassword;
    TextView tvErrorProfile;
    TextView tvErrorPassword;
    ImageView ivProfile;
    Button btnProfilePic;
    Button btnUpdateProfile;
    Button btnUpdatePassword;

    Uri selectedImage = null;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_profile, container, false);

        etName = view.findViewById(R.id.etProfileName);
        etEmail = view.findViewById(R.id.etProfileEmail);
        etCurrentPassword = view.findViewById(R.id.etProfileCurrentPassword);
        etNewPassword = view.findViewById(R.id.etProfileNewPassword);
        tvErrorProfile = view.findViewById(R.id.tvErrorProfile);
        tvErrorPassword = view.findViewById(R.id.tvErrorPassword);
        btnUpdateProfile = view.findViewById(R.id.btnProfileSave);
        btnUpdatePassword = view.findViewById(R.id.btnProfilePassword);
        ivProfile = view.findViewById(R.id.ivProfile);
        btnProfilePic = view.findViewById(R.id.btnChangePic);

        btnUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateProfile();
            }
        });

        btnUpdatePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdatePassword();
            }
        });

        btnProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if ((ContextCompat.checkSelfPermission(getContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
                        && (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
                    pickFromGallery();
                }else{
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, STORAGE_REQUEST_CODE);
                }
            }
        });

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ClearErrors();
                    if (getActivity() == null) {
                        return;
                    }
                    MyUser user = dataSnapshot.getValue(MyUser.class);
                    etEmail.setText(user.email);
                    etName.setText(user.nickname);

                    //Image Loading
                    StorageReference imageReference = FirebaseStorage.getInstance().getReference().child(user.image);
                    Glide.with(getContext()).load(imageReference).into(ivProfile);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        return view;
    }

    private void UpdateProfile(){
        ClearErrors();
        //upload image first
        final String imgName = UUID.randomUUID().toString() + Calendar.getInstance().getTimeInMillis();
        if(selectedImage != null){
            StorageReference ref = FirebaseStorage.getInstance().getReference().child(imgName);
            ref.putFile(selectedImage).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        continueProfileUpdate(true, imgName);
                    }else{
                        Toast.makeText(getActivity(), "Upload Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }else{
            continueProfileUpdate(false, imgName);
        }
    }

    private void continueProfileUpdate(Boolean updateImage, String imageName){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        final DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
        final String name = etName.getText().toString().trim();
        final String new_email = etEmail.getText().toString().trim().toLowerCase();
        if(!checkProfile(name, new_email)){
            return;
        }

        if(updateImage){
            userDatabase.child("image").setValue(imageName);
        }
        if(new_email.equals(user.getEmail())){
            //update nickname only
            userDatabase.child("nickname").setValue(name);
            Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_SHORT).show();
        }else{
            SharedPreferences pref = getActivity().getSharedPreferences("myinfo", MODE_PRIVATE);
            String password = pref.getString("password", "");
            if(password.equals("")){
                tvErrorProfile.setText("Error: Please sign out and login. Should fix this error.");
                tvErrorProfile.setVisibility(View.VISIBLE);
                return;
            }
            AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);

            user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        user.updateEmail(new_email).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    userDatabase.child("email").setValue(new_email);
                                    userDatabase.child("nickname").setValue(name);
                                    Toast.makeText(getActivity(), "Profile Updated", Toast.LENGTH_SHORT).show();
                                }else{
                                    Exception e = task.getException();
                                    if(e instanceof FirebaseAuthUserCollisionException){
                                        tvErrorProfile.setText("Email already in use.");
                                        tvErrorProfile.setVisibility(View.VISIBLE);
                                    }else{
                                        if(e instanceof FirebaseAuthInvalidCredentialsException){
                                            tvErrorProfile.setText(e.getMessage());
                                            tvErrorProfile.setVisibility(View.VISIBLE);
                                        }else{
                                            Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            }
                        });
                    }else{
                        Exception e = task.getException();
                        Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void UpdatePassword(){
        ClearErrors();
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            return;
        }

        final String currentPassword = etCurrentPassword.getText().toString().trim();
        final String newPassword = etNewPassword.getText().toString().trim();

        if(!checkPassword(currentPassword, newPassword)){
            return;
        }

        SharedPreferences pref = getActivity().getSharedPreferences("myinfo", MODE_PRIVATE);
        final String password = pref.getString("password", "");
        if(password.equals("")){
            tvErrorPassword.setText("Error: Please sign out and login. Should fix this error.");
            tvErrorPassword.setVisibility(View.VISIBLE);
            return;
        }

        if(!currentPassword.equals(password)){
            tvErrorPassword.setText("Wrong current password was entered.");
            tvErrorPassword.setVisibility(View.VISIBLE);
            return;
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                SharedPreferences pref = getActivity().getSharedPreferences("myinfo", MODE_PRIVATE);
                                SharedPreferences.Editor editor = pref.edit();
                                editor.remove("password");
                                editor.putString("password", password);
                                editor.apply();
                                Toast.makeText(getActivity(), "Password Updated", Toast.LENGTH_SHORT).show();
                            }else{
                                Exception e = task.getException();
                                Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }else{
                    Exception e = task.getException();
                    Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkProfile(String new_name, String new_email){
        if(new_name.isEmpty()){
            tvErrorProfile.setText("Name is required");
            tvErrorProfile.setVisibility(View.VISIBLE);
            return false;
        }

        if(new_email.isEmpty()){
            tvErrorProfile.setText("Email is required");
            tvErrorProfile.setVisibility(View.VISIBLE);
            return false;
        }

        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        if(!new_email.matches(emailPattern)){
            tvErrorProfile.setText("Email needs a valid format");
            tvErrorProfile.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private boolean checkPassword(String currentPassword, String newPassword){
        if(currentPassword.length() < 8){
            tvErrorPassword.setText("Wrong Current Password");
            tvErrorPassword.setVisibility(View.VISIBLE);
            return false;
        }

        if(newPassword.length() < 8){
            tvErrorPassword.setText("New Password should be more than 8 characters.");
            tvErrorPassword.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private void ClearErrors(){
        tvErrorProfile.setText("");
        tvErrorProfile.setVisibility(View.INVISIBLE);

        tvErrorPassword.setText("");
        tvErrorPassword.setVisibility(View.INVISIBLE);
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

    public void onActivityResult(int requestCode,int resultCode,Intent data){
        // Result code is RESULT_OK only if the user selects an Image
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode){
                case GALLERY_REQUEST_CODE:
                    //data.getData return the content URI for the selected Image
                    selectedImage = data.getData();
                    String[] filePathColumn = { MediaStore.Images.Media.DATA };
                    // Get the cursor
                    Cursor cursor = getContext().getContentResolver().query(selectedImage, filePathColumn, null, null, null);
                    // Move to first row
                    cursor.moveToFirst();
                    //Get the column index of MediaStore.Images.Media.DATA
                    int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                    //Gets the String value in the column
                    String imgDecodableString = cursor.getString(columnIndex);
                    cursor.close();
                    // Set the Image in ImageView after decoding the String
                    ivProfile.setImageBitmap(BitmapFactory.decodeFile(imgDecodableString));
                    break;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case STORAGE_REQUEST_CODE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    pickFromGallery();
                }
                return;
            }
        }
    }


}

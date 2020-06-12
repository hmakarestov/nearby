package fhict.nl.nearby;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
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

import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {

    View view;
    EditText etName;
    EditText etEmail;
    EditText etCurrentPassword;
    EditText etNewPassword;
    TextView tvErrorProfile;
    TextView tvErrorPassword;
    Button btnUpdateProfile;
    Button btnUpdatePassword;
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

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
            userDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    ClearErrors();
                    MyUser user = dataSnapshot.getValue(MyUser.class);
                    etEmail.setText(user.email);
                    etName.setText(user.nickname);
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
                                    Log.v("ProfileUpdate", task.getException().getMessage() + e);
                                }
                            }
                        });
                    }else{
                        Exception e = task.getException();
                        Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.v("Profile-AUTH", task.getException().getMessage());
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
                                Log.v("PasswordUpdate", task.getException().getMessage());
                            }
                        }
                    });
                }else{
                    Exception e = task.getException();
                    Toast.makeText(getActivity(), "Unknown Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.v("Password-AUTH", task.getException().getMessage());
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
}

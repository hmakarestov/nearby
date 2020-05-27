package fhict.nl.nearby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FriendsFragment extends Fragment {
    View view;
    EditText editTextFriendIdName;
    Button buttonAddFriend;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_friends, container, false);
        editTextFriendIdName = view.findViewById(R.id.editText_add_friend);
        buttonAddFriend = view.findViewById(R.id.button_add_friend);
        return view;
    }

    public void addFriend(String id){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference allUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");

    }
}

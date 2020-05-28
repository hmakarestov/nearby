package fhict.nl.nearby;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendsFragment extends Fragment {
    View view;
    EditText editTextFriendIdName;
    Button buttonAddFriend;

    //used to store all users id and user names
    Map<String, Object> map = new HashMap<>();

    //list view to store friend list
    private ListView list;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> arrayList;
    //used to get the current user id
    FirebaseUser user;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        view =  inflater.inflate(R.layout.fragment_friends, container, false);

        //list view stuff
        list = (ListView)view.findViewById(R.id.listviewFriends);
        arrayList = new ArrayList<>();
        adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayList);
        list.setAdapter(adapter);

        editTextFriendIdName = view.findViewById(R.id.editText_add_friend);
        buttonAddFriend = view.findViewById(R.id.button_add_friend);
        buttonAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend(editTextFriendIdName.getText().toString());
            }
        });

        user = FirebaseAuth.getInstance().getCurrentUser();
        //get all users id
        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users");

        user_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //put all the users in the map, used to see if the added friend is a registered user id
                for(DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()){
                    map.put(dataSnapshotUser.getKey(), "userName");
                    //if the current id is the logged user, it will go through all his friend list and add them to the listview
                    if(dataSnapshotUser.getKey().equals(user.getUid())){
                        for(DataSnapshot dataSnapshotFriends : dataSnapshotUser.child("friends").getChildren()){
                            adapter.add(dataSnapshotFriends.getKey());
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });
        return view;
    }

    public void addFriend(String id){
        //get the logged user id
        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());

        //the friend will be added only if the id is not from the current logged user or if the friend is a registered user id
        if(map.containsKey(id) && !(id.equals(user.getUid()))){
            Map<String, Object> mapNew = new HashMap<>();
            mapNew.put(id, "userName");
            user_db.child("friends").updateChildren(mapNew);
        }
    }
}

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


    List<String> requestList;
    ListView listView;

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
                adapter.clear();
                //put all the users in the map, used to see if the added friend is a registered user id
                for(DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()){
                    map.put(dataSnapshotUser.getKey(), dataSnapshotUser.child("nickname").getValue().toString());
                    map.put(dataSnapshotUser.child("nickname").getValue().toString(), dataSnapshotUser.getKey());
                    //if the current id is the logged user, it will go through all his friend list and add them to the listview
                    if(dataSnapshotUser.getKey().equals(user.getUid())){
                        for(DataSnapshot dataSnapshotFriends : dataSnapshotUser.child("friends").getChildren()){
                            for(DataSnapshot dataSnapshotFriendsNickname : dataSnapshot.getChildren()){
                                if(dataSnapshotFriends.getKey().equals(dataSnapshotFriendsNickname.getKey())){
                                    adapter.add(dataSnapshotFriendsNickname.child("nickname").getValue().toString());
                                }
                            }
                        }
                    }
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });


        //SHOW FRIEND REQUESTS
        listView = view.findViewById(R.id.listViewRequests);
        DatabaseReference friends_db = FirebaseDatabase.getInstance().getReference().child("friendRequests");
        friends_db.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                requestList = new ArrayList<>();
                for(DataSnapshot dataSnapshotFriends : dataSnapshot.getChildren()){
                    if(dataSnapshotFriends.getKey().equals(user.getUid())){
                        for(DataSnapshot dataSnapshot1 : dataSnapshotFriends.getChildren()){
                            requestList.add(dataSnapshot1.getKey());
                        }
                    }
                }
                //creating the adapter
                FriendListAdapter adapter = new FriendListAdapter(getContext(), R.layout.friend_request_list, requestList);
                //attaching adapter to the listview
                listView.setAdapter(adapter);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) { }
        });



        return view;
    }

    public void addFriend(String nickname){
        //get the logged user id
        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users");

        //the friend will be added only if the id is not from the current logged user or if the friend is a registered user id
        if(map.containsValue(nickname)){
            DatabaseReference request_db = FirebaseDatabase.getInstance().getReference("friendRequests");
            Map<String, Object> requests = new HashMap<>();
            requests.put(user.getUid(), nickname);
            String id_user = (String)map.get(nickname);
            request_db.child(id_user).updateChildren(requests);
        }
    }
}

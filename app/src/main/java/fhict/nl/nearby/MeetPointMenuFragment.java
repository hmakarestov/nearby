package fhict.nl.nearby;

import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static androidx.constraintlayout.widget.Constraints.TAG;

import java.util.ArrayList;


public class MeetPointMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_meet_point_menu, container, false);


        ListView list = (ListView) view.findViewById(R.id.friends_list_share);
        ArrayList<String> arrayList = new ArrayList<>();
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, arrayList);


        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users");


        user_db.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()) {
                    //if the current id is the logged user, it will go through all his friend list and add them to the listview
                    if (dataSnapshotUser.getKey().equals(user.getUid())) {
                        for(DataSnapshot dataSnapshotFriends : dataSnapshotUser.child("friends").getChildren()){
                            for(DataSnapshot dataSnapshotFriendsNickname : dataSnapshot.getChildren()){
                                if(dataSnapshotFriends.getKey().equals(dataSnapshotFriendsNickname.getKey())){
                                    adapter.add(dataSnapshotFriendsNickname.child("nickname").getValue().toString());
                                    Log.d(TAG, "Friend found");
                                }
                            }
                        }
                    }
                }
            }

        @Override
        public void onCancelled (@NonNull DatabaseError databaseError){

        }});
        list.setAdapter(adapter);

        return view;
    }

}

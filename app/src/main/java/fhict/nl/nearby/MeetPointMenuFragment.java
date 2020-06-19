package fhict.nl.nearby;

import android.content.Context;
import android.graphics.Color;
import android.nfc.Tag;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import static androidx.constraintlayout.widget.Constraints.TAG;

import java.io.Console;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class MeetPointMenuFragment extends Fragment {

    private FragmentActivity myContext;
    Map<String, Object> map = new HashMap<>();

    @Override
    public void onAttach(Context context) {
        myContext=getActivity();
        super.onAttach(context);
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View view = inflater.inflate(R.layout.fragment_meet_point_menu, container, false);


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

        Button cancelBtn = (Button) view.findViewById(R.id.cancel);
        final Fragment fragment = this;
        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapsFragment.KillFragment();
            }
        });


        final ListView Fview = view.findViewById(R.id.friends_list_share);
        final ArrayList<String> friends_share = new ArrayList<String>();
        Fview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String nickname = parent.getItemAtPosition(position).toString();
                if (!friends_share.contains(nickname))
                {
                    friends_share.add(nickname);
                    Log.d("NewFriend", nickname);
                }

            }
        });


        Button shareBtn = (Button) view.findViewById((R.id.Share));
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("friends");
                Log.d("current user", FirebaseAuth.getInstance().getCurrentUser().getUid());

                user_db.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot dataSnapshotUser : dataSnapshot.getChildren()) {
                            map.put(dataSnapshotUser.getKey(), dataSnapshotUser.child("nickname").getValue().toString());
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) { }
                });
                for (String nickname: friends_share)
                {

                    if(map.containsValue(nickname)){
                        MapsFragment.MapMarker marker = MapsFragment.getCurrentMarker();
                        marker.AddFriend(nickname);
                    }
                }
            }
        });

        Button saveBtn;

        return view;
    }

}

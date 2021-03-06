package fhict.nl.nearby;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FriendListAdapter extends ArrayAdapter<String> {
    //the list values in the List of type hero
    List<String> requestList;
    //activity context
    Context context;
    //the layout resource file for the list items
    int resource;


    public FriendListAdapter(Context context, int resource, List<String> requestList) {
        super(context, resource, requestList);
        this.context = context;
        this.resource = resource;
        this.requestList = requestList;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        View view = layoutInflater.inflate(resource, null, false);
        final TextView textViewName = view.findViewById(R.id.textView_request_friend_name);
        DatabaseReference name = FirebaseDatabase.getInstance().getReference().child("users").child(requestList.get(position)).child("nickname");
        name.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                textViewName.setText(dataSnapshot.getValue().toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        Button buttonAccept = view.findViewById(R.id.button_friend_request_accept);
        buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                //add friend to the current user
                DatabaseReference user_db = FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid());
                Map<String, Object> friendsMap = new HashMap<>();
                friendsMap.put(requestList.get(position), "userName");
                user_db.child("friends").updateChildren(friendsMap);

                //add the friend to the second user
                DatabaseReference another_user_db = FirebaseDatabase.getInstance().getReference().child("users").child(requestList.get(position));
                Map<String, Object> frnd = new HashMap<>();
                frnd.put(user.getUid(), "userName");
                another_user_db.child("friends").updateChildren(frnd);

                //remove friend request
                DatabaseReference request_db = FirebaseDatabase.getInstance().getReference("friendRequests").child(user.getUid());
                request_db.removeValue();
            }
        });
        Button decline = view.findViewById(R.id.button_friend_request_deny);
        decline.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //remove friend request
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                DatabaseReference request_db = FirebaseDatabase.getInstance().getReference("friendRequests").child(user.getUid());
                request_db.removeValue();
            }
        });
        return view;
    }
}

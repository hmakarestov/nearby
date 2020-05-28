package fhict.nl.nearby;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private static final int RC_SIGN_IN = 123;

    View view;
    SupportMapFragment mapFragment;
    GoogleMap gm;
    MarkerOptions markerOptions;
    LatLng locationCoordonates;

    TextView textViewName;
    MarkerOptions multiplemarkers = new MarkerOptions();
    static String currentUserId;
    public static Boolean logOff = false;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //inflate the fragment
        view = inflater.inflate(R.layout.fragment_maps, container, false);
        textViewName = view.findViewById(R.id.textView_user_testing);

        //insert map inside the fragment view "map"
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //check permissions for location
        LocationManager locationManager = (LocationManager)getActivity().getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.

        }
        //Right now permission for location has to be given manually, it's not implemented yet. NO PERMISSION = CRASH
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);

        //called to see if any user is logged in
        checkCurrentUser();

        return view;
    }

    //called the first time when the map loads | when you open the app
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;
    }

    //this is called when the location is changed. This will change the markers position
    @Override
    public void onLocationChanged(Location location) {
        //update the location to the database
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userDatabase.child("lat").setValue(location.getLatitude());
        userDatabase.child("lng").setValue(location.getLongitude());
    }

    //NOT USED, part of the LocationListener interface
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) { }
    @Override
    public void onProviderEnabled(String provider) { }
    @Override
    public void onProviderDisabled(String provider) { }

    //method to check if any user is logged in. If not, will call createSignInIntent()
    public void checkCurrentUser() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            //user is logged
            textViewName.setText("LOGGED IN " +user.getEmail());
            currentUserId = user.getUid();

            //update the location of all users who are logged in
            DatabaseReference allUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            allUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    gm.clear(); //clear the markers on the map, new ones will be inserted
                    //get info about all the users individually
                    for(DataSnapshot dataSnapshotUsers : dataSnapshot.getChildren()){
                        if(dataSnapshotUsers.getKey().equals(user.getUid())){
                            for(DataSnapshot dataSnapshotFriends : dataSnapshotUsers.child("friends").getChildren()){
                                for(DataSnapshot dataSnapshotInfoUser : dataSnapshot.getChildren()){
                                    if(dataSnapshotInfoUser.getKey().equals(dataSnapshotFriends.getKey()) || dataSnapshotInfoUser.getKey().equals(user.getUid())){
                                        if((Boolean)dataSnapshotInfoUser.child("logged").getValue()){
                                            double lat = Double.valueOf(dataSnapshotInfoUser.child("lat").getValue().toString());
                                            double lng = Double.valueOf(dataSnapshotInfoUser.child("lng").getValue().toString());
                                            locationCoordonates = new LatLng(lat, lng);
                                            multiplemarkers.position(locationCoordonates);
                                            multiplemarkers.title(dataSnapshotInfoUser.getKey());
                                            gm.addMarker(multiplemarkers);
                                        }
                                        //set the logged value to true/false
                                        if(dataSnapshotInfoUser.getKey().equals(currentUserId)){
                                            DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("logged");
                                            if((Boolean)dataSnapshotInfoUser.child("logged").getValue() && logOff){
                                                user.setValue(false);
                                            }
                                            else if(!logOff){
                                                user.setValue(true);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });
        }
    }
}

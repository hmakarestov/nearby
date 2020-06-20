package fhict.nl.nearby;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.service.autofill.Dataset;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {
    private static final int RC_SIGN_IN = 123;
    private static View statview;

    static MapMarker currentMarker = null;
    static String key = null;

    View view;
    SupportMapFragment mapFragment;
    GoogleMap gm;
    MarkerOptions markerOptions;

    Location userLocation;
    LatLng userLatLng;
    LatLng locationCoordonates;
    Marker meetingPoint = null;
    boolean centered = true;
    TextView textViewName;
    MarkerOptions multiplemarkers = new MarkerOptions();
    static String currentUserId;
    static Fragment newFragment;
    //public static Boolean logOff = false;
    public static FragmentManager fm;

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
            requestPermissions(
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION },
                            10);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            userLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        }


        //called to see if any user is logged in
        checkCurrentUser();
        Button centerBtn = view.findViewById(R.id.recenter);
        centerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (centered == false)
                {
                    centered = true;
                    gm.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
                }
            }
        });


        statview = view;
        return view;
    }

    //called the first time when the map loads | when you open the app
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;
        gm.getUiSettings().setZoomControlsEnabled(true);
        gm.getUiSettings().setMyLocationButtonEnabled(true);
        gm.getUiSettings().isCompassEnabled();

        gm.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {

            }
        });
        gm.setOnMapLongClickListener(this);
        gm.setOnMapClickListener(this);
        gm.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int reason) {
                if (reason ==REASON_GESTURE) {
                    centered=false;
                }
            }
        });
        fm = getFragmentManager();
        gm.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {

                if (marker.equals(meetingPoint))
                {
                    centered = false;
                    gm.moveCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                    newFragment = new MeetPointMenuFragment();
                    FrameLayout markerFrame = view.findViewById(R.id.MarkerFrame);
                    markerFrame.setVisibility(View.VISIBLE);


                    // consider using Java coding conventions (upper first char class names!!!)
                    FragmentTransaction transaction = fm.beginTransaction();

                    // Replace whatever is in the fragment_container view with this fragment,
                    // and add the transaction to the back stack
                    transaction.replace(R.id.MeetPointFragm, newFragment);

                    transaction.addToBackStack(null);

                    // Commit the transaction
                    transaction.commit();
                    return true;
                }
                return  false;
            }
        });
    }


    //this is called when the location is changed. This will change the markers position
    @Override
    public void onLocationChanged(Location location) {
        //update the location to the database
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userDatabase.child("lat").setValue(location.getLatitude());
        userDatabase.child("lng").setValue(location.getLongitude());
        if (centered == true) {
            userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            gm.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        }
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
            if (gm!=null)
            {
                gm.clear(); //clear the markers on the map, new ones will be inserted
            }

            //update the location of all users who are logged in
            DatabaseReference allUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
            allUsersDatabase.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    //get info about all the users individually
                    for(DataSnapshot dataSnapshotUsers : dataSnapshot.getChildren()){
                        if(dataSnapshotUsers.getKey().equals(user.getUid())){
                            double lat = Double.valueOf(dataSnapshotUsers.child("lat").getValue().toString());
                            double lng = Double.valueOf(dataSnapshotUsers.child("lng").getValue().toString());
                            locationCoordonates = new LatLng(lat, lng);
                            multiplemarkers.position(locationCoordonates);
                            multiplemarkers.title(dataSnapshotUsers.child("nickname").getValue().toString());
                            multiplemarkers.draggable(false);
                            gm.addMarker(multiplemarkers);
                            for(DataSnapshot dataSnapshotFriends : dataSnapshotUsers.child("friends").getChildren()){
                                for(DataSnapshot dataSnapshotInfoUser : dataSnapshot.getChildren()){
                                    if(dataSnapshotInfoUser.getKey().equals(dataSnapshotFriends.getKey()) || dataSnapshotInfoUser.getKey().equals(user.getUid())){
                                        if((Boolean)dataSnapshotInfoUser.child("logged").getValue() && (Boolean)dataSnapshotInfoUser.child("showLocation").getValue()){
                                            lat = Double.valueOf(dataSnapshotInfoUser.child("lat").getValue().toString());
                                            lng = Double.valueOf(dataSnapshotInfoUser.child("lng").getValue().toString());
                                            locationCoordonates = new LatLng(lat, lng);
                                            multiplemarkers.position(locationCoordonates);
                                            multiplemarkers.title(dataSnapshotInfoUser.child("nickname").getValue().toString());
                                            gm.addMarker(multiplemarkers);
                                        }
                                        /*
                                        //set the logged value to true/false
                                        if(dataSnapshotInfoUser.getKey().equals(currentUserId)){
                                            DatabaseReference user = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId).child("logged");

                                            Log.d("Change","Location changed");
                                            if((Boolean)dataSnapshotInfoUser.child("logged").getValue() && logOff){
                                                user.setValue(false);
                                            }
                                            else if(!logOff){
                                                user.setValue(true);
                                            }
                                        }
                                        */
                                    }
                                }
                            }
                        }
                    }
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) { }
            });

            DatabaseReference allMarkers = FirebaseDatabase.getInstance().getReference().child("markers");
            allMarkers.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshotMarker : dataSnapshot.getChildren()){
                        if (dataSnapshotMarker.child("User").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                        {
                            LatLng latLng = new LatLng(Double.parseDouble(dataSnapshotMarker.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshotMarker.child("Longitude").getValue().toString()));
                            meetingPoint = gm.addMarker(new MarkerOptions().position(latLng).title("Meeting point")
                                    .draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                            currentMarker = new MapMarker(latLng, FirebaseAuth.getInstance().getCurrentUser().getUid());
                            Log.d("marker added", "asdasd");
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });

            DatabaseReference friendMarkers = allMarkers;
            friendMarkers.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for(DataSnapshot dataSnapshotMarker : dataSnapshot.getChildren()) {
                        for (DataSnapshot friend : dataSnapshotMarker.child("Friends").getChildren()) {

                            Log.d("friend marker", friend.getValue().toString());

                            if (friend.getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {

                                LatLng latLng = new LatLng(Double.parseDouble(dataSnapshotMarker.child("Latitude").getValue().toString()), Double.parseDouble(dataSnapshotMarker.child("Longitude").getValue().toString()));
                                gm.addMarker(new MarkerOptions().position(latLng).title(dataSnapshotMarker.child("Title").getValue().toString())
                                        .draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });





        }
    }

    //create a pin when you hold on a point on the map
    //if you hold on the pin you can drag it to a different location
    @Override
    public void onMapLongClick(LatLng latLng) {
        if(meetingPoint==null)
        {
            meetingPoint = gm.addMarker(new MarkerOptions().position(latLng).title("Meeting point")
                    .draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Log.d("meeting null", "asda");

        }
        else
        {
//            Toast.makeText(this.getContext(),"You already have a marker",
            currentMarker.RemoveMarker();
            meetingPoint = null;
            gm.clear();
            meetingPoint = gm.addMarker(new MarkerOptions().position(latLng).title("Meeting point")
                    .draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Log.d("meeting not null", "das");

        }


        LatLng coordinates = meetingPoint.getPosition();
        DatabaseReference marker_db = FirebaseDatabase.getInstance().getReference().child("markers");
        MapMarker newMarker = new MapMarker(coordinates, FirebaseAuth.getInstance().getCurrentUser().getUid());
        currentMarker = newMarker;
        key = marker_db.push().getKey();
        marker_db.child(key).setValue(newMarker);// updateChildren(friendsMap);
        checkCurrentUser();
    }

    @Override
    public void onMapClick(LatLng latLng) {
        if(meetingPoint==null)
        {

        }
    }

    public static void KillFragment()
    {
        // consider using Java coding conventions (upper first char class names!!!)
        FragmentTransaction transaction = fm.beginTransaction();

        // Replace whatever is in the fragment_container view with this fragment,
        // and add the transaction to the back stack
        //transaction.remove(newFragment);
        transaction.hide(newFragment);

        FrameLayout frameLayout = statview.findViewById(R.id.MarkerFrame);
        frameLayout.setVisibility(statview.INVISIBLE);

        transaction.addToBackStack(null);

        // Commit the transaction
        transaction.commit();
    }
    public  static MapMarker getCurrentMarker()
    {
        return currentMarker;
    }

    public  class MapMarker{
        public double Latitude, Longitude;
        public String User;
        public Map<String,Object> friends;
        public String title;

        public void ChangeTitle(String title)
        {
            this.title = title;
        }

        public MapMarker(LatLng coordinates, String user)
        {
            Latitude = coordinates.latitude;
            Longitude = coordinates.longitude;
            User = user;
            friends = new HashMap<String, Object>();
        }

        public void AddFriend(String user)
        {
            friends.put("friend",user);
        }

        public void UpdateMarkerInDb()
        {
            final DatabaseReference markers = FirebaseDatabase.getInstance().getReference().child("markers");
            markers.child(key).child("Friends").setValue(friends);
            markers.child(key).child("Title").setValue(title);
        }

        public void  RemoveMarker()
        {
            if (key!=null)
            {
                Log.d("remove marker", key.toString());
                DatabaseReference markerToBeDeleted = FirebaseDatabase.getInstance().getReference().child("markers").child(key);
                markerToBeDeleted.removeValue();
                meetingPoint.remove();
            }
            else
            {
                final DatabaseReference markers = FirebaseDatabase.getInstance().getReference().child("markers");
                markers.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot markerInstance: dataSnapshot.getChildren()) {
                            if (markerInstance.child("User").getValue().equals(FirebaseAuth.getInstance().getCurrentUser().getUid()))
                            {
                                key = markerInstance.getKey();
                                markers.child(markerInstance.getKey()).removeValue();
                                break;
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
            }
        }
    }


}

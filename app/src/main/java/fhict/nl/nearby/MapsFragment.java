package fhict.nl.nearby;

import android.Manifest;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.Uri;
import android.os.AsyncTask;
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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.internal.Constants;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.core.Platform;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener, GoogleMap.OnMapLongClickListener,
        GoogleMap.OnMapClickListener {
    private static View statview;
    private GeofencingClient geofencingClient;
    private GeofenceHelper geofenceHelper;
    private int FINE_LOCATION_ACCESS_REQUEST_CODE = 10001;
    private float GEOFENCE_RADIUS = 200;
    private String GEOFENCE_ID = "SOME_GEOFENCE_ID";
    private static final String TAG = "MapsFragment";

    static MapMarker currentMarker = null;
    static String key = null;

    View view;
    SupportMapFragment mapFragment;
    GoogleMap gm;

    Location userLocation;
    LatLng userLatLng;
    HashMap<String, Marker> meetingPoints = new HashMap<>();
    boolean centered = true;
    TextView textViewName;
    MarkerOptions multiplemarkers = new MarkerOptions();
    static String currentUserId;
    static Fragment newFragment;
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
        geofencingClient = LocationServices.getGeofencingClient(this.getActivity());
        geofenceHelper = new GeofenceHelper(this.getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                            new String[] {Manifest.permission.ACCESS_FINE_LOCATION },
                            10);
        }
        else{
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, this);
            userLocation = locationManager.getLastKnownLocation(locationManager.GPS_PROVIDER);
            if(userLocation != null){
                userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
            }
        }

        //called to see if any user is logged in
        checkCurrentUser();

        statview = view;
        return view;
    }

    //called the first time when the map loads | when you open the app
    @Override
    public void onMapReady(GoogleMap googleMap) {
        gm = googleMap;
        gm.getUiSettings().setZoomControlsEnabled(true);
        //gm.getUiSettings().setMyLocationButtonEnabled(true);
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

                if (meetingPoints.containsValue(marker))
                {
                    currentMarker = new MapMarker(marker.getPosition(), FirebaseAuth.getInstance().getCurrentUser().getUid());
                    for (Map.Entry<String, Marker> entry : meetingPoints.entrySet()) {
                        if(entry.getValue().equals(marker)){
                            key = entry.getKey();
                        }
                    }
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
        enableUserLocation();
    }


    //this is called when the location is changed. This will change the markers position
    @Override
    public void onLocationChanged(Location location) {
        //update the location to the database
        DatabaseReference userDatabase = FirebaseDatabase.getInstance().getReference().child("users").child(currentUserId);
        userDatabase.child("lat").setValue(round(location.getLatitude(), 4));
        userDatabase.child("lng").setValue(round(location.getLongitude(), 4));
        if (centered == true) {
            userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
            gm.moveCamera(CameraUpdateFactory.newLatLng(userLatLng));
        }
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = BigDecimal.valueOf(value);
        bd = bd.setScale(places, RoundingMode.HALF_UP);
        return bd.doubleValue();
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
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference();
            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (gm!=null)
                    {
                        gm.clear(); //clear the markers on the map, new ones will be inserted
                    }

                    DataSnapshot usersDB = dataSnapshot.child("users");
                    for (DataSnapshot dataSnapShotUser : usersDB.getChildren()) {
                        if(dataSnapShotUser.getKey().equals(user.getUid())){
                            for (DataSnapshot usersFriends : dataSnapShotUser.child("friends").getChildren()) {
                                for (DataSnapshot dataSnapShotInfoUser : usersDB.getChildren()) {
                                    if(dataSnapShotInfoUser.getKey().equals(usersFriends.getKey()) || dataSnapShotInfoUser.getKey().equals(user.getUid())){
                                        if((Boolean)dataSnapShotInfoUser.child("logged").getValue() && (Boolean)dataSnapShotInfoUser.child("showLocation").getValue()){
                                            Double lat = Double.valueOf(dataSnapShotInfoUser.child("lat").getValue().toString());
                                            Double lng = Double.valueOf(dataSnapShotInfoUser.child("lng").getValue().toString());
                                            final String tmpName = dataSnapShotInfoUser.child("nickname").getValue().toString();
                                            String url = dataSnapShotInfoUser.child("image").getValue().toString();

                                            final LatLng tmpLatlng = new LatLng(lat, lng);
                                            Task<Uri> uriTask = FirebaseStorage.getInstance().getReference().child(url).getDownloadUrl();
                                            uriTask.addOnCompleteListener(new OnCompleteListener<Uri>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Uri> task) {
                                                    if(task.isSuccessful()){
                                                        TaskParam taskParam = new TaskParam(tmpLatlng, tmpName, task.getResult().toString());
                                                        new LoadImageIntoMarker().execute(taskParam);
                                                    }else{
                                                        TaskParam taskParam = new TaskParam(tmpLatlng, tmpName, "");
                                                        new LoadImageIntoMarker().execute(taskParam);
                                                    }
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }

                    DataSnapshot markersDB = dataSnapshot.child("markers");
                    meetingPoints = new HashMap<>();
                    for (DataSnapshot markerSnapShot : markersDB.getChildren()) {
                        LatLng latLng = new LatLng(Double.parseDouble(markerSnapShot.child("Latitude").getValue().toString()), Double.parseDouble(markerSnapShot.child("Longitude").getValue().toString()));
                        String title = "Meeting Point";
                        Object titleObject = markerSnapShot.child("Title").getValue();
                        if(titleObject != null){
                            if(!((String)titleObject).isEmpty()){
                                title = (String)titleObject;
                            }
                        }
                        if(markerSnapShot.child("User").getValue().equals(user.getUid())){
                            meetingPoints.put(markerSnapShot.getKey(), gm.addMarker(new MarkerOptions().position(latLng).title(title)
                                    .draggable(true).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW))));
                        }

                        for(DataSnapshot friend : markerSnapShot.child("Friends").getChildren()){
                            if (friend.getValue().toString().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                                gm.addMarker(new MarkerOptions().position(latLng).title(title)
                                        .draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                                addCircle(latLng, GEOFENCE_RADIUS);
                                addGeofence(latLng, GEOFENCE_RADIUS);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {}
            });
        }
    }

    //create a pin when you hold on a point on the map
    @Override
    public void onMapLongClick(LatLng latLng) {
        gm.addMarker(new MarkerOptions().position(latLng).title("Meeting point")
                .draggable(false).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
        addCircle(latLng, GEOFENCE_RADIUS);
        addGeofence(latLng, GEOFENCE_RADIUS);

        DatabaseReference marker_db = FirebaseDatabase.getInstance().getReference().child("markers");
        MapMarker newMarker = new MapMarker(latLng, FirebaseAuth.getInstance().getCurrentUser().getUid());
        key = marker_db.push().getKey();
        marker_db.child(key).setValue(newMarker);
    }

    @Override
    public void onMapClick(LatLng latLng) {
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
                DatabaseReference markerToBeDeleted = FirebaseDatabase.getInstance().getReference().child("markers").child(key);
                markerToBeDeleted.removeValue();
                currentMarker = null;
                key = null;
            }
        }
    }

    private void enableUserLocation()
    {
        if(ContextCompat.checkSelfPermission(this.getActivity(),Manifest.permission.ACCESS_FINE_LOCATION) ==
        PackageManager.PERMISSION_GRANTED)
        {
            gm.setMyLocationEnabled(true);
        }
        else
        {
            if(ActivityCompat.shouldShowRequestPermissionRationale(this.getActivity(), Manifest.permission.ACCESS_FINE_LOCATION))
            {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
            else
            {
                ActivityCompat.requestPermissions(this.getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        FINE_LOCATION_ACCESS_REQUEST_CODE);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == FINE_LOCATION_ACCESS_REQUEST_CODE)
        {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                //we have the permission
                gm.setMyLocationEnabled(true);
            }
        }
    }

    private void addCircle(LatLng lt, float radius)
    {
        CircleOptions circleOptions = new CircleOptions();
        circleOptions.center(lt);
        circleOptions.radius(radius);
        circleOptions.strokeColor(Color.YELLOW);
        circleOptions.fillColor(Color.YELLOW);
        circleOptions.strokeWidth(2);
        gm.addCircle(circleOptions);
    }
    private void addGeofence(LatLng latLng, float radius)
    {
        Geofence geofence = geofenceHelper.getGeofence(GEOFENCE_ID,latLng,radius,
                Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_DWELL |
                        Geofence.GEOFENCE_TRANSITION_EXIT);
        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
        PendingIntent pendingIntent = geofenceHelper.getPendingIntent();

        geofencingClient.addGeofences(geofencingRequest, pendingIntent)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        String errorMessage = geofenceHelper.getErrorString(e);
                    }
                });
    }

    private class LoadImageIntoMarker extends AsyncTask<TaskParam, Void, TaskParam>{

        @Override
        protected TaskParam doInBackground(TaskParam... taskParams) {
            String url = taskParams[0].url;
            if(!url.equals("")){
                try {
                    URL myUrl =  new URL(url);
                    Bitmap bmp = BitmapFactory.decodeStream(myUrl.openConnection().getInputStream());
                    Bitmap resizedBMP = Bitmap.createScaledBitmap(bmp, 100, 100, false);
                    taskParams[0].setBmp(resizedBMP);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return taskParams[0];
        }

        @Override
        protected void onPostExecute(TaskParam taskParam) {
            super.onPostExecute(taskParam);
            multiplemarkers.position(taskParam.latLng);
            multiplemarkers.title(taskParam.name);
            multiplemarkers.draggable(false);
            if(taskParam.bmp != null){
                multiplemarkers.icon(BitmapDescriptorFactory.fromBitmap(taskParam.bmp));
            }
            gm.addMarker(multiplemarkers);
        }
    }

    private class TaskParam{
        LatLng latLng;
        String name;
        String url;
        Bitmap bmp;

        TaskParam(LatLng latLng, String name, String url){
            this.latLng = latLng;
            this.name = name;
            this.url = url;
            this.bmp = null;
        }

        public void setBmp(Bitmap bmp){
            this.bmp = bmp;
        }
    }
}

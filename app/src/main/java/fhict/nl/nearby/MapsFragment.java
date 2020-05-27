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

public class MapsFragment extends Fragment implements OnMapReadyCallback, LocationListener {
    private static final int RC_SIGN_IN = 123;

    View view;
    SupportMapFragment mapFragment;
    GoogleMap gm;
    MarkerOptions markerOptions;
    LatLng locationCoordonates;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        //inflate the fragment
        view = inflater.inflate(R.layout.fragment_maps, container, false);

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
        return view;
    }

    //called the first time when the map loads | when you open the app
    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.gm = googleMap;
        locationCoordonates = new LatLng(-34, 151);
        markerOptions = new MarkerOptions().position(locationCoordonates);
        gm.addMarker(markerOptions.title("BLA BLA BLA"));
        gm.moveCamera(CameraUpdateFactory.newLatLng(locationCoordonates));
    }

    //this is called when the location is changed. This will change the marker position
    @Override
    public void onLocationChanged(Location location) {
        gm.clear();
        locationCoordonates = new LatLng(location.getLatitude(), location.getLongitude());
        markerOptions.position(locationCoordonates);
        gm.addMarker(markerOptions.title("BLA BLA BLA"));
        gm.moveCamera(CameraUpdateFactory.newLatLng(locationCoordonates));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}

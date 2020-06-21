package fhict.nl.nearby;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

import java.util.List;

import static androidx.constraintlayout.widget.Constraints.TAG;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //Toast.makeText(context,"Geofence triggered...", Toast.LENGTH_SHORT).show();

        NotificationHelper notificationHelper = new NotificationHelper(context);

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if(geofencingEvent.hasError())
        {
            //Log.d(TAG, "onReceive: Error receiving Geofence Event...");
            return;
        }

//        List<Geofence> geofencelist.getTriggeringGeofences();
//        for (Geofence geofence: geofencelist)
//        {
//            Log.d(TAG, "onReceive: " + geofencingEvent.getRequestId());
//        }

        int transitionType = geofencingEvent.getGeofenceTransition();

        switch (transitionType)
        {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                notificationHelper.sendHighPriorityNotification("Target reached!", "You reached the meeting point successfully." ,
                        MenuActivity.class);
                break;
        }
    }
}

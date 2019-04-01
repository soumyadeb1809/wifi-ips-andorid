import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;


import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;



import java.util.List;




import static android.content.Context.LOCATION_SERVICE;
import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;
import static android.location.LocationManager.PASSIVE_PROVIDER;

public class InternalLocationManager {

    private static InternalLocationManager internalLocationManager;
    private LocationManager locationManager;
    private boolean checkForAddress;

    private Context context;



    public static void startLocationTracking(Context context) {

        if (internalLocationManager == null) {

            synchronized (InternalLocationManager.class) {

                if (internalLocationManager == null) {

                    internalLocationManager = new InternalLocationManager();
                    internalLocationManager.context = context;

                    internalLocationManager.locationManager = (LocationManager) context
                            .getSystemService(LOCATION_SERVICE);


                    internalLocationManager.getInternalLocation();
                }
            }
        }


    }



    public static void checkAddress(boolean checkForAddress) {
        internalLocationManager.checkForAddress = checkForAddress;
    }


    private void updateLocationLogs() {


    }

    private void showServiceLog(String s) {
        Log.i(InternalLocationManager.class.getSimpleName(), TextUtils.isEmpty(s) ? "NA" : s);
    }

   /* public static void setDetectCityInterface(DetectCityInterface detectCityInterface) {
        internalLocationManager.detectCityInterface =detectCityInterface;
    }*/

    private void getInternalLocation() {

        final int LOCATION_UPDATE_TIME = 1000;

        if (locationManager != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
               // sendFailedAddress();
                return;
            }

            if (locationManager == null) {
                //sendFailedAddress();
                return;
            }

            List<String> locationProvider = locationManager.getProviders(true);

            LocationProvider mainProvider = null;

            if (locationProvider.contains(GPS_PROVIDER))
                mainProvider = locationManager.getProvider(GPS_PROVIDER);
            else if (locationProvider.contains(NETWORK_PROVIDER))
                mainProvider = locationManager.getProvider(NETWORK_PROVIDER);
            else if (locationProvider.contains(LocationManager.PASSIVE_PROVIDER))
                mainProvider = locationManager.getProvider(PASSIVE_PROVIDER);

            if (mainProvider == null) {
                //sendFailedAddress();
                return;
            }


            locationManager.requestLocationUpdates(mainProvider.getName(), LOCATION_UPDATE_TIME, 0, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {

                    if (location == null) {
                      /*  if(checkForAddress && detectCityInterface != null)
                            detectCityInterface.noCityDetected();*/

                        return;
                    }

                }

                @Override
                public void onStatusChanged(String s, int i, Bundle bundle) {

                }

                @Override
                public void onProviderEnabled(String s) {

                }

                @Override
                public void onProviderDisabled(String s) {
                    //sendFailedAddress();
                }
            });
        } else {
           /* if (checkForAddress && detectCityInterface != null)
                //detectCityInterface.noCityDetected();
            else
                //ExternalProcessManager.showToast(context , "Location manager is null");*/
        }

        updateLocationLogs();

    }

/*    private void sendFailedAddress(){
        if(checkForAddress && detectCityInterface != null )
            detectCityInterface.noCityDetected();
    }*/

}
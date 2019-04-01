package com.soumyadeb.wifitrilateration;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.soumyadeb.wifitrilateration.network.api.ApiClient;
import com.soumyadeb.wifitrilateration.network.api.ApiInterface;
import com.soumyadeb.wifitrilateration.network.dto.UpdateLocationResponse;

import java.text.SimpleDateFormat;
import java.util.Date;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int PERMISSION_REQ = 10;
    private static final int MIN_UPDATE_TIME_IN_MS = 1000;
    private static final int MIN_UPDATE_DIST_IN_M = 0;

    private GoogleMap mMap;
    private LocationManager locationManager;
    private LocationListener listener;

    private Button btnPublishLoc;
    private TextView tvLastPublished;

    private Location currentLocation = null;
    
    private AlertDialog alert;
    private ImageView imgSsid;
    private SharedPreferences sp;

    private ApiInterface apiInterface;
    private ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        apiInterface =  ApiClient.getApiClient().create(ApiInterface.class);

        sp = getSharedPreferences("kjsdhfkjhsdkf", MODE_PRIVATE);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        initializeUi();
        setUpAlert();

    }

    private void initializeUi() {

        tvLastPublished = findViewById(R.id.txt_last_pub);
        btnPublishLoc = findViewById(R.id.btn_publish);
        imgSsid = findViewById(R.id.btn_ssid);

        String update = sp.getString("updated", "");

        if(TextUtils.isEmpty(update)){
            tvLastPublished.setText("Last published on -NA-");
        }
        else {
            tvLastPublished.setText(update);
        }
        
        btnPublishLoc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ssid = sp.getString("ssid", "");
                if(TextUtils.isEmpty(ssid)){
                    alert.show();
                }
                else {
                    publishDataToServer(ssid, currentLocation);
                }
            }
        });

        imgSsid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.show();
            }
        });

        progressDialog = new ProgressDialog(this);

    }

    private void publishDataToServer(String ssid, Location currentLocation) {

        progressDialog.setMessage("Publishing location...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        final Call<UpdateLocationResponse> updateLocationCall = apiInterface.doUpdateLocation(ssid, currentLocation.getLatitude(), currentLocation.getLongitude());

        updateLocationCall.enqueue(new Callback<UpdateLocationResponse>() {
            @Override
            public void onResponse(Call<UpdateLocationResponse> call, Response<UpdateLocationResponse> response) {
                progressDialog.dismiss();

                UpdateLocationResponse updateLocationResponse = response.body();
                if(updateLocationResponse.getResult().equals("SUCCESS")){
                    Toast.makeText(MapsActivity.this, "Location updated SUCCESSFULLY", Toast.LENGTH_SHORT).show();
                    Date date = new Date();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm, dd MMM");
                    tvLastPublished.setText("Last published on " + dateFormat.format(date));
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("updated", tvLastPublished.getText().toString());
                    editor.commit();
                }
                else {
                    Toast.makeText(MapsActivity.this, "FAILED TO UPDATE location", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UpdateLocationResponse> call, Throwable t) {
                progressDialog.dismiss();
                t.printStackTrace();
                Toast.makeText(MapsActivity.this, "Unknown error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void setUpLocationListener() {
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                currentLocation = location;
                updateMapMarkers(currentLocation);
                Log.d("asddsaf","Location: " + location.getLongitude() + ", " + location.getLatitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpLocationListener();
        Log.d("asddsaf", "Map ready");
        startReceivingUpdates();
    }


    public void updateMapMarkers(Location location){
        mMap.clear();

        LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(currentLatLng).title("Current Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 19f));

    }

    void startReceivingUpdates(){

        // first check for permissions
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,PERMISSION_REQ);
            }
            return;
        }

        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_UPDATE_TIME_IN_MS, MIN_UPDATE_DIST_IN_M, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, MIN_UPDATE_TIME_IN_MS, MIN_UPDATE_DIST_IN_M, listener);
        locationManager.requestLocationUpdates(LocationManager.PASSIVE_PROVIDER, MIN_UPDATE_TIME_IN_MS, MIN_UPDATE_DIST_IN_M, listener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case PERMISSION_REQ:
                startReceivingUpdates();
                break;
            default:
                break;
        }
    }
    
    void setUpAlert(){

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View dialogLayout = inflater.inflate(R.layout.ssid_input, null);
        alertBuilder.setView(dialogLayout);
        alertBuilder.setTitle("Edit SSID");

        final EditText etSsid = dialogLayout.findViewById(R.id.et_ssid);

        String ssid = sp.getString("ssid", "");

        etSsid.setText(ssid);
        
        alertBuilder.setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("ssid", etSsid.getText().toString());
                editor.commit();
            }
        });
        
        alertBuilder.setNegativeButton("CANCEL", null);

        alert = alertBuilder.create();
        
    }
}

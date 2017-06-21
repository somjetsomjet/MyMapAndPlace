package com.example.somjetr.mymapandplace;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by somjet.r on 2017-05-25.
 */

public class GpsActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

//    get current gps + manual send          ok
//    permission                             ok
//    detect เปิดปิด gps                         ok
//    auto send                              ok
//    network ไม่ได้ผล ได้แต่ gps                   ok
//    เปิดปิด gps ต้อง manual เอง                   ok


    private GoogleMap ggMap;
    private LocationManager locationManager;
    private String provider;
    private double lat;
    private double lng;
    Marker mMotor = null;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gps);


        //------map-----
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ggMap);
        mapFragment.getMapAsync(this);


        // Get the location manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // Define the criteria how to select the locatioin provider -> use
        // default
        //Criteria criteria = new Criteria();
        //provider = locationManager.getBestProvider(criteria, true);



        //---------btn                                                                                               //    get current gps + manual send
        Button btnGet = (Button) findViewById(R.id.btnGet);
        btnGet.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                GetCurrent();
            }
        });



    }


    private  void GetCurrent(){                                                                                      //    get current gps + manual send

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))                                       //   detect เปิดปิด gps
            provider = LocationManager.GPS_PROVIDER;
        else if(locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            //provider = LocationManager.NETWORK_PROVIDER;
            Toast.makeText(GpsActivity.this, "please turn-on GPS", Toast.LENGTH_SHORT).show();
            return;
        }else{
            Toast.makeText(GpsActivity.this, "please turn-on GPS", Toast.LENGTH_SHORT).show();
            return;
        }


        //                                                                                                                              //permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)  != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                showMessageOKCancel("You need to allow access to GPS",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ActivityCompat.requestPermissions(GpsActivity.this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
                            }
                        });
                return;
            }

            //not first time
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_PERMISSIONS);
            return;
        }else {
            HaveGpsPermission();
        }

    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // Permission Granted
                    HaveGpsPermission();
                } else {
                    // Permission Denied
                    Toast.makeText(GpsActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }




    private void showMessageOKCancel(String message, DialogInterface.OnClickListener okListener) {
        new AlertDialog.Builder(GpsActivity.this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }






    private  void HaveGpsPermission() {

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //always do
        }

        locationManager.requestLocationUpdates(provider, 1000, 1, this);        //and     1000ms       1m       //auto send

        Location location = locationManager.getLastKnownLocation(provider);

        // Initialize the location fields
        if (location != null) {
            lat =  (location.getLatitude());
            lng = (location.getLongitude());
            DrawMarker(lat, lng);
            Toast.makeText(GpsActivity.this, String.valueOf(lat) + "-" +  String.valueOf(lng), Toast.LENGTH_SHORT).show();

        } else {
            //bug emu
            Toast.makeText(GpsActivity.this, "bug emu, after close and open location in setting must send location : Location not available", Toast.LENGTH_SHORT).show();

        }
    }


        @Override
    public void onLocationChanged(Location location) {
        lat =  (location.getLatitude());                                                                 //auto send
        lng = (location.getLongitude());                                                                   //manual send

        DrawMarker(lat, lng);

        Toast.makeText(GpsActivity.this, String.valueOf(lat) + "-" +  String.valueOf(lng), Toast.LENGTH_SHORT).show();

    }

    private void DrawMarker(double lat, double lng){
        if(ggMap != null) {
            if (mMotor != null)
                mMotor.remove();
            mMotor = ggMap.addMarker(new MarkerOptions().position(new LatLng(lat, lng)).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.motorcycle)));
            ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng), 15));
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(this, "Enabled(app) new provider " + provider,                                   //   detect เปิดปิด gps
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onProviderDisabled(String provider) {
        Toast.makeText(this, "Disabled(app) provider " + provider,                                      //   detect เปิดปิด gps
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        ggMap = googleMap;

        ggMap.getUiSettings().setZoomControlsEnabled(true);
        ggMap.getUiSettings().setCompassEnabled(true);
        ggMap.getUiSettings().setMapToolbarEnabled(false);

        GetCurrent();
    }
}

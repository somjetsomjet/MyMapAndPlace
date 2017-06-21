package com.example.somjetr.mymapandplace;

import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

/**
 * Created by somjet.r on 2017-05-30.
 */

public class GpsShowActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap ggMap;
    private Button btnSend;
    private LatLng current;
    private LatLng oldCurrent;
    private Handler handler;

    int i = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpsshow);

        //------map-----
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ggMap);
        mapFragment.getMapAsync(this);



        //---------btn
        current = new LatLng(13.6964031, 100.5943842);
        oldCurrent = current;
        btnSend = (Button) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                current =  GetDriverCurrent();
            }
        });

    }

    @Override
    public void onBackPressed()
    {
        handler.removeMessages(0);
        super.onBackPressed();
    }

    public LatLng GetDriverCurrent()
    {
        i++;

        //reset
        if(i==3)
            i=0;

        if(i==0)
            return new LatLng(13.6964031, 100.5943842);
        else if (i==1)
            return new LatLng(13.6962842, 100.5988263);
        else if (i==2)
            return new LatLng(13.6992021, 100.5992598);
        else
            return null;
    }

    private double bearingBetweenLocations(LatLng latLng1, LatLng latLng2) {

        double PI = 3.14159;
        double lat1 = latLng1.latitude * PI / 180;
        double long1 = latLng1.longitude * PI / 180;
        double lat2 = latLng2.latitude * PI / 180;
        double long2 = latLng2.longitude * PI / 180;

        double dLon = (long2 - long1);

        double y = Math.sin(dLon) * Math.cos(lat2);
        double x = Math.cos(lat1) * Math.sin(lat2) - Math.sin(lat1)
                * Math.cos(lat2) * Math.cos(dLon);

        double brng = Math.atan2(y, x);

        brng = Math.toDegrees(brng);
        brng = (brng + 360) % 360;

        return brng;
    }

    private void rotateMarker(final Marker marker, final float toRotation) {


        final long duration = 1000;

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        final float startRotation = marker.getRotation();

        final Interpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {

                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                float rot = t * toRotation + (1 - t) * startRotation;
                marker.setRotation(rot);

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });

    }

    public void animateMarker(final Marker marker, final LatLng toPosition) {

        final long duration = 3000;

        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();
        Projection proj = ggMap.getProjection();
        Point startPoint = proj.toScreenLocation(marker.getPosition());
        final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        final Interpolator interpolator = new LinearInterpolator();
        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed / duration);

                double lng = t * toPosition.longitude + (1 - t) * startLatLng.longitude;
                double lat = t * toPosition.latitude + (1 - t) * startLatLng.latitude;

                marker.setPosition(new LatLng(lat, lng));

                if (t < 1.0) {
                    handler.postDelayed(this, 16);
                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        ggMap = googleMap;

        ggMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                        this, R.raw.style_map_json));

        ggMap.getUiSettings().setZoomControlsEnabled(true);
        ggMap.getUiSettings().setCompassEnabled(true);
        ggMap.getUiSettings().setMapToolbarEnabled(false);

        final LatLng newLocaation0, newLocaation1, newLocaation2, newLocaation3;


        newLocaation0 = new LatLng(13.6963, 100.59);
        newLocaation1 = new LatLng(13.696289, 100.598914);
        newLocaation2 = new LatLng(13.70331, 100.5997520);
        newLocaation3 = new LatLng(13.698639, 100.595221);
        final Marker marker = ggMap.addMarker(new MarkerOptions()
                .position(current)
                .draggable(true)
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.big))
        //        .flat(true)

        );

        ggMap.animateCamera(CameraUpdateFactory.newLatLngZoom(current, 15));

        //marker.setRotation(90);

        ggMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

                //  marker.setAnchor(0.4f, -0.3f);
            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                marker.setTitle(marker.getPosition().toString());
                marker.showInfoWindow();
                oldCurrent = marker.getPosition();
            }
        });



        final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.add(newLocaation0);
        options.add(newLocaation1);
        options.add(newLocaation2);
        options.add(newLocaation3);

        ggMap.addPolyline(options);





        //delay
        handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {





                float bearing = 0;

                if(oldCurrent != current) {

                    oldCurrent = current;

                    //animate move marker
                    animateMarker(marker,current);

                    //find angle      0    1   2  ...  359
                    bearing = (float) bearingBetweenLocations(marker.getPosition(), current);


                    //rotate               U
                    rotateMarker(marker, bearing);
                }

                Toast.makeText(GpsShowActivity.this, "get current driver position every 5s ", Toast.LENGTH_SHORT).show();
                handler.postDelayed(this,5000);
            }
        }, 5000);






    }
}

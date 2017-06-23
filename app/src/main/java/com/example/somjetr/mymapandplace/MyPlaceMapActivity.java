package com.example.somjetr.mymapandplace;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MyPlaceMapActivity extends FragmentActivity implements  OnMapReadyCallback {

    private GoogleMap ggMap;
    private Button btnSelect;
    private Marker marker;
    private PlaceAutocompleteFragment txtLoc;
    private LocationManager locationManager;
    private Location location;
    private LatLng oldTarget;
    private SupportMapFragment fm;
    private FromEvent fe = FromEvent.Drag;

    private enum FromEvent {
        Init,
        Zoom,
        InputText,
        Drag,
        MyLocation,
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplacemap);

        //-----map
        fm = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        fm.getMapAsync(this);

        //-----textbox
        txtLoc = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.txtLoc);
        txtLoc.setFilter(new AutocompleteFilter.Builder().setCountry("TH").build());

        txtLoc.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                fe = FromEvent.InputText;
                ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom( place.getLatLng(), 15));
                SetMarkerCenter(place.getLatLng());
                marker.setTitle(place.getName().toString());
            }

            @Override
            public void onError(Status status) {
            }
        });


        //-----button
        btnSelect = (Button) findViewById(R.id.btnSelect);
        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                intent.putExtra("location_name", marker.getTitle());
                intent.putExtra("location_lat", marker.getPosition().latitude);
                intent.putExtra("location_lng", marker.getPosition().longitude);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

    }

    public String GetAddress (){

//        String Geocoder_PlaceWebService = "Geocoder"/*"PlaceWebService"*/;

        String address = "";

//        if(Geocoder_PlaceWebService == "Geocoder") {
            Geocoder geocoder;
            List<Address> addresses = null;
            geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

            try {
                addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                //addresses = geocoder.getFromLocation(13.6942398,100.4942557, 1);
                //addresses = geocoder.getFromLocation(13.6983917,100.6005393, 8);

            } catch (IOException e) {
                e.printStackTrace();
            }


            if (addresses.size() != 0)
                address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        }else if (Geocoder_PlaceWebService == "PlaceWebService") {
//            new CallWebService(marker.getPosition()).execute();
//        }

        return  address;

    }


//    //-----Call web service direction------
//    class CallWebService extends AsyncTask<Void,Void,String> {
//        private LatLng p;
//
//        public CallWebService(LatLng p) {
//            this.p = p;
//        }
//
//        @Override
//        protected String doInBackground(Void... params) {
//
//            //-----call webService map direction api
//            String out = "";
//
//            String ggDrtUrl = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%1$s,%2$s&rankby=distance&key=%3$s"
//                    , p.latitude, p.longitude, "AIzaSyD9sw1nVib-8gsX7nEwgNKuW0y6vgUgv2c");
//
//            URL url;
//            HttpURLConnection urlConnection = null;
//            try {
//                url = new URL(ggDrtUrl);
//                urlConnection = (HttpURLConnection) url.openConnection();
//                InputStream in = urlConnection.getInputStream();
//                BufferedReader myReader = new BufferedReader(new InputStreamReader(in));
//                String receiveString = "";
//                StringBuilder stringBuilder = new StringBuilder();
//
//                while ((receiveString = myReader.readLine()) != null) {
//                    stringBuilder.append(receiveString);
//                }
//                myReader.close();
//                out = stringBuilder.toString();
//
//            } catch (Exception e) {
//                e.printStackTrace();
//
//            } finally {
//                if (urlConnection != null)
//                    urlConnection.disconnect();
//            }
//
//            return out;
//        }
//
//        @Override
//        protected void onPostExecute(String result) {
//            super.onPostExecute(result);
//            if (result != null) {
//                SetAddress(result);
//            }
//        }
//    }
//
//    //-----for web service
//    public void SetAddress (String in){
//        JSONObject json;
//        try {
//            json = new JSONObject(in);
//
//            JSONObject result = json.getJSONArray("results").getJSONObject(0);
//            String ad = result.getString("name");
//
//            txtLoc.setText(ad);
//            marker.setTitle(ad);
//            marker.showInfoWindow();
//            if(ad == "")
//                marker.hideInfoWindow();
//
//
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//    }


    public  void SetMarkerCenter (LatLng ll){
        if(ll == null) {
            LatLng centerOfMap = ggMap.getCameraPosition().target;
            marker.setPosition(centerOfMap);
        }else{
            marker.setPosition(ll);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMapX) {





        ggMap = googleMapX;
        ggMap.getUiSettings().setZoomControlsEnabled(true);
        ggMap.getUiSettings().setCompassEnabled(true);
        ggMap.getUiSettings().setMapToolbarEnabled(false);

        //-----gps current
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                fe = FromEvent.Init;
                ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                marker = ggMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).title("").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.targett)));
            }else{
                fe = FromEvent.Init;
                LatLng centerOfMap = ggMap.getCameraPosition().target;
                marker = ggMap.addMarker(new MarkerOptions().position(centerOfMap).title("").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.targett)));
            }

            ggMap.setMyLocationEnabled(true);
            ggMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    if (ContextCompat.checkSelfPermission(MyPlaceMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (location != null) {
                            fe = FromEvent.MyLocation;
                            ggMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));
                            SetMarkerCenter(new LatLng(location.getLatitude(), location.getLongitude()));
                            return true;
                        }
                    }
                    return false;
                }
            });
        }else{
            Toast.makeText(MyPlaceMapActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT).show();

            //initial marker center
            fe = FromEvent.Init;
            LatLng centerOfMap = ggMap.getCameraPosition().target;
            marker = ggMap.addMarker(new MarkerOptions().position(centerOfMap).title("").draggable(false).icon(BitmapDescriptorFactory.fromResource(R.drawable.targett)));
        }







        final Handler handler = new Handler();
         handler.postDelayed(new Runnable() {
            @Override
            public void run() {

                //-----img oldTarget
                ImageView imgTarget = (ImageView) findViewById(R.id.imgTarget);
                imgTarget.setImageResource(R.drawable.target);
                imgTarget.setX(fm.getView().getWidth()/2 - imgTarget.getDrawable().getIntrinsicWidth()/2);
                imgTarget.setY(fm.getView().getHeight()/2- imgTarget.getDrawable().getIntrinsicHeight()/2);


                if(imgTarget.getX() == -25 && imgTarget.getY() == 25 ) {
                    handler.postDelayed(this, 1000);
                    Toast.makeText(MyPlaceMapActivity.this, Float.toString(imgTarget.getX()) + "---"+ Float.toString(imgTarget.getY()), Toast.LENGTH_SHORT).show();
                }
            }
        }, 1);













        //move start
        ggMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                marker.hideInfoWindow();
                marker.setVisible(false);
                oldTarget = ggMap.getCameraPosition().target;
            }
        });

        //stop move
        ggMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                marker.setVisible(true);

                if(oldTarget != null && oldTarget.latitude == ggMap.getCameraPosition().target.latitude && oldTarget.longitude == ggMap.getCameraPosition().target.longitude)
                    fe = FromEvent.Zoom;

                if(fe == FromEvent.Drag)
                    SetMarkerCenter(null);

                if(fe == FromEvent.Drag || fe == FromEvent.MyLocation || fe == FromEvent.Init) {
                    String ad = GetAddress();
                    txtLoc.setText(ad);
                    marker.setTitle(ad);
                    marker.showInfoWindow();
                    if(ad == "")
                        marker.hideInfoWindow();
                }else if(fe == FromEvent.InputText || fe == FromEvent.Zoom) {
                    marker.showInfoWindow();
                }

                //default drag
                fe = FromEvent.Drag;
            }
        });

    }
}
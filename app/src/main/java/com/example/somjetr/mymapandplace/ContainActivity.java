package com.example.somjetr.mymapandplace;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by somjet.r on 2017-06-23.
 */

public class ContainActivity  extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap ggMap;
    private LatLng latLng;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contain);

        //------map-----
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ggMap);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        ggMap = googleMap;

        ggMap.getUiSettings().setZoomControlsEnabled(true);
        ggMap.getUiSettings().setCompassEnabled(true);
        ggMap.getUiSettings().setMapToolbarEnabled(false);

        //connex
        ggMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(13.694706, 100.500576), 15));

        //connex
        Marker marker = ggMap.addMarker(new MarkerOptions()
                .position(new LatLng(13.694706, 100.500576))
                .draggable(true));


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
                latLng = marker.getPosition();
                new GetContain().execute();
            }
        });



        final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
        options.add(new LatLng(13.696792, 100.495216));
        options.add(new LatLng(13.694749, 100.497319));
        options.add(new LatLng(13.692831, 100.495559));
        options.add(new LatLng(13.691392, 100.500237));
        options.add(new LatLng(13.693039, 100.505773));
        options.add(new LatLng(13.697501, 100.505880));
        options.add(new LatLng(13.696729, 100.502769));
        options.add(new LatLng(13.694061, 100.503670));
        options.add(new LatLng(13.693998, 100.499915));
        options.add(new LatLng(13.699752, 100.501288));
        options.add(new LatLng(13.696792, 100.495216));


        ggMap.addPolyline(options);


    }




    class GetContain extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... Void) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.35:31956/api/My/PostGetContain");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("Latitude", latLng.latitude);
                obj.put("Longitude", latLng.longitude);

                os.writeBytes(obj.toString());

                os.flush();
                os.close();

                conn.connect();


                JSONObject objOut = null;

                int responseCode1 = conn.getResponseCode();
                if (responseCode1 == HttpURLConnection.HTTP_OK) {


                    InputStream in = conn.getInputStream();
                    BufferedReader myReader = new BufferedReader(new InputStreamReader(in));
                    String receiveString = "";
                    StringBuilder stringBuilder = new StringBuilder();

                    while ((receiveString = myReader.readLine()) != null) {
                        stringBuilder.append(receiveString);
                    }
                    myReader.close();
                    String out = stringBuilder.toString();

                    objOut = new JSONObject(out);

                }

                return objOut.get("status").toString();

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(ContainActivity.this, "Is " + result, Toast.LENGTH_SHORT).show();

        }

    }


}

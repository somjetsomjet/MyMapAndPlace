package com.example.somjetr.mymapandplace;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
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
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap ggMap;
    private Marker mFrom;
    private Marker mTo;
    private LatLng llFrom;
    private LatLng llTo;
    private Polyline line;
    private TextView lblDistanceValue;
    private TextView lblTimeValue;
    private ArrayList<Marker> waypoints;
    private LocationManager lm;
    private LocationListener ll;
    private Marker mMotor;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 123;
    final private int REQUEST_CODE_ASK_PERMISSIONS_c = 456;
    private GoogleApiClient mGoogleApiClient;
    private boolean optimize = false;
    private TextView lblOrder;

    //-----Create-----
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        // ------place picker------
        // Intent i = new Intent(getApplicationContext(), PlaceActivity.class);
        // startActivity(i);

        waypoints = new ArrayList<Marker>();


        //------get current------

        buildGoogleApiClient();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                //.addApi(LocationServices.API)
                .addApi(Places.PLACE_DETECTION_API)
                //.addApi(Places.GEO_DATA_API)
                .enableAutoManage(this, null)
                .build();


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {


            PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi.getCurrentPlace(mGoogleApiClient, null);
            result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                @Override
                public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                    for (PlaceLikelihood placeLikelihood : likelyPlaces) {


                        Toast.makeText(MapActivity.this, placeLikelihood.getPlace().getName(), Toast.LENGTH_SHORT).show();

                        break;
                    }
                    likelyPlaces.release();
                }
            });


        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS_c);
            }
            return;
        }


        //-----GPS------
        lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        ll = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (mMotor != null)
                    mMotor.remove();
                mMotor = ggMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(), location.getLongitude())).draggable(true).icon(BitmapDescriptorFactory.fromResource(R.drawable.motorcycle)));
                ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                //Toast.makeText(MapActivity.this, "5s", Toast.LENGTH_SHORT).show();
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
        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, ll);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_CODE_ASK_PERMISSIONS);
            }
            return;
        }


        //-----button Opt-----
        final Button btnOpt = (Button) findViewById(R.id.btnOpt);
        btnOpt.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {


                if(optimize == false) {
                    optimize = true;
                    btnOpt.setText("Opt:True");
                }else{
                    optimize = false;
                    btnOpt.setText("Opt:False");
                }
                Direction();
            }
        });

        //-----button way-----
        Button btnWay = (Button) findViewById(R.id.btnWay);
        btnWay.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                //max 2
                if (waypoints.size() >= 2)
                    return;

                int name = waypoints.size() + 1;

                MarkerOptions mo = new MarkerOptions().position(new LatLng(13.696096251245065, 100.5004033818841)).title("Way: " + name).draggable(true);
                if (name == 1)
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.w1));
                else if (name == 2)
                    mo.icon(BitmapDescriptorFactory.fromResource(R.drawable.w2));

                Marker w = ggMap.addMarker(mo);
                waypoints.add(w);
                Direction();
            }
        });

        //-----button del-----
        Button btnDel = (Button) findViewById(R.id.btnDel);
        btnDel.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                if (waypoints.size() == 0)
                    return;

                Marker m = waypoints.get(waypoints.size() - 1);
                m.remove();
                waypoints.remove(waypoints.size() - 1);
                Direction();
            }
        });


        //------textbox-----
        lblDistanceValue = (TextView) findViewById(R.id.lblDistanceValue);
        lblTimeValue = (TextView) findViewById(R.id.lblTimeValue);
        lblOrder = (TextView) findViewById(R.id.lblOrder);

        //------map-----
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.ggMap);
        mapFragment.getMapAsync(this);


        //------from-----
        PlaceAutocompleteFragment autocompleteFragmentFrom = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.txtFrom);

        AutocompleteFilter typeFilterFrom = new AutocompleteFilter.Builder().setCountry("TH")
                .build();

        autocompleteFragmentFrom.setFilter(typeFilterFrom);


        autocompleteFragmentFrom.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (mFrom != null)
                    mFrom.remove();
                llFrom = place.getLatLng();

                mFrom = ggMap.addMarker(new MarkerOptions().position(llFrom).title("From: " + place.getName() + " " + place.getAddress()).draggable(true));
                ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom(llFrom, 15));


                Direction();
            }

            @Override
            public void onError(Status status) {
            }
        });

        //------to-----
        PlaceAutocompleteFragment autocompleteFragmentTo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.txtTo);

        AutocompleteFilter typeFilterTo = new AutocompleteFilter.Builder().setCountry("TH")
                .build();

        autocompleteFragmentTo.setFilter(typeFilterTo);

        autocompleteFragmentTo.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (mTo != null)
                    mTo.remove();
                llTo = place.getLatLng();

                mTo = ggMap.addMarker(
                        new MarkerOptions().position(llTo).title("To: " + place.getName() + " " + place.getAddress()).draggable(true)

                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.flag))
                );
                ggMap.moveCamera(CameraUpdateFactory.newLatLngZoom(llTo, 15));


                Direction();
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    //---------current-----------
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }
    @Override
    public void onConnected(Bundle connectionHint) {
        // Provides a simple way of getting a device's location and is well suited for
        // applications that do not require a fine-grained location and that do not need location
        // updates. Gets the best and most recent location currently available, which may be null
        // in rare cases when a location is not available.
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {

            return;
        }
        Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Toast.makeText(this,mLastLocation.getLatitude() + "---" + mLastLocation.getLongitude(), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "R.string.no_location_detected", Toast.LENGTH_LONG).show();
        }
    }
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Toast.makeText(this, "R.string.fail", Toast.LENGTH_LONG).show();
    }
    @Override
    public void onConnectionSuspended(int cause) {
        // The connection to Google Play services was lost for some reason. We call connect() to
        // attempt to re-establish the connection.
        mGoogleApiClient.connect();
    }

    //-----Premission------
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_PERMISSIONS:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, ll);
                    }
                } else {
                    // Permission Denied
                    Toast.makeText(MapActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;

            case REQUEST_CODE_ASK_PERMISSIONS_c:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                            .getCurrentPlace(mGoogleApiClient, null);
                    result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                        @Override
                        public void onResult(PlaceLikelihoodBuffer likelyPlaces) {
                            for (PlaceLikelihood placeLikelihood : likelyPlaces) {


                                Toast.makeText(MapActivity.this, placeLikelihood.getPlace().getName(), Toast.LENGTH_SHORT).show();

                                break;
                            }
                            likelyPlaces.release();
                        }
                    });

                } else {
                    // Permission Denied
                    Toast.makeText(MapActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT)
                            .show();
                }
                break;




            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    //-----convert waypoints + call web service direction-----
    public void Direction() {
        if (mFrom != null && mTo != null) {


            String strWaypoints = "";

            for(int i = 0; i< waypoints.size(); i++)
            {
                Marker m = waypoints.get(i);
                strWaypoints += m.getPosition().latitude + "," + m.getPosition().longitude + "|";
            }
            if(!strWaypoints.isEmpty())
                strWaypoints = "&waypoints=" + "optimize:" + (optimize == true ? "true" : "false") + "|" + strWaypoints.substring(0,strWaypoints.length()-1);


            new CallWebService(mFrom.getPosition(), mTo.getPosition(), strWaypoints).execute();
        }
    }

    //-----onMapReady + move marker-----
    @Override
    public void onMapReady(GoogleMap googleMap) {
        ggMap = googleMap;

        ggMap.getUiSettings().setZoomControlsEnabled(true);
        ggMap.getUiSettings().setCompassEnabled(true);
        ggMap.getUiSettings().setMapToolbarEnabled(false);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            ggMap.setMyLocationEnabled(true);
        }else{
            Toast.makeText(MapActivity.this, "ACCESS_FINE_LOCATION Denied", Toast.LENGTH_SHORT).show();
        }



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

               // marker.setAnchor(0, 0);

            //    Toast.makeText(getApplicationContext(), marker.getId() + "---" + mFrom.getId(), Toast.LENGTH_LONG).show();

                Geocoder geocoder;
                List<Address> addresses = null;
                geocoder = new Geocoder(getApplicationContext(), Locale.getDefault());

                try {
                    addresses = geocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                } catch (IOException e) {
                    e.printStackTrace();
                }

                String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                if(marker.equals(mFrom))
                {
                    PlaceAutocompleteFragment autocompleteFragmentFrom = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.txtFrom);
                    autocompleteFragmentFrom.setText(address);

                }else if(marker.equals(mTo)){
                    PlaceAutocompleteFragment autocompleteFragmentTo = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(R.id.txtTo);
                    autocompleteFragmentTo.setText(address);
                }

                //String city = addresses.get(0).getLocality();
                //String state = addresses.get(0).getAdminArea();
                //String country = addresses.get(0).getCountryName();
                //String postalCode = addresses.get(0).getPostalCode();
                //String knownName = addresses.get(0).getFeatureName();




                marker.hideInfoWindow();
                marker.setTitle(address);
                marker.showInfoWindow();

                Direction();
            }
        });
    }

    //-----decodePoly-----
    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    //-----Call web service direction------
    class CallWebService extends AsyncTask<Void,Void,String> {
        private LatLng pIdFrom;
        private LatLng pIdTo;

        private String waypoints;


        public CallWebService(LatLng pIdFrom, LatLng pIdTo, String waypoints) {
            this.pIdFrom = pIdFrom;
            this.pIdTo = pIdTo;
            this.waypoints = waypoints;
        }

        @Override
        protected String doInBackground(Void... params) {

            //-----call webService map direction api
            String out = "";

            String ggDrtUrl = String.format("https://maps.googleapis.com/maps/api/directions/json?origin=%1$s,%2$s&destination=%3$s,%4$s%5$s&avoid=tolls|highways|ferries&key=%6$s"
                    , pIdFrom.latitude, pIdFrom.longitude, pIdTo.latitude, pIdTo.longitude, waypoints, "AIzaSyD9sw1nVib-8gsX7nEwgNKuW0y6vgUgv2c");

            URL url;
            HttpURLConnection urlConnection = null;
            try {
                url = new URL(ggDrtUrl);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in));
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = myReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                myReader.close();
                out = stringBuilder.toString();

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
                if (urlConnection != null)
                    urlConnection.disconnect();
            }

            return out;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null)
                DrawMap(result);
        }
    }

    //-----Draw-----
    private void DrawMap(String out) {
        if (line != null) {
            line.remove();
        }

        try {
            final PolylineOptions options = new PolylineOptions().width(5).color(Color.BLUE).geodesic(true);
            lblDistanceValue.setText("");
            lblTimeValue.setText("");
            lblOrder.setText("");

            final JSONObject json = new JSONObject(out);

            JSONObject routes = json.getJSONArray("routes").getJSONObject(0);
            JSONArray legs = routes.getJSONArray("legs");
            JSONArray waypoint_order = routes.getJSONArray("waypoint_order");

            for (int j = 0; j < waypoint_order.length() ; j++) {
                lblOrder.setText((lblOrder.getText() == "" ? "" : lblOrder.getText() + ", ") + (((int)(waypoint_order.get(j))+1)));
            }

            //loop way point
            for (int j = 0; j < legs.length() ; j++){
                JSONObject leg = legs.getJSONObject(j);

                JSONObject distance = leg.getJSONObject("distance");
                String distanceText = distance.getString("text");
                lblDistanceValue.setText((lblDistanceValue.getText() == "" ? "" : lblDistanceValue.getText() + " + ") + distanceText);

                JSONObject duration = leg.getJSONObject("duration");
                String durationText = duration.getString("text");
                lblTimeValue.setText((lblTimeValue.getText() == "" ? "" : lblTimeValue.getText() + " + ") + durationText);


                JSONArray steps = leg.getJSONArray("steps");


                //loop line
                for (int i = 0; i < steps.length(); i++) {
                    JSONObject polyline = steps.getJSONObject(i).getJSONObject("polyline");
                    String polylinePoints = polyline.getString("points");

                    List<LatLng> list = decodePoly(polylinePoints);

                    for (int z = 0; z < list.size(); z++) {
                        LatLng point = list.get(z);
                        options.add(point);
                    }

                }
            }

            line = ggMap.addPolyline(options);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


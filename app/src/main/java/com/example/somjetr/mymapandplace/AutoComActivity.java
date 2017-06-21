package com.example.somjetr.mymapandplace;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by somjet.r on 2017-05-31.
 */

public class AutoComActivity extends AppCompatActivity {

    private AutoCompleteTextView auto;
    private ArrayAdapter<MyLocation> adapter;
    private int i = 0;
    private Timer timer;
    private Handler handler;
    private MyLocation selectedItem;
    private TextView txtV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_autocom);


        //------link
        Button btnLink = (Button) findViewById(R.id.btnLink);
        btnLink.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                //ref:        https://developers.google.com/maps/documentation/urls/guide
                //            don't have parameter for avoid highways
                String uri = "https://www.google.com/maps/dir/?api=1&destination=13.696289,100.598914&dir_action=navigate&travelmode=driving";

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                startActivity(intent);

            }
        });

        txtV = (TextView) findViewById(R.id.txtV);



        auto = (AutoCompleteTextView)findViewById(R.id.auto);
        auto.setThreshold(1);//will start working from first character


        //-----------on select autocomplete
        auto.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                //for stop request google api. step after select item in list, 1 call afterTextChanged, 2 call setOnItemClickListener
                if(handler!=null)
                    handler.removeMessages(0);

                //if want to use auto.setText, must call stop request google api on next line
                //example
                //auto.setText("Connex b");
                //if(handler!=null)
                //    handler.removeMessages(0);


                selectedItem = (MyLocation) parent.getItemAtPosition(position);

                txtV.setText(selectedItem.text2);

                if(selectedItem.value.length() == 0) {
                    auto.setText("");
                    txtV.setText("");
                    return;
                }

                new CallWebServicePD().execute(selectedItem.value);
            }
        });

        //-----------on text change
        auto.addTextChangedListener(new TextWatcher() {

            @Override
            public void afterTextChanged(Editable s) {

                txtV.setText("");

                final CharSequence x = s;

                if(x.length()==0)
                    return;


                handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        i++;
                        Toast.makeText(AutoComActivity.this, Integer.toString(i) + " - " + x.toString(), Toast.LENGTH_SHORT).show();
                        new CallWebServicePA().execute(x);

                    }
                }, 1000);
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(handler!=null)
                    handler.removeMessages(0);
            }
        });

    }


    //-----Call web service place details------
    class CallWebServicePD extends AsyncTask<CharSequence,Void,String> {


        @Override
        protected String doInBackground(CharSequence... params) {

            //-----call webService map direction api
            String out = "";

            String ggDrtUrl = String.format("https://maps.googleapis.com/maps/api/place/details/json?placeid=%1$s&key=%2$s"
                    ,params[0], "AIzaSyD9sw1nVib-8gsX7nEwgNKuW0y6vgUgv2c");

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
            if (result != null) {
                SetDetail(result);
            }
        }
    }




    //-----Call web service place auto complete------
    class CallWebServicePA extends AsyncTask<CharSequence,Void,String> {


        @Override
        protected String doInBackground(CharSequence... params) {

            //-----call webService map direction api
            String out = "";

            String ggDrtUrl = String.format("https://maps.googleapis.com/maps/api/place/autocomplete/json?input=%1$s&components=country:th&key=%2$s"
                    ,params[0], "AIzaSyD9sw1nVib-8gsX7nEwgNKuW0y6vgUgv2c");

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
            if (result != null) {
                SetAddress(result);
            }
        }
    }


    //-----for web service Place Details
    public void SetDetail (String in){
        JSONObject json;
        try {
            json = new JSONObject(in);

            JSONObject result = json.getJSONObject("result");
            JSONObject geometry = result.getJSONObject("geometry");
            JSONObject location = geometry.getJSONObject("location");

            float lat = Float.valueOf(location.getString("lat"));
            float lng = Float.valueOf(location.getString("lng"));

            selectedItem.lat = lat;
            selectedItem.lng = lng;

            Toast.makeText(AutoComActivity.this, selectedItem.text1 +"\n"+ selectedItem.text2 + "\n" + selectedItem.value + "\n" + selectedItem.lat + "\n" + selectedItem.lng, Toast.LENGTH_LONG).show();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //-----for web service Place Autocomplete
    public void SetAddress (String in){
        JSONObject json;
        try {

            ArrayList<MyLocation> myList = new ArrayList<MyLocation>();




            json = new JSONObject(in);
            JSONArray predictions = json.getJSONArray("predictions");

            for (int j = 0; j < predictions.length() ; j++) {
                JSONObject prediction = predictions.getJSONObject(j);
                JSONObject structured_formatting = predictions.getJSONObject(j).getJSONObject("structured_formatting");
                //String description = prediction.getString("description");

                String main_text = structured_formatting.getString("main_text");
                String secondary_text = structured_formatting.getString("secondary_text");

                String place_id = prediction.getString("place_id");


                myList.add(new MyLocation(main_text,secondary_text, place_id));

            }

            if(myList.size()==0)
                myList.add(new MyLocation("Not Found", "try again", ""));


            adapter = new MyItemAdapter(this, R.layout.autocomplete_twoline, myList);

            auto.setAdapter(adapter);

            adapter.notifyDataSetChanged();



        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}

class MyLocation {
    public String text1;
    public String text2;
    public String value;
    public float lat;
    public float lng;

    public MyLocation(String text1, String text2, String value) {
        this.text1 = text1;
        this.text2 = text2;
        this.value = value;
    }

    @Override
    public String toString(){
        return text1;
    }
}

class MyItemAdapter extends ArrayAdapter<MyLocation> {
    private ArrayList<MyLocation> objects;

    public MyItemAdapter(Context context, int textViewResourceId, ArrayList<MyLocation> objects) {
        super(context, textViewResourceId, objects);
        this.objects = objects;
    }

    public View getView(int position, View convertView, ViewGroup parent){
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.autocomplete_twoline, null);

        MyLocation myLocation = objects.get(position);

        TextView txtV1 = (TextView) v.findViewById(R.id.txtV1);
        TextView txtV2 = (TextView) v.findViewById(R.id.txtV2);

        txtV1.setText(myLocation.text1);
        txtV2.setText(myLocation.text2);

        return v;
    }
}
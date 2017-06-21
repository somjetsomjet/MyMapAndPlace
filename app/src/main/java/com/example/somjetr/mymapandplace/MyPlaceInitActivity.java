package com.example.somjetr.mymapandplace;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.ui.PlacePicker;

/**
 * Created by somjet.r on 2017-05-25.
 */

public class MyPlaceInitActivity extends AppCompatActivity {
    private int MAP = 2;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myplaceinit);




        Button btnPlace = (Button) findViewById(R.id.btnPlace);
        btnPlace.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                Intent intent = new Intent(getApplicationContext(), MyPlaceMapActivity.class);
                startActivityForResult(intent, MAP);
            }
        });




    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {

        if (resultCode == Activity.RESULT_OK && requestCode == MAP)
        {
            double lat = (double) data.getExtras().get("location_lat");
            double lng = (double) data.getExtras().get("location_lng");
            String location_name = (String) data.getExtras().get("location_name");
            EditText txtPlace = (EditText) findViewById(R.id.txtPlace);
            txtPlace.setText(location_name);

            Toast.makeText(MyPlaceInitActivity.this, lat+","+lng, Toast.LENGTH_LONG).show();
        }else{
            super.onActivityResult(requestCode, resultCode, data);
        }
    }
}

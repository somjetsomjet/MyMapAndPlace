package com.example.somjetr.mymapandplace;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

/**
 * Created by somjet.r on 2017-05-25.
 */

public class MenuActivity extends AppCompatActivity {

    public static final String OMISE_PKEY = "pkey_test_589yy3jk6y6hp498vvl";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //load lang
        SharedPreferences settings = getSharedPreferences("MyApp", 0);
        String lang = settings.getString("lang", "").toString();
        if(lang == "")
            SetLang("th");
        else
            SetLang(lang);

        setContentView(R.layout.activity_menu);

        Button btnMap = (Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MapActivity.class);
                startActivity(i);
            }
        });

        Button btnPlace = (Button) findViewById(R.id.btnPlace);
        btnPlace.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PlaceActivity.class);
                startActivity(i);
            }
        });

        Button btnGps = (Button) findViewById(R.id.btnGps);
        btnGps.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GpsActivity.class);
                startActivity(i);
            }
        });

        Button btnPlaceCopy = (Button) findViewById(R.id.btnPlaceCopy);
        btnPlaceCopy.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), MyPlaceInitActivity.class);
                startActivity(i);
            }
        });

        Button btnSign = (Button) findViewById(R.id.btnSign);
        btnSign.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignatureActivity.class);
                startActivity(i);
            }
        });

        Button btnGpsShow = (Button) findViewById(R.id.btnGpsShow);
        btnGpsShow.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), GpsShowActivity.class);
                startActivity(i);
            }
        });

        Button btnAuto = (Button) findViewById(R.id.btnAuto);
        btnAuto.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), AutoComActivity.class);
                startActivity(i);
            }
        });

        Button btnCamera = (Button) findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), CameraActivity.class);
                startActivity(i);
            }
        });

        Button btnCamera2 = (Button) findViewById(R.id.btnCamera2);
        btnCamera2.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Camera2Activity.class);
                startActivity(i);
            }
        });

        Button btnBrowse = (Button) findViewById(R.id.btnBrowse);
        btnBrowse.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), BrowsePicAndResizeActivity.class);
                startActivity(i);
            }
        });


        Button btnCreditCard = (Button) findViewById(R.id.btnCreditCard);
        btnCreditCard.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), ChargeCreditCardActivity.class);
                startActivity(i);
            }
        });

        Button btnSprite = (Button) findViewById(R.id.btnSprite);
        btnSprite.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SpriteActivity.class);
                startActivity(i);
            }
        });



        //set button lang
        final Button btnLang = (Button) findViewById(R.id.btnLang);

        if(Locale.getDefault().toString().equals("th") || Locale.getDefault().toString().equals("th_TH")) {
            btnLang.setText("Thai");
        }else{
            btnLang.setText("English");
        }

        //switch lang
        btnLang.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                String languageToLoad = "";

                if(Locale.getDefault().toString().equals("th")  || Locale.getDefault().toString().equals("th_TH")) {
                    languageToLoad = "en"; // your language
                }else{
                    languageToLoad = "th"; // your language
                }

                SetLang(languageToLoad);
                recreate();
            }
        });

    }

    private void SetLang(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());

        Log.v("menu","set lang");

        //save lang
        SharedPreferences settings = getSharedPreferences("MyApp", 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString("lang",lang);
        editor.commit();
    }



}

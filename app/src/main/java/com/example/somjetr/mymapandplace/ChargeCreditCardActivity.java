package com.example.somjetr.mymapandplace;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import co.omise.android.models.Token;
import co.omise.android.ui.CreditCardActivity;

import static com.example.somjetr.mymapandplace.MenuActivity.OMISE_PKEY;

/**
 * Created by somjet.r on 2017-06-14.
 */

public class ChargeCreditCardActivity  extends AppCompatActivity {

    public static final int REQUEST_CC = 100;

    private String userId;
    private ArrayList<String> ddText = new ArrayList<String>();
    private ArrayList<String> ddValue = new ArrayList<String>();
    private ArrayAdapter<String> adapter;
    private Spinner spnCardList;
    private String selected;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chargecreditcard);

        TextView txtUserId = (TextView) findViewById(R.id.txtUserId);
        userId = txtUserId.getText().toString();

        Button btnAddCard = (Button) findViewById(R.id.btnAddCard);
        btnAddCard.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent(ChargeCreditCardActivity.this, CreditCardActivity.class);
                intent.putExtra(CreditCardActivity.EXTRA_PKEY, OMISE_PKEY);
                startActivityForResult(intent, REQUEST_CC);
            }
        });

        Button btnDelCard = (Button) findViewById(R.id.btnDelCard);
        btnDelCard.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {

                new DelCreditCard().execute();
            }
        });

        new LoadCreditCardList().execute();


//        ArrayAdapter<String> SpinerAdapter;
//        String[] arrayItems = {"Strawberry","Chocolate","Vanilla"};
//        final int[] actualValues={10,20,30};
//
//        SpinerAdapter = new ArrayAdapter<String>(this,
//                android.R.layout.simple_spinner_dropdown_item, arrayItems);


        spnCardList = (Spinner) findViewById(R.id.spnCardList);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, ddText);
        spnCardList.setAdapter(adapter);


        spnCardList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                selected = ddValue.get(arg2);
                Toast.makeText(ChargeCreditCardActivity.this, "Select " + ddText.get(arg2) + " --- " + selected, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
                // TODO Auto-generated method stub

            }
        });

        final EditText txtAmount = (EditText) findViewById(R.id.txtAmount);

        Button btnCharge = (Button) findViewById(R.id.btnCharge);
        btnCharge.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new ChargeCustomer().execute(Double.parseDouble(txtAmount.getText().toString()));
            }
        });

        Button btnCharge3ds = (Button) findViewById(R.id.btnCharge3ds);
        btnCharge3ds.setOnClickListener(new Button.OnClickListener() {
            public void onClick(View v) {
                new ChargeCustomer3ds().execute(Double.parseDouble(txtAmount.getText().toString()));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CC:
                if (resultCode == CreditCardActivity.RESULT_CANCEL) {
                    Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
                    return;
                }

                //create customer
                Token token = data.getParcelableExtra(CreditCardActivity.EXTRA_TOKEN_OBJECT);
                new CreateCustomer().execute(token);


                //reload page for new dropdown

                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
        }
    }

    class LoadCreditCardList extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... Void) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.30:31956/api/Payment/PostGetCreditCardList");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("userId", userId);


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

                ddText = new ArrayList<String>();
                ddValue = new ArrayList<String>();

                if(objOut.get("status").toString().equals("ok")) {

                    JSONArray jList = (JSONArray)objOut.get("list");

                    for (int i = 0; i < jList.length(); i++) {
                        String value = jList.getJSONObject(i).getString("omiseCardId");
                        String text = jList.getJSONObject(i).getString("creditCardName");

                        ddText.add(text);
                        ddValue.add(value);
                    }







                    return objOut.get("status").toString();



                }
                else
                    return objOut.get("status").toString() + ", " + objOut.get("msg").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            adapter = new ArrayAdapter<String>(ChargeCreditCardActivity.this, android.R.layout.simple_spinner_dropdown_item, ddText);
            spnCardList.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            Toast.makeText(ChargeCreditCardActivity.this, "Load Credit card list is " + result, Toast.LENGTH_SHORT).show();
        }

    }

    class CreateCustomer extends AsyncTask<Token, Void, String> {

        @Override
        protected String doInBackground(Token... token) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.30:31956/api/Payment/PostAddCreditCard");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("userId", userId);
                obj.put("token", token[0].id);


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

                if(objOut.get("status").toString().equals("ok"))
                    return objOut.get("status").toString();
                else
                    return objOut.get("status").toString() + ", " + objOut.get("msg").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(ChargeCreditCardActivity.this, "Add Credit card is " + result, Toast.LENGTH_SHORT).show();

            new LoadCreditCardList().execute();

        }

    }

    class ChargeCustomer3ds extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... amount) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.30:31956/api/Payment/PostChargeCreditCard3ds");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("userId", userId);
                obj.put("omiseCardId", selected);
                obj.put("amount", amount[0]);

                Random r = new Random();
                int orderNo = r.nextInt(999999999);
                obj.put("orderNo", orderNo);


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

                if(objOut.get("status").toString().equals("ok"))
                    return objOut.get("urlBank").toString();
                else
                    return objOut.get("status").toString() + ", " + objOut.get("msg").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            //Toast.makeText(ChargeCreditCardActivity.this, "Charge Credit card 3ds is " + result, Toast.LENGTH_SHORT).show();
            final WebView wb = (WebView) findViewById(R.id.webView);
            //wb.setWebViewClient(new WebViewClient());

            final TextView txtUrl = (TextView) findViewById(R.id.txtUrl);

            txtUrl.setText(result);

            wb.setWebViewClient(new WebViewClient() {


                @Override
                public boolean shouldOverrideUrlLoading(WebView view, String url) {
                    txtUrl.setText(url);

                    if(url.equals("http://192.168.0.30:31956/PaymentComplete/Index")){

                        final Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                wb.loadUrl("about:blank");
                                txtUrl.setText("");
                            }
                        }, 3000);

                    }


                    // return true; //Indicates WebView to NOT load the url;
                    return false; //Allow WebView to load url
                }
            });

            wb.getSettings().setJavaScriptEnabled(true);
            wb.loadUrl(result);//(result);

        }

    }

    class ChargeCustomer extends AsyncTask<Double, Void, String> {

        @Override
        protected String doInBackground(Double... amount) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.30:31956/api/Payment/PostChargeCreditCard");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("userId", userId);
                obj.put("omiseCardId", selected);
                obj.put("amount", amount[0]);

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

                if(objOut.get("status").toString().equals("ok"))
                    return objOut.get("status").toString();
                else
                    return objOut.get("status").toString() + ", " + objOut.get("msg").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(ChargeCreditCardActivity.this, "Charge Credit card is " + result, Toast.LENGTH_SHORT).show();


        }

    }

    class DelCreditCard extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... Void) {

            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.30:31956/api/Payment/PostDelCreditCard");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");


                DataOutputStream os = new DataOutputStream(conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("userId", userId);
                obj.put("omiseCardId", selected);

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

                if(objOut.get("status").toString().equals("ok"))
                    return objOut.get("status").toString();
                else
                    return objOut.get("status").toString() + ", " + objOut.get("msg").toString();
            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(ChargeCreditCardActivity.this, "Delete Credit card is " + result, Toast.LENGTH_SHORT).show();
            new LoadCreditCardList().execute();

        }

    }

}
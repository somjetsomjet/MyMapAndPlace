package com.example.somjetr.mymapandplace;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by somjet.r on 2017-05-30.
 */

public class SignatureActivity  extends AppCompatActivity {
    Signature signature;
    Paint paint;
    LinearLayout mySignature;
    Button btnClear;
    Button btnSave;
    ImageView imgV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signature);

        btnSave = (Button) findViewById(R.id.btnSave);
        btnClear = (Button) findViewById(R.id.btnClear);
        mySignature = (LinearLayout) findViewById(R.id.mySignature);
        imgV = (ImageView) findViewById(R.id.imgV);

        signature = new Signature(this, null);
        mySignature.addView(signature);

        btnSave.setOnClickListener(onButtonClick);
        btnClear.setOnClickListener(onButtonClick);

    }

    Button.OnClickListener onButtonClick = new Button.OnClickListener() {
        @Override
        public void onClick(View v) {
            // TODO Auto-generated method stub
            if (v == btnClear) {
                signature.clear();
            } else if (v == btnSave) {
                signature.save();
            }
        }
    };

    public class Signature extends View {
        static final float STROKE_WIDTH = 10f;
        static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        Paint paint = new Paint();
        Path path = new Path();

        float lastTouchX;
        float lastTouchY;
        final RectF dirtyRect = new RectF();

        public Signature(Context context, AttributeSet attrs) {
            super(context, attrs);

            this.setBackgroundResource(R.drawable.note);
            this.getBackground().setAlpha(50);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLUE);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        public void save() {
            Bitmap returnedBitmap = Bitmap.createBitmap(mySignature.getWidth(),
                    mySignature.getHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(returnedBitmap);
            Drawable bgDrawable = mySignature.getBackground();
            if (bgDrawable != null)
                bgDrawable.draw(canvas);
            else
                canvas.drawColor(Color.WHITE);
            mySignature.draw(canvas);

            ByteArrayOutputStream bs = new ByteArrayOutputStream();
            returnedBitmap.compress(Bitmap.CompressFormat.PNG, 50, bs);
            Intent intent = new Intent();
            intent.putExtra("byteArray", bs.toByteArray());
            setResult(1, intent);



            //save sd card
            new SaveImage("fn "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())).execute(bs.toByteArray());

            //upload
            new UploadImage("fn "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())).execute(bs.toByteArray());

            //download
            new DownloadImage("fn "+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime())).execute();



            //-----save
//            Bitmap b = BitmapFactory.decodeByteArray(
//                                            intent.getByteArrayExtra("byteArray"),
//                                            0,
//                                            intent.getByteArrayExtra("byteArray").length);
//            imgV.setImageBitmap(b);
//
//
//            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
//            connection.setDoOutput(true);
//            connection.setRequestProperty("Content-Type", "image/jpeg");
//            connection.setRequestMethod(method.toString());
//            OutputStream outputStream = connection.getOutputStream();
//
//            ByteArrayOutputStream bos = new ByteArrayOutputStream(outputStream);
//            bitmap.compress(CompressFormat.JPEG, 100, bos);
//
//            bout.close();
//            outputStream.close();
//
//
//
//



            //finish();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    class SaveImage extends AsyncTask<byte[], String, String> {

        String fileName = "";
        public SaveImage(String fileName){
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(byte[]... jpeg) {
            File photo=new File(Environment.getExternalStorageDirectory(), fileName+".jpeg");

            if (photo.exists()) {
                photo.delete();
            }
            String ab = "";
            try {



                if (ContextCompat.checkSelfPermission(SignatureActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED
                        || ContextCompat.checkSelfPermission(SignatureActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)  != PackageManager.PERMISSION_GRANTED
                        ) {
                                        ActivityCompat.requestPermissions(SignatureActivity.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 123);
                    return null;
                }



                FileOutputStream fos=new FileOutputStream(photo.getPath());

                fos.write(jpeg[0]);
                fos.close();

                ab = photo.getAbsolutePath();
                String cc = photo.getCanonicalPath();

                //Toast.makeText(SignatureActivity.this, "Save Complete a " + ab , Toast.LENGTH_SHORT).show();
               // Toast.makeText(SignatureActivity.this, "Save Complete c" + cc, Toast.LENGTH_SHORT).show();
            }
            catch (java.io.IOException e) {
                Toast.makeText(SignatureActivity.this, "error" + e.getMessage(), Toast.LENGTH_LONG).show();
            }

            return(ab);
        }


        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            Toast.makeText(SignatureActivity.this, "Save Complete. " + result, Toast.LENGTH_SHORT).show();
        }
    }

    class UploadImage extends AsyncTask<byte[], String, String> {

        String fileName = "";
        public UploadImage(String fileName){
            this.fileName = fileName;
        }

        @Override
        protected String doInBackground(byte[]... jpeg) {


            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.34:29875/api/My/Post123");


                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
//                conn.setReadTimeout(10000);
//                conn.setConnectTimeout(15000);
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Content-Type", "application/json");




                DataOutputStream os = new DataOutputStream (conn.getOutputStream());


                JSONObject obj = new JSONObject();
                obj.put("fileName" , fileName+".jpeg");
                obj.put("dataString" ,  Base64.encodeToString(jpeg[0],Base64.DEFAULT));

                os.writeBytes(obj.toString());

                os.flush();
                os.close();

                conn.connect();




                JSONObject objOut = null;

                int responseCode1 = conn.getResponseCode();
                if(responseCode1 == HttpURLConnection.HTTP_OK){


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

            Toast.makeText(SignatureActivity.this, "Upload Complete. " + result, Toast.LENGTH_SHORT).show();
        }
    }

    class DownloadImage extends AsyncTask<Void, String, Bitmap> {

        String fileName = "";
        public DownloadImage(String fileName){
            this.fileName = fileName;
        }

        @Override
        protected Bitmap doInBackground(Void... params) {


            int responseCode;
            String response;
            URL url = null;
            try {
                url = new URL("http://192.168.0.34:29875/api/My/Get123?fileName=" + fileName + ".jpeg");


                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader myReader = new BufferedReader(new InputStreamReader(in));
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((receiveString = myReader.readLine()) != null) {
                    stringBuilder.append(receiveString);
                }
                myReader.close();
                String out = stringBuilder.toString();


                JSONObject objOut = new JSONObject(out);
                byte[] bOut =  Base64.decode( objOut.get("dataString").toString(), Base64.DEFAULT);


                Bitmap b = BitmapFactory.decodeByteArray(bOut, 0, bOut.length);


                return b;

            } catch (Exception e) {
                e.printStackTrace();
            }


            return null;
        }


        @Override
        protected void onPostExecute(Bitmap result) {
            super.onPostExecute(result);
            imgV.setImageBitmap(result);
            Toast.makeText(SignatureActivity.this, "Download Complete.", Toast.LENGTH_SHORT).show();
        }
    }

}

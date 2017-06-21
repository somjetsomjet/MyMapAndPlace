package com.example.somjetr.mymapandplace;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by somjet.r on 2017-06-02.
 */

public class BrowsePicAndResizeActivity extends AppCompatActivity {

    String picturePath = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browsepic);

        Button btnBrowse = (Button) findViewById(R.id.btnBrowse);
        btnBrowse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {


                Intent i = new Intent(Intent.ACTION_GET_CONTENT,null);
                //i.setType("*/*");

                i.setType("image/*");


                //Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, 123);
            }
        });

        Button btnResize = (Button) findViewById(R.id.btnResize);
        btnResize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                //<100kb
                byte[] byteArray = ResizeImg(null,100, picturePath);

                Bitmap b = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length) ;

                ImageView imgViewRe = (ImageView) findViewById(R.id.imgViewRe);
                imgViewRe.setImageBitmap(b);



                File photo=new File(Environment.getExternalStorageDirectory(), "123"+".jpeg");
                FileOutputStream out = null;
                try {


                    out = new FileOutputStream(photo.getPath());
                    out.write(byteArray);
                    out.close();

//                    out = new FileOutputStream(photo.getPath());
//                    b.compress(Bitmap.CompressFormat.JPEG, 100, out);
//
//                    out.close();
                } catch (Exception e) {
                    Toast.makeText(BrowsePicAndResizeActivity.this, "error", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }

                Toast.makeText(BrowsePicAndResizeActivity.this, "Save Complete. " + photo.getPath(), Toast.LENGTH_SHORT).show();


                //-----file size
//                ByteArrayOutputStream stream = new ByteArrayOutputStream();
//                b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                byte[] imageInByte = stream.toByteArray();
//                long lengthbmp = imageInByte.length;


                //-----file size
                File file = new File(photo.getPath());
                long length = file.length() / 1024; // Size in KB

                Toast.makeText(BrowsePicAndResizeActivity.this, "After: " +  length +" KB", Toast.LENGTH_LONG).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 123 && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };
            Cursor cursor = null;

            //recent
            if (selectedImage.getHost().contains("com.android.providers.media")) {
                String wholeID = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {
                    wholeID = DocumentsContract.getDocumentId(selectedImage);
                }
                String id = wholeID.split(":")[1];
                String sel = MediaStore.Images.Media._ID + "=?";

                cursor = getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        filePathColumn, sel, new String[]{id}, null);
            }else {
                cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
            }

            cursor.moveToFirst();

            int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
            picturePath = cursor.getString(columnIndex);
            cursor.close();





            Bitmap b = BitmapFactory.decodeFile(picturePath);
            if(b == null) {
                Toast.makeText(BrowsePicAndResizeActivity.this, "Error, please select only picture file.", Toast.LENGTH_LONG).show();
                return;
            }


            ImageView imageView = (ImageView) findViewById(R.id.imgView);
            imageView.setImageBitmap(b);


            //-----file size
            File file = new File(picturePath);
            long length = file.length() / 1024; // Size in KB

            Toast.makeText(BrowsePicAndResizeActivity.this, "Before: " +  length +" KB", Toast.LENGTH_LONG).show();
        }
    }




    public static byte[] ResizeImg(Bitmap b, int requireKb, String picturePath){

        int quality = 101;
        byte[] imageInByte = null;
        ByteArrayOutputStream stream = null;


        if(picturePath.length() > 0){
            File file = new File(picturePath);
            long length = file.length() / 1024; // Size in KB
            if(length <= requireKb){
                int size = (int) file.length();
                byte[] bytes = new byte[size];

                BufferedInputStream buf = null;
                try {
                    buf = new BufferedInputStream(new FileInputStream(file));
                    buf.read(bytes, 0, bytes.length);
                    buf.close();
                    return bytes;
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else {
                b = BitmapFactory.decodeFile(picturePath);
            }
        }


        //quality
        do {
            quality--;
            stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, quality, stream);
            imageInByte = stream.toByteArray();
        } while (imageInByte.length/1024 > requireKb && quality != 0);

        //scale
        if(imageInByte.length/1024 > requireKb) {
            stream = new ByteArrayOutputStream();
            b.compress(Bitmap.CompressFormat.JPEG, 100, stream);
            imageInByte = stream.toByteArray();

            int scale = 1;

            do {
                scale++;
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inSampleSize = scale;
                Bitmap newB = BitmapFactory.decodeByteArray(imageInByte, 0, imageInByte.length, o);


                stream = new ByteArrayOutputStream();
                newB.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                imageInByte = stream.toByteArray();

            } while (imageInByte.length / 1024 > requireKb);
        }

        return imageInByte;






//            try {
//                // Decode image size
//                BitmapFactory.Options o = new BitmapFactory.Options();
//                o.inJustDecodeBounds = true;
//                BitmapFactory.decodeStream(new FileInputStream(f), null, o);
//
//                // Find the correct scale value. It should be the power of 2.
//                int scale = 1;
//                while(o.outWidth / scale / 2 >= REQUIRED_SIZE &&
//                        o.outHeight / scale / 2 >= REQUIRED_SIZE) {
//                    scale *= 2;
//                }
//
//                // Decode with inSampleSize
//                BitmapFactory.Options o2 = new BitmapFactory.Options();
//                o2.inSampleSize = scale;
//
//
//                //test
//
//                Bitmap a =  BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
//                int w = a.getWidth();
//                int h = a.getHeight();
//
//                return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
//            } catch (FileNotFoundException e) {}
//            return null;


    }


}

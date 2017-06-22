//http://www.edu4java.com/en/androidgame/androidgame4.html

package com.example.somjetr.mymapandplace;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by somjet.r on 2017-06-22.
 */

public class SpriteActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sprite);

        Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.bad1);

        int width = bmp.getWidth() / 3;
        int height = bmp.getHeight() /4;

        Bitmap new1 = Bitmap.createBitmap(bmp, 0, 0, width, height);
        Bitmap new2 = Bitmap.createBitmap(bmp, dpToPx(32), dpToPx(32), width, height);

        ImageView   imgV2 = (ImageView) findViewById(R.id.imgV2);
        imgV2.setImageBitmap(new1);

        ImageView   imgV3 = (ImageView) findViewById(R.id.imgV3);
        imgV3.setImageBitmap(new2);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

}

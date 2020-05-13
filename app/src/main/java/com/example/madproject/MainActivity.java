package com.example.madproject;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.util.SparseArray;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.text.TextBlock;
import com.google.android.gms.vision.text.TextRecognizer;

import java.io.FileNotFoundException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int requestPermissionID = 101;
    private final int select_photo = 1;
    SurfaceView mCameraView;
    TextView mTextView;
    ImageView gallery_image;
    Button selectImg;
    TextRecognizer textRecognizer;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        selectImg = findViewById(R.id.buttonSelect);
        textRecognizer = new TextRecognizer.Builder(getApplicationContext()).build();
        gallery_image = (ImageView) findViewById(R.id.gallery_imageview);
        mTextView = findViewById(R.id.text_view);
        selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Intent to gallery
                Intent in = new Intent(Intent.ACTION_PICK);
                in.setType("image/*");
                startActivityForResult(in, select_photo);
            }
        });
//        startCameraSource();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != requestPermissionID) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            try {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    public static Bitmap decodeUri(Context context, Uri uri,
                                   final int requiredSize) throws FileNotFoundException {
        BitmapFactory.Options o = new BitmapFactory.Options();
        o.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, o);

        int width_tmp = o.outWidth, height_tmp = o.outHeight;
        int scale = 1;

        while (true) {
            if (width_tmp / 2 < requiredSize || height_tmp / 2 < requiredSize)
                break;
            width_tmp /= 2;
            height_tmp /= 2;
            scale *= 2;
        }

        BitmapFactory.Options o2 = new BitmapFactory.Options();
        o2.inSampleSize = scale;
        return BitmapFactory.decodeStream(context.getContentResolver()
                .openInputStream(uri), null, o2);
    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null,
                    null, null);
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    protected void onActivityResult(int requestcode, int resultcode,
                                    Intent imagereturnintent) {
        super.onActivityResult(requestcode, resultcode, imagereturnintent);
        switch (requestcode) {
            case select_photo:
                if (resultcode == RESULT_OK) {
                    try {

                        Uri imageuri = imagereturnintent.getData();

                        Bitmap bitmap = decodeUri(MainActivity.this, imageuri, 300);
                        if (bitmap != null) {
                            gallery_image.setImageBitmap(bitmap);
                            Frame outputFrame = new Frame.Builder().setBitmap(bitmap).build();
                            SparseArray<TextBlock> text=textRecognizer.detect(outputFrame);
                            for (int i = 0; i < text.size(); ++i) {
                                TextBlock item = text.valueAt(i);
                                if (item != null && item.getValue() != null) {
                                    mTextView.setText(item.getValue());
                                }
                            }
                        }

                        else
                            Toast.makeText(MainActivity.this,
                                    "Error while decoding image.",
                                    Toast.LENGTH_SHORT).show();
                    } catch (FileNotFoundException e) {

                        e.printStackTrace();
                        Toast.makeText(MainActivity.this, "File not found.",
                                Toast.LENGTH_SHORT).show();
                    }
                }
        }
    }
}
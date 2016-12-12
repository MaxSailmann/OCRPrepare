package com.example.sailmannma54177.ocrprepare;

import android.app.Activity;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

import static java.lang.Math.sqrt;


/**
 * Created by sailmannma54177 on 28.11.2016.
 */

public class Kamera extends Activity implements SurfaceHolder.Callback {

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Bitmap bitmap = null;
    boolean previewing = false;
    Button takepicture;

    private TessBaseAPI mTess;
    String datapath = "";
    private GoogleApiClient client;

    private int displaywidth;
    private int displayheight;

    private int cardwidth;
    private int cardheight;


    private int textviewwidth;
    private int textviewheight;

    private String debugstring;
    int testcounter = 0;

    int top_canvas;
    int bottom_canvas;
    int left_canvas;
    int right_canvas;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.kamera);
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
        cameraInitialisation();
    }

    Camera.ShutterCallback myShutterCallback = new Camera.ShutterCallback() {
        @Override
        public void onShutter() {

        }
    };
    Camera.PictureCallback myPictureCallback_RAW = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

        }
    };
    Camera.PictureCallback myPictureCallback_JPG = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] bytes, Camera camera) {

            bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);


            //Rotate Bitmap 90°
            /*
            Matrix matrix = new Matrix();
            matrix.postRotate(90);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);*/

            truncateBitmap();

            mTess = new TessBaseAPI();
            datapath = getFilesDir() + "/tesseract/";
            checkFile(new File(datapath + "tessdata/"));
            String language = "eng";
            mTess.init(datapath, language);


            BitmapDrawable ob = new BitmapDrawable(getResources(), bitmap);

            ViewGroup container = (ViewGroup) findViewById(R.id.container);
            container.removeAllViews();
            container.addView(getLayoutInflater().inflate(R.layout.frame_result, null));

            TextView textView = (TextView) findViewById(R.id.ocrview);
            //textView.setText(debugstring);

            ImageView imageView = (ImageView) findViewById(R.id.imageview);
            imageView.setBackgroundDrawable(ob);
            findViewById(R.id.newpicture).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    cameraInitialisation();
                }
            });
            findViewById(R.id.startOcr).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mTess.setImage(bitmap);
                    String OCResult = mTess.getUTF8Text();
                    TextView OCRTextView = (TextView) findViewById(R.id.ocrview);
                    OCRTextView.setText(OCResult);
                }
            });
        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int heigth) {
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }
        if (camera != null) {
            try {
                //camera.setDisplayOrientation(90);
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {

            }
        });
        camera.startPreview();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    public void cameraInitialisation() {

        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.frame_kamera, null));
        getWindow().setFormat(PixelFormat.UNKNOWN);

        TextView right = (TextView) findViewById(R.id.testviewright);
        TextView left = (TextView) findViewById(R.id.textviewleft);
        TextView top = (TextView) findViewById(R.id.textviewtop);
        //TextView bottom = (TextView) findViewById(R.id.textviewbottom);

        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayheight = dm.heightPixels;
        displaywidth = dm.widthPixels;

        surfaceView = (SurfaceView) findViewById(R.id.surfaceview);

        calculateTextViewSize();

        right.setWidth(textviewwidth);
        left.setWidth(textviewwidth);
        top.setHeight(textviewheight);
        //bottom.setHeight(textviewheight);

        cardwidth = displaywidth - textviewwidth - textviewwidth;
        cardheight = (int) (cardwidth * 1.396825);


        left_canvas = textviewwidth + (int) (0.063492 * cardwidth);
        right_canvas = textviewwidth + (int) (0.936507 * cardwidth);
        top_canvas = textviewheight + (int) (0.0454545 * cardheight);
        bottom_canvas = textviewheight + (int) (0.090909 * cardheight);


        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        FocusView focusView = new FocusView(this, top_canvas, bottom_canvas, left_canvas, right_canvas);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.focusframe);
        frameLayout.addView(focusView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


        takepicture = (Button) findViewById(R.id.capture);
        takepicture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.takePicture(myShutterCallback, myPictureCallback_RAW, myPictureCallback_JPG);
            }
        });

        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.autofocusable);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takepicture.setEnabled(false);
                camera.autoFocus(myAutoFocusCallback);
            }
        });

    }

    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
            takepicture.setEnabled(true);
        }
    };

    private void copyFile() {
        try {
            String filepath = datapath + "/tessdata/eng.traineddata";
            AssetManager assetManager = getAssets();
            InputStream inputStream = assetManager.open("tessdata/eng.traineddata");
            OutputStream outputStream = new FileOutputStream(filepath);

            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
            outputStream.flush();
            outputStream.close();
            inputStream.close();

            File file = new File(filepath);
            if (!file.exists()) {
                throw new FileNotFoundException();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkFile(File dir) {

        if (!dir.exists() && dir.mkdirs()) {
            copyFile();
        }
        if (dir.exists()) {
            String datafilepath = datapath + "/tessdata/eng.traineddata";
            File datafile = new File(datafilepath);
            if (!datafile.exists()) {
                copyFile();
            }
        }
    }

    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void calculateTextViewSize() {
        textviewwidth = (int) (displaywidth * 0.1);
        textviewheight = (int) (displayheight * 0.2);
    }

    public void truncateBitmap() {

        int start_width;
        int start_height;
        int end_width;
        int end_height;

        double resolutionfactor;


        bitmap = Bitmap.createScaledBitmap(bitmap, 1200, 900, false);


        Log.d("Displaymetrics", "Height = " + displayheight);
        Log.d("Displaymetrics", "Width = " + displaywidth);
        Log.d("Bitmapmetrics", "Height = " + bitmap.getHeight());
        Log.d("Bitmapmetrics", "Width = " + bitmap.getWidth());

        resolutionfactor = (double) (displaywidth) / (double) (bitmap.getWidth());

        Log.d("ResolutionFactor", "Factor = " + resolutionfactor);

        start_width = (int) (left_canvas / resolutionfactor);
        end_width = (int) ((right_canvas - left_canvas) / resolutionfactor);

        int test = (bitmap.getHeight() - (int) (displayheight / resolutionfactor)) / 2;

        start_height = test + (int) (top_canvas / resolutionfactor);
        end_height = (int) ((bottom_canvas - top_canvas) / resolutionfactor);


        Log.d("Startpunkte", "start_height = " + start_height);
        Log.d("Startpunkte", "start_width = " + start_width);
        Log.d("Endpunkte", "end_height = " + end_height);
        Log.d("Endpunkte", "end_width = " + end_width);


        debugstring = "DisplayHeight = " + displayheight + "\nDisplayWidth = " + displaywidth + "\nBitmapHeigt = " + bitmap.getHeight() + "\nBitmapWidth = " + bitmap.getWidth() + "\nStartHeight = " + start_height + "\n EndHeight = " + end_height + "\nStartWidth = " + start_width + "\nEndWidth = " + end_width;


        bitmap = Bitmap.createBitmap(bitmap, start_width, start_height, end_width, end_height);
        bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        Log.d("Bitmapmetrics", "Height = " + bitmap.getHeight());
        Log.d("Bitmapmetrics", "Width = " + bitmap.getWidth());

        double scale = bitmap.getWidth() / 240;


        //bitmap = Bitmap.createScaledBitmap(bitmap,240,(int)(bitmap.getHeight()/scale),false);


        Log.d("Bitmapmetrics", "Height = " + bitmap.getHeight());
        Log.d("Bitmapmetrics", "Width = " + bitmap.getWidth());


        //pixelTest();
    }


    private void pixelTest() {
        int width = bitmap.getHeight();
        int height = bitmap.getWidth();
        int counter = 0;

        Bitmap mutableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

        for (int i = 0; i < width; i++) {
            for (int n = 0; n < height; n++) {
                int pixel = bitmap.getPixel(n, i);
                int red = Color.red(pixel);
                int blue = Color.blue(pixel);
                int green = Color.green(pixel);

                if (red < 150 && blue < 150 && green < 150) {
                    Log.d("Pixel no " + counter, "isBlack == True");
                    mutableBitmap.setPixel(n, i, Color.BLACK);
                } else {
                    mutableBitmap.setPixel(n, i, Color.WHITE);
                }

                counter++;

            }

            bitmap = Bitmap.createBitmap(mutableBitmap);


        }
    }
}

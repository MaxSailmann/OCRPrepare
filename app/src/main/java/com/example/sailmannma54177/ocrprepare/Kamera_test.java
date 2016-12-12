package com.example.sailmannma54177.ocrprepare;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sailmannma54177 on 08.12.2016.
 */

public class Kamera_test extends Activity implements SurfaceHolder.Callback {

    private Camera camera = null;
    private SurfaceHolder holder = null;

    private TessBaseAPI mTess;
    String datapath = "";
    String OCResult;

    private ArrayList<String> results = new ArrayList<>();

    private Handler h;
    private Runnable runnable;

    private int preview_width;
    private int preview_height;

    private int displaywidth;
    private int displayheight;

    private Bitmap preview;

    private boolean startOCR = false;

    private Button button1;
    private Button button2;
    private Button button3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //Ansicht initialisieren
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.kamera);

        //Displaymetrics speichern
        DisplayMetrics dm = getResources().getDisplayMetrics();
        displayheight = dm.heightPixels;
        displaywidth = dm.widthPixels;

        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.frame_kamera, null));

        //SurfaceView initialisieren
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Autofocus implementieren
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.autofocusable);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(myAutoFocusCallback);
            }
        });

        //BitMap speichern
        findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startOCR) {
                    startOCR = false;
                    if (h != null) {
                        h.removeCallbacks(runnable);
                        Log.d("Test", "Runnable stopped");
                    }
                    ViewGroup container = (ViewGroup) findViewById(R.id.container);
                    container.removeAllViews();
                    container.addView(getLayoutInflater().inflate(R.layout.frame_result, null));

                    BitmapDrawable ob = new BitmapDrawable(getResources(), preview);
                    ImageView imageView = (ImageView) findViewById(R.id.imageview);
                    imageView.setBackgroundDrawable(ob);

                    Log.d("Bitmapmetrics", "Height = " + preview.getHeight() + " Width = " + preview.getWidth());

                    TextView textView = (TextView) findViewById(R.id.ocrview);
                    textView.setText("Height = " + preview.getHeight() + " Width = " + preview.getWidth() + "\n" + OCResult);

                    findViewById(R.id.startOcr).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                            String query = OCResult;
                            String[] splited = query.split("\\s+");
                            StringBuffer search = new StringBuffer();

                            for (String test : splited) {
                                search.append(test).append("+");
                                Log.d("Test", test);
                            }

                            String final_url = url + search.toString().substring(0, search.toString().length() - 1);
                            Log.d("URL", search.toString().substring(0, search.toString().length() - 1));
                            Uri uri = Uri.parse(final_url);
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                        }
                    });
                }
            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startOCR) {
                    startOCR = false;
                    h.removeCallbacks(runnable);
                    Log.d("startOCR", "false");


                } else {
                    startOCR = true;
                    Log.d("startOCR", "true");

                    h = new Handler();
                    final int delay = 1000; //milliseconds

                    h.postDelayed(new Runnable() {
                        public void run() {
                            runnable = this;
                            callOCR();
                            h.postDelayed(this, delay);
                        }
                    }, delay);
                }
            }
        });

        //OCR initialisieren
        mTess = new TessBaseAPI();
        datapath = getFilesDir() + "/tesseract/";
        checkFile(new File(datapath + "tessdata/"));
        String language = "eng";
        mTess.init(datapath, language);
    }


    @Override
    protected void onResume() {
        super.onResume();

        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        container.removeAllViews();
        container.addView(getLayoutInflater().inflate(R.layout.frame_kamera, null));

        //SurfaceView initialisieren
        final SurfaceView surfaceView = (SurfaceView) findViewById(R.id.surfaceview);
        holder = surfaceView.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        //Autofocus implementieren
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.autofocusable);
        relativeLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                camera.autoFocus(myAutoFocusCallback);
            }
        });

        //BitMap speichern
        findViewById(R.id.capture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (h != null) {
                    h.removeCallbacks(runnable);
                    Log.d("Test", "Runnable stopped");
                }

                if (startOCR) {
                    startOCR = false;
                    ViewGroup container = (ViewGroup) findViewById(R.id.container);
                    container.removeAllViews();
                    container.addView(getLayoutInflater().inflate(R.layout.frame_result, null));

                    BitmapDrawable ob = new BitmapDrawable(getResources(), preview);
                    ImageView imageView = (ImageView) findViewById(R.id.imageview);
                    imageView.setBackgroundDrawable(ob);

                    Log.d("Bitmapmetrics", "Height = " + preview.getHeight() + " Width = " + preview.getWidth());

                    TextView textView = (TextView) findViewById(R.id.ocrview);
                    textView.setText("Height = " + preview.getHeight() + " Width = " + preview.getWidth() + "\n" + OCResult);

                    if (results.size() >= 3) {
                        button1 = (Button) findViewById(R.id.button1);
                        button2 = (Button) findViewById(R.id.button2);
                        button3 = (Button) findViewById(R.id.button3);

                        button3.setText(results.get(results.size() - 1));
                        button2.setText(results.get(results.size() - 2));
                        button1.setText(results.get(results.size() - 3));

                        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                                String query = button1.getText().toString();
                                String[] splited = query.split("\\s+");
                                StringBuffer search = new StringBuffer();

                                for (String test : splited) {
                                    search.append(test).append("+");
                                    Log.d("Test", test);
                                }

                                String final_url = url + search.toString().substring(0, search.toString().length() - 1);
                                Log.d("URL", search.toString().substring(0, search.toString().length() - 1));
                                Uri uri = Uri.parse(final_url);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                finish();
                            }
                        });
                        findViewById(R.id.button2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                                String query = button2.getText().toString();
                                String[] splited = query.split("\\s+");
                                StringBuffer search = new StringBuffer();

                                for (String test : splited) {
                                    search.append(test).append("+");
                                    Log.d("Test", test);
                                }

                                String final_url = url + search.toString().substring(0, search.toString().length() - 1);
                                Log.d("URL", search.toString().substring(0, search.toString().length() - 1));
                                Uri uri = Uri.parse(final_url);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                finish();
                            }
                        });
                        findViewById(R.id.button3).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                                String query = button3.getText().toString();
                                String[] splited = query.split("\\s+");
                                StringBuffer search = new StringBuffer();

                                for (String test : splited) {
                                    search.append(test).append("+");
                                    Log.d("Test", test);
                                }

                                String final_url = url + search.toString().substring(0, search.toString().length() - 1);
                                Log.d("URL", search.toString().substring(0, search.toString().length() - 1));
                                Uri uri = Uri.parse(final_url);
                                startActivity(new Intent(Intent.ACTION_VIEW, uri));
                                finish();
                            }
                        });
                    }


                    findViewById(R.id.startOcr).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                            String query = OCResult;
                            String[] splited = query.split("\\s+");
                            StringBuffer search = new StringBuffer();

                            for (String test : splited) {
                                search.append(test).append("+");
                                Log.d("Test", test);
                            }

                            String final_url = url + search.toString().substring(0, search.toString().length() - 1);
                            Log.d("URL", search.toString().substring(0, search.toString().length() - 1));
                            Uri uri = Uri.parse(final_url);
                            startActivity(new Intent(Intent.ACTION_VIEW, uri));
                            finish();
                        }
                    });
                }
            }
        });
        findViewById(R.id.start).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (startOCR) {
                    startOCR = false;
                    h.removeCallbacks(runnable);
                    Log.d("startOCR", "false");


                } else {
                    startOCR = true;
                    Log.d("startOCR", "true");

                    h = new Handler();
                    final int delay = 1000; //milliseconds

                    h.postDelayed(new Runnable() {
                        public void run() {
                            runnable = this;
                            callOCR();
                            h.postDelayed(this, delay);
                        }
                    }, delay);
                }
            }
        });

        camera = Camera.open();
        if (camera != null) {
            Camera.Parameters p = camera.getParameters();
            List<Camera.Size> list = p.getSupportedPreviewSizes();
            //Camera.Size size = list.get(list.size() - 1);
            //Camera.Size size = list.get(0);

            //preview_height = size.height;
            //preview_width = size.width;
            preview_height = 720;
            preview_width = 960;

            p.setPreviewSize(preview_width, preview_height);
            //p.setColorEffect(Camera.Parameters.EFFECT_MONO);
            camera.setParameters(p);

            Log.d("Kamera-Preview", "Widht: " + camera.getParameters().getPreviewSize().width + " Height: " + camera.getParameters().getPreviewSize().height);
            Log.d("Displaymetrics", "Widht: " + displaywidth + " Height: " + displayheight);
            holder.addCallback(this);


            FocusView focusView = new FocusView(this, 312, 408, 96, 864);
            FrameLayout frameLayout = (FrameLayout) findViewById(R.id.focusframe);
            frameLayout.addView(focusView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


            final TextView textView = (TextView) findViewById(R.id.textview_ocrpreview);
            textView.setText("");
            final TextView textView1 = (TextView) findViewById(R.id.textview_previewsize);
            textView1.setText("");
            /*
            for (int n = 0; n < list.size(); n++) {
                Camera.Size s = list.get(n);
                textView1.setText(textView1.getText() + "\n" + "Widht: " + s.width + " Height: " + s.height);
                Log.d("Übernommener Wert", "Widht: " + s.width + " Height: " + s.height);
            }
            */

            //Auf Kamera PreView zugreifen
            camera.setPreviewCallback(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] bytes, Camera camera) {
                    if (startOCR) {

                        //Kamera Preview in BitMap umwandeln
                        YuvImage temp = new YuvImage(bytes, camera.getParameters().getPreviewFormat(), camera.getParameters().getPreviewSize().width, camera.getParameters().getPreviewSize().height, null);
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        temp.compressToJpeg(new Rect(0, 0, temp.getWidth(), temp.getHeight()), 80, os);
                        preview = BitmapFactory.decodeByteArray(os.toByteArray(), 0, os.toByteArray().length);
                        preview = Bitmap.createBitmap(preview, 96, 312, 768, 96);

                        /*
                        //Previs mit OCR abscannen
                        mTess.setImage(preview);
                        String OCResult = mTess.getUTF8Text();
                        OCResult = OCResult.replaceAll("[^a-zA-Z\\s]","").replaceAll("\\s+", " ");
                        TextView OCRTextView = (TextView) findViewById(R.id.textview_ocrpreview);
                        OCRTextView.setText(OCResult);
                        */


                    }
                }
            });
        }
    }

    //Kamera freigeben
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        startOCR = false;
        if (h != null) {
            Log.d("Test", "Runnable stopped");
            h.removeCallbacks(runnable);
        }


        if (camera != null) {
            camera.stopPreview();
            holder.removeCallback(this);
            camera.setPreviewCallback(null);
            camera.release();
            camera = null;
            Log.d("surfaceDestroyed", "destroyed");

        }
    }

    //Kamera PreView starten
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    //CopyFile für Tesseract
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

    //CheckFile für Tesseract
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

    //Kamera AutoFocus beim Tippen auf den Bildschirm
    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean b, Camera camera) {
        }
    };

    public void callOCR() {
        if (startOCR) {
            mTess.setImage(preview);
            OCResult = mTess.getUTF8Text();
            OCResult = OCResult.replaceAll("[^a-zA-Z\\s]", "").replaceAll("\\s+", " ");
            TextView OCRTextView = (TextView) findViewById(R.id.textview_ocrpreview);
            OCRTextView.setText(OCResult);
            results.add(OCResult);
            mTess.end();
        }
    }
}

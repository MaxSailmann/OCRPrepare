package com.example.sailmannma54177.ocrprepare;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends Activity {
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editText = (EditText) findViewById(R.id.edittext);

        findViewById(R.id.button_scan).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(MainActivity.this, Kamera_test.class);
                startActivity(intent);
            }
        });
        findViewById(R.id.search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (editText.getText().toString() != "") {
                    String url = "http://gatherer.wizards.com/Pages/Card/Details.aspx?name=";
                    String query = editText.getText().toString().trim();
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
            }
        });


    }
}

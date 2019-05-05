package com.ashraf.faraa.livebus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class LocateStopsActivity extends AppCompatActivity {

    LinearLayout linearLayout;
    AutoCompleteTextView stopsACTV;
    String[] stops;
    String[] coords;

    TextView orTextView;
    Button searchMyLocationButton;
    Button singleStopSearchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_locate_stops);

        linearLayout = findViewById(R.id.locateStopsLinearLayout);

        stopsACTV = findViewById(R.id.locateStopsACTV);
        stopsACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stopsACTV.showDropDown();
                return false;
            }
        });

        stopsACTV.setThreshold(0);

        orTextView = findViewById(R.id.orTextView);
        searchMyLocationButton = findViewById(R.id.searchMyLocationButton);
        singleStopSearchButton = findViewById(R.id.locateSingleStopSearchButton);
        singleStopSearchButton.setVisibility(View.INVISIBLE);

        orTextView.setVisibility(View.INVISIBLE);
        searchMyLocationButton.setVisibility(View.INVISIBLE);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        new FillACTV().start();

        searchMyLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        singleStopSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correctStop = false;

                String chosenStop = stopsACTV.getText().toString();
                String stopCoords = null;

                for (int i = 0; i < stops.length; i++) {
                    if(stops[i].equals(chosenStop)) {
                        correctStop = true;
                        stopCoords = coords[i];
                    }
                }

                if(correctStop)
                    new DisplayMap(stopCoords).start();
                else {
                    Toast.makeText(LocateStopsActivity.this, "Choose only from list!", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    class DisplayMap extends Thread {

        String gpsCoords;

        DisplayMap(String coords) {
            this.gpsCoords = coords;
        }

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    linearLayout.removeAllViews();
                    singleStopSearchButton.setVisibility(View.INVISIBLE);

                    WebView webView = new WebView(LocateStopsActivity.this);
                    webView.getSettings().setJavaScriptEnabled(true);
                    webView.loadUrl("https://www.google.com/maps/place/" + gpsCoords + "/@" + gpsCoords + ",15z");

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                    webView.setLayoutParams(params);

                    orTextView.setVisibility(View.INVISIBLE);
                    searchMyLocationButton.setVisibility(View.INVISIBLE);

                    linearLayout.addView(webView);
                }
            });

            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    singleStopSearchButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    class FillACTV extends Thread {
        public void run() {
            try {
                stops = getContentFromURL(new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/hyd_stops_location"))
                        .split(";");
            }
            catch (MalformedURLException e) {
                e.printStackTrace();
            }

            coords = new String[stops.length];

            for (int i = 0; i < stops.length; i++) {
                coords[i] = stops[i].split(",")[1] + "," + stops[i].split(",")[2];
                stops[i] = stops[i].split(",")[0];
            }
            final ArrayAdapter<String> stopsAdapter = new ArrayAdapter<>(LocateStopsActivity.this, android.R.layout.simple_list_item_1, stops);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopsACTV.setAdapter(stopsAdapter);
                    singleStopSearchButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private String getContentFromURL(URL url) {
        String urlContent = "";

        URLConnection con = null;

        try {
            con = url.openConnection();
        } catch (Exception e) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocateStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }

        InputStream text = null;

        try {
            text = con.getInputStream();
        } catch (Exception e) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocateStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }

        BufferedReader br = null;

        if(text != null) {
            br = new BufferedReader(new InputStreamReader(text));
        }
        else {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocateStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }

        String idkWhy;

        try {
            while ((idkWhy = br.readLine()) != null) {
                urlContent += idkWhy;
            }
        } catch (Exception e1) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(LocateStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }
        return urlContent;
    }
}

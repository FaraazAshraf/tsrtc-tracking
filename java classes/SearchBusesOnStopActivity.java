package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class SearchBusesOnStopActivity extends AppCompatActivity {

    String chosenStop;
    LinearLayout busesLinearLayout;

    ProgressBar loadingCircle;

    String[] allStops = null;
    String[] busIdDepotType;

    TextView noBusesTextView;

    AutoCompleteTextView stopsACTV;
    Button searchButton;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_buses_on_stop);

        noBusesTextView = findViewById(R.id.noBusesTextView);
        noBusesTextView.setVisibility(View.INVISIBLE);
        busesLinearLayout = findViewById(R.id.linearLayout);
        searchButton = findViewById(R.id.searchBusesOnStopSearchButton);

        stopsACTV = findViewById(R.id.searchBusesOnStopACTV);

        stopsACTV.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.INVISIBLE);

        stopsACTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopsACTV.showDropDown();
            }
        });

        stopsACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                stopsACTV.showDropDown();
                return false;
            }
        });

        stopsACTV.setThreshold(0);

        loadingCircle = findViewById(R.id.searchBusesOnStopLoadingCircle);
        loadingCircle.setVisibility(View.VISIBLE);

        if(connectedToInternet()) {
            busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");
            new MakeStopsACTV().start();
        }
        else {
            Toast.makeText(this, "Network error", Toast.LENGTH_SHORT).show();
        }

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                busesLinearLayout.removeAllViews();
                noBusesTextView.setVisibility(View.INVISIBLE);
                chosenStop = stopsACTV.getText().toString();
                if(connectedToInternet()) {
                    boolean validStop = false;
                    for(String stop : allStops)
                        if(stop.equals(chosenStop))
                            validStop = true;

                    if(validStop) {
                        searchButton.setVisibility(View.INVISIBLE);
                        new LogAction(chosenStop).start();
                        new DisplayBuses(chosenStop).start();
                    }
                    else {
                        Toast.makeText(SearchBusesOnStopActivity.this, "Choose only from list", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                    Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

    }

    public class MakeStopsACTV extends Thread {
        public void run() {
            URL url = null;

            try {
                url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/hyd_stops_from_server");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String urlContent = getContentFromURL(url);
            int numStops = urlContent.split(";").length;

            allStops = new String[numStops];

            for(int i = 0; i < numStops; i++) {
                allStops[i] = urlContent.split(";")[i].split(",")[0];
            }
            final ArrayAdapter<String> allStopsAdapter = new ArrayAdapter<>(SearchBusesOnStopActivity.this, android.R.layout.simple_list_item_1, allStops);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopsACTV.setAdapter(allStopsAdapter);
                    loadingCircle.setVisibility(View.INVISIBLE);
                    stopsACTV.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    public class DisplayBuses extends Thread {

        String chosenStop;
        String[] urlContent;

        ArrayList<Button> buttons = new ArrayList<>();
        ArrayList<String> buses = new ArrayList<>();

        public DisplayBuses(String chosenStop) {
            this.chosenStop = chosenStop;
        }

        public void run() {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingCircle.setVisibility(View.VISIBLE);
                }
            });

            URL url = null;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + chosenStop.replaceAll(" ", "%20") + ",0,67&flag=7");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            urlContent = getContentFromURL(url).split(";");
            //AP11Z7500-METRO DELUXE,156V,MEHDIPATNAM,23:28:59,863,17.325068,78.565514,NGOS COLONY;

            if (!(urlContent[0].equals("No records found.") || urlContent[0].equals("null") || urlContent[0].equals(""))) {
                for (int i = 0; i < urlContent.length; i++) {
                    final String singleBusData = urlContent[i].replaceFirst("-", ",");

                    String busID = singleBusData.split(",")[5];
                    String busRegNum = null, busType = null;

                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(busID)) {
                            busRegNum = regAndID[0];
                            busType = regAndID[3].toUpperCase();
                        }
                    }

                    String busRoute = singleBusData.split(",")[2];
                    String busTowards = singleBusData.split(",")[3];
                    String busETA = singleBusData.split(",")[4];

                    if (!buses.contains(busRegNum)) {
                        Button b = new Button(SearchBusesOnStopActivity.this);
                        b.setText("\n" +
                                busRegNum + "   -   " + busType + "\n" +
                                "Route:  " + busRoute.replace("300D", "300/126") + "\n" +
                                "Towards: " + busTowards + "\n" +
                                "ETA to " + chosenStop + ": " + busETA
                                + "\n");

                        if (busType.equals("METRO EXPRESS")) {
                            b.setBackgroundResource(R.drawable.blue_button_bg);
                            b.setTextColor(Color.WHITE);
                        } else if (busType.equals("METRO DELUXE")) {
                            b.setBackgroundResource(R.drawable.green_button_bg);
                            b.setTextColor(Color.WHITE);
                        } else if (busType.equals("LOW FLOOR AC")) {
                            b.setBackgroundResource(R.drawable.red_button_bg);
                            b.setTextColor(Color.WHITE);
                        } else if(busType.equals("METRO LUXURY AC")) {
                            b.setBackgroundResource(R.drawable.pink_button_bg);
                            b.setTextColor(Color.WHITE);
                        }

                        final String finalBusRegNum = busRegNum;
                        b.setOnClickListener(new View.OnClickListener() {
                            public void onClick(View v) {
                                Intent singleBusIntent = new Intent(SearchBusesOnStopActivity.this, SingleBusActivity.class);
                                singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                startActivity(singleBusIntent);
                            }
                        });

                        buttons.add(b);
                        buses.add(busRegNum);
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingCircle.setVisibility(View.INVISIBLE);
                    if(buttons.size() > 0) {
                        for (Button button : buttons) {
                            busesLinearLayout.addView(button);
                        }
                    }
                    else {
                        noBusesTextView.setVisibility(View.VISIBLE);
                    }
                    searchButton.setVisibility(View.VISIBLE);
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
                    Toast.makeText(SearchBusesOnStopActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnStopActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnStopActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnStopActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }
        return urlContent;
    }

    private boolean connectedToInternet() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }

    private class LogAction extends Thread {

        String logString;

        public LogAction (String logString) {
            this.logString = logString;
        }

        public void run() {
            URL url = null;

            String currentDate = new SimpleDateFormat("MM-dd HH:mm").format(new Date());

            logString = currentDate + "-" + logString;

            try {
                logString = URLEncoder.encode(logString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(logString.length() <= 50) {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=name,fafafafa@fsfsfsfs.com,9534343434," + logString + ",0,8,mobile,0,67&flag=15");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String dummy = getContentFromURL(url);
            }
        }
    }
}

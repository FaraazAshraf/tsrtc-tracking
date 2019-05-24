package com.ashraf.faraa.livebus;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AllStopsActivity extends AppCompatActivity {

    LinearLayout stopsLinearLayout;
    TextView titleTextView;
    String busRegNum, busType;
    boolean keepRefreshing = true;
    String busIDString;

    ScrollView allStopsScrollView;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_stops);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        busIDString = getIntent().getExtras().getString("busIDString");
        stopsLinearLayout = findViewById(R.id.allStopsLinearLayout);

        allStopsScrollView = findViewById(R.id.allStopsScrollView);

        busRegNum = getIntent().getExtras().getString("busRegNum");
        busType = getIntent().getExtras().getString("busType");

        titleTextView = findViewById(R.id.allStopsTitleTextView);

        titleTextView.setText("Searching...");

        new RefreshStops10Sec().start();

    }

    public void onBackPressed() {
        keepRefreshing = false;
        super.onBackPressed();
    }

    protected void onPause() {
        super.onPause();
        keepRefreshing = false;
    }

    protected void onResume() {
        super.onResume();
        if(!keepRefreshing) {
            keepRefreshing = true;
            new RefreshStops10Sec().start();
        }
    }

    public class RefreshStops10Sec extends Thread {
        public void run() {
            while(keepRefreshing) {
                if(keepRefreshing) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AllStops(busIDString).start();
                        }
                    });
                }
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class AllStops extends Thread {

        String busIDString;
        String stopsDataUnformattedString;
        String[] allStopsData;

        public AllStops(String busIDString) {
            this.busIDString = busIDString;
        }

        @Override
        public void run() {

            URL url = null;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busIDString + "&flag=13");
            } catch (MalformedURLException e) {
                //should never happen
            }

            //we get the data for the next refresh
            stopsDataUnformattedString = getContentFromURL(url);
            allStopsData = stopsDataUnformattedString.split(";");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    stopsLinearLayout.removeAllViews();
                    titleTextView.setText(busRegNum + "  -  " + busType);
                }
            });


            if(stopsDataUnformattedString.equals("No records found.")) {
                boolean noStops = true;
                keepRefreshing = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(AllStopsActivity.this, "No stops found.", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                });
                while(noStops) {
                    //no stops data. activity must close.
                }
            }
            else {
                int lastSeenAtStopIndex = 12345;
                for (int i = (allStopsData.length) - 1; i >= 0; i--) {
                    String[] singleStopData = allStopsData[i].split(",");
                    if (singleStopData[2].equals("Y")) {
                        lastSeenAtStopIndex = i;
                        break;
                    }
                }

                if(lastSeenAtStopIndex == 12345 || lastSeenAtStopIndex == (allStopsData.length) - 1) {
                    keepRefreshing = false;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(AllStopsActivity.this, "Stops data not found.", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    });
                }

                for (int i = 0; i <= lastSeenAtStopIndex; i++) {
                    final Button b = new Button(AllStopsActivity.this);
                    b.setBackgroundResource(R.drawable.passed);
                    b.setTextSize(15);
                    b.setTextColor(Color.BLACK);

                    String stopName = allStopsData[i].split(",")[1];
                    String passedTime = allStopsData[i].split(",")[4];
                    if (passedTime.equalsIgnoreCase("null") || allStopsData[i].split(",")[2].equals("N")) {
                        b.setText(stopName + "\nno record");
                    } else {
                        b.setText(stopName + "\npassed at " + passedTime.substring(11, 16));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopsLinearLayout.addView(b);
                        }
                    });
                }

                int numStopsBelowPassedStops = 0;

                for (int i = lastSeenAtStopIndex + 1; i < allStopsData.length; i++) {
                    final Button b = new Button(AllStopsActivity.this);
                    b.setBackgroundResource(R.drawable.yetto);

                    b.setTextSize(15);
                    b.setClickable(false);
                    b.setTextColor(Color.BLACK);

                    String stopName = allStopsData[i].split(",")[1];
                    String passedTime = allStopsData[i].split(",")[4];

                    if (passedTime.equalsIgnoreCase("null")) {
                        b.setText(stopName + "\nno ETA available");
                    } else {
                        b.setText(stopName + "\nETA " + passedTime.substring(11, 16));
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            stopsLinearLayout.addView(b);
                        }
                    });

                    if(numStopsBelowPassedStops < 3) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                b.getParent().requestChildFocus(b,b);
                            }
                        });
                        numStopsBelowPassedStops++;
                    }
                }
            }
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
                    Toast.makeText(AllStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AllStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AllStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(AllStopsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
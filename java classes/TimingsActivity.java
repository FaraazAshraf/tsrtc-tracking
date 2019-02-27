package com.ashraf.faraa.livebus;

import android.graphics.Color;
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

public class TimingsActivity extends AppCompatActivity {

    AutoCompleteTextView fromStopACTV;
    AutoCompleteTextView toStopACTV;

    ProgressBar loadingCircle;

    LinearLayout linearLayout;

    String type;
    String[] allStops;

    Button searchButton;

    TextView disclaimerTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timings);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        disclaimerTextView = findViewById(R.id.disclaimerTextView);
        disclaimerTextView.setVisibility(View.INVISIBLE);

        searchButton = findViewById(R.id.timingsSearchButton);

        linearLayout = findViewById(R.id.linearLayout);

        fromStopACTV = findViewById(R.id.fromStopACTV);
        toStopACTV = findViewById(R.id.toStopACTV);

        loadingCircle = findViewById(R.id.loadingCircle);
        loadingCircle.setVisibility(View.INVISIBLE);

        fromStopACTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fromStopACTV.showDropDown();
            }
        });
        fromStopACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fromStopACTV.showDropDown();
                return false;
            }
        });

        toStopACTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toStopACTV.showDropDown();
            }
        });
        toStopACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                toStopACTV.showDropDown();
                return false;
            }
        });

        fromStopACTV.setThreshold(0);
        toStopACTV.setThreshold(0);

        toStopACTV.setVisibility(View.INVISIBLE);
        fromStopACTV.setVisibility(View.INVISIBLE);
        searchButton.setVisibility(View.INVISIBLE);

        final Button loadCityStopsButton = findViewById(R.id.loadCityStopsButton);
        final Button loadLDStopsButton = findViewById(R.id.loadLDStopsButton);

        loadCityStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "city";
                loadCityStopsButton.setVisibility(View.INVISIBLE);
                loadLDStopsButton.setVisibility(View.INVISIBLE);
                new MakeStopsACTV().start();
            }
        });

        loadLDStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                type = "long";
                loadCityStopsButton.setVisibility(View.INVISIBLE);
                loadLDStopsButton.setVisibility(View.INVISIBLE);
                new MakeStopsACTV().start();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean correctFrom = false, correctTo = false;
                String chosenFromStop = fromStopACTV.getText().toString();
                String chosenToStop = toStopACTV.getText().toString();

                linearLayout.removeAllViews();

                for(String stop : allStops)
                    if(stop.equals(chosenFromStop))
                        correctFrom = true;

                for(String stop : allStops)
                    if(stop.equals(chosenToStop))
                        correctTo = true;

                if(correctFrom && correctTo) {
                    new DisplayTimings(chosenFromStop, chosenToStop).start();
                }
                else {
                    Toast.makeText(TimingsActivity.this, "Choose from list only", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private class DisplayTimings extends Thread {

        String fromStop;
        String toStop;

        DisplayTimings(String fromStop, String toStop) {
            this.fromStop = fromStop;
            this.toStop = toStop;
        }

        public void run() {
            URL url = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchButton.setVisibility(View.INVISIBLE);
                    disclaimerTextView.setVisibility(View.INVISIBLE);
                    loadingCircle.setVisibility(View.VISIBLE);
                }
            });

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+ URLEncoder.encode(fromStop, "UTF-8") +","+ URLEncoder.encode(toStop, "UTF-8") +"&flag=14");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchButton.setVisibility(View.VISIBLE);
                    loadingCircle.setVisibility(View.INVISIBLE);
                }
            });

            String timings[] = getContentFromURL(url).split(";");

            if(timings[0].equals("No records found.") || timings[0].equals("")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(TimingsActivity.this, "Sorry, no direct bus found.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        disclaimerTextView.setVisibility(View.VISIBLE);
                    }
                });
                for(String timing : timings) {
                    String time = timing.split(",")[0];
                    String route = timing.split(",")[1];
                    String busType = timing.split(",")[2];
                    busType = busType.toUpperCase();

                    final Button b = new Button(TimingsActivity.this);
                    b.setClickable(false);
                    busType = busType.replace("INDRA", "RAJADHANI").replace("METRO DELUX AC", "LOW FLOOR AC").replace("METRO LUXURY OL", "METRO LUXURY AC");
                    b.setText("\nSCHEDULED TIME: " + time +
                            "\nBUS TYPE: " + busType +
                            "\nROUTE: " + route + "\n");

                    if(busType.equals("METRO DELUXE") || busType.equals("INDRA") || busType.contains("GARUDA")) {
                        b.setBackgroundResource(R.drawable.deluxe_bg);
                        b.setTextColor(Color.WHITE);
                    }
                    else if(busType.equals("METRO LUXURY AC") || busType.equals("DELUXE")) {
                        b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                        b.setTextColor(Color.WHITE);
                    }
                    else if(busType.contains("EXPRESS")) {
                        b.setBackgroundResource(R.drawable.express_bg);
                        b.setTextColor(Color.WHITE);
                    }
                    else {
                        b.setBackgroundResource(R.drawable.lf_bg);
                        b.setTextColor(Color.WHITE);
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            linearLayout.addView(b);
                        }
                    });
                }
            }

        }
    }

    private class MakeStopsACTV extends Thread {
        public void run() {
            URL url = null;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingCircle.setVisibility(View.VISIBLE);
                }
            });

            try {
                if(type.equals("city"))
                    url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/hyd_stops_from_server");
                else
                    url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/long_distance_stops_from_server");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String urlContent = getContentFromURL(url);
            int numStops = urlContent.split(";").length;

            allStops = new String[numStops];

            for(int i = 0; i < numStops; i++) {
                allStops[i] = urlContent.split(";")[i].split(",")[0];
            }
            final ArrayAdapter<String> allStopsAdapter = new ArrayAdapter<>(TimingsActivity.this, android.R.layout.simple_list_item_1, allStops);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fromStopACTV.setAdapter(allStopsAdapter);
                    toStopACTV.setAdapter(allStopsAdapter);
                    loadingCircle.setVisibility(View.INVISIBLE);
                    fromStopACTV.setVisibility(View.VISIBLE);
                    toStopACTV.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private String getContentFromURL(URL url) {
        String urlContent = new String();

        URLConnection con = null;

        try {
            con = url.openConnection();
        } catch (Exception e) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(TimingsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TimingsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TimingsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(TimingsActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

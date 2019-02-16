package com.ashraf.faraa.livebus;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

public class AllStopsActivity extends AppCompatActivity {

    LinearLayout stopsLinearLayout;
    TextView titleTextView;
    String busRegNum, busType;
    boolean keepRefreshing = true;
    String busIDString;

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

        busRegNum = getIntent().getExtras().getString("busRegNum");
        busType = getIntent().getExtras().getString("busType");

        titleTextView = findViewById(R.id.allStopsTitleTextView);

        busRegNum = busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                .replace("AP11Z4039", "TS07Z4039");

        titleTextView.setText("Searching...");

        new AllStops(busIDString).execute();
        new Refresh10Sec().start();

    }

    public void onBackPressed() {
        keepRefreshing = false;
        super.onBackPressed();
    }

    public class Refresh10Sec extends Thread {
        public void run() {
            while(keepRefreshing) {
                try {
                    Thread.sleep(12000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(keepRefreshing) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AllStops(busIDString).execute();
                        }
                    });
                }
            }
        }
    }

    private class AllStops extends AsyncTask<Void, Void, Void> {

        String busIDString;
        String stopsDataUnformattedString;
        String[] allStopsData;

        public AllStops(String busIDString) {
            this.busIDString = busIDString;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            URL url = null;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busIDString + "&flag=13");
            } catch (MalformedURLException e) {
                //should never happen
            }

            stopsDataUnformattedString = getContentFromURL(url);
            allStopsData = stopsDataUnformattedString.split(";");

            return null;
        }

        protected void onPostExecute(Void aVoid) {
            stopsLinearLayout.removeAllViews();

            titleTextView.setText(busRegNum + "  -  " + busType);

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
                    String singleStopData[] = allStopsData[i].split(",");
                    if (singleStopData[2].equals("Y")) {
                        lastSeenAtStopIndex = i;
                        break;
                    }
                }

                for (int i = 0; i <= lastSeenAtStopIndex; i++) {
                    Button b = new Button(AllStopsActivity.this);
                    b.setBackgroundResource(R.drawable.passed);
                    b.setTextSize(15);
                    b.setTextColor(Color.BLACK);

                    String stopName = allStopsData[i].split(",")[1];
                    String passedTime = allStopsData[i].split(",")[4];
                    if (passedTime.equalsIgnoreCase("null")) {
                        b.setText(stopName + "  -  No record");
                    } else {
                        b.setText(stopName + "  -  passed at " + passedTime.substring(11, 16));
                    }
                    stopsLinearLayout.addView(b);
                }

                for (int i = lastSeenAtStopIndex + 1; i < allStopsData.length; i++) {
                    Button b = new Button(AllStopsActivity.this);
                    b.setBackgroundResource(R.drawable.yetto);
                    b.setTextSize(15);
                    b.setClickable(false);
                    b.setTextColor(Color.BLACK);

                    String stopName = allStopsData[i].split(",")[1];
                    String passedTime = allStopsData[i].split(",")[4];

                    if (passedTime.equalsIgnoreCase("null")) {
                        b.setText(stopName + "  -  NO ETA");
                    } else {
                        b.setText(stopName + "  -  ETA " + passedTime.substring(11, 16));
                    }

                    stopsLinearLayout.addView(b);
                }
            }
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
                    new AlertDialog.Builder(AllStopsActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Internet error")
                            .setMessage("Please check your internet and try again.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            }).show();
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
                    new AlertDialog.Builder(AllStopsActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Internet error")
                            .setMessage("Please check your internet and try again.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            }).show();
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
                    new AlertDialog.Builder(AllStopsActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Internet error")
                            .setMessage("Please check your internet and try again.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            }).show();
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
                    new AlertDialog.Builder(AllStopsActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Internet error")
                            .setMessage("Please check your internet and try again.")
                            .setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }

                            }).show();
                }
            });
            while (errorFlag) {
                //fix ur enternetz!
            }
        }

        return urlContent;
    }
}

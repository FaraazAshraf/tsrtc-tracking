package com.ashraf.faraa.livebus;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
    String type;
    LinearLayout busesLinearLayout;

    ProgressBar loadingCircle;
    ProgressDialog loadingCircle2;

    String allStops[] = null;

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
        loadingCircle.setVisibility(View.INVISIBLE);

        final Button loadCityStopsButton = findViewById(R.id.loadCityStopsButton);
        final Button loadLDStopsButton = findViewById(R.id.loadLDStopsButton);

        loadCityStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingCircle.setVisibility(View.VISIBLE);
                type = "city";
                loadCityStopsButton.setVisibility(View.INVISIBLE);
                loadLDStopsButton.setVisibility(View.INVISIBLE);
                if(connectedToInternet()) {
                    try {
                        new MakeStopsACTV().start();
                    } catch (Exception e) {
                        Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        loadLDStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingCircle.setVisibility(View.VISIBLE);
                type = "long";
                loadCityStopsButton.setVisibility(View.INVISIBLE);
                loadLDStopsButton.setVisibility(View.INVISIBLE);
                if(connectedToInternet()) {
                    try {
                        new MakeStopsACTV().start();
                    } catch (Exception e) {
                        Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
                else {
                    Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                busesLinearLayout.removeAllViews();
                noBusesTextView.setVisibility(View.INVISIBLE);
                chosenStop = stopsACTV.getText().toString();
                if(connectedToInternet()) {
                    try {

                        boolean validStop = false;
                        for(String stop : allStops)
                            if(stop.equals(chosenStop))
                                validStop = true;

                        if(validStop) {
                            new LogAction(chosenStop).start();
                            new DisplayBuses(chosenStop).execute();
                        }
                        else {
                            Toast.makeText(SearchBusesOnStopActivity.this, "Choose only from list", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                    }
                }
                else
                    Toast.makeText(SearchBusesOnStopActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
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
                if(type.equals("city"))
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=0,67&flag=27");
                else
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=0,67&flag=26");
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

    public class DisplayBuses extends AsyncTask<Void, Void, Void> {

        String chosenStop;
        String urlContent;

        ArrayList<Button> buttons = new ArrayList<>();
        ArrayList<String> buses = new ArrayList<>();

        public DisplayBuses(String chosenStop) {
            this.chosenStop = chosenStop;
        }

        @Override
        protected void onPreExecute() {
            loadingCircle2 = new ProgressDialog(SearchBusesOnStopActivity.this);
            loadingCircle2.setMessage("Searching...");
            loadingCircle2.setCancelable(false);
            loadingCircle2.setInverseBackgroundForced(false);
            loadingCircle2.show();
        }

        protected Void doInBackground(Void... voids) {
            URL url = null;
            if(type.equals("city")) {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + chosenStop.replaceAll(" ", "%20") + ",0,67&flag=7");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                urlContent = getContentFromURL(url);
                //AP11Z7500-METRO DELUXE,156V,MEHDIPATNAM,23:28:59,863,17.325068,78.565514,NGOS COLONY;

                if (!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                    for (int i = 0; i < urlContent.split(";").length; i++) {
                        final String singleBusData = urlContent.split(";")[i].replaceFirst("-", ",");
                        final String busRegNum = singleBusData.split(",")[0];
                        String busType = singleBusData.split(",")[1];
                        String busRoute = singleBusData.split(",")[2];
                        String busTowards = singleBusData.split(",")[3];
                        String busETA = singleBusData.split(",")[4];

                        if(busRegNum.equals("AP11Z6086") || busRegNum.equals("AP11Z6087") ||
                                busRegNum.equals("AP11Z6084") || busRegNum.equals("AP11Z6093") ||
                                busRegNum.equals("AP11Z6096")) {
                            busType = "METRO LUXURY AC";
                        }

                        if (!buses.contains(busRegNum)) {
                            Button b = new Button(SearchBusesOnStopActivity.this);
                            b.setText("\n" +
                                    busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                                            .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                                            .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                                            .replace("AP11Z4039", "TS07Z4039").replace("AP7Z4004", "TS07Z4004")
                                            .replace("AP7Z4020", "TS07Z4020").replace("AP07Z4008", "TS07Z4008") + "   -   " + busType + "\n" +
                                    "Route:  " + busRoute + "\n" +
                                    "Towards: " + busTowards + "\n" +
                                    "ETA to " + chosenStop + ": " + busETA
                            + "\n");

                            if (busType.contains("EXPRESS")) {
                                b.setBackgroundResource(R.drawable.express_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.contains("DELUXE")) {
                                b.setBackgroundResource(R.drawable.deluxe_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.contains("LOW FLOOR")) {
                                b.setBackgroundResource(R.drawable.lf_bg);
                                b.setTextColor(Color.WHITE);
                            } else if(busType.equals("METRO LUXURY AC")) {
                                b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                b.setTextColor(Color.WHITE);
                            }

                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent singleBusIntent = new Intent(SearchBusesOnStopActivity.this, SingleBusActivity.class);
                                    singleBusIntent.putExtra("busRegNumString", busRegNum);
                                    startActivity(singleBusIntent);
                                }
                            });

                            buttons.add(b);
                            buses.add(busRegNum);
                        }
                    }
                }
            }

            else {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + chosenStop.replaceAll(" ", "%20") + ",5,67&flag=7");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                urlContent = getContentFromURL(url);
                //AP11Z7500-METRO DELUXE,156V,MEHDIPATNAM,23:28:59,863,17.325068,78.565514,NGOS COLONY;

                if (!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                    for (int i = 0; i < urlContent.split(";").length; i++) {
                        String singleBusData = urlContent.split(";")[i].replaceFirst("-", ",");
                        final String busRegNum = singleBusData.split(",")[0];
                        String busType = singleBusData.split(",")[1];
                        String busRoute = singleBusData.split(",")[2];
                        String busTowards = singleBusData.split(",")[3];
                        String busSource = singleBusData.split(",")[8];
                        String busETA = singleBusData.split(",")[4];

                        if (!buses.contains(busRegNum)) {
                            Button b = new Button(SearchBusesOnStopActivity.this);
                            b.setText("\n" +
                                    busRegNum + "   -   " + busType.replace("INDRA", "RAJADHANI") + "\n" +
                                    "Route:  " + busRoute + "\n" +
                                    "Source: " + busSource + "\n" +
                                    "Towards: " + busTowards + "\n" +
                                    "ETA to " + chosenStop + ": " + busETA
                            + "\n");

                            if (busType.contains("EXPRESS")) {
                                b.setBackgroundResource(R.drawable.express_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.contains("DELUXE")) {
                                b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.contains("SUPER LUXURY")) {
                                b.setBackgroundResource(R.drawable.lf_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.contains("GARUDA") || busType.contains("INDRA")) {
                                b.setBackgroundResource(R.drawable.deluxe_bg);
                                b.setTextColor(Color.WHITE);
                            }

                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    Intent singleBusIntent = new Intent(SearchBusesOnStopActivity.this, SingleBusActivity.class);
                                    singleBusIntent.putExtra("busRegNumString", busRegNum);
                                    startActivity(singleBusIntent);
                                }
                            });

                            buttons.add(b);
                            buses.add(busRegNum);
                        }
                    }
                }
            }
            return null;
        }

        protected void onPostExecute(Void aVoid) {
            loadingCircle2.hide();
            if(buttons.size() > 0) {
                for (Button button : buttons) {
                    busesLinearLayout.addView(button);
                }
            }
            else {
                noBusesTextView.setVisibility(View.VISIBLE);
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
                    new AlertDialog.Builder(SearchBusesOnStopActivity.this)
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
                    new AlertDialog.Builder(SearchBusesOnStopActivity.this)
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
                    new AlertDialog.Builder(SearchBusesOnStopActivity.this)
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
                urlContent = idkWhy;
            }
        } catch (Exception e1) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    new AlertDialog.Builder(SearchBusesOnStopActivity.this)
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
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=name,fafafafa@fsfsfsfs.com,9534343434," + logString + ",0,6,mobile,0,67&flag=15");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String dummy = getContentFromURL(url);
            }
        }
    }
}

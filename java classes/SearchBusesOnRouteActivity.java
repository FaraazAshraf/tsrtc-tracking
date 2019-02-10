package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

public class SearchBusesOnRouteActivity extends AppCompatActivity {

    ArrayList<String> validBuses;
    ArrayList<String> validStops;
    String chosenRoute;
    ProgressBar progressBar;
    TextView actionTextView;

    boolean keepScanning;
    boolean noBusesFound;

    TextView noBusesTextView;

    Button scanButtonMethod1;
    Button scanButtonMethod2;

    EditText routeNumEditText;

    LinearLayout busesLinearLayout;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_buses_on_route);

        progressBar = findViewById(R.id.routeNumSearchProgressBar);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.INVISIBLE);
        actionTextView = findViewById(R.id.routeNumSearchProgressEditText);
        actionTextView.setText("Searchstring: " + chosenRoute);
        actionTextView.setVisibility(View.INVISIBLE);

        noBusesTextView = findViewById(R.id.noBusesTextView);
        noBusesTextView.setVisibility(View.INVISIBLE);

        busesLinearLayout = findViewById(R.id.linearLayout);
        validBuses = new ArrayList<>();
        validStops = new ArrayList<>();

        keepScanning = true;

        scanButtonMethod1 = findViewById(R.id.scanButtonMethod1);
        scanButtonMethod1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeNumEditText = findViewById(R.id.routeNumEditText);
                chosenRoute = routeNumEditText.getText().toString().toUpperCase();
                if(chosenRoute.equals(""))
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Please enter something!", Toast.LENGTH_LONG).show();
                else {
                    if(connectedToInternet()) {
                        new LogAction(chosenRoute).start();
                        chosenRoute = chosenRoute.split(" ")[0].split("/")[0];
                        noBusesTextView.setVisibility(View.INVISIBLE);
                        validBuses.clear();
                        validStops.clear();
                        noBusesFound = true;
                        busesLinearLayout.removeAllViews();
                        keepScanning = true;
                        progressBar.setProgress(0);
                        scanButtonMethod1.setVisibility(View.INVISIBLE);
                        scanButtonMethod2.setVisibility(View.INVISIBLE);
                        new RunScannerMethod1().start();
                    }
                    else
                        Toast.makeText(SearchBusesOnRouteActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                }
            }
        });

        scanButtonMethod2 = findViewById(R.id.scanButtonMethod2);
        scanButtonMethod2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                routeNumEditText = findViewById(R.id.routeNumEditText);
                chosenRoute = routeNumEditText.getText().toString().toUpperCase();
                if(chosenRoute.equals(""))
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Please enter something!", Toast.LENGTH_LONG).show();
                else {
                    if(connectedToInternet()) {
                        new LogAction(chosenRoute).start();
                        chosenRoute = chosenRoute.split(" ")[0].split("/")[0];
                        noBusesTextView.setVisibility(View.INVISIBLE);
                        validBuses.clear();
                        validStops.clear();
                        noBusesFound = true;
                        busesLinearLayout.removeAllViews();
                        keepScanning = true;
                        progressBar.setProgress(0);
                        scanButtonMethod1.setVisibility(View.INVISIBLE);
                        scanButtonMethod2.setVisibility(View.INVISIBLE);
                        new RunScannerMethod2().start();
                    }
                    else
                        Toast.makeText(SearchBusesOnRouteActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
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

    private class RunScannerMethod1 extends Thread {

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionTextView.setText("Searching for route data. Please wait...");
                    actionTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            URL url = null;
            String urlContent;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=0,67&flag=28");

            } catch (MalformedURLException e) {
                keepScanning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchBusesOnRouteActivity.this, "ERROR. Check your internet and try again.", Toast.LENGTH_LONG).show();
                    }
                });
                return;
            }
            urlContent = getContentFromURL(url);
            final String[] allRoutes = urlContent.split(";");
            //10,SECUNDERABAD,SANATH NAGAR ...list of all routes + routeNums

            int numValidRoutes = 0, completedRoutes = 0;

            for(String singleRoute : allRoutes) {
                if(singleRoute.contains(chosenRoute))
                    numValidRoutes++;
            }

            if(numValidRoutes == 0) {
                keepScanning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchBusesOnRouteActivity.this, "Route not found.", Toast.LENGTH_LONG).show();
                        actionTextView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
                noBusesTextView.setVisibility(View.INVISIBLE);
            }
            else {
                for (int i = 0; i < allRoutes.length && keepScanning; i++) {

                    final int i2 = i;

                    final int finalCompleted = completedRoutes;
                    final int finalNumValid = numValidRoutes;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progressBar.setProgress((finalCompleted * 100) / finalNumValid);
                        }
                    });

                    if (allRoutes[i].contains(chosenRoute)) {
                        completedRoutes++;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                actionTextView.setText("Inspecting " + allRoutes[i2].split(",")[0]);
                            }
                        });
                        try {
                            url = new URL("http://125.16.1.204:8080//bats/appQuery.do?query=" + allRoutes[i].split(",")[0] + ",0,67&flag=8");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        urlContent = getContentFromURL(url);
                        //183,SECUNDERABAD TO SANATH NAGAR;...list of sub-routes WITHIN the main route
                        //eg. 300D will give DSNR->JNTU and JNTU->DSNR as sub-routes

                        if (!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                            String singleRouteData[] = urlContent.split(";");
                            //183,SECUNDERABAD TO SANATH NAGAR
                            for (String singleRoute : singleRouteData) {
                                try {
                                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + singleRoute.split(",")[0] + "&flag=12");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                urlContent = getContentFromURL(url);
                                //now we have the buses reportedly running on that particular sub-route
                                //1874,AP11Z7281,17.263527,78.389748,RANIGUNJ 2,AP11Z7281-LOW FLOOR AC;
                                //we need to take the IDs and verify if they are live using flag=13

                                if (!urlContent.equalsIgnoreCase("No records found.") && !urlContent.equalsIgnoreCase("null") && !urlContent.equalsIgnoreCase("")) {
                                    String[] possibleBuses = urlContent.split(";");
                                    for (String bus : possibleBuses) {

                                        String busID = bus.split(",")[0];
                                        try {
                                            url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=13");
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
                                        urlContent = getContentFromURL(url);

                                        String[] stopsDataFormattedArray = urlContent.split(";");

                                        try {
                                            url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=21");
                                        } catch (MalformedURLException e) {
                                            e.printStackTrace();
                                        }
                                        urlContent = getContentFromURL(url);

                                        String[] VMUFormatted = urlContent.split(",");
                                        String busStatus = VMUFormatted[10].toUpperCase();
                                        String busType = VMUFormatted[9].toUpperCase();
                                        String lastUpdated = VMUFormatted[5].substring(0, VMUFormatted[5].lastIndexOf("."));

                                        String source = stopsDataFormattedArray[0].split(",")[1];
                                        String destination = stopsDataFormattedArray[stopsDataFormattedArray.length - 1].split(",")[1];

                                        int lastSeenAtStopIndex = 12345;
                                        for (int j = (stopsDataFormattedArray.length) - 1; j >= 0; j--) {
                                            String singleStopData[] = stopsDataFormattedArray[j].split(",");
                                            if (singleStopData[2].equals("Y")) {
                                                lastSeenAtStopIndex = j;
                                                break;
                                            }
                                        }

                                        boolean valid = true;

                                        if (busStatus.equalsIgnoreCase("Buses in Depot"))
                                            valid = false;
                                        if (lastSeenAtStopIndex == 12345 ||
                                                lastSeenAtStopIndex == (stopsDataFormattedArray.length) - 1)
                                            valid = false;

                                        if (valid) {
                                            String lastUpdatedTime = lastUpdated.substring(lastUpdated.indexOf(" ") + 1, lastUpdated.length());
                                            String lastSeenTimeString = stopsDataFormattedArray[lastSeenAtStopIndex].split(",")[4];
                                            lastSeenTimeString = lastSeenTimeString.substring(11, 19);
                                            int timeDifference = Integer.parseInt(lastUpdatedTime.replace(":", ""))
                                                    - Integer.parseInt(lastSeenTimeString.replace(":", ""));
                                            String lastUpdatedTimeHour = lastUpdatedTime.substring(0, 2);
                                            String lastSeenTimeHour = lastSeenTimeString.substring(0, 2);
                                            String nextStopEta = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[4];

                                            if (((lastUpdatedTimeHour.equalsIgnoreCase(lastSeenTimeHour) && timeDifference >= 1000)
                                                    ||
                                                    (!lastUpdatedTimeHour.equalsIgnoreCase(lastSeenTimeHour) && timeDifference >= 5000))
                                                    && (nextStopEta.equalsIgnoreCase("null"))
                                                    && (!destination.contains("AIRPORT") && !destination.contains("PUSHPAK"))
                                                    && (!source.contains("AIRPORT") && !source.contains("PUSHPAK"))) {
                                                valid = false;
                                            }
                                        }

                                        if (valid) {
                                            /*
                                            if previous stop was seen less than 10 mins ago
                                            and it's not an airport bus
                                            and there is some next stop
                                            then display the bus as a valid bus on this route
                                             */
                                            String nextStop = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[1];
                                            final String busRegNum = VMUFormatted[0].toUpperCase();

                                            if (busRegNum.equals("AP11Z6086") || busRegNum.equals("AP11Z6087") ||
                                                    busRegNum.equals("AP11Z6084") || busRegNum.equals("AP11Z6093") ||
                                                    busRegNum.equals("AP11Z6096")) {
                                                busType = "METRO LUXURY AC";
                                            }

                                            String buttonText = "\n"
                                                    + busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                                                    .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                                                    .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                                                    .replace("AP11Z4039", "TS07Z4039").replace("AP7Z4004", "TS07Z4004")
                                                    .replace("AP7Z4020", "TS07Z4020").replace("AP07Z4008", "TS07Z4008") + "   -   " + busType + "\n" +
                                                    "Route:  " + allRoutes[i].split(",")[0] + "\n" +
                                                    "Towards: " + destination + "\n" +
                                                    "Next stop: " + nextStop
                                                    + "\n";

                                            final Button b = new Button(SearchBusesOnRouteActivity.this);
                                            b.setText(buttonText);
                                            if (busType.contains("EXPRESS")) {
                                                b.setBackgroundResource(R.drawable.express_bg);
                                                b.setTextColor(Color.WHITE);
                                            } else if (busType.contains("DELUXE")) {
                                                b.setBackgroundResource(R.drawable.deluxe_bg);
                                                b.setTextColor(Color.WHITE);
                                            } else if (busType.contains("LOW FLOOR")) {
                                                b.setBackgroundResource(R.drawable.lf_bg);
                                                b.setTextColor(Color.WHITE);
                                            } else if (busType.contains("LUXURY")) {
                                                b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                                b.setTextColor(Color.WHITE);
                                            }
                                            b.setOnClickListener(new View.OnClickListener() {
                                                public void onClick(View v) {
                                                    Intent singleBusIntent = new Intent(SearchBusesOnRouteActivity.this, SingleBusActivity.class);
                                                    singleBusIntent.putExtra("busRegNumString", busRegNum);
                                                    keepScanning = false;
                                                    startActivity(singleBusIntent);
                                                }
                                            });

                                            noBusesFound = false;

                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    busesLinearLayout.addView(b);
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionTextView.setText("Finished searching.");
                    progressBar.setProgress(100);
                    scanButtonMethod1.setVisibility(View.VISIBLE);
                    scanButtonMethod2.setVisibility(View.VISIBLE);
                }
            });

            if(noBusesFound) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noBusesTextView.setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }

    private class RunScannerMethod2 extends Thread {

        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionTextView.setText("Searching for route data. Please wait...");
                    actionTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.VISIBLE);
                }
            });
            URL url = null;
            String urlContent;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=0,67&flag=28");
                //10,SECUNDERABAD,SANATH NAGAR;

            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            urlContent = getContentFromURL(url);
            final String[] allRoutes = urlContent.split(";");
            //10,SECUNDERABAD,SANATH NAGAR

            for(int i = 0; i < allRoutes.length && keepScanning; i++) {
                if(allRoutes[i].contains(chosenRoute)) {
                    final int i2 = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionTextView.setText("Inspecting " + allRoutes[i2].split(",")[0]);
                        }
                    });
                    try {
                        url = new URL("http://125.16.1.204:8080//bats/appQuery.do?query=" + allRoutes[i].split(",")[0] + ",0,67&flag=8");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }
                    urlContent = getContentFromURL(url);
                    //183,SECUNDERABAD TO SANATH NAGAR;

                    if(!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                        String singleRouteData[] = urlContent.split(";");
                        ////183,SECUNDERABAD TO SANATH NAGAR
                        for (String singleRoute : singleRouteData) {
                            try {
                                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+  singleRoute.split(",")[0] +",0,67&flag=4");
                            } catch (MalformedURLException e) {
                                e.printStackTrace();
                            }

                            urlContent = getContentFromURL(url);
                            //787,17.43451,78.50125,SECUNDERABAD,183;
                            if(!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                                String[] allStops = urlContent.split(";");
                                for (String stop : allStops) {
                                    if (!validStops.contains(stop.split(",")[3])) {
                                        validStops.add(stop.split(",")[3]);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if(validStops.size() == 0) {
                keepScanning = false;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(SearchBusesOnRouteActivity.this, "Route not found.", Toast.LENGTH_LONG).show();
                        actionTextView.setVisibility(View.INVISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                        scanButtonMethod1.setVisibility(View.VISIBLE);
                        scanButtonMethod2.setVisibility(View.VISIBLE);
                    }
                });
                noBusesTextView.setVisibility(View.INVISIBLE);
                return;
            }

            for (int i = 0; i < validStops.size()-1 && keepScanning;/*dynamic increment*/) {

                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + validStops.get(i).replace("airport pushpak", "airport shamshabad pushpak stop").replace(" ", "%20") + ",0,67&flag=7");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                final int progress = 100*(i+1)/(validStops.size());
                final String currentStopName = validStops.get(i);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(progress);
                        actionTextView.setText("Scanning " + currentStopName);
                    }
                });

                urlContent = getContentFromURL(url);
                //AP11Z7159-METRO EXPRESS,1P/25I,KOTI,10:16:01,645,17.466459,78.505463,OLD ALWAL;

                if (!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                    String[] allBuses;
                    allBuses = urlContent.split(";");

                    for (int n = 0; n < allBuses.length; n++) {
                        allBuses[n] = allBuses[n].replaceFirst("-", ",");
                        if (allBuses[n].split(",")[2].contains(chosenRoute)) {
                            if (!validBuses.contains(allBuses[n].split(",")[0])) {

                                URL url2 = null;
                                try {
                                    url2 = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + allBuses[n].split(",")[5] + "&flag=13");
                                } catch (MalformedURLException e) {
                                    //should never happen
                                }
                                String stopsDataUnformattedString = getContentFromURL(url2);
                                String[] stopsDataFormattedArray = stopsDataUnformattedString.split(";");
                                int lastSeenAtStopIndex = 12345;
                                for (int k = (stopsDataFormattedArray.length)-1; k >=0; k--) {
                                    String singleStopData[] = stopsDataFormattedArray[k].split(",");
                                    if (singleStopData[2].equals("Y")) {
                                        lastSeenAtStopIndex = k;
                                        break;
                                    }
                                }

                                String nextStop = "error";
                                String nextStopETA = "error";

                                if(lastSeenAtStopIndex < stopsDataFormattedArray.length) {
                                    nextStop = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[1];
                                    if(stopsDataFormattedArray[lastSeenAtStopIndex + 1].length() > 10)
                                        nextStopETA = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[4].substring(11, 16);
                                }

                                if(nextStopETA.equals("null"))
                                    continue;

                                validBuses.add(allBuses[n].split(",")[0]);

                                String busType = allBuses[n].split(",")[1];
                                String busRegNum = allBuses[n].split(",")[0];

                                if(busRegNum.equals("AP11Z6086") || busRegNum.equals("AP11Z6087") ||
                                        busRegNum.equals("AP11Z6084") || busRegNum.equals("AP11Z6093") ||
                                        busRegNum.equals("AP11Z6096")) {
                                    busType = "METRO LUXURY AC";
                                }

                                String buttonText = "\n" + busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                                        .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                                        .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                                        .replace("AP11Z4039", "TS07Z4039").replace("AP7Z4004", "TS07Z4004")
                                        .replace("AP7Z4020", "TS07Z4020").replace("AP07Z4008", "TS07Z4008") + "   -   " + busType +
                                        "\nRoute: " + allBuses[n].split(",")[2] +
                                        "\nTowards: " + allBuses[n].split(",")[3] +
                                        "\nNext stop: " + nextStop + " at " + nextStopETA + "\n";

                                final Button b = new Button(SearchBusesOnRouteActivity.this);
                                b.setText(buttonText);
                                if (allBuses[n].split(",")[1].contains("EXPRESS")) {
                                    b.setBackgroundResource(R.drawable.express_bg);
                                    b.setTextColor(Color.WHITE);
                                } else if (allBuses[n].split(",")[1].contains("DELUXE")) {
                                    b.setBackgroundResource(R.drawable.deluxe_bg);
                                    b.setTextColor(Color.WHITE);
                                } else if (allBuses[n].split(",")[1].contains("LOW FLOOR")) {
                                    b.setBackgroundResource(R.drawable.lf_bg);
                                    b.setTextColor(Color.WHITE);
                                } else if (busType.equals("METRO LUXURY AC")) {
                                    b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                    b.setTextColor(Color.WHITE);
                                }

                                final String busRegNumString = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(SearchBusesOnRouteActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", busRegNumString);
                                        keepScanning = false;
                                        startActivity(singleBusIntent);
                                    }
                                });

                                b.setVisibility(View.VISIBLE);

                                noBusesFound = false;

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        busesLinearLayout.addView(b);
                                    }
                                });
                            }
                        }
                    }//for all buses
                }
                if(validStops.size() > 50)
                    i += 5;
                else if(validStops.size() > 25)
                    i += 4;
                else if(validStops.size() > 10)
                    i += 3;
                else //less than 10 stops to search
                    i += 1;
            }//for valid stops

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionTextView.setText("Finished searching.");
                    progressBar.setProgress(100);
                    scanButtonMethod1.setVisibility(View.VISIBLE);
                    scanButtonMethod2.setVisibility(View.VISIBLE);
                }
            });

            if(noBusesFound) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        noBusesTextView.setVisibility(View.VISIBLE);
                    }
                });
            }

        }
    }

    public void onBackPressed() {
        keepScanning = false;
        super.onBackPressed();
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
                    new AlertDialog.Builder(SearchBusesOnRouteActivity.this)
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
                    new AlertDialog.Builder(SearchBusesOnRouteActivity.this)
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
                    new AlertDialog.Builder(SearchBusesOnRouteActivity.this)
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
                    new AlertDialog.Builder(SearchBusesOnRouteActivity.this)
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

            logString = currentDate + "- ROUTE: " + logString;

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

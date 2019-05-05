package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class SearchBusesOnRouteActivity extends AppCompatActivity {

    ArrayList<String> validBuses;
    ArrayList<String> validStops;

    String[] busIdDepotType;

    String chosenRoute;

    ProgressBar progressBar;
    TextView actionTextView;

    boolean keepScanning;
    boolean noBusesFound;

    TextView noBusesTextView;

    Button routeSearchButton;

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

        routeNumEditText = findViewById(R.id.routeNumEditText);

        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        routeSearchButton = findViewById(R.id.routeNumSearchButton);
        routeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                chosenRoute = routeNumEditText.getText().toString().toUpperCase();

                if(chosenRoute.equals(""))
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Please enter something!", Toast.LENGTH_LONG).show();
                else {
                    if(connectedToInternet()) {
                        chosenRoute = chosenRoute.split(" ")[0].split("/")[0];
                        noBusesTextView.setVisibility(View.INVISIBLE);
                        validBuses.clear();
                        validStops.clear();
                        noBusesFound = true;
                        busesLinearLayout.removeAllViews();
                        keepScanning = true;
                        progressBar.setProgress(0);
                        routeSearchButton.setVisibility(View.INVISIBLE);
                        new RunScannerMethod1().start();
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
                e.printStackTrace();
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
                        Toast.makeText(SearchBusesOnRouteActivity.this, "Route not found", Toast.LENGTH_LONG).show();
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
                            progressBar.setProgress((finalCompleted * 50) / finalNumValid);
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
                            url = new URL("http://125.16.1.204:8080//bats/appQuery.do?query=" + allRoutes[i].split(",")[0].replace(" ", "%20") + ",0,67&flag=8");
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        urlContent = getContentFromURL(url);
                        //183,SECUNDERABAD TO SANATH NAGAR;...list of sub-routes WITHIN the main route
                        //eg. 300D will give DSNR->JNTU and JNTU->DSNR as sub-routes
                        //we need those IDs to put in flag=12 to get buses

                        String[] subRoutes = urlContent.split(";");

                        if (!(urlContent.equals("No records found.") || urlContent.equals("null")
                                || urlContent.equals(""))) {

                            //183,SECUNDERABAD TO SANATH NAGAR
                            for (String subRoute : subRoutes) {

                                //building validStops for method 2 is done here only to improve speed
                                try {
                                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + subRoute.split(",")[0] + ",0,67&flag=4");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }

                                urlContent = getContentFromURL(url);
                                //787,17.43451,78.50125,SECUNDERABAD,183;
                                if (!(urlContent.equals("No records found.") || urlContent.equals("null") || urlContent.equals(""))) {
                                    String[] allStops = urlContent.split(";");
                                    for (int j = 0; j < allStops.length; /*dynamic increment*/) {

                                        String stop = allStops[j];
                                        if (!validStops.contains(stop.split(",")[3])) {
                                            validStops.add(stop.split(",")[3]);
                                        }

                                        if (allStops.length > 50)
                                            j += 8;
                                        else if (allStops.length > 35)
                                            j += 6;
                                        else if (allStops.length > 10)
                                            j += 4;
                                        else //less than 10 stops to search
                                            j += 1;
                                    }
                                }
                                //above code is for building validStops used in method 2

                                try {
                                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + subRoute.split(",")[0] + "&flag=12");
                                } catch (MalformedURLException e) {
                                    e.printStackTrace();
                                }
                                urlContent = getContentFromURL(url);
                                //now we have the buses reportedly running on that particular sub-route
                                //1874,AP11Z7281,17.263527,78.389748,RANIGUNJ 2,AP11Z7281-LOW FLOOR AC;
                                //we need to take the IDs and verify if they are live using flag=13 (10 mins rule)

                                if (!urlContent.equalsIgnoreCase("No records found.") && !urlContent.equalsIgnoreCase("null")
                                        && !urlContent.equalsIgnoreCase("")) {
                                    String[] possibleBuses = urlContent.split(";");
                                    for (String bus : possibleBuses) {

                                        //bus = 1874,AP11Z7281,17.263527,78.389748,RANIGUNJ 2,AP11Z7281-LOW FLOOR AC;

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

                                        String busType = null;
                                        String busRegNum = null;

                                        for(int j = 0; j < busIdDepotType.length; j++) {
                                            String[] singleSavedBus = busIdDepotType[j].split(",");
                                            if(singleSavedBus[1].equals(busID)) {
                                                busRegNum = singleSavedBus[0];
                                                busType = singleSavedBus[3].toUpperCase();
                                            }
                                        }

                                        if(!validBuses.contains(busRegNum)) {
                                            validBuses.add(busRegNum);

                                            String lastUpdated = VMUFormatted[5].substring(0, VMUFormatted[5].lastIndexOf("."));

                                            String source = stopsDataFormattedArray[0].split(",")[1];
                                            String destination = stopsDataFormattedArray[stopsDataFormattedArray.length - 1].split(",")[1];

                                            int lastSeenAtStopIndex = 12345;
                                            for (int j = (stopsDataFormattedArray.length) - 1; j >= 0; j--) {
                                                String[] singleStopData = stopsDataFormattedArray[j].split(",");
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
                                                String lastUpdatedTime = lastUpdated.substring(lastUpdated.indexOf(" ") + 1);
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

                                                String buttonText = "\n"
                                                        + busRegNum + "   -   " + busType + "\n" +
                                                        "Route:  " + allRoutes[i].split(",")[0].replace("300D", "300/126") + "\n" +
                                                        "Towards: " + destination + "\n" +
                                                        "Next stop: " + nextStop
                                                        + "\n";

                                                final Button b = new Button(SearchBusesOnRouteActivity.this);
                                                b.setText(buttonText);
                                                if (busType.equals("METRO EXPRESS")) {
                                                    b.setBackgroundResource(R.drawable.express_bg);
                                                    b.setTextColor(Color.WHITE);
                                                } else if (busType.equals("METRO DELUXE")) {
                                                    b.setBackgroundResource(R.drawable.deluxe_bg);
                                                    b.setTextColor(Color.WHITE);
                                                } else if (busType.equals("LOW FLOOR AC")) {
                                                    b.setBackgroundResource(R.drawable.lf_bg);
                                                    b.setTextColor(Color.WHITE);
                                                } else if (busType.equals("METRO LUXURY AC")) {
                                                    b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                                    b.setTextColor(Color.WHITE);
                                                }
                                                final String finalBusRegNum = busRegNum;
                                                b.setOnClickListener(new View.OnClickListener() {
                                                    public void onClick(View v) {
                                                        Intent singleBusIntent = new Intent(SearchBusesOnRouteActivity.this, SingleBusActivity.class);
                                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
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

                //below here is method 2

                for (int j = 0; j < validStops.size()-1 && keepScanning; j++) {

                    try {
                        url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + validStops.get(j).replace("airport pushpak", "airport shamshabad pushpak stop").replace(" ", "%20") + ",0,67&flag=7");
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    final int progress = 50+(50*(j+1)/(validStops.size()));
                    final String currentStopName = validStops.get(j);
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

                            String busID = allBuses[n].split(",")[5];
                            String busRegNum = null;
                            String busType = null;

                            for(int k = 0; k < busIdDepotType.length; k++) {
                                String[] regAndID = busIdDepotType[k].split(",");
                                if(regAndID[1].equals(busID)) {
                                    busRegNum = regAndID[0];
                                    busType = regAndID[3].toUpperCase();
                                }
                            }

                            if (allBuses[n].split(",")[2].contains(chosenRoute)) {
                                if (!validBuses.contains(busRegNum)) {
                                    //*********above line needs to be changed to use modified reg nums

                                    URL url2 = null;
                                    try {
                                        url2 = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=13");
                                    } catch (MalformedURLException e) {
                                        //should never happen
                                    }
                                    String stopsDataUnformattedString = getContentFromURL(url2);
                                    String[] stopsDataFormattedArray = stopsDataUnformattedString.split(";");
                                    int lastSeenAtStopIndex = 12345;
                                    for (int k = (stopsDataFormattedArray.length)-1; k >=0; k--) {
                                        String[] singleStopData = stopsDataFormattedArray[k].split(",");
                                        if (singleStopData[2].equals("Y")) {
                                            lastSeenAtStopIndex = k;
                                            break;
                                        }
                                    }

                                    String nextStop = "undefined";
                                    String nextStopETA = "undefined";

                                    if(lastSeenAtStopIndex < stopsDataFormattedArray.length) {
                                        nextStop = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[1];
                                        if(stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[4].length() > 15)
                                            nextStopETA = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[4].substring(11, 16);
                                    }

                                    if(nextStopETA.equals("undefined"))
                                        continue;

                                    validBuses.add(busRegNum);

                                    String buttonText = "\n" + busRegNum + "   -   " + busType +
                                            "\nRoute: " + allBuses[n].split(",")[2].replace("300D", "300/126") +
                                            "\nTowards: " + allBuses[n].split(",")[3] +
                                            "\nNext stop: " + nextStop + " at " + nextStopETA + "\n";

                                    final Button b = new Button(SearchBusesOnRouteActivity.this);
                                    b.setText(buttonText);
                                    if (busType.equals("METRO EXPRESS")) {
                                        b.setBackgroundResource(R.drawable.express_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if (busType.equals("METRO DELUXE")) {
                                        b.setBackgroundResource(R.drawable.deluxe_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if (busType.equals("LOW FLOOR AC")) {
                                        b.setBackgroundResource(R.drawable.lf_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if(busType.equals("METRO LUXURY AC")) {
                                        b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                        b.setTextColor(Color.WHITE);
                                    }

                                    final String busRegNumString = busRegNum;
                                    b.setOnClickListener(new View.OnClickListener() {
                                        public void onClick(View v) {
                                            Intent singleBusIntent = new Intent(SearchBusesOnRouteActivity.this, SingleBusActivity.class);
                                            singleBusIntent.putExtra("busRegNumString", busRegNumString);
                                            singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
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
                }//for valid stops

                //above here is method 2

            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    actionTextView.setText("Finished searching.");
                    progressBar.setProgress(100);
                    routeSearchButton.setVisibility(View.VISIBLE);
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
        String urlContent = "";

        URLConnection con = null;

        try {
            con = url.openConnection();
        } catch (Exception e) {
            boolean errorFlag = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SearchBusesOnRouteActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
}

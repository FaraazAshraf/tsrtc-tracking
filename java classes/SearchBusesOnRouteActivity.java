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

    ArrayList<String> checkedBuses;
    ArrayList<String> validRoutes;

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

    String[] allGitRoutes;

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
        checkedBuses = new ArrayList<>();
        validRoutes = new ArrayList<>();

        keepScanning = true;

        routeNumEditText = findViewById(R.id.routeNumEditText);

        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        routeSearchButton = findViewById(R.id.routeNumSearchButton);
        routeSearchButton.setVisibility(View.INVISIBLE);

        new GetGitRoutes().start();

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
                        checkedBuses.clear();
                        validRoutes.clear();
                        noBusesFound = true;
                        busesLinearLayout.removeAllViews();
                        keepScanning = true;
                        progressBar.setProgress(0);
                        routeSearchButton.setVisibility(View.INVISIBLE);
                        new SearchRoute().start();
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

    private class GetGitRoutes extends Thread {
        public void run() {

            String urlContent = null;

            try {
                urlContent = getContentFromURL(new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/routes_and_ids"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            allGitRoutes = urlContent.split(";");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    routeSearchButton.setVisibility(View.VISIBLE);
                }
            });
        }
    }

    private class SearchRoute extends Thread {

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
            String urlContent = null;

            for(String subRoute : allGitRoutes) {

                String currentRouteNum = subRoute.split(",")[1].toUpperCase();
                String currentRouteID = subRoute.split(",")[0];

                if(currentRouteNum.contains(chosenRoute) && !validRoutes.contains(currentRouteID + ",")
                && ((chosenRoute.length() >= 3
                        || (chosenRoute.length() <= 2 && currentRouteNum.length() == chosenRoute.length())
                        || (chosenRoute.length() <= 2 && currentRouteNum.substring(currentRouteNum.indexOf(chosenRoute) + chosenRoute.length()).length() >= 1 && currentRouteNum.indexOf(chosenRoute) == 0 && !((int)currentRouteNum.charAt(chosenRoute.length()) >= (int)'0' && (int)currentRouteNum.charAt(chosenRoute.length()) <= (int)'9'))
                        || (chosenRoute.length() <= 2 && currentRouteNum.substring(currentRouteNum.indexOf(chosenRoute) + chosenRoute.length()).length() == 0 && currentRouteNum.indexOf(chosenRoute) >= 1 && !((int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) - 1) >= (int)'0' && (int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) - 1) <= (int)'9'))
                        || (chosenRoute.length() <= 2 && currentRouteNum.substring(currentRouteNum.indexOf(chosenRoute) + chosenRoute.length()).length() >= 1 && currentRouteNum.indexOf(chosenRoute) >= 1 && !((int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) + chosenRoute.length()) >= (int)'0' && (int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) + chosenRoute.length()) <= (int)'9') && !((int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) - 1) >= (int)'0' && (int)currentRouteNum.charAt(currentRouteNum.indexOf(chosenRoute) - 1) <= (int)'9'))
                ))) {
                    //if subRoute ID is not searched yet
                    //if chosenRoute length >= 3, proceed
                    //if chosenRoute length <= 2 then...
                        //if chosenRoute and currentRoute lengths are same eg. 10 and 10 then proceed
                        //else if only next chars exist it should not be a number
                        //if only previous chars exist it should not be a number
                        //if both exist neither should be a number
                    validRoutes.add(subRoute);
                }
            }

            if(validRoutes.size() == 0) {
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

                int numSubRoutesChecked = 0;

                //below here is using git routes
                for (String subRoute : validRoutes) {

                    final String currentRouteNum = subRoute.split(",")[1].toUpperCase();
                    String currentRouteID = subRoute.split(",")[0];

                    numSubRoutesChecked++;
                    final int progress = (100 * (numSubRoutesChecked)) / (validRoutes.size());

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            actionTextView.setText("Inspecting " + currentRouteNum);
                            progressBar.setProgress(progress);
                        }
                    });

                    try {
                        url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + currentRouteID + "&flag=12");
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

                            for (int j = 0; j < busIdDepotType.length; j++) {
                                String[] singleSavedBus = busIdDepotType[j].split(",");
                                if (singleSavedBus[1].equals(busID)) {
                                    busRegNum = singleSavedBus[0];
                                    busType = singleSavedBus[3].toUpperCase();
                                }
                            }

                            if (!checkedBuses.contains(busRegNum)) {
                                checkedBuses.add(busRegNum);

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
                                            "Route:  " + currentRouteNum + "\n" +
                                            "Towards: " + destination + "\n" +
                                            "Next stop: " + nextStop
                                            + "\n";

                                    final Button b = new Button(SearchBusesOnRouteActivity.this);
                                    b.setText(buttonText);
                                    if (busType.equals("METRO EXPRESS")) {
                                        b.setBackgroundResource(R.drawable.blue_button_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if (busType.equals("METRO DELUXE")) {
                                        b.setBackgroundResource(R.drawable.green_button_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if (busType.equals("LOW FLOOR AC")) {
                                        b.setBackgroundResource(R.drawable.red_button_bg);
                                        b.setTextColor(Color.WHITE);
                                    } else if (busType.equals("METRO LUXURY AC")) {
                                        b.setBackgroundResource(R.drawable.pink_button_bg);
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
                //above here is using git routes
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

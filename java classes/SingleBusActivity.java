package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Date;

public class SingleBusActivity extends AppCompatActivity {

    TextView mainTextView;
    TextView busIDTextView;

    String source;
    String destination;
    String lastSeenStopString;
    String lastSeenTimeString;
    String nextStop;

    String busRegNumString;
    String busIDString;
    String busType;
    String busDepot;

    Button showAllStopsButton;
    Button gpsButton;

    boolean keepRefreshing = true;

    SharedPreferences sharedPref;
    Button addFavButton;
    Button removeFavButton;

    String[] busIdDepotType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_bus);

        String preference_file_key = "com.ashraf.faraa.livebus.sharedPrefs";

        sharedPref = this.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        gpsButton = findViewById(R.id.gpsButton);
        gpsButton.setVisibility(View.INVISIBLE);

        mainTextView = findViewById(R.id.mainTextView);
        mainTextView.setGravity(Gravity.CENTER);

        busIDTextView = findViewById(R.id.busIDTextView);
        busIDTextView.setGravity(Gravity.CENTER);

        busRegNumString = getIntent().getExtras().getString("busRegNumString");
        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        mainTextView.setText("Connecting to server...");
        new GetBusIDString().start();

        showAllStopsButton = findViewById(R.id.showAllStopsButton);
        showAllStopsButton.setVisibility(View.INVISIBLE);
        showAllStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connectedToInternet()) {
                    Intent allStopsActivityIntent = new Intent(SingleBusActivity.this, AllStopsActivity.class);
                    allStopsActivityIntent.putExtra("busIDString", busIDString);
                    allStopsActivityIntent.putExtra("busRegNum", busRegNumString);
                    allStopsActivityIntent.putExtra("busType", busType);
                    startActivity(allStopsActivityIntent);
                }
                else
                    Toast.makeText(SingleBusActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                keepRefreshing = false;
                onBackPressed();
            }
        });

        addFavButton = findViewById(R.id.addFavButton);
        removeFavButton = findViewById(R.id.removeFavButton);

        addFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!sharedPref.contains("favouriteBuses")) {
                    String favouriteBuses = "";
                    favouriteBuses += (busIDString + ";");

                    editor.putString("favouriteBuses", favouriteBuses);
                    editor.apply();
                }
                else {
                    String favouriteBuses = sharedPref.getString("favouriteBuses", "DEFAULT");
                    favouriteBuses += (busIDString + ";");

                    editor.putString("favouriteBuses", favouriteBuses);

                    editor.apply();
                }

                addFavButton.setVisibility(View.INVISIBLE);
                removeFavButton.setVisibility(View.VISIBLE);
            }
        });

        removeFavButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String favouriteBuses = sharedPref.getString("favouriteBuses", "DEFAULT");

                String busIDToDelete = busIDString;

                favouriteBuses = favouriteBuses.replace(busIDToDelete + ";", "");

                editor.putString("favouriteBuses", favouriteBuses);

                editor.apply();

                removeFavButton.setVisibility(View.INVISIBLE);
                addFavButton.setVisibility(View.VISIBLE);
            }
        });

    }

    protected void onPause() {
        super.onPause();
        keepRefreshing = false;
    }

    protected void onResume() {
        super.onResume();
        if(!keepRefreshing) {
            keepRefreshing = true;
            new Refresh10Sec().start();
        }
    }

    public void onBackPressed() {
        keepRefreshing = false;
        super.onBackPressed();
    }

    public class Refresh10Sec extends Thread {
        public void run() {
            while(keepRefreshing) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(keepRefreshing) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new GetBusData().start();
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

    private class GetBusData extends Thread {

        public void run() {

            System.out.println(new Date().toString());

            //weHaveStopsData to see if flag=13 has anything
            //VMUDisconnected to see if VMU data is less than 10 days old
            //hey look they're the exact same length UwU
            boolean weHaveStopsData = true;
            boolean VMUDisconnected = false;

            URL url = null;

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busIDString + "&flag=13");
            } catch (MalformedURLException e) {
                //should never happen
            }

            String stopsDataUnformattedString = getContentFromURL(url);
            if(stopsDataUnformattedString.equals("No records found.")) {
                weHaveStopsData = false;
            }

            try {
                url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busIDString + "&flag=21");
            } catch (MalformedURLException e) {
                //should never happen
            }

            String VMUData = getContentFromURL(url);
            String[] VMUFormatted = VMUData.split(",");

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    busIDTextView.setVisibility(View.VISIBLE);
                }
            });


            if(VMUData.equals("No records found.")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mainTextView.setText("This bus has been rebuilt or scrapped.\nGPS has been disconnected.");
                    }
                });
                keepRefreshing = false;
            }
            else {

                /*now we have a number of cases to consider
                1. bus was not updated for 10 days
                2. bus was updated in 10 days, but flag=13 has no result
                3. both links have result, now here some subcases
                    i. if bus is in depot, ignore everything else and display as not running
                    ii. bus is not in depot, but flag=13 is wrong
                    iii. flag=13 needs to be checked if wrong or not, and display accordingly
                 */


                String busStatus = VMUFormatted[10].toUpperCase();

                String busLandmark = VMUFormatted[4].toUpperCase();
                busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];
                busLandmark = "near " + busLandmark;

                final String lastUpdated = VMUFormatted[5].substring(0,VMUFormatted[5].lastIndexOf("."));

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        gpsButton.setVisibility(View.VISIBLE);
                    }
                });
                gpsButton.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        //startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://www.google.com/maps/place/"+gpsCoords+"/@"+gpsCoords+",12z")));
                        Intent tracking = new Intent(SingleBusActivity.this, GPSActivity.class);
                        tracking.putExtra("busID", busIDString);
                        startActivity(tracking);
                    }
                });

                boolean differentMonth = false;

                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                double currentDateDouble = Double.parseDouble(currentDate.replaceAll("-", "").replaceAll(":","").replaceAll(" ", ""));

                if(!currentDate.substring(5,7).equals(lastUpdated.substring(5,7))) {
                    differentMonth = true;
                }

                double lastUpdatedDouble = Double.parseDouble(lastUpdated.replaceAll("-", "").replaceAll(":","").replaceAll(" ", ""));

                //now we have two doubles, currentDateDouble and lastUpdatedDouble
                //both are of the format yyyyMMddHHmmSS
                //we need to verify that the bus was updated in the past 10 days
                //currentDateDouble - lastUpdatedDouble should be greater than 10 days

                if(((currentDateDouble - lastUpdatedDouble > 10000000) && (!differentMonth))
                || ((differentMonth) && (currentDateDouble-lastUpdatedDouble > 80000000))) {
                    keepRefreshing = false;
                    VMUDisconnected = true;
                    // if the bus was not updated for 10 days, it means VMU disconnected
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            gpsButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }

                //case 1: if buses haven't been updated for 10+ days
                if(VMUDisconnected) {
                    if(busStatus.equalsIgnoreCase("Buses in depot")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText(busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n in " + busDepot + " DEPOT\n" +
                                        "Location last updated:\n" + lastUpdated
                                        + "\n\nGPS device is currently offline.");
                            }
                        });
                    }
                    else {
                        final String finalBusLandmark = busLandmark;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText(busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n" + finalBusLandmark +
                                        "\nLocation last updated:\n" + lastUpdated
                                        + "\n\nGPS device is currently offline.");
                            }
                        });
                    }
                }

                // case 2: if flag=21 was updated in 10 days but flag=13 gives nothing
                else if(!weHaveStopsData) {
                    if(busStatus.equalsIgnoreCase("Buses in depot")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText("Bus is currently not running.\n\n"+
                                        busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n in " + busDepot + " DEPOT\n" +
                                        "Location last updated:\n" + lastUpdated);
                            }
                        });
                    }
                    else {
                        final String finalBusLandmark1 = busLandmark;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText(busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n" + finalBusLandmark1 +
                                        "\nLocation last updated:\n" + lastUpdated);
                            }
                        });
                    }
                }
                else {
                    //case 3: both links have some result. need to check if flag=13 is live.

                    String[] stopsDataFormattedArray = stopsDataUnformattedString.split(";");
                    source = stopsDataFormattedArray[0].split(",")[1];
                    destination = stopsDataFormattedArray[stopsDataFormattedArray.length - 1].split(",")[1];

                    int lastSeenAtStopIndex = 12345;
                    for (int i = (stopsDataFormattedArray.length)-1; i >=0; i--) {
                        String[] singleStopData = stopsDataFormattedArray[i].split(",");
                        if (singleStopData[2].equals("Y")) {
                            lastSeenAtStopIndex = i;
                            break;
                        }
                    }

                    //now starts the various conditions
                    //case 3. i: if bus is in depot
                    if(busStatus.equalsIgnoreCase("Buses in Depot")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText("Bus is currently not running. \n\n"+
                                        busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n in " + busDepot + " DEPOT\n" +
                                        "Location last updated:\n" + lastUpdated);
                            }
                        });
                    }

                    //case 3. ii:
                    //flag=13 data is wrong (not a single stop passed || last stop is Y).
                    //so, display as if no stop data
                    else if(lastSeenAtStopIndex == 12345 ||
                            lastSeenAtStopIndex == (stopsDataFormattedArray.length)-1) {
                        final String finalBusLandmark2 = busLandmark;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText(busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n" + finalBusLandmark2 + "\n" +
                                        "Location last updated:\n" + lastUpdated);
                            }
                        });
                    }

                    //case 3. iii:
                    //bus has been on SOME of the stops on the programmed route (not last stop).
                    //we now need to see if the bus is still on that route
                    else {
                        nextStop = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[1];
                        String nextStopEta = stopsDataFormattedArray[lastSeenAtStopIndex + 1].split(",")[4];
                        if (!nextStopEta.equalsIgnoreCase("null"))
                            nextStopEta = nextStopEta.substring(11, 19);

                        lastSeenTimeString = stopsDataFormattedArray[lastSeenAtStopIndex].split(",")[4];
                        lastSeenStopString = stopsDataFormattedArray[lastSeenAtStopIndex].split(",")[1];

                        //2018-10-21 12:04:35,
                        boolean sameHour = false;
                        boolean sameDay = false;

                        if(lastSeenTimeString.substring(11,13).equals(lastUpdated.substring(11,13)))
                            sameHour = true;
                        if(lastSeenTimeString.substring(8,10).equals(lastUpdated.substring(8,10)))
                            sameDay = true;

                        double timeDifference = Double.parseDouble(lastUpdated.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""))
                                - Double.parseDouble(lastSeenTimeString.replaceAll("-", "").replaceAll(" ", "").replaceAll(":", ""));

                        lastSeenTimeString = lastSeenTimeString.substring(11,19);
                    /*if last seen stop was more than 10 mins ago
                        and there is no ETA for the next stop
                        and it is not an airport bus
                        then bus is probably on a different route, so display as if no stop data*/
                        if(((sameHour && timeDifference >= 1000)
                                ||
                                (!sameHour && sameDay && timeDifference >= 5000)
                                        || (!sameDay && timeDifference >= 765000))
                                && (nextStopEta.equalsIgnoreCase("null"))
                                && (!destination.contains("AIRPORT"))
                                && (!source.contains("AIRPORT"))) {
                            final String finalBusLandmark3 = busLandmark;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mainTextView.setText(busRegNumString + " - " + busType +
                                            "\nDepot: " + busDepot + "\n\n" +
                                            "Last known location:\n" + finalBusLandmark3 + "\n" +
                                            "Location last updated: \n" + lastUpdated);
                                    showAllStopsButton.setVisibility(View.INVISIBLE);
                                }
                            });

                        }

                        //if the bus is on the flag=13 route (last stop seen less than 10 mins ago)
                        //then display that route data
                        else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    showAllStopsButton.setVisibility(View.VISIBLE);
                                }
                            });
                            if (!nextStopEta.equalsIgnoreCase("null")) {
                                final String finalNextStopEta = nextStopEta;
                                final String finalBusLandmark5 = busLandmark;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainTextView.setText(busRegNumString + " - " + busType + "\n" +
                                                "Depot: " + busDepot +
                                                "\n\nBus route:\n" + source + "\nto:\n"
                                                + destination
                                                + "\n\nLast seen at " + lastSeenTimeString + " at\n"+ lastSeenStopString
                                                + "\n\nNext stop:\n" + nextStop
                                                + "\nETA:\n" + finalNextStopEta + "\n\n" +
                                                "Last known location:\n" + finalBusLandmark5 +"\n" +
                                                "Location last updated: \n" + lastUpdated);
                                    }
                                });
                            }
                            else {
                                final String finalBusLandmark4 = busLandmark;
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mainTextView.setText(busRegNumString + " - " + busType + "\n" +
                                                "Depot: " + busDepot +
                                                "\n\nBus route:\n" + source + "\nto:\n"
                                                + destination
                                                + "\n\nLast seen at " + lastSeenTimeString + " at\n"+ lastSeenStopString
                                                + "\n\nNext stop:\n" + nextStop + "\n\n" +
                                                "Last known location:\n" + finalBusLandmark4 +"\n" +
                                                "Location last updated: \n" + lastUpdated);
                                    }
                                });
                            }
                        }
                    }
                }//flag=13 gives something
            } //else if we have VMU data
        }//run()
    }

    private class GetBusIDString extends Thread {
        public void run() {

            for(int i = 0; i < busIdDepotType.length; i++) {
                String[] regAndID = busIdDepotType[i].split(",");

                if(regAndID[0].equals(busRegNumString)) {
                    busIDString = regAndID[1];
                    busDepot = regAndID[2];
                    busType = regAndID[3].toUpperCase();
                }
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    busIDTextView.setText("Bus ID:\n" + busIDString);
                    if(connectedToInternet()) {
                        new GetBusData().start();
                        keepRefreshing = true;
                        new Refresh10Sec().start();
                    }
                    else {
                        Toast.makeText(SingleBusActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
            });

            if(!sharedPref.contains("favouriteBuses")) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        addFavButton.setVisibility(View.VISIBLE);
                    }
                });
            }
            else {
                String favouriteBuses = sharedPref.getString("favouriteBuses", "No records found.");
                if(favouriteBuses.contains(busIDString + ";")) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            removeFavButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
                else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addFavButton.setVisibility(View.VISIBLE);
                        }
                    });
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
                    Toast.makeText(SingleBusActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SingleBusActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SingleBusActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(SingleBusActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

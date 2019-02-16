package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import java.util.Dictionary;
import java.util.Hashtable;

public class SingleBusActivity extends AppCompatActivity {

    TextView mainTextView;
    TextView busIDTextView;
    String source;
    String destination;
    String lastSeenStopString;
    String lastSeenTimeString;
    String nextStop;
    String busRegNumStringWrong;
    String busRegNumString;
    final Dictionary<String, String> busesAndIDs = new Hashtable();

    String busType;

    Button showAllStopsButton;
    Button gpsButton;

    boolean keepRefreshing = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_bus);

        gpsButton = findViewById(R.id.gpsButton);
        gpsButton.setVisibility(View.INVISIBLE);

        mainTextView = findViewById(R.id.mainTextView);
        mainTextView.setGravity(Gravity.CENTER);

        busIDTextView = findViewById(R.id.busIDTextView);
        busIDTextView.setGravity(Gravity.CENTER);

        busRegNumString = getIntent().getExtras().getString("busRegNumString");

        busRegNumString = busRegNumString.replace("TS07Z3998", "AP11Z3998").replace("TS07Z4017", "AP11Z4017")
                .replace("TS07Z4015", "AP11Z4015").replace("TS07Z3998", "AP11Z4040")
                .replace("TS07Z4041", "AP11Z4041").replace("TS07Z4046", "AP11Z4046")
                .replace("TS07Z4039", "AP11Z4039").replace("TS07Z4004", "AP7Z4004")
                .replace("TS07Z4020", "AP7Z4020").replace("TS07Z4008", "AP07Z4008");

        mainTextView.setText("Searching...");
        busRegNumStringWrong = busRegNumString;
        busRegNumString = busRegNumString.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                .replace("AP11Z4039", "TS07Z4039").replace("AP7Z4004", "TS07Z4004")
                .replace("AP7Z4020", "TS07Z4020").replace("AP07Z4008", "TS07Z4008");

        new PopulateDictionary().start();

        showAllStopsButton = findViewById(R.id.showAllStopsButton);
        showAllStopsButton.setVisibility(View.INVISIBLE);
        showAllStopsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connectedToInternet()) {
                    Intent allStopsActivityIntent = new Intent(SingleBusActivity.this, AllStopsActivity.class);
                    allStopsActivityIntent.putExtra("busIDString", busesAndIDs.get(busRegNumStringWrong));
                    allStopsActivityIntent.putExtra("busRegNum", busRegNumString);
                    allStopsActivityIntent.putExtra("busType", busType);
                    startActivity(allStopsActivityIntent);
                }
                else
                    Toast.makeText(SingleBusActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
            }
        });

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                keepRefreshing = false;
                onBackPressed();
            }
        });

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
                            new GetBusData(busesAndIDs.get(busRegNumStringWrong)).start();
                        }
                    });
                }
            }
        }
    }

    private class GetBusData extends Thread {

        String busIDString;

        public GetBusData(String busIDString) {
            this.busIDString = busIDString;
        }

        public void run() {

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
            if(stopsDataUnformattedString.equals("No records found."))
                weHaveStopsData = false;

            URL url2 = null;
            try {
                url2 = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busIDString + "&flag=21");
            } catch (MalformedURLException e) {
                //should never happen
            }

            String VMUData = getContentFromURL(url2);
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
                        mainTextView.setText("GPS has been disconnected.");
                    }
                });
                keepRefreshing = false;
            }
            else {
                final String gpsCoords = VMUFormatted[6] + "," + VMUFormatted[7];
                String busStatus = VMUFormatted[10].toUpperCase();
                final String busDepot = VMUFormatted[8].toUpperCase();
                String busLandmark = VMUFormatted[4].toUpperCase();
                busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];
                busLandmark = "near " + busLandmark;
                busType = VMUFormatted[9].toUpperCase().replace("INDRA","RAJADHANI");
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
                        Intent tracking = new Intent(SingleBusActivity.this, ViewLocationActivity.class);
                        tracking.putExtra("busID", busIDString);
                        startActivity(tracking);
                    }
                });

                if(busRegNumString.equals("AP11Z6086") || busRegNumString.equals("AP11Z6087") ||
                        busRegNumString.equals("AP11Z6084") || busRegNumString.equals("AP11Z6093") ||
                        busRegNumString.equals("AP11Z6096")
                || busRegNumString.contains("TS10")) {
                    busType = "METRO LUXURY AC";
                }

                boolean differentMonth = false;

                String currentDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
                currentDate = currentDate.replaceAll("-", "").replaceAll(":","").replaceAll(" ", "");
                double currentDateDouble = Double.parseDouble(currentDate);
                String lastUpdated2 = lastUpdated;
                if(!currentDate.substring(5,7).equals(lastUpdated2.substring(5,7))) {
                    differentMonth = true;
                }
                lastUpdated2 = lastUpdated2.replaceAll("-", "").replaceAll(":","").replaceAll(" ", "");
                double lastUpdatedDouble = Double.parseDouble(lastUpdated2);

                if(((currentDateDouble-lastUpdatedDouble > 10000000) && (!differentMonth))
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

                //if buses haven't been updated for 10+ days
                if(VMUDisconnected) {
                    if(busStatus.equalsIgnoreCase("Buses in depot")) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mainTextView.setText(busRegNumString + "  -  " + busType + "\n" +
                                        "Depot: " + busDepot + "\n\n " +
                                        "Last known location:\n in " + busDepot + " DEPOT\n" +
                                        "Location last updated:\n" + lastUpdated
                                        + "\n\nVMU GPS device has been disconnected.\nLive data not available.");
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
                                        + "\n\nVMU GPS device has been disconnected.\nLive data not available.");
                            }
                        });
                    }
                }
                //if flag=21 gives something but flag=13 gives nothing
                else if(!weHaveStopsData) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAllStopsButton.setVisibility(View.INVISIBLE);
                        }
                    });
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
                    //we have both GPS data and STOPS data (STOPS may not be current)
                    String[] stopsDataFormattedArray = stopsDataUnformattedString.split(";");
                    source = stopsDataFormattedArray[0].split(",")[1];
                    destination = stopsDataFormattedArray[stopsDataFormattedArray.length - 1].split(",")[1];

                    int lastSeenAtStopIndex = 12345;
                    for (int i = (stopsDataFormattedArray.length)-1; i >=0; i--) {
                        String singleStopData[] = stopsDataFormattedArray[i].split(",");
                        if (singleStopData[2].equals("Y")) {
                            lastSeenAtStopIndex = i;
                            break;
                        }
                    }

                    //now starts the various conditions

                    //if bus is in depot
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

                    //oute data is wrong (bus hasn't even passed a single stop on the set route).
                    //so, display as if no stop data
                    //the condition after the || indicates a rare case where the last stop is counted as PASSED
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

                    //bus has been on SOME of the stops on the programmed route.
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
                        and the bus is not an airport bus
                        then bus is probably on a different route, so display as if no stop data*/
                        if(((sameHour && timeDifference >= 1000)
                                ||
                                (!sameHour && sameDay && timeDifference >= 5000)
                                        || (!sameDay && timeDifference >= 765000))
                                && (nextStopEta.equalsIgnoreCase("null"))
                                && (!destination.contains("AIRPORT") && !destination.contains("PUSHPAK"))
                                && (!source.contains("AIRPORT") && !source.contains("PUSHPAK"))) {
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

    private class PopulateDictionary extends Thread {
        public void run() {
            String urlContent = null;
            try {
                urlContent = getContentFromURL(new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/tsrtc-buses"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            String fileContentsArray[] = urlContent.split(";");

            for(int i = 0; i < fileContentsArray.length; i++) {
                String regAndID[] = fileContentsArray[i].split(",");
                busesAndIDs.put(regAndID[0], regAndID[1]);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    busIDTextView.setText("Bus ID:\n" + busesAndIDs.get(busRegNumStringWrong));
                    if(connectedToInternet())
                        new GetBusData(busesAndIDs.get(busRegNumStringWrong)).start();
                    else {
                        Toast.makeText(SingleBusActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    keepRefreshing = true;
                    new Refresh10Sec().start();
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
                    new AlertDialog.Builder(SingleBusActivity.this)
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
                    new AlertDialog.Builder(SingleBusActivity.this)
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
                    new AlertDialog.Builder(SingleBusActivity.this)
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
                    new AlertDialog.Builder(SingleBusActivity.this)
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
}

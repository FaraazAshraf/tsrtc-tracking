package com.ashraf.faraa.livebus;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
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

public class ShowAirportBusesActivity extends AppCompatActivity {

    String route;
    LinearLayout airportBusesLinearLayout;
    String[] busIdDepotType;

    ArrayList<String> possibleIDs = new ArrayList<>();

    TextView airportBusesSearchingTextView;
    TextView noAirportBusesTextView;
    ProgressBar showAirportBusesProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_airport_buses);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        airportBusesLinearLayout = findViewById(R.id.airportBusesLinearLayout);

        route = getIntent().getStringExtra("route");

        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        airportBusesSearchingTextView = findViewById(R.id.airportBusesSearchingTextView);
        showAirportBusesProgressBar = findViewById(R.id.showAirportBusesProgressBar);
        noAirportBusesTextView = findViewById(R.id.noAirportBusesTextView);
        noAirportBusesTextView.setVisibility(View.INVISIBLE);

        airportBusesSearchingTextView.setText("Searching route: " + route);

        new ShowBuses().start();

    }

    class ShowBuses extends Thread {
        public void run() {

            int shownBuses = 0;
            int i = 0;

            if(route.equals("AB")) {
                for(String singleBus : busIdDepotType) {
                    String[] bus = singleBus.split(",");
                    if((bus[2].equals("MIYAPUR 2") || bus[2].equals("BHEL")) && bus[3].equals("Metro Luxury AC")) {
                        possibleIDs.add(bus[1]);
                    }
                }
                for(String id : possibleIDs) {
                    
                    i++;
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAirportBusesProgressBar.setProgress(finalI *100/possibleIDs.size());
                        }
                    });
                    
                    String urlContent = null;
                    try {
                        urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=21"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    String busLandmark = urlContent.split(",")[4].toUpperCase();
                    busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];

                    double busX = Double.parseDouble(urlContent.split(",")[6]);
                    double busY = Double.parseDouble(urlContent.split(",")[7]);

                    String busRegNum = null;
                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(id)) {
                            busRegNum = regAndID[0];
                        }
                    }

                    if(!(urlContent.split(",")[10].equals("Buses in Depot") || urlContent.split(",")[10].equals("Inactive") || urlContent.split(",")[10].equalsIgnoreCase("VMU Logged Out"))) {
                        try {
                            urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=13"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if(!urlContent.equals("No records found.")) {

                            String destination = urlContent.split(";")[urlContent.split(";").length - 1].split(",")[1];
                            if(destination.equals("AIRPORT DROP")) {
                                destination = "AIRPORT";
                            }
                            else if(destination.contains("KEERTHI")) {
                                destination = "BHEL";
                            }

                            if(!urlContent.contains("JNTU") && busRegNum.contains("TS10UB")) {

                                String buttonText;

                                if(busX < 17.238879) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Last seen: near "+ busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                        else {
                            if((busRegNum.contains("TS10UB") && busY < 78.341148 && busX > 17.435108) || //Miyapur towards BHEL above ORR
                                    (busX > 17.451958 && busY < 78.380946 && busX < 17.493825 && busY > 78.350763) || //Between Cyber Towers and Allwyn
                                    (busRegNum.contains("TS09Z") && busX < 17.438200 && busY < 78.363892) || //Volvos on ORR and airport
                                    (busRegNum.contains("TS09Z") && busX < 17.378939 && busY < 78.455820)) {
                                String buttonText;
                                if(busX < 17.238879 && busRegNum.contains("TS09Z")) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: BHEL\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Route: AB\n" +
                                            "Last seen: near " + busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);

                                if(busRegNum.contains("TS10UB"))
                                    b.setBackgroundResource(R.drawable.green_button_bg);
                                else
                                    b.setBackgroundResource(R.drawable.pink_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                    }
                }
            }

            else if(route.equals("AJ")) {
                for(String singleBus : busIdDepotType) {
                    String[] bus = singleBus.split(",");
                    if(bus[2].equals("MIYAPUR 2") && bus[3].equals("Metro Luxury AC")) {
                        possibleIDs.add(bus[1]);
                    }
                }
                for(String id : possibleIDs) {
                    
                    i++;
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAirportBusesProgressBar.setProgress(finalI *100/possibleIDs.size());
                        }
                    });
                    
                    String urlContent = null;
                    try {
                        urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=21"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    String busLandmark = urlContent.split(",")[4].toUpperCase();
                    busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];

                    double busX = Double.parseDouble(urlContent.split(",")[6]);
                    double busY = Double.parseDouble(urlContent.split(",")[7]);

                    String busRegNum = null;
                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(id)) {
                            busRegNum = regAndID[0];
                        }
                    }

                    if(!(urlContent.split(",")[10].equals("Buses in Depot") || urlContent.split(",")[10].equals("Inactive") || urlContent.split(",")[10].equalsIgnoreCase("VMU Logged Out"))) {
                        try {
                            urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=13"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if(!urlContent.equals("No records found.")) {

                            String destination = urlContent.split(";")[urlContent.split(";").length - 1].split(",")[1];
                            if(destination.equals("AIRPORT DROP")) {
                                destination = "AIRPORT";
                            }
                            else if(destination.contains("MIYAPUR")) {
                                destination = "MIYAPUR";
                            }

                            if(!urlContent.contains("LINGAMPALLY")) {

                                String buttonText;

                                if(busX < 17.238879) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Last seen: near "+ busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                        else {
                            if((busY > 78.381132 && busX > 17.452315) //Cyber Towers to JNTU and Miyapur to JNTU
                                ||(busX > 17.493368 && busY > 78.362240)) {
                                String buttonText;
                                buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                        "Route: AJ\n" +
                                        "Last seen: near "+ busLandmark + "\n");

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                    }
                }
            }

            else if(route.equals("AC")) {
                for(String singleBus : busIdDepotType) {
                    String[] bus = singleBus.split(",");
                    if(bus[2].equals("RANIGUNJ 2") && bus[3].equals("Metro Luxury AC")) {
                        possibleIDs.add(bus[1]);
                    }
                }
                for(String id : possibleIDs) {
                    
                    i++;
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAirportBusesProgressBar.setProgress(finalI *100/possibleIDs.size());
                        }
                    });
                    
                    String urlContent = null;
                    try {
                        urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=21"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    String busLandmark = urlContent.split(",")[4].toUpperCase();
                    busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];

                    double busX = Double.parseDouble(urlContent.split(",")[6]);
                    double busY = Double.parseDouble(urlContent.split(",")[7]);

                    String busRegNum = null;
                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(id)) {
                            busRegNum = regAndID[0];
                        }
                    }

                    if(!(urlContent.split(",")[10].equals("Buses in Depot") || urlContent.split(",")[10].equals("Inactive") || urlContent.split(",")[10].equalsIgnoreCase("VMU Logged Out"))) {
                        try {
                            urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=13"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if(!urlContent.equals("No records found.")) {

                            String destination = urlContent.split(";")[urlContent.split(";").length - 1].split(",")[1];
                            if(destination.equals("AIRPORT DROP")) {
                                destination = "AIRPORT";
                            }
                            else if(destination.contains("AIRPORT")) {
                                destination = "AIRPORT";
                            }

                            if(urlContent.contains("SHAMSHABAD,Y") || urlContent.contains("AIRPORT OLD PUSHPAK ,Y")
                            || urlContent.contains("SHAMSHABAD,N,4,20") || urlContent.contains("AIRPORT OLD PUSHPAK ,N,5,20")
                            || urlContent.contains("SHAMSHABAD,N,2,20") || busX < 17.395477) {

                                String buttonText;

                                if(busX < 17.238879) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Last seen: near "+ busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.pink_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                        else {
                            if(busX < 17.395477 && busX > 17.217574 && busY < 78.474433) { //below Rethibowli, bounded by airport and MP
                                String buttonText;

                                if(busX < 17.238879 && busRegNum.contains("TS09Z")) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: JBS\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Route: AC\n" +
                                            "Last seen: near " + busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.pink_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                    }
                }
            }

            else if(route.equals("AM")) {
                for(String singleBus : busIdDepotType) {
                    String[] bus = singleBus.split(",");
                    if(bus[2].equals("CANTONMENT") && bus[3].equals("Metro Luxury AC")) {
                        possibleIDs.add(bus[1]);
                    }
                }
                
                for(String id : possibleIDs) {
                    
                    i++;
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAirportBusesProgressBar.setProgress(finalI *100/possibleIDs.size());
                        }
                    });
                    
                    String urlContent = null;
                    try {
                        urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=21"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    String busLandmark = urlContent.split(",")[4].toUpperCase();
                    busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];

                    double busX = Double.parseDouble(urlContent.split(",")[6]);
                    double busY = Double.parseDouble(urlContent.split(",")[7]);

                    String busRegNum = null;
                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(id)) {
                            busRegNum = regAndID[0];
                        }
                    }

                    if(!(urlContent.split(",")[10].equals("Buses in Depot") || urlContent.split(",")[10].equals("Inactive") || urlContent.split(",")[10].equalsIgnoreCase("VMU Logged Out"))) {
                        try {
                            urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=13"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if(!urlContent.equals("No records found.")) {

                            String destination = urlContent.split(";")[urlContent.split(";").length - 1].split(",")[1];
                            if(destination.equals("AIRPORT DROP")) {
                                destination = "AIRPORT";
                            }
                            else if(destination.equals("PICKET")) {
                                destination = "JBS";
                            }
                            else if(destination.equals("JUBILEE BUS STATION")) {
                                destination = "JBS";
                            }

                            if(urlContent.contains("ARAMGHAR")) {
                                String buttonText;

                                if(busX < 17.238879) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Last seen: near "+ busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                            else if(((busY < 78.424735) || (busX > 17.293774 && busY < 78.495649))) {
                                String buttonText;
                                buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                        "Route: AM\n" +
                                        "Last seen: near "+ busLandmark + "\n");

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                        else {
                            if((busY < 78.424735) || (busX > 17.293774 && busY < 78.495649)) {
                                String buttonText;
                                buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                        "Route: AM\n" +
                                        "Last seen: near "+ busLandmark + "\n");

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                    }
                }
            }

            else if(route.equals("AL")) {
                //6984, 6985
                for(String singleBus : busIdDepotType) {
                    String[] bus = singleBus.split(",");
                    if(bus[2].equals("CANTONMENT") && bus[3].equals("Metro Luxury AC")) {
                        possibleIDs.add(bus[1]);
                    }
                }
                for(String id : possibleIDs) {
                    
                    i++;
                    final int finalI = i;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showAirportBusesProgressBar.setProgress(finalI *100/possibleIDs.size());
                        }
                    });
                    
                    String urlContent = null;
                    try {
                        urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=21"));
                    } catch (MalformedURLException e) {
                        e.printStackTrace();
                    }

                    String busLandmark = urlContent.split(",")[4].toUpperCase();
                    busLandmark = busLandmark.split(" FROM ")[busLandmark.split(" FROM ").length-1];

                    double busX = Double.parseDouble(urlContent.split(",")[6]);
                    double busY = Double.parseDouble(urlContent.split(",")[7]);

                    String busRegNum = null;
                    for(int j = 0; j < busIdDepotType.length; j++) {
                        String[] regAndID = busIdDepotType[j].split(",");
                        if(regAndID[1].equals(id)) {
                            busRegNum = regAndID[0];
                        }
                    }

                    if(!(urlContent.split(",")[10].equals("Buses in Depot") || urlContent.split(",")[10].equals("Inactive") || urlContent.split(",")[10].equalsIgnoreCase("VMU Logged Out"))) {
                        try {
                            urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+id+"&flag=13"));
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        }
                        if(!urlContent.equals("No records found.")) {

                            String destination = urlContent.split(";")[urlContent.split(";").length - 1].split(",")[1];
                            if(destination.equals("AIRPORT DROP")) {
                                destination = "AIRPORT";
                            }
                            else if(destination.equals("PICKET")) {
                                destination = "JBS";
                            }
                            else if(destination.equals("JUBILEE BUS STATION")) {
                                destination = "JBS";
                            }

                            if(!urlContent.contains("ARAMGHAR")) {

                                String buttonText;

                                if(busX < 17.238879) {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Current location: AIRPORT PUSHPAK STOP\n");
                                }
                                else {
                                    buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                            "Destination: " + destination + "\n" +
                                            "Last seen: near "+ busLandmark + "\n");
                                }

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                            else if((busX < 17.386835 && busY > 78.438962) || (busX < 17.431044 && busY > 78.506699)) {
                                String buttonText;
                                buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                        "Route: AL\n" +
                                        "Last seen: near " + busLandmark + "\n");

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                        else {
                            if((busX < 17.386835 && busY > 78.438962) || (busX < 17.431044 && busY > 78.506699)) {
                                String buttonText;
                                buttonText = ("\n" + busRegNum + "   -   " + "METRO LUXURY AC" + "\n" +
                                        "Route: AL\n" +
                                        "Last seen: near "+ busLandmark + "\n");

                                final Button b = new Button(ShowAirportBusesActivity.this);
                                b.setText(buttonText);
                                b.setBackgroundResource(R.drawable.green_button_bg);
                                b.setTextColor(Color.WHITE);
                                final String finalBusRegNum = busRegNum;
                                b.setOnClickListener(new View.OnClickListener() {
                                    public void onClick(View v) {
                                        Intent singleBusIntent = new Intent(ShowAirportBusesActivity.this, SingleBusActivity.class);
                                        singleBusIntent.putExtra("busRegNumString", finalBusRegNum);
                                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                        startActivity(singleBusIntent);
                                    }
                                });
                                b.setVisibility(View.VISIBLE);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        airportBusesLinearLayout.addView(b);
                                    }
                                });
                                shownBuses++;
                            }
                        }
                    }
                }
            }

            final int finalShownBuses = shownBuses;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    airportBusesSearchingTextView.setText("Finished searching.");
                    showAirportBusesProgressBar.setVisibility(View.INVISIBLE);
                    if(finalShownBuses == 0) {
                        noAirportBusesTextView.setVisibility(View.VISIBLE);
                        airportBusesSearchingTextView.setVisibility(View.INVISIBLE);
                    }
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
                    Toast.makeText(ShowAirportBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ShowAirportBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ShowAirportBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ShowAirportBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

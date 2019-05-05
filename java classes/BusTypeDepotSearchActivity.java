package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class BusTypeDepotSearchActivity extends AppCompatActivity {

    AutoCompleteTextView busTypesACTV;
    String[] busTypes = {"ALL TYPES", "METRO EXPRESS", "METRO DELUXE", "LOW FLOOR AC", "METRO LUXURY AC", "EXPRESS", "DELUXE", "GARUDA", "GARUDA PLUS", "RAJADHANI", "METRO EXPRESS AL", "SUPER LUXURY"};

    AutoCompleteTextView busDepotsACTV;
    String[] depots = {"ALL DEPOTS", "MUSHEERABAD 2", "HAYATNAGAR 2", "MIYAPUR 2", "HAYATNAGAR 1", "MIDHANI", "BHEL", "NAGAR KURNOOL", "KARIMNAGAR 1", "WANAPARTHY", "KARIMNAGAR 2", "BARKATPURA", "SIRICILLA", "GADWAL", "VEMULAWADA", "GODAVARIKHANI", "UPPAL", "HCU", "CANTONMENT", "MUSHEERABAD 1", "KUKATPALLY", "FAROOQNAGAR", "CHENGICHERLA", "DILSUKH NAGAR", "RANIGUNJ 2", "KUSHAIGUDA", "RANIGUNJ 1", "RAJENDRANAGAR", "FALAKNUMA", "IBRAHIMPATNAM", "MEDCHAL", "KACHIGUDA", "WARANGAL 1", "MEHDIPATNAM", "HAKIMPET", "BANDLAGUDA", "KOLLAPUR", "MIYAPUR 1", "JEEDIMETLA", "MANTHANI", "METPALLI", "KORUTLA", "JAGITYALA", "MANCHIRYALA", "NARAYANPET", "MAHBUBNAGAR", "SHADNAGAR", "HYDERABAD 2", "PICKET", "HYDERABAD 3", "HYDERABAD 1", "ASIFABAD", "DUBBAK", "HUSNABAD", "SIDDIPET", "ZAHEERABAD", "GAJWELPRAGNAPUR", "NIZAMABAD 2", "MEDAK", "MIRYALAGUDA", "MANUGURU", "KHAMMAM", "MADHIRA", "UTNOOR", "BHADRACHALAM", "ADILABAD", "HANMAKONDA", "NARASMPET", "KOTHAGUDEM", "SATTUPALLY"};

    AutoCompleteTextView typeOfSearchACTV;
    String[] searchTypes = {"RUNNING BUSES (ACTIVE)", "INACTIVE BUSES", "ALL BUSES"};

    ProgressBar progressBar;
    TextView progressTextView;

    Button searchButton;

    LinearLayout busesLinearLayout;

    boolean continueSearching = false;
    boolean searchInProgress = false;

    TextView noBusesTextView;

    String[] busIdDepotType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bus_type_depot_search);

        noBusesTextView = findViewById(R.id.noBusesTextView);
        noBusesTextView.setVisibility(View.INVISIBLE);

        busesLinearLayout = findViewById(R.id.busTypeDepotLinearLayout);

        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        progressBar = findViewById(R.id.busTypeDepotProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressTextView = findViewById(R.id.busTypeDepotProgressTextView);
        progressTextView.setVisibility(View.INVISIBLE);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                continueSearching = false;
                onBackPressed();
            }
        });

        busTypesACTV = findViewById(R.id.busTypesACTV);
        busTypesACTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                busTypesACTV.showDropDown();
            }
        });
        busTypesACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                busTypesACTV.showDropDown();
                return false;
            }
        });
        ArrayAdapter<String> busTypesAdapter = new ArrayAdapter<>(BusTypeDepotSearchActivity.this, android.R.layout.simple_list_item_1, busTypes);
        busTypesACTV.setAdapter(busTypesAdapter);
        busTypesACTV.setThreshold(0);

        busDepotsACTV = findViewById(R.id.busDepotsACTV);
        busDepotsACTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                busDepotsACTV.showDropDown();
            }
        });
        busDepotsACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                busDepotsACTV.showDropDown();
                return false;
            }
        });
        ArrayAdapter<String> busDepotsAdapter = new ArrayAdapter<>(BusTypeDepotSearchActivity.this, android.R.layout.simple_list_item_1, depots);
        busDepotsACTV.setAdapter(busDepotsAdapter);
        busDepotsACTV.setThreshold(0);

        typeOfSearchACTV = findViewById(R.id.typeOfSearchACTV);
        typeOfSearchACTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeOfSearchACTV.showDropDown();
            }
        });
        typeOfSearchACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                typeOfSearchACTV.showDropDown();
                return false;
            }
        });
        final ArrayAdapter<String> typeOfSearchAdapter = new ArrayAdapter<>(BusTypeDepotSearchActivity.this, android.R.layout.simple_list_item_1, searchTypes);
        typeOfSearchACTV.setAdapter(typeOfSearchAdapter);
        typeOfSearchACTV.setThreshold(0);

        searchButton = findViewById(R.id.busTypeDepotSearchButton);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                noBusesTextView.setVisibility(View.INVISIBLE);
                continueSearching = true;

                String chosenBusType = busTypesACTV.getText().toString();
                String chosenDepot = busDepotsACTV.getText().toString();
                String chosenSearchType = typeOfSearchACTV.getText().toString();

                chosenBusType = chosenBusType.trim();
                chosenDepot = chosenDepot.trim();
                chosenSearchType = chosenSearchType.trim();

                boolean validType = false;
                boolean validDepot = false;
                boolean validSearchType = false;

                for(String busType : busTypes) {
                    if(busType.equalsIgnoreCase(chosenBusType))
                        validType = true;
                }

                for(String depot : depots) {
                    if(depot.equalsIgnoreCase(chosenDepot))
                        validDepot = true;
                }

                for(String searchType : searchTypes) {
                    if(searchType.equalsIgnoreCase(chosenSearchType))
                        validSearchType = true;
                }

                if(searchInProgress) {
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Please wait for current search to end.", Toast.LENGTH_LONG).show();
                }
                else if(validType && validDepot && validSearchType) {
                    progressTextView.setText("");
                    progressBar.setVisibility(View.VISIBLE);
                    progressTextView.setVisibility(View.VISIBLE);
                    if(connectedToInternet()) {
                        searchButton.setVisibility(View.INVISIBLE);
                        new DisplayBuses(chosenBusType, chosenDepot, chosenSearchType).start();
                    }
                    else
                        Toast.makeText(BusTypeDepotSearchActivity.this, "Internet problem, try again.", Toast.LENGTH_LONG).show();
                }
                else if(!validType) {
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Invalid bus type!", Toast.LENGTH_LONG).show();
                }
                else if(!validDepot) {
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Invalid depot!", Toast.LENGTH_LONG).show();
                }
                else if(!validSearchType) {
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Invalid search type!", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private class DisplayBuses extends Thread {

        String chosenBusType, chosenDepot, chosenSeachType;
        DisplayBuses(String chosenBusType, String chosenDepot, String chosenSearchType) {
            this.chosenBusType = chosenBusType;
            this.chosenDepot = chosenDepot;
            this.chosenSeachType = chosenSearchType;
        }

        int shownBuses = 0;

        public void run() {
            searchInProgress = true;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    busesLinearLayout.removeAllViews();
                }
            });

            ArrayList<String> validIDs = new ArrayList<String>();

            for(String bus : busIdDepotType) {
                String currentBusType = bus.split(",")[3];
                String currentDepot = bus.split(",")[2];
                String currentBusID = bus.split(",")[1];

                if((currentBusType.equalsIgnoreCase(chosenBusType) || chosenBusType.equals("ALL TYPES"))
                        && (currentDepot.equalsIgnoreCase(chosenDepot) || chosenDepot.equals("ALL DEPOTS"))) {
                    validIDs.add(currentBusID);
                }
            }

            final int numBuses = validIDs.size();

            if(numBuses == 0) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(BusTypeDepotSearchActivity.this, chosenDepot + " does not have " + chosenBusType, Toast.LENGTH_LONG).show();
                    }
                });
            }
            else {
                for (int i = 0; i < numBuses && continueSearching; i++) {
                    String id = validIDs.get(i);
                    URL url = null;
                    try {
                        url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + id + "&flag=21");
                    } catch (MalformedURLException e) {
                        //should never happen
                    }

                    if (getContentFromURL(url).equals("No records found.")) {
                        //do nothing, erroneous VMU
                    }
                    else {
                        String[] singleBus = getContentFromURL(url).split(",");

                        boolean valid;

                        if ((singleBus[10].equals("Inactive") || singleBus[10].equals("VMU Logged Out")
                                || singleBus[10].equals("Buses in Depot")) && (chosenSeachType.equals("ALL BUSES") ||
                        chosenSeachType.equals("INACTIVE BUSES")))
                            valid = true;
                        else
                            valid = (chosenSeachType.equals("RUNNING BUSES (ACTIVE)") || chosenSeachType.equals("ALL BUSES"))
                                    && (!(singleBus[10].equals("Inactive") || singleBus[10].equals("VMU Logged Out")
                                    || singleBus[10].equals("Buses in Depot")));

                        if (valid) {
                            final Button b = new Button(BusTypeDepotSearchActivity.this);

                            String busRegNum = null;
                            String busType = null;

                            for(int j = 0; j < busIdDepotType.length; j++) {
                                String[] regAndID = busIdDepotType[j].split(",");
                                if(regAndID[1].equals(id)) {
                                    busRegNum = regAndID[0];
                                    busType = regAndID[3].toUpperCase();
                                }
                            }

                            String buttonText;

                            if(singleBus[10].equals("Buses in Depot")) {
                                buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                                        "Depot: " + singleBus[8] + "\n" +
                                        "Last seen: in " + singleBus[8] + " depot\n");
                            }
                            else {
                                buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                                        "Depot: " + singleBus[8] + "\n" +
                                        "Last seen: near " + singleBus[4].split("from ")[1] + "\n");
                            }

                            b.setText(buttonText);

                            if (busType.contains("EXPRESS")) {
                                b.setBackgroundResource(R.drawable.express_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.equals("METRO DELUXE") || busType.contains("GARUDA") || busType.equals("RAJADHANI")) {
                                b.setBackgroundResource(R.drawable.deluxe_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.equals("LOW FLOOR AC") || busType.equals("SUPER LUXURY") || busType.equals("CITY ORDINARY") || busType.equals("HI TECH")) {
                                b.setBackgroundResource(R.drawable.lf_bg);
                                b.setTextColor(Color.WHITE);
                            } else if (busType.equals("DELUXE") || busType.equals("VENNELA") || busType.equals("METRO LUXURY AC")) {
                                b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                                b.setTextColor(Color.WHITE);
                            }

                            final String busRegNumString = singleBus[0];
                            b.setOnClickListener(new View.OnClickListener() {
                                public void onClick(View v) {
                                    continueSearching = false;
                                    searchInProgress = false;
                                    Intent singleBusIntent = new Intent(BusTypeDepotSearchActivity.this, SingleBusActivity.class);
                                    singleBusIntent.putExtra("busRegNumString", busRegNumString);
                                    singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                                    startActivity(singleBusIntent);
                                }
                            });
                            b.setVisibility(View.VISIBLE);
                            shownBuses+=1;
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    busesLinearLayout.addView(b);
                                }
                            });
                        }
                    }
                    progressBarUpdate((i + 1) * 100 / numBuses);
                    progressTextViewUpdate((i + 1) + "/" + numBuses + " buses scanned.");
                }
                if(shownBuses == 0) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            noBusesTextView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }
            searchInProgress = false;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    searchButton.setVisibility(View.VISIBLE);
                }
            });

        }

        public void progressBarUpdate(int progress) {
            progressBar.setProgress(progress);
        }
        public void progressTextViewUpdate(final String progress) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    progressTextView.setText(progress);
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
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(BusTypeDepotSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

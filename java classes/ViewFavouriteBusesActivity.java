package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
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

public class ViewFavouriteBusesActivity extends AppCompatActivity {

    LinearLayout linearLayout;

    String[] busIdDepotType;

    Button clearFavouritesButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_favourite_buses);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        linearLayout = findViewById(R.id.favouritesLinearLayout);

        final TextView noFavouritesTextView = findViewById(R.id.noFavouritesTextView);

        String preference_file_key = "com.ashraf.faraa.livebus.sharedPrefs";

        SharedPreferences sharedPref;
        sharedPref = this.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPref.edit();

        clearFavouritesButton = findViewById(R.id.clearFavouritesButton);
        clearFavouritesButton.setVisibility(View.INVISIBLE);
        clearFavouritesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(ViewFavouriteBusesActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Clear favourites?")
                                .setMessage("Are you sure you want to clear all favourites? This action cannot be undone.")
                                .setCancelable(false)
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which) {
                                        editor.putString("favouriteBuses", "");
                                        editor.apply();
                                        linearLayout.removeAllViews();
                                        noFavouritesTextView.setVisibility(View.VISIBLE);
                                        clearFavouritesButton.setVisibility(View.INVISIBLE);
                                    }

                                }).show();
                    }
                });
            }
        });

        if(!sharedPref.contains("favouriteBuses")) {
            noFavouritesTextView.setVisibility(View.VISIBLE);
        }
        else {
            if(sharedPref.getString("favouriteBuses", "DEFAULT").equals("") || sharedPref.getString("favouriteBuses", "DEFAULT").equals("DEFAULT") || sharedPref.getString("favouriteBuses", "DEFAULT").equals("No records found.")) {
                noFavouritesTextView.setVisibility(View.VISIBLE);
            }
            else {
                busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");
                new ShowFavourites(sharedPref.getString("favouriteBuses", "DEFAULT")).start();
            }
        }
    }
    private class ShowFavourites extends Thread {

        String[] busIDs;

        ShowFavourites(String busIDs) {
            this.busIDs = busIDs.split(";");
        }

        public void run() {
            for(String busID : busIDs) {
                String busDetails = null;
                try {
                    busDetails = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query="+busID+"&flag=21"));
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                if(busDetails.equals("No records found."))
                    continue;

                String busRegNum = null;
                String busType = null;

                for(int j = 0; j < busIdDepotType.length; j++) {
                    String[] singleSavedBus = busIdDepotType[j].split(",");
                    if(singleSavedBus[1].equals(busID)) {
                        busRegNum = singleSavedBus[0];
                        busType = singleSavedBus[3].toUpperCase();
                    }
                }

                String busLocation = busDetails.split(",")[4].split("from")[1];
                String busStatus = busDetails.split(",")[10];
                String busDepot = busDetails.split(",")[8];

                final Button b = new Button(ViewFavouriteBusesActivity.this);

                String buttonText;

                if(busStatus.equals("Buses in Depot")) {
                    buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                            "Depot: " + busDepot + "\n" +
                            "Last seen: in " + busDepot + " depot\n");
                }
                else {
                    buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                            "Depot: " + busDepot + "\n" +
                            "Last seen: near" + busLocation + "\n");
                }

                b.setText(buttonText);

                if (busType.contains("EXPRESS")) {
                    b.setBackgroundResource(R.drawable.blue_button_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.equals("METRO DELUXE") || busType.contains("GARUDA") || busType.equals("RAJADHANI")) {
                    b.setBackgroundResource(R.drawable.green_button_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.equals("LOW FLOOR AC") || busType.equals("SUPER LUXURY") || busType.equals("CITY ORDINARY") || busType.equals("HI TECH")) {
                    b.setBackgroundResource(R.drawable.red_button_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.equals("DELUXE") || busType.equals("VENNELA") || busType.equals("METRO LUXURY AC")) {
                    b.setBackgroundResource(R.drawable.pink_button_bg);
                    b.setTextColor(Color.WHITE);
                }

                final String busRegNumString = busRegNum;
                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent singleBusIntent = new Intent(ViewFavouriteBusesActivity.this, SingleBusActivity.class);
                        singleBusIntent.putExtra("busRegNumString", busRegNumString);
                        singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                        startActivity(singleBusIntent);
                    }
                });
                b.setVisibility(View.VISIBLE);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        linearLayout.addView(b);
                    }
                });
            }
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    clearFavouritesButton.setVisibility(View.VISIBLE);
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
                    Toast.makeText(ViewFavouriteBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewFavouriteBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewFavouriteBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewFavouriteBusesActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
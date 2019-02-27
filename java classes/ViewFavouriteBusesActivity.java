package com.ashraf.faraa.livebus;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
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

        TextView noFavouritesTextView = findViewById(R.id.noFavouritesTextView);

        String preference_file_key = "com.ashraf.faraa.livebus.sharedPrefs";

        SharedPreferences sharedPref;
        sharedPref = this.getSharedPreferences(preference_file_key, Context.MODE_PRIVATE);

        if(!sharedPref.contains("favouriteBuses")) {
            noFavouritesTextView.setVisibility(View.VISIBLE);
        }
        else {
            if(sharedPref.getString("favouriteBuses", "DEFAULT").equals("") || sharedPref.getString("favouriteBuses", "DEFAULT").equals("DEFAULT")) {
                noFavouritesTextView.setVisibility(View.VISIBLE);
            }
            else {
                new ShowFavourites(sharedPref.getString("favouriteBuses", "DEFAULT")).start();
            }
        }
    }
    private class ShowFavourites extends Thread {

        String busIDs[];

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

                String busRegNum = busDetails.split(",")[0].toUpperCase();
                String busLocation = busDetails.split(",")[4].split("from")[1];
                String busStatus = busDetails.split(",")[10];
                String busType = busDetails.split(",")[9].replace("Indra", "RAJADHANI").toUpperCase();
                String busDepot = busDetails.split(",")[8];

                final Button b = new Button(ViewFavouriteBusesActivity.this);

                if(busRegNum.equals("AP11Z6086") || busRegNum.equals("AP11Z6087") ||
                        busRegNum.equals("AP11Z6084") || busRegNum.equals("AP11Z6093") ||
                        busRegNum.equals("AP11Z6096")
                        || busRegNum.contains("TS10")) {
                    busType = "METRO LUXURY AC";
                }

                busRegNum = busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                        .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                        .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                        .replace("AP11Z4039", "TS07Z4039").replace("AP7Z4004", "TS07Z4004")
                        .replace("AP7Z4020", "TS07Z4020").replace("AP07Z4008", "TS07Z4008");

                if(busRegNum.equals("TS07Z4024") || busRegNum.equals("TS07Z4023") ||
                        busRegNum.equals("TS07Z4001") || busRegNum.equals("TS07Z4053") ||
                        busRegNum.equals("TS07Z4031") || busRegNum.equals("TS07Z4030")
                        || busRegNum.equals("TS07Z4002") || busRegNum.equals("TS07Z4034")
                        || busRegNum.equals("TS07Z4056") || busRegNum.equals("TS07Z4046")
                        || busRegNum.equals("TS07Z4041") || busRegNum.equals("TS07Z4040")
                        || busRegNum.equals("TS07Z4039")) {
                    busType = "METRO DELUXE";
                }

                String buttonText;

                if(busStatus.equals("Buses in Depot")) {
                    buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                            "Depot: " + busDepot + "\n" +
                            "Last seen: in " + busDepot + " depot\n");
                }
                else {
                    buttonText = ("\n" + busRegNum + "   -   " + busType + "\n" +
                            "Depot: " + busDepot + "\n" +
                            "Last seen: near " + busLocation + "\n");
                }

                b.setText(buttonText);

                if (busType.contains("EXPRESS")) {
                    b.setBackgroundResource(R.drawable.express_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.equals("METRO DELUXE") || busType.contains("GARUDA") || busType.equals("RAJADHANI")) {
                    b.setBackgroundResource(R.drawable.deluxe_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.contains("LOW FLOOR") || busType.equals("SUPER LUXURY") || busType.equals("CITY ORDINARY") || busType.equals("HI TECH")) {
                    b.setBackgroundResource(R.drawable.lf_bg);
                    b.setTextColor(Color.WHITE);
                } else if (busType.equals("DELUXE") || busType.equals("VENNELA") || busType.equals("METRO LUXURY AC")) {
                    b.setBackgroundResource(R.drawable.deluxe_ld_bg);
                    b.setTextColor(Color.WHITE);
                }

                final String busRegNumString = busRegNum;
                b.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        Intent singleBusIntent = new Intent(ViewFavouriteBusesActivity.this, SingleBusActivity.class);
                        singleBusIntent.putExtra("busRegNumString", busRegNumString);
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

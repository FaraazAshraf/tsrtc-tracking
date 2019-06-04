package com.ashraf.faraa.livebus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class AirportLinerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_airport_liner);

        final String[] busIdDepotType;
        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        Button ABSearchButton = findViewById(R.id.ABSearchButton);
        Button AJSearchButton = findViewById(R.id.AJSearchButton);
        Button ACSearchButton = findViewById(R.id.ACSearchButton);
        Button AMSearchButton = findViewById(R.id.AMSearchButton);
        Button ALSearchButton = findViewById(R.id.ALSearchButton);

        ABSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent airportLinerToShowBusesIntent = new Intent(AirportLinerActivity.this, ShowAirportBusesActivity. class);
                airportLinerToShowBusesIntent.putExtra("route", "AB");
                airportLinerToShowBusesIntent.putExtra("busIdDepotType", busIdDepotType);
                startActivity(airportLinerToShowBusesIntent);
            }
        });

        AJSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent airportLinerToShowBusesIntent = new Intent(AirportLinerActivity.this, ShowAirportBusesActivity. class);
                airportLinerToShowBusesIntent.putExtra("route", "AJ");
                airportLinerToShowBusesIntent.putExtra("busIdDepotType", busIdDepotType);
                startActivity(airportLinerToShowBusesIntent);
            }
        });

        ACSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent airportLinerToShowBusesIntent = new Intent(AirportLinerActivity.this, ShowAirportBusesActivity. class);
                airportLinerToShowBusesIntent.putExtra("route", "AC");
                airportLinerToShowBusesIntent.putExtra("busIdDepotType", busIdDepotType);
                startActivity(airportLinerToShowBusesIntent);
            }
        });

        AMSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent airportLinerToShowBusesIntent = new Intent(AirportLinerActivity.this, ShowAirportBusesActivity. class);
                airportLinerToShowBusesIntent.putExtra("route", "AM");
                airportLinerToShowBusesIntent.putExtra("busIdDepotType", busIdDepotType);
                startActivity(airportLinerToShowBusesIntent);
            }
        });

        ALSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent airportLinerToShowBusesIntent = new Intent(AirportLinerActivity.this, ShowAirportBusesActivity. class);
                airportLinerToShowBusesIntent.putExtra("route", "AL");
                airportLinerToShowBusesIntent.putExtra("busIdDepotType", busIdDepotType);
                startActivity(airportLinerToShowBusesIntent);
            }
        });

        final ScrollView liveRouteScrollView = findViewById(R.id.liveRouteScrollView);
        liveRouteScrollView.setVisibility(View.INVISIBLE);

        final Button airportLinerTimingsButton = findViewById(R.id.airportLinerTimingsButton);
        final Button airportLinerLiveButton = findViewById(R.id.airportLinerLiveButton);

        airportLinerLiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                liveRouteScrollView.setVisibility(View.VISIBLE);
                airportLinerLiveButton.setVisibility(View.INVISIBLE);
                airportLinerTimingsButton.setVisibility(View.INVISIBLE);
            }
        });

        airportLinerTimingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(AirportLinerActivity.this, AirportLinerTimingsActivity.class));
            }
        });

        new CheckServerStatus().start();
    }

    class CheckServerStatus extends Thread {
        public void run() {

            URL url = null;
            String urlContent = null;

            try {
                url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/airport_server_status");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            urlContent = getContentFromURL(url);
            if(urlContent.equals("0")) {
                //do nothing, no msg to display
            }
            else {
                final String finalUrlContent = urlContent;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(AirportLinerActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle(finalUrlContent.split(";")[0])
                                .setMessage(finalUrlContent.split(";")[1])
                                .setCancelable(false)
                                .setPositiveButton("Ok", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }

                                }).show();
                    }
                });
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
                    new AlertDialog.Builder(AirportLinerActivity.this)
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
                    new AlertDialog.Builder(AirportLinerActivity.this)
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
                    new AlertDialog.Builder(AirportLinerActivity.this)
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
                    new AlertDialog.Builder(AirportLinerActivity.this)
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
}

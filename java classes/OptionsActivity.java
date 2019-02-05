package com.ashraf.faraa.livebus;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class OptionsActivity extends AppCompatActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        new CheckVersion().start();

        final DrawerLayout drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView leftNav = findViewById(R.id.leftNav);
        leftNav.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int id = menuItem.getItemId();

                if(id == R.id.bookTickets) {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.tsrtconline.in/oprs-mobile/")));
                }
                else if(id == R.id.hireABus) {
                    startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("http://www.tsrtconline.in/oprs-mobile/guest/specialHire.do")));
                }
                else if(id == R.id.howThisWorks) {
                    startActivity(new Intent(OptionsActivity.this, HowThisWorksActivity.class));
                }
                else if(id == R.id.cityInfo) {
                    startActivity(new Intent(OptionsActivity.this, CityServicesActivity.class));
                }
                else if(id == R.id.timings) {
                    startActivity(new Intent(OptionsActivity.this, TimingsActivity.class));
                }
                else if(id == R.id.emergencyNos) {
                    new AlertDialog.Builder(OptionsActivity.this)
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setTitle("Emergency numbers")
                            .setMessage("Police : 100\n" +
                                    "Fire : 101\n" +
                                    "Ambulance: 102, 108\n" +
                                    "Cyb Police (Whatsapp): 9490617444\n" +
                                    "SHE TEAM (Whatsapp): 9490616555")
                            .setPositiveButton("Done", new DialogInterface.OnClickListener()
                            {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            }).show();
                }
                else if(id == R.id.feedback) {
                    startActivity(new Intent(OptionsActivity.this, FeedbackActivity.class));
                }

                return true;
            }
        });

        Button menuButton = findViewById(R.id.menuButton);
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        Button numberplateSearchButton = findViewById(R.id.numberplateSearchButton);
        Button busTypeSearchButton = findViewById(R.id.busTypeAndDepotSearchButton);
        Button stopSearchButton = findViewById(R.id.stopSearchButton);
        Button routeNumSearchButton = findViewById(R.id.routeNumSearchButton);

        numberplateSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionsActivity.this, NumberplateSearchActivity.class));
            }
        });

        busTypeSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionsActivity.this, BusTypeDepotSearchActivity.class));
            }
        });

        stopSearchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(OptionsActivity.this, SearchBusesOnStopActivity.class));
            }
        });

        routeNumSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(OptionsActivity.this, SearchBusesOnRouteActivity.class));
            }
        });

    }
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setTitle("Close App")
                .setMessage("Are you sure you want to close the app?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
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
                    new AlertDialog.Builder(OptionsActivity.this)
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
                    new AlertDialog.Builder(OptionsActivity.this)
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
                    new AlertDialog.Builder(OptionsActivity.this)
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
                    new AlertDialog.Builder(OptionsActivity.this)
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

    private class CheckVersion extends Thread {
        public void run() {
            URL url = null;
            try {
                url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/versioncode");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            String urlContent = getContentFromURL(url);
            if(urlContent.equals("31")) {
                //do nothing because it is correct version
            }
            else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new AlertDialog.Builder(OptionsActivity.this)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setTitle("Update available!")
                                .setMessage("Please update the app to the latest version for better performance.")
                                .setCancelable(false)
                                .setNegativeButton("Later", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                })
                                .setPositiveButton("Update now", new DialogInterface.OnClickListener()
                                {
                                    public void onClick(DialogInterface dialog, int which) {
                                        startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.ashraf.faraa.livebus")));
                                    }

                                }).show();
                    }
                });
            }

            try {
                url = new URL("https://raw.githubusercontent.com/FaraazAshraf/tsrtc-tracking/master/message");
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
                        new AlertDialog.Builder(OptionsActivity.this)
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
}
package com.ashraf.faraa.livebus;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ViewLocationActivity extends AppCompatActivity {

    WebView webView1;
    WebView webView2;
    boolean keepRefreshing = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_location);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        webView1 = findViewById(R.id.webView1);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.setVisibility(View.INVISIBLE);

        webView2 = findViewById(R.id.webView2);
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.setVisibility(View.INVISIBLE);

        webView1.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; //True if the listener has consumed the event, false otherwise.
            }
        });

        webView2.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true; //True if the listener has consumed the event, false otherwise.
            }
        });

        String busID = getIntent().getExtras().getString("busID");

        new StartTracking(busID).start();
        new Refresh10Sec(busID).start();
    }

    public void onBackPressed() {
        keepRefreshing = false;
        super.onBackPressed();
    }

    public class Refresh10Sec extends Thread {

        String busID;

        Refresh10Sec (String busID) {
            this.busID = busID;
        }

        public void run() {
            while(keepRefreshing) {
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(keepRefreshing) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new StartTracking(busID).start();
                        }
                    });
                }
            }
        }
    }

    private class StartTracking extends Thread {

        String busID;
        int count = 0;

        StartTracking(String busID) {
            this.busID = busID;
        }

        public void run() {
            URL url = null;

            count++;

            if(count % 2 == 0) {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=21");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                final String gpsCoords = getContentFromURL(url).split(",")[6] + "," + getContentFromURL(url).split(",")[7];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView1.loadUrl("https://www.google.com/maps/place/" + gpsCoords + "/@" + gpsCoords + ",12z");
                        webView1.setVisibility(View.INVISIBLE);
                    }
                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView2.setVisibility(View.INVISIBLE);
                        webView1.setVisibility(View.VISIBLE);
                    }
                });
            }
            else {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=21");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }

                final String gpsCoords = getContentFromURL(url).split(",")[6] + "," + getContentFromURL(url).split(",")[7];

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView2.loadUrl("https://www.google.com/maps/place/" + gpsCoords + "/@" + gpsCoords + ",12z");
                        webView2.setVisibility(View.INVISIBLE);
                    }
                });
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView1.setVisibility(View.INVISIBLE);
                        webView2.setVisibility(View.VISIBLE);
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
                    new AlertDialog.Builder(ViewLocationActivity.this)
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
                    new AlertDialog.Builder(ViewLocationActivity.this)
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
                    new AlertDialog.Builder(ViewLocationActivity.this)
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
                    new AlertDialog.Builder(ViewLocationActivity.this)
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

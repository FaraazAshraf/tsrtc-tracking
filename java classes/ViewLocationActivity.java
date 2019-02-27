package com.ashraf.faraa.livebus;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class ViewLocationActivity extends AppCompatActivity {

    int count = 0;

    boolean keepRefreshing = true;

    String gpsCoordsOld = "null";

    WebView webView1, webView2;
    TextView lastUpdatedTextView;

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

        lastUpdatedTextView = findViewById(R.id.viewLocationLastUpdatedTextView);

        webView1 = findViewById(R.id.webView1);
        webView1.getSettings().setJavaScriptEnabled(true);
        webView1.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });
        webView2 = findViewById(R.id.webView2);
        webView2.getSettings().setJavaScriptEnabled(true);
        webView2.setWebViewClient(new WebViewClient() {
            public boolean shouldOverrideUrlLoading(WebView view, String url){
                // do your handling codes here, which url is the requested url
                // probably you need to open that url rather than redirect:
                view.loadUrl(url);
                return false; // then it is not handled by default action
            }
        });

        String busID = getIntent().getExtras().getString("busID");

        new RunTracker(busID).start();

    }

    public void onBackPressed() {
        keepRefreshing = false;
        super.onBackPressed();
    }

    private class RunTracker extends Thread {

        String busID;

        RunTracker(String busID) {
            this.busID = busID;
        }

        public void run() {

            while(keepRefreshing) {
                new ShowLocation(busID).start();
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class ShowLocation extends Thread {

        String busID;

        ShowLocation(String busID) {
            this.busID = busID;
        }

        public void run() {

            String urlContent = null;

            try {
                urlContent = getContentFromURL(new URL("http://125.16.1.204:8080/bats/appQuery.do?query=" + busID + "&flag=21"));
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

            count++;

            final String gpsCoords = urlContent.split(",")[6] + "," + urlContent.split(",")[7];

            if(gpsCoords.equals(gpsCoordsOld)) {
                //if the bus is not moving then let the user move around the map. no harm done.
            }
            else {
                gpsCoordsOld = gpsCoords;

                if (count % 2 == 0) {

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView1.loadUrl("https://www.google.com/maps/place/" + gpsCoords + "/@" + gpsCoords + ",13z");
                            webView1.setVisibility(View.GONE);
                            webView2.setVisibility(View.VISIBLE);
                        }
                    });

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final String finalUrlContent = urlContent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView2.setVisibility(View.GONE);
                            webView1.setVisibility(View.VISIBLE);
                            lastUpdatedTextView.setText("Last updated: " + finalUrlContent.split(",")[5]);
                        }
                    });

                } else {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView2.loadUrl("https://www.google.com/maps/place/" + gpsCoords + "/@" + gpsCoords + ",13z");
                            webView2.setVisibility(View.GONE);
                            webView1.setVisibility(View.VISIBLE);
                        }
                    });

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    final String finalUrlContent = urlContent;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            webView1.setVisibility(View.GONE);
                            webView2.setVisibility(View.VISIBLE);
                            lastUpdatedTextView.setText("Last updated: " + finalUrlContent.split(",")[5]);
                        }
                    });
                }
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
                    Toast.makeText(ViewLocationActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewLocationActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewLocationActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(ViewLocationActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

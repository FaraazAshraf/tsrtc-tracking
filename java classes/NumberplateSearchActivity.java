package com.ashraf.faraa.livebus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class NumberplateSearchActivity extends AppCompatActivity {

    AutoCompleteTextView allBusesACTV;
    Toolbar toolbar;
    TextView loadingNumberplatesTextView;

    Button searchButton;

    String [] allBusRegNums;

    String[] busIdDepotType;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numerplate_search);

        loadingNumberplatesTextView = findViewById(R.id.loadingNumberplatesTextView);
        loadingNumberplatesTextView.setVisibility(View.VISIBLE);
        loadingNumberplatesTextView.setText("Connecting...");

        allBusesACTV = findViewById(R.id.allBusesACTV);
        allBusesACTV.setThreshold(0);
        allBusesACTV.setVisibility(View.INVISIBLE);

        searchButton = findViewById(R.id.numberplateSearchButton);
        searchButton.setVisibility(View.INVISIBLE);

        busIdDepotType = getIntent().getExtras().getStringArray("busIdDepotType");

        new ShowAllBuses().start();

        allBusesACTV.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                allBusesACTV.showDropDown();
            }
        });

        allBusesACTV.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                allBusesACTV.showDropDown();
                return false;
            }
        });

        toolbar = findViewById(R.id.toolBar);
        toolbar.setVisibility(View.INVISIBLE);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                onBackPressed();
            }
        });

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                String chosenBusRegNum = allBusesACTV.getText().toString().replaceAll(" ", "").toUpperCase();

                boolean validRegNum = false;
                for(String singleBus : allBusRegNums)
                    if(singleBus.equals(chosenBusRegNum))
                        validRegNum = true;

                if(validRegNum) {
                    Intent singleBusIntent = new Intent(NumberplateSearchActivity.this, SingleBusActivity.class);

                    singleBusIntent.putExtra("busRegNumString", chosenBusRegNum);
                    singleBusIntent.putExtra("busIdDepotType", busIdDepotType);
                    startActivity(singleBusIntent);
                }
                else {
                    Toast.makeText(NumberplateSearchActivity.this, "Choose only from list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class ShowAllBuses extends Thread {
        public void run() {

            allBusRegNums = new String[busIdDepotType.length];
            final int numBuses = allBusRegNums.length;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    loadingNumberplatesTextView.setText("Connected!\nLoading buses....");
                }
            });

            for(int i = 0; i < numBuses; i++) {
                String busRegNum = busIdDepotType[i].split(",")[0];
                allBusRegNums[i] = busRegNum;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> allBusesAdapter = new ArrayAdapter<>(NumberplateSearchActivity.this, android.R.layout.simple_list_item_1, allBusRegNums);
                    allBusesACTV.setAdapter(allBusesAdapter);

                    loadingNumberplatesTextView.setVisibility(View.INVISIBLE);

                    toolbar.setVisibility(View.VISIBLE);
                    allBusesACTV.setVisibility(View.VISIBLE);
                    searchButton.setVisibility(View.VISIBLE);
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
                    Toast.makeText(NumberplateSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NumberplateSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NumberplateSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(NumberplateSearchActivity.this, "Connection error", Toast.LENGTH_SHORT).show();
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

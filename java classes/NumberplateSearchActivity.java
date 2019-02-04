package com.ashraf.faraa.livebus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NumberplateSearchActivity extends AppCompatActivity {

    AutoCompleteTextView allBusesACTV;
    ProgressBar progressBar;
    Toolbar toolbar;
    TextView regNumDisplayTextView;

    Button searchButton;

    String [] allBusRegNums;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_numerplate_search);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        regNumDisplayTextView = findViewById(R.id.regNumDisplayTextView);

        allBusesACTV = findViewById(R.id.allBusesACTV);
        allBusesACTV.setThreshold(0);

        allBusesACTV.setVisibility(View.INVISIBLE);

        searchButton = findViewById(R.id.scanButtonMethod1);
        searchButton.setVisibility(View.INVISIBLE);

        new ShowAllBuses().execute();

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

        allBusesACTV.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus)
                    allBusesACTV.showDropDown();
                else allBusesACTV.showDropDown();
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

                    new LogAction(chosenBusRegNum).start();

                    singleBusIntent.putExtra("busRegNumString", chosenBusRegNum);
                    startActivity(singleBusIntent);
                }
                else {
                    Toast.makeText(NumberplateSearchActivity.this, "Choose only from list", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class LogAction extends Thread {

        String logString;

        public LogAction (String logString) {
            this.logString = logString;
        }

        public void run() {
            URL url = null;

            String currentDate = new SimpleDateFormat("MM-dd HH:mm").format(new Date());

            logString = currentDate + "-" + logString;

            try {
                logString = URLEncoder.encode(logString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            if(logString.length() <= 50) {
                try {
                    url = new URL("http://125.16.1.204:8080/bats/appQuery.do?query=name,fafafafa@fsfsfsfs.com,9534343434," + logString + ",0,5,mobile,0,67&flag=15");
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                }
                String dummy = getContentFromURL(url);
            }
        }
    }

    private class ShowAllBuses extends AsyncTask<Void, ProgressPublisher, Void> {

        String fileContents;

        protected void onPreExecute() {
            regNumDisplayTextView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
        }

        protected Void doInBackground(Void... voids) {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(
                        new InputStreamReader(getAssets().open("BusIDDepotTypeTS.txt")));
            } catch (IOException e) {
                //won't occur
            }
            fileContents = new String();
            String line;

            try {
                while ((line = reader.readLine()) != null) {
                    fileContents += line;
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }

            allBusRegNums = new String[fileContents.split(";").length];
            int numBuses = allBusRegNums.length;
            for(int i = 0; i < numBuses; i++) {
                String busRegNum = fileContents.split(";")[i].split(",")[0];
                busRegNum = busRegNum.replace("AP11Z3998", "TS07Z3998").replace("AP11Z4017", "TS07Z4017")
                        .replace("AP11Z4015", "TS07Z4015").replace("AP11Z4040", "TS07Z4040")
                        .replace("AP11Z4041", "TS07Z4041").replace("AP11Z4046", "TS07Z4046")
                        .replace("AP11Z4039", "TS07Z4039");
                allBusRegNums[i] = busRegNum;
                publishProgress(new ProgressPublisher((i*100/numBuses), busRegNum));
            }

            return null;
        }

        protected void onPostExecute(Void aVoid) {

            ArrayAdapter<String> allBusesAdapter = new ArrayAdapter<>(NumberplateSearchActivity.this, android.R.layout.simple_list_item_1, allBusRegNums);
            allBusesACTV.setAdapter(allBusesAdapter);
            progressBar.setVisibility(View.INVISIBLE);
            toolbar.setVisibility(View.VISIBLE);
            allBusesACTV.setVisibility(View.VISIBLE);
            regNumDisplayTextView.setVisibility(View.INVISIBLE);
            searchButton.setVisibility(View.VISIBLE);
        }

        protected void onProgressUpdate(ProgressPublisher... values) {
                progressBar.setProgress(values[0].progress);
                regNumDisplayTextView.setText("Loading "+values[0].busRegNum);
        }
    }

    private class ProgressPublisher {

        int progress;
        String busRegNum;

        public ProgressPublisher(int progress, String busRegNum) {
            this.progress = progress;
            this.busRegNum = busRegNum;
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
                    new AlertDialog.Builder(NumberplateSearchActivity.this)
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
                    new AlertDialog.Builder(NumberplateSearchActivity.this)
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
                    new AlertDialog.Builder(NumberplateSearchActivity.this)
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
                    new AlertDialog.Builder(NumberplateSearchActivity.this)
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

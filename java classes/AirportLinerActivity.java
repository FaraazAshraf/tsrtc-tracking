package com.ashraf.faraa.livebus;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;

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
    }
}

package ru.spbau.savethemoment.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.spbau.savethemoment.R;

public class ChooseLocationActivity extends FragmentActivity implements OnMapReadyCallback {
    public static final String POSITION_LAT_LNG_NAME = "PositionLatLng";

    private LatLng initialLatLng;
    private Marker locationMarker;
    private Intent result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = getIntent();
        if (data.hasExtra(POSITION_LAT_LNG_NAME)) {
            initialLatLng = data.getParcelableExtra(POSITION_LAT_LNG_NAME);
        }

        setContentView(R.layout.activity_choose_location);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        result = new Intent();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (initialLatLng == null) {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).visible(false));
        } else {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(initialLatLng).visible(true));
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationMarker.setPosition(latLng);
                locationMarker.setVisible(true);
                result.putExtra(POSITION_LAT_LNG_NAME, locationMarker.getPosition());
                setResult(RESULT_OK, result);
            }
        });
    }
}

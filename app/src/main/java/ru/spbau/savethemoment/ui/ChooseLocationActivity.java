package ru.spbau.savethemoment.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import ru.spbau.savethemoment.R;

public class ChooseLocationActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String POSITION_LAT_LNG_NAME = "PositionLatLng";
    private static final int FINE_LOCATION_REQUEST_CODE = 0;
    private static final int ZOOM = 10;

    private LatLng initialLatLng;
    private Marker locationMarker;
    private Intent result;
    private GoogleMap googleMap;

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent data = getIntent();
        initialLatLng = data.getParcelableExtra(POSITION_LAT_LNG_NAME);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();


        setContentView(R.layout.activity_choose_location);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.choose_location_fragment);
        mapFragment.getMapAsync(this);
        result = new Intent();
    }

    @Override
    protected void onResume() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        googleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (locationMarker != null && !locationMarker.isVisible()) {
            getCurrentLocation();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == FINE_LOCATION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (locationMarker != null && !locationMarker.isVisible()) {
                    getCurrentLocation();
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (initialLatLng == null) {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).visible(false));
            getCurrentLocation();
        } else {
            locationMarker = googleMap.addMarker(new MarkerOptions().position(initialLatLng).visible(true));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationMarker.getPosition(), ZOOM));
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

    private void getCurrentLocation() {
        if (!googleApiClient.isConnected()) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, FINE_LOCATION_REQUEST_CODE);
            }
            return;
        }
        Location currentLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
        locationMarker.setPosition(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
        locationMarker.setVisible(true);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(locationMarker.getPosition(), ZOOM));
    }
}

package ru.spbau.savethemoment.ui;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

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

import java.util.ArrayList;
import java.util.List;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.FetchAddressIntentService;

public class ChooseLocationActivity extends AppCompatActivity implements
        OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {
    public static final String POSITION_LAT_LNG_NAME = "PositionLatLng";
    private static final int FINE_LOCATION_REQUEST_CODE = 0;
    private static final int ZOOM = 10;
    public static final String ADDRESS = "Address";

    private LatLng initialLatLng;
    private Marker locationMarker;
    private Intent result;
    private GoogleMap googleMap;
    private AddressReceiver addressReceiver;

    private GoogleApiClient googleApiClient;
    private Toolbar toolbar;

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
        addressReceiver = new AddressReceiver(null);

        toolbar = (Toolbar) findViewById(R.id.tool_bar_chooselocation);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chooselocation, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menuitem_choose_location_ok) {
            ChooseLocationActivity.this.finish();
            return true;
        }
        return false;
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
            setResultPosition(locationMarker.getPosition());
        }
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                locationMarker.setPosition(latLng);
                locationMarker.setVisible(true);
                setResultPosition(locationMarker.getPosition());
            }
        });
    }

    private void setResultPosition(LatLng latLng) {
        result.putExtra(POSITION_LAT_LNG_NAME, latLng);
        setResult(RESULT_OK, result);
        requestAddress(locationMarker.getPosition());
    }

    private void requestAddress(LatLng position) {
        Intent intent = new Intent(this, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.RECEIVER, addressReceiver);
        intent.putExtra(FetchAddressIntentService.LAT_LNG, position);
        startService(intent);
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

        setResultPosition(locationMarker.getPosition());
    }

    private class AddressReceiver extends ResultReceiver {
        public AddressReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            if (resultCode == FetchAddressIntentService.RESULT_OK) {
                Address address = resultData.getParcelable(FetchAddressIntentService.RESULT_ADDRESS);
                List<String> addressLines = new ArrayList<>();
                for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressLines.add(address.getAddressLine(i));
                }
                String addressString = TextUtils.join("\n", addressLines);
                result.putExtra(ADDRESS, addressString);
            }
        }
    }
}

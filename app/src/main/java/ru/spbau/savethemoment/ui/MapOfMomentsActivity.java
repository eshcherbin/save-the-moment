package ru.spbau.savethemoment.ui;

import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashSet;
import java.util.UUID;

import ru.spbau.savethemoment.R;
import ru.spbau.savethemoment.common.Moment;
import ru.spbau.savethemoment.momentmanager.MomentManager;
import ru.spbau.savethemoment.momentmanager.MomentsLoader;

public class MapOfMomentsActivity extends FragmentActivity implements
        OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener,
        LoaderManager.LoaderCallbacks<Cursor> {

    public static final String MOMENT = "Moment";
    public static final String IS_SINGLE_MOMENT = "IsSingleMoment";
    public static final int SINGLE_MOMENT_ZOOM = 10;
    private static final String TAGS = "Tags";

    private static final int LOADER_ID = 0;

    private boolean isSingleMoment;
    private Moment singleMoment;

    private GoogleMap googleMap;

    private HashSet<String> tagsToFilter = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_of_moments);

        Intent data = getIntent();
        isSingleMoment = data.getBooleanExtra(IS_SINGLE_MOMENT, false);
        if (isSingleMoment) {
            singleMoment = data.getParcelableExtra(MOMENT);
        }

        if (data.hasExtra(TAGS)) {
            tagsToFilter = (HashSet<String>) data.getSerializableExtra(TAGS);
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_of_moments_fragment);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isSingleMoment) {
            if (tagsToFilter == null) {
                getLoaderManager().restartLoader(LOADER_ID, null, this);
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(TAGS, tagsToFilter);
                getLoaderManager().restartLoader(LOADER_ID, bundle, this);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        if (isSingleMoment) {
            Location location = singleMoment.getLocation();
            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(location.getLatitude(),
                    location.getLongitude()))
                    .visible(true));
            marker.setTag(singleMoment.getId());
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), SINGLE_MOMENT_ZOOM));
        } else {
            if (tagsToFilter == null) {
                getLoaderManager().initLoader(LOADER_ID, null, this);
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable(TAGS, tagsToFilter);
                getLoaderManager().initLoader(LOADER_ID, bundle, this);
            }
    //        getLoaderManager().initLoader(LOADER_ID, null, this);
        }
        googleMap.setOnMarkerClickListener(this);
    }


    @Override
    public boolean onMarkerClick(Marker marker) {
        if (!isSingleMoment) {
            UUID momentId = (UUID) marker.getTag();
            Intent intent = new Intent(this, MomentViewActivity.class);
            intent.putExtra(MomentViewActivity.MOMENT_ID, momentId);
            startActivity(intent);
        } else {
            finish();
        }
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new MomentsLoader(this, tagsToFilter, true);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        googleMap.clear();
        while (data.moveToNext()) {
            double longitude = data.getDouble(data.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LONGITUDE));
            double latitude = data.getDouble(data.getColumnIndexOrThrow(MomentManager.MOMENT_LOCATION_LATITUDE));
            UUID momentId = UUID.fromString(data.getString(data.getColumnIndexOrThrow(MomentManager.MOMENT_ID)));
            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)));
            marker.setTag(momentId);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        if (googleMap != null) {
            googleMap.clear();
        }
    }
}

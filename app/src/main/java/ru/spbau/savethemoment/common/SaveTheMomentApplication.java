package ru.spbau.savethemoment.common;

import android.app.Application;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;

public class SaveTheMomentApplication extends Application implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private GoogleApiClient googleApiClient;
    private static SaveTheMomentApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();

        instance = this;
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_APPFOLDER)
                .build();

        googleApiClient.connect();
    }

    public static synchronized SaveTheMomentApplication getInstance() {
        return instance;
    }

    public static GoogleApiClient getGoogleApiClient() {
        return getInstance().googleApiClient;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}

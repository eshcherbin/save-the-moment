package ru.spbau.savethemoment.common;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

public class FetchAddressIntentService extends IntentService {

    public static final String LAT_LNG = "LatLng";
    public static final String RECEIVER = "Receiver";
    public static final String RESULT_ADDRESS = "Result";

    public static final int MAX_RESULTS = 1;
    public static final int RESULT_FAILED = 1;
    public static final int RESULT_OK = 0;

    private ResultReceiver receiver;

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Geocoder geocoder = new Geocoder(this);

        LatLng latLng = intent.getParcelableExtra(LAT_LNG);
        receiver = intent.getParcelableExtra(RECEIVER);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, MAX_RESULTS);
        } catch (IOException e) {
            deliverResults(RESULT_FAILED, null);
        }

        if (addresses == null || addresses.isEmpty()) {
            deliverResults(RESULT_FAILED, null);
        } else {
            Address address = addresses.get(0);
            deliverResults(RESULT_OK, address);
        }
    }

    private void deliverResults(int resultCode, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_ADDRESS, address);
        receiver.send(resultCode, bundle);
    }
}

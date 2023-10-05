package com.example.realtimelocationtrackergoogle6;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsResponse;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "MapActivity";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private boolean mLocationPermissionGranted = false;
    private static final int LOCATION_STATIC_REQUEST_CODE = 1234;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15f;
    private AutoCompleteTextView mSearchText;
    private ImageView mGps;
    private AutocompleteSupportFragment autocompleteSupportFragment;
    PlacesClient placesClient;
    private AutocompletePredictionsAdapter predictionsAdapter;
    private ListView suggestionsListView;
    private ArrayAdapter<String> suggestionsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        mSearchText=findViewById(R.id.input_search);
        mGps=findViewById(R.id.ic_gps);
        suggestionsListView = findViewById(R.id.suggestionsListView);

        getLocationPermission();

        Places.initialize(getApplicationContext(), "AIzaSyCnmiKpxfugodUGpYp5KZu7yl5ozXh8cDo");
        placesClient = Places.createClient(this);
//        predictionsAdapter = new AutocompletePredictionsAdapter(this, placesClient);
//        mSearchText.setAdapter(predictionsAdapter);
//        // Set up item click listener
//        mSearchText.setOnItemClickListener((adapterView, view, i, l) -> {
//            AutocompletePrediction prediction = predictionsAdapter.getItem(i);
//            // Handle the selected prediction
//            String placeId = prediction.getPlaceId();
//            // Fetch details for the selected place using placeId if needed
//        });





//        if(!Places.isInitialized()){
//            Places.initialize(getApplicationContext(),"AIzaSyCnmiKpxfugodUGpYp5KZu7yl5ozXh8cDo");
//        }
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//// Replace R.id.map with your actual fragment container ID
//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) fragmentManager.findFragmentById(R.id.fragment_search);
//        assert autocompleteFragment != null;
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
//
//        fragmentTransaction.commit();





//        mSearchText.setOnItemClickListener((adapterView, view, i, l) -> {
//            AutocompletePrediction prediction = (AutocompletePrediction) adapterView.getItemAtPosition(i);
//            // Handle the selected prediction
//            String placeId = prediction.getPlaceId();
//            // Fetch details for the selected place using placeId if needed
//        });
//
//// Set up Autocomplete predictions adapter
//        AutocompletePredictionsAdapter predictionsAdapter = new AutocompletePredictionsAdapter(this, placesClient);
//        mSearchText.setAdapter(predictionsAdapter);

        // Initialize suggestions adapter
        suggestionsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        suggestionsListView.setAdapter(suggestionsAdapter);

        // Set up text change listener for EditText
        mSearchText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                // Handle text change and fetch suggestions
//                fetchSuggestions(charSequence.toString());
                String query = charSequence.toString().trim();

                // Check if the query is not empty and contains at least 3 characters
                if (query.length() >= 3) {
                    // Fetch suggestions only if the query is valid
                    fetchSuggestions(query);
                } else {
                    // If the query is empty or less than 3 characters, clear suggestions and hide the ListView
                    suggestionsAdapter.clear();
                    suggestionsAdapter.notifyDataSetChanged();
                    suggestionsListView.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {}
        });

        // Set up item click listener for suggestions
        suggestionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                String selectedSuggestion = suggestionsAdapter.getItem(position);
                // Handle the selected suggestion
                mSearchText.setText(selectedSuggestion);
                suggestionsListView.setVisibility(View.GONE);
            }
        });





    }
    private void fetchSuggestions(String query) {
        // Create a FindAutocompletePredictionsRequest object
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(query)
                .build();

        // Fetch predictions from Places API
        placesClient.findAutocompletePredictions(request)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FindAutocompletePredictionsResponse response = task.getResult();
                        if (response != null) {
                            List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                            List<String> suggestionStrings = new ArrayList<>();

                            // Extract suggestion strings from predictions
                            for (AutocompletePrediction prediction : predictions) {
                                suggestionStrings.add(prediction.getFullText(null).toString());
                            }

                            // Update suggestionsAdapter with fetched suggestions
                            suggestionsAdapter.clear();
                            suggestionsAdapter.addAll(suggestionStrings);
                            suggestionsAdapter.notifyDataSetChanged();

                            // Show the suggestions list
                            suggestionsListView.setVisibility(View.VISIBLE);
                        }
                    } else {
                        // Handle error
                        Exception exception = task.getException();
                        if (exception instanceof ApiException) {
                            ApiException apiException = (ApiException) exception;
                            Log.e(TAG, "Place not found: " + apiException.getStatusCode());
                        }
                    }
                });
    }


    public void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting the current device's location");
//        Toast.makeText(this, "getting the current device's location", Toast.LENGTH_SHORT).show();

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        try {
            if (mLocationPermissionGranted) {
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                    return;
                }
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: Found the location");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, "My Location");
                        } else {
                            Log.d(TAG, "Current Location is null");
                            Toast.makeText(MapActivity.this, "Unable to get current Location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }

        } catch (Exception e) {
            Log.d(TAG, "getDeviceLocation: SecurityException: " + e.getMessage());
        }
    }


    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
//        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mMap = googleMap;

        if (mLocationPermissionGranted) {
//            Toast.makeText(this, "getting the current device's location", Toast.LENGTH_SHORT).show();
            getDeviceLocation();

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED && ActivityCompat
                    .checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                    PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            init();
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title){
        Log.d(TAG, "moveCamera: moving camera to: Lat: "+latLng.latitude+" and "+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));

        MarkerOptions options=new MarkerOptions()
                .position(latLng)
                .title(title);
        mMap.addMarker(options);
    }
    
    private void init(){
//        Toast.makeText(this, "init: initialising", Toast.LENGTH_SHORT).show();

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                Toast.makeText(MapActivity.this, "KeyPress checking", Toast.LENGTH_SHORT).show();
                if(actionId == EditorInfo.IME_ACTION_SEARCH
                        ||actionId==EditorInfo.IME_ACTION_DONE
                        ||event.getAction()==KeyEvent.ACTION_DOWN
                        ||event.getAction()==KeyEvent.KEYCODE_ENTER){
                    //Execute method for searching
                    geoLocate();
                }
                return false;
            }
        });

        mGps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDeviceLocation();
            }
        });



//        if(!Places.isInitialized()){
//            Places.initialize(getApplicationContext(),"AIzaSyCnmiKpxfugodUGpYp5KZu7yl5ozXh8cDo");
//        }
//        FragmentManager fragmentManager = getSupportFragmentManager();
//        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//
//// Replace R.id.map with your actual fragment container ID
//        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment) fragmentManager.findFragmentById(R.id.map);
//        assert autocompleteFragment != null;
//        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.LAT_LNG, Place.Field.NAME));
//
//        fragmentTransaction.commit();




//        Toast.makeText(this, "init: stopping", Toast.LENGTH_SHORT).show();
    }

    private void geoLocate(){
//        Toast.makeText(this, "geoLocate: starting", Toast.LENGTH_SHORT).show();
        String searchString=mSearchText.getText().toString();
        Geocoder geocoder=new Geocoder(MapActivity.this);
        List<Address> list= new ArrayList<>();
        try{
            list=geocoder.getFromLocationName(searchString,1);
        } catch (IOException e) {
            Toast.makeText(this, "geoLocate: Exception: "+e.getMessage(), Toast.LENGTH_SHORT).show();
        }
        assert list != null;
        if(list.size()>0){
            Address address=list.get(0);

            Log.d(TAG,"geoLocate: found a location: "+address.toString());
//            Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()),DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    private void initMap(){
        Toast.makeText(this, "Initialising Map", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        assert mapFragment != null;
        mapFragment.getMapAsync(MapActivity.this);
    }

    private void getLocationPermission(){
//        Toast.makeText(this, "getLocationPermission", Toast.LENGTH_SHORT).show();
        String[] permission={Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(this.getApplicationContext(),FINE_LOCATION)==
                PackageManager.PERMISSION_GRANTED){
            if(ContextCompat.checkSelfPermission(this.getApplicationContext(),COARSE_LOCATION)==
                    PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted=true;
//                Toast.makeText(this, "getlocationPermission: Requesting Permission", Toast.LENGTH_SHORT).show();
                initMap();
            }else{
//                Toast.makeText(this, "getlocationPermission: Requesting Permission when denied", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(this,
                        permission,
                        LOCATION_STATIC_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(this,
                    permission,
                    LOCATION_STATIC_REQUEST_CODE);
        }
//        Toast.makeText(this, "getLocationPermission: Couldn't get permission", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Toast.makeText(this, "Requesting Permission", Toast.LENGTH_SHORT).show();
        mLocationPermissionGranted = false;

        switch (requestCode) {
            case LOCATION_STATIC_REQUEST_CODE: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    //initialise our map
//                    Toast.makeText(this, "Initialising map after Requesting Permission", Toast.LENGTH_SHORT).show();
                    initMap();
                }
            }
        }

    }
}
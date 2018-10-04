package com.example.joan.covision;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;


import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;



public class SomeFragment extends Fragment implements OnMapReadyCallback , LocationListener {

	int TAG_CODE_PERMISSION_LOCATION=2;
	private static final String TAG = "MapActivity";
	private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
	private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
	private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
	private static final float DEFAULT_ZOOM = 15f;

	//widgets
	private EditText mSearchText;
	private ImageView mGps;

	//vars
	private Boolean mLocationPermissionsGranted = false;
	private FusedLocationProviderClient mFusedLocationProviderClient;

	MapView mapView;
	GoogleMap mMap;
	public LocationManager locationManager;


	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.some_layout, container, false);

		mapView = (MapView) v.findViewById(R.id.mapview);
		mapView.onCreate(savedInstanceState);
 
		// Gets to GoogleMap from the MapView and does initialization stuff
		mapView.getMapAsync(this);

		mSearchText = (EditText) v.findViewById(R.id.input_search);
		mGps = (ImageView) v.findViewById(R.id.ic_gps);
		getLocationPermission();

		return v;
	}

	@Override
	public void onResume() {
		mapView.onResume();
		super.onResume();
	}
 
	@Override
	public void onDestroy() {
		super.onDestroy();
		mapView.onDestroy();
		locationManager.removeUpdates(this);

	}
 
	@Override
	public void onLowMemory() {
		super.onLowMemory();
		mapView.onLowMemory();
		locationManager.removeUpdates(this);

	}

	@Override
	public void onMapReady(GoogleMap googleMap) {


		Toast.makeText(getActivity(), "Map is Ready", Toast.LENGTH_SHORT).show();
		Log.d(TAG, "onMapReady: map is ready");
		mMap = googleMap;

		if (mLocationPermissionsGranted) {
			getDeviceLocation();

			if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
					!= PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(),
					Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
				return;
			}
			mMap.setMyLocationEnabled(true);
			mMap.getUiSettings().setMyLocationButtonEnabled(false);

			init();
		}

		mMap.getUiSettings().setMyLocationButtonEnabled(false);
		if (ContextCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) ==
				PackageManager.PERMISSION_GRANTED &&
				ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
						PackageManager.PERMISSION_GRANTED) {
			googleMap.setMyLocationEnabled(true);
			mMap.setMyLocationEnabled(true);
			googleMap.getUiSettings().setMyLocationButtonEnabled(true);

			// Needs to call MapsInitializer before doing any CameraUpdateFactory calls
			MapsInitializer.initialize(this.getActivity());
			// Updates the location and zoom of the MapView
			CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(43.1, -87.9), 10);
			mMap.animateCamera(cameraUpdate);
		} else {
			ActivityCompat.requestPermissions(getActivity(), new String[] {
							Manifest.permission.ACCESS_FINE_LOCATION,
							Manifest.permission.ACCESS_COARSE_LOCATION },
					TAG_CODE_PERMISSION_LOCATION);
		}

	}

	private void init(){
		Log.d(TAG, "init: initializing");
		mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			@Override
			public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
				if(actionId == EditorInfo.IME_ACTION_SEARCH
						|| actionId == EditorInfo.IME_ACTION_DONE
						|| keyEvent.getAction() == KeyEvent.ACTION_DOWN
						|| keyEvent.getAction() == KeyEvent.KEYCODE_ENTER
						|| keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER){
					//execute our method for searching
					Log.d(TAG, "trato de buscar");
					geoLocate();
					Log.d(TAG, "trate de buscar");
				}
				Log.d(TAG, "no se pudo entrar a buscar");
				return false;
			}
		});
		mGps.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				Log.d(TAG, "onClick: clicked gps icon");
				getDeviceLocation();
			}
		});
		hideSoftKeyboard();
	}


	private void geoLocate(){
		Log.d(TAG, "geoLocate: geolocating");
		String searchString = mSearchText.getText().toString();
		Geocoder geocoder = new Geocoder(getContext());
		List<Address> list = new ArrayList<>();
		try{
			list = geocoder.getFromLocationName(searchString, 1);
		}catch (IOException e){
			Log.e(TAG, "geoLocate: IOException: " + e.getMessage() );
		}
		if(list.size() > 0){
			Address address = list.get(0);

			Log.d(TAG, "geoLocate: found a location: " + address.toString());
			//Toast.makeText(this, address.toString(), Toast.LENGTH_SHORT).show();

			moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM,
					address.getAddressLine(0));
		}
	}

	private void getDeviceLocation(){
		Log.d(TAG, "getDeviceLocation: getting the devices current location");
		mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());
		try{
			if(mLocationPermissionsGranted){

				final Task location = mFusedLocationProviderClient.getLastLocation();
				if (location != null) {
					location.addOnCompleteListener(new OnCompleteListener() {
						@Override
						public void onComplete(@NonNull Task task) {
							if(task.isSuccessful()){
								Log.d(TAG, "onComplete: found location!");
								Location currentLocation = (Location) task.getResult();
								moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()),
										DEFAULT_ZOOM,
										"My Location");
							}else{
								Log.d(TAG, "onComplete: current location is null");
								Toast.makeText(getActivity(), "unable to get current location", Toast.LENGTH_SHORT).show();
							}
						}
					});
				}
				else{
					getLocationPermission();
				}

			}
		}catch (SecurityException e){
			Log.e(TAG, "getDeviceLocation: SecurityException: " + e.getMessage() );
		}
	}

	private void moveCamera(LatLng latLng, float zoom, String title){
		Log.d(TAG, "moveCamera: moving the camera to: lat: " + latLng.latitude + ", lng: " + latLng.longitude );
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));

		if(!title.equals("My Location")){
			MarkerOptions options = new MarkerOptions()
					.position(latLng)
					.title(title);
			mMap.addMarker(options);
		}
		hideSoftKeyboard();
	}

	private void hideSoftKeyboard(){
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
	}


	private void getLocationPermission(){
		locationManager = (LocationManager)  getActivity().getSystemService(getContext().LOCATION_SERVICE);


		Log.d(TAG, "getLocationPermission: getting location permissions");
		String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
				Manifest.permission.ACCESS_COARSE_LOCATION};
		if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
				FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
			if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
					COURSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
				mLocationPermissionsGranted = true;
			}else{
				ActivityCompat.requestPermissions(getActivity(),
						permissions,
						LOCATION_PERMISSION_REQUEST_CODE);
			}
		}else{
			ActivityCompat.requestPermissions(getActivity(),
					permissions,
					LOCATION_PERMISSION_REQUEST_CODE);
		}
	}
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		Log.d(TAG, "onRequestPermissionsResult: called.");
		mLocationPermissionsGranted = false;

		switch(requestCode){
			case LOCATION_PERMISSION_REQUEST_CODE:{
				if(grantResults.length > 0){
					for(int i = 0; i < grantResults.length; i++){
						if(grantResults[i] != PackageManager.PERMISSION_GRANTED){
							mLocationPermissionsGranted = false;
							Log.d(TAG, "onRequestPermissionsResult: permission failed");
							return;
						}
					}
					Log.d(TAG, "onRequestPermissionsResult: permission granted");
					mLocationPermissionsGranted = true;
				}
			}
		}
	}

	@Override
	public void onLocationChanged(Location location) {
		//Hey, a non null location! Sweet!

		//remove location callback:
		locationManager.removeUpdates(this);

	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {

	}

	@Override
	public void onProviderEnabled(String provider) {

	}

	@Override
	public void onProviderDisabled(String provider) {

	}


	public static Fragment newInstance() {
	    SomeFragment sm = new SomeFragment();
	    return sm;
    }
}
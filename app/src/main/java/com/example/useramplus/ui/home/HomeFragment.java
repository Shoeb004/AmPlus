package com.example.useramplus.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.useramplus.Callback.IFirebaseDriverInformation;
import com.example.useramplus.Callback.IFirebaseFailedListener;
import com.example.useramplus.Common;
import com.example.useramplus.Model.DriverGeoModel;
import com.example.useramplus.Model.DriverInfoModel;
import com.example.useramplus.Model.GeoQueryModel;
import com.example.useramplus.R;
import com.example.useramplus.databinding.FragmentHomeBinding;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
//import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import butterknife.ButterKnife;
import io.reactivex.Observable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
//import java.util.Observable;
//
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class HomeFragment extends Fragment implements OnMapReadyCallback, IFirebaseFailedListener, IFirebaseDriverInformation {

//    @BindView(R.id.activity_main)
//    SlidingUpPanelLayout slidingUpPanelLayout;
//    @BindView(R.id.text_welcome)
    TextView text_welcome;
    private AutocompleteSupportFragment autocompleteSupportFragment;


    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private SupportMapFragment mapFragment;

    //Location
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    //Load driver
    private double distance = 1.0;  //default in km
    private static final double LIMIT_RANGE = 10.0; //km
    private Location previousLocation, currentLocation;
    private boolean firsTime = true;

    //Listener
    IFirebaseDriverInformation iFirebaseDriverInformation;
    IFirebaseFailedListener iFirebaseFailedListener;
    private String cityName;

    // V18

//    private CompositeDisposable compositeDisposable = new CompositeDisposable();
//    private IGoogleAPI iGoogleAPI;

//    @Override
//    public void onStop() {
//        compositeDisposable.clear();
//        super.onStop();
//    }

    @Override
    public void onDestroy() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        init();
        initViews(root);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return root;
    }

    private void initViews(View root) {
//        TextView text_welcome = getChildFragmentManager()
        ButterKnife.bind(this,root);

//        Common.setWelcomeMessage(text_welcome);
    }

    public void init() {

        Places.initialize(getContext(),getString(R.string.google_api_key));
        autocompleteSupportFragment = (AutocompleteSupportFragment)getChildFragmentManager()
                .findFragmentById(R.id.autoComplete_fragment);
        autocompleteSupportFragment.setPlaceFields(Arrays.asList(Place.Field.ID,Place.Field.ADDRESS,Place.Field.NAME,Place.Field.LAT_LNG));
        autocompleteSupportFragment.setHint(getString(R.string.where_to));
        autocompleteSupportFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onError(@NonNull Status status) {
                Snackbar.make(getView(),""+status.getStatusMessage(),Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Snackbar.make(getView(),""+place.getLatLng(),Snackbar.LENGTH_LONG).show();
            }
        });

//        iGoogleAPI = RetrofitClient.getInstance().create(IGoogleAPI.class);


        iFirebaseFailedListener = this;
        iFirebaseDriverInformation = this;

        locationRequest = new LocationRequest();
        locationRequest.setSmallestDisplacement(10f);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                LatLng newPosition = new LatLng(locationResult.getLastLocation().getLatitude(),
                        locationResult.getLastLocation().getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(newPosition, 18f));

                //If user has change Location, calculate and laod driver
                if (firsTime) {
                    previousLocation = currentLocation = locationResult.getLastLocation();
                    firsTime = false;

                    setRestrictedPlacesInCountry(locationResult.getLastLocation());

                } else {
                    previousLocation = currentLocation;
                    currentLocation = locationResult.getLastLocation();
                }

                if (previousLocation.distanceTo(currentLocation) / 1000 <= LIMIT_RANGE) // Not over range
                    loadAvailableDrivers();
                else {
                    //Do nothing
                }
            }
        };

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());

        loadAvailableDrivers();

    }

    private void setRestrictedPlacesInCountry(Location location) {
        try {
            Geocoder geocoder = new Geocoder(getContext(),Locale.getDefault());
            List<Address> addressList = geocoder.getFromLocation(location.getLatitude(),location.getLongitude(),1);
            if (addressList.size() > 0)
                autocompleteSupportFragment.setCountry(addressList.get(0).getCountryCode());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void loadAvailableDrivers() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(getView(), getString(R.string.permission_require), Snackbar.LENGTH_SHORT).show();
            return;
        }
        fusedLocationProviderClient.getLastLocation()
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                        //load all available driver in city
                        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                        List<Address> addressList;
                        try {

                            // error
                            addressList = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(),1);
                            if (addressList.size() > 0)
                                cityName = addressList.get(0).getLocality();
                            if (!TextUtils.isEmpty(cityName)) {
                                //Query
                                DatabaseReference driver_location_ref = FirebaseDatabase.getInstance()
                                        .getReference(Common.DRIVERS_LOCATION_REFERENCE)
                                        .child(cityName);
                                GeoFire gf = new GeoFire(driver_location_ref);
                                GeoQuery geoQuery = gf.queryAtLocation(new GeoLocation(location.getLatitude(),
                                        location.getLongitude()), distance);
                                geoQuery.removeAllListeners();

                                geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
                                    @Override
                                    public void onKeyEntered(String key, GeoLocation location) {
                                        Common.driversFound.add(new DriverGeoModel(key, location));
                                    }

                                    @Override
                                    public void onKeyExited(String key) {

                                    }

                                    @Override
                                    public void onKeyMoved(String key, GeoLocation location) {

                                    }

                                    @Override
                                    public void onGeoQueryReady() {
                                        if (distance <= LIMIT_RANGE) {
                                            distance++;
                                            loadAvailableDrivers();
                                        } else {
                                            distance = 1.0; //Rest it
                                            addDriverMarker();
                                        }

                                    }

                                    @Override
                                    public void onGeoQueryError(DatabaseError error) {
                                        Snackbar.make(getView(), error.getMessage(), Snackbar.LENGTH_SHORT).show();

                                    }
                                });

                                //Listen to new driver in city and range
                                driver_location_ref.addChildEventListener(new ChildEventListener() {
                                    @Override
                                    public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String previousChildName) {
                                        //Have new driver
                                        GeoQueryModel geoQueryModel = dataSnapshot.getValue(GeoQueryModel.class);
                                        GeoLocation geoLocation = new GeoLocation(geoQueryModel.getL().get(0),
                                                geoQueryModel.getL().get(1));
                                        DriverGeoModel driverGeoModel = new DriverGeoModel(dataSnapshot.getKey(),
                                                geoLocation);
                                        Location newDriverLocation = new Location("");
                                        newDriverLocation.setLatitude(geoLocation.latitude);
                                        newDriverLocation.setLongitude(geoLocation.longitude);
                                        float newDistance = location.distanceTo(newDriverLocation) / 1000; // in km
                                        if (newDistance <= LIMIT_RANGE)
                                            findDriverByKey(driverGeoModel); // if driver find add to map
                                    }

                                    @Override
                                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                                    }

                                    @Override
                                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });

                            }
                            else
                                Snackbar.make(getView(),getString(R.string.city_name_empty),Snackbar.LENGTH_LONG).show();

                        } catch (IOException e) {
//                            throw new RuntimeException(e);
                            Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }

                    }
                });
    }

    private void addDriverMarker() {
        if (Common.driversFound.size() > 0)
        {
            Observable.fromIterable(Common.driversFound)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(driverGeoModel ->{
                        //On next
                        findDriverByKey(driverGeoModel);
                    }, throwable -> {
                        Snackbar.make(getView(), throwable.getMessage(), Snackbar.LENGTH_SHORT).show();
                    },()->{

                    });

        }
        else
        {
            Snackbar.make(getView(),getString(R.string.driver_not_found),Snackbar.LENGTH_SHORT).show();
        }
    }

    private void findDriverByKey(DriverGeoModel driverGeoModel) {
        FirebaseDatabase.getInstance()
                .getReference(Common.DRIVER_INFO_REFERENCE)
                .child(driverGeoModel.getKey())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren())
                        {
                            driverGeoModel.setDriverInfoModel(dataSnapshot.getValue(DriverInfoModel.class));
                            iFirebaseDriverInformation.onDriverInfoLoadSuccess(driverGeoModel);
                        }
                        else
                            iFirebaseFailedListener.onFirebaseLoadFailed(getString(R.string.not_found_key)+driverGeoModel.getKey());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        iFirebaseFailedListener.onFirebaseLoadFailed(databaseError.getMessage());
                    }
                });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {

        mMap = googleMap;

        //Request permission to add current location
        Dexter.withContext(getContext())
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse permissionGrantedResponse) {
                        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                            return;
                        }
                        mMap.setMyLocationEnabled(true);
                        mMap.getUiSettings().setMyLocationButtonEnabled(true);
                        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                            @Override
                            public boolean onMyLocationButtonClick() {
                                if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                                        && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                                    return false;
                                }
                                fusedLocationProviderClient.getLastLocation()
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Snackbar.make(getView(), e.getMessage(), Snackbar.LENGTH_SHORT).show();
                                            }
                                        }).addOnSuccessListener(new OnSuccessListener<Location>() {
                                            @Override
                                            public void onSuccess(Location location) {
                                                LatLng userLatLng = new LatLng(location.getLatitude(),location.getLongitude());
                                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 18f));

                                            }
                                        });
                                return true;
                            }
                        });

                        //set layout button
                        View locationButton = ((View)mapFragment.getView().findViewById(Integer.parseInt("1"))
                                .getParent())
                                .findViewById(Integer.parseInt("2"));
                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) locationButton.getLayoutParams();
                        //Roght Bottom
                        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, 0);
                        params.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                        params.setMargins(0,0,0,250);
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
                        Snackbar.make(getView(), permissionDeniedResponse.getPermissionName() +" need enable"
                        ,Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permissionRequest, PermissionToken permissionToken) {

                    }
                })
                .check();


        mMap.getUiSettings().setZoomControlsEnabled(true);

        try {
            boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(),
                    R.raw.uber_maps_style));
            if (!success)
                Log.e("AMPLUS","Style parsing error");
        }catch (Resources.NotFoundException e){
            Log.e("AMPLUS",e.getMessage());
        }

    }

    @Override
    public void onFirebaseLoadFailed(String message) {
        Snackbar.make(getView(),message,Snackbar.LENGTH_SHORT).show();
    }

    @Override
    public void onDriverInfoLoadSuccess(DriverGeoModel driverGeoModel) {

        //if already have marker with this key, does not set again
        if (!Common.markerList.containsKey(driverGeoModel.getKey()))
            Common.markerList.put(driverGeoModel.getKey(),
                    mMap.addMarker(new MarkerOptions()
                            .position(new LatLng(driverGeoModel.getGeoLocation().latitude,
                                    driverGeoModel.getGeoLocation().longitude))
                            .flat(true)
                            .title(Common.buildName(driverGeoModel.getDriverInfoModel().getFirstName(),
                                    driverGeoModel.getDriverInfoModel().getLastName()))
                            .snippet(driverGeoModel.getDriverInfoModel().getPhoneNumber())
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.car))));

        if (!TextUtils.isEmpty(cityName))
        {
            DatabaseReference driverLocation = FirebaseDatabase.getInstance()
                    .getReference(Common.DRIVERS_LOCATION_REFERENCE)
                    .child(cityName)
                    .child(driverGeoModel.getKey());
            driverLocation.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if (!dataSnapshot.hasChildren())
                    {
                        if (Common.markerList.get(driverGeoModel.getKey()) != null)
                            Common.markerList.get(driverGeoModel.getKey()).remove(); //remove marker
                        Common.markerList.remove(driverGeoModel.getKey()); //remove marker information
                        // V18
//                        Common.driverLocationSubscribe.remove(driverGeoModel.getKey()); //Remove driver information too
                        driverLocation.removeEventListener(this); //remove event listener
                    }
                    //V18
                    // V18

//                    else
//                    {
//                        if (Common.markerList.get(driverGeoModel.getKey()) != null)
//                        {
//
//                            GeoQueryModel geoQueryModel = dataSnapshot.getValue(GeoQueryModel.class);
//                            AnimationModel animationModel = new AnimationModel(false, geoQueryModel);
//                            if (Common.driverLocationSubscribe.get(driverGeoModel.getKey()) != null)
//                            {
//                                Marker currentMarker = Common.markerList.get(driverGeoModel.getKey());
//                                AnimationModel oldPosition = Common.driverLocationSubscribe.get(driverGeoModel.getKey());
//
//                                String from = new StringBuilder()
//                                        .append(oldPosition.getGeoQueryModel().getL().get(0))
//                                        .append(",")
//                                        .append(oldPosition.getGeoQueryModel().getL().get(1))
//                                        .toString();
//
//                                String to = new StringBuilder()
//                                        .append(animationModel.getGeoQueryModel().getL().get(0))
//                                        .append(",")
//                                        .append(animationModel.getGeoQueryModel().getL().get(1))
//                                        .toString();
//
//                                moveMarkerAnimation(driverGeoModel.getKey(),animationModel, currentMarker,from,to);
//                            }
//                            else
//                            {
//                                // first location init
//                                Common.driverLocationSubscribe.put(driverGeoModel.getKey(), animationModel);
//                            }
//
//                        }
//                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    Snackbar.make(getView(),databaseError.getMessage(),Snackbar.LENGTH_SHORT).show();
                }
            });
        }
    }
    //V18
    // V18
//    private void moveMarkerAnimation(String key, AnimationModel animationModel, Marker currentMarker, String from, String to) {
//        if (!animationModel.isRun())
//        {
//            //Request API
//            compositeDisposable.add(iGoogleAPI.getDirections("driving", "less_driving",
//                    from,to,
//                    getString(R.string.google_api_key))
//                    .subscribeOn(Schedulers.io())
//                    .observeOn(AndroidSchedulers.mainThread())
//                    .subscribe(returnResult ->{
//                        Log.d("API_RETURN", returnResult);
//
//                        try {
//                            //Parse jason
//                            JSONObject jsonObject = new JSONObject(returnResult);
//                            JSONArray jsonArray = jsonObject.getJSONArray("routes");
//                            for (int i=0;i<jsonArray.length();i++)
//                            {
//                                JSONObject route = jsonArray.getJSONObject(i);
//                                JSONObject poly = route.getJSONObject("overview_polyline");
//                                String polyline = poly.getString("points");
////                                polylineList = Common.decodePoly(polyline);
//                                animationModel.setPolylineList(Common.decodePoly(polyline));
//                            }
//
//
//                            // Moving
////                            index = -1;
////                            next = 1;
//                            animationModel.setIndex(-1);
//                            animationModel.setNext(1);
//
//
//                            Runnable runnable = new Runnable() {
//                                @Override
//                                public void run() {
//                                    if (animationModel.getPolylineList() != null &&
//                                            animationModel.getPolylineList().size()> 1)
//                                    {
//                                        if (animationModel.getIndex() < animationModel.getPolylineList().size() - 2)
//                                        {
////                                            index++;
//                                            animationModel.setIndex(animationModel.getIndex()+1);
//
////                                            next = index+1;
//                                            animationModel.setNext(animationModel.getIndex()+1);
////                                            start = polylineList.get(index);
//                                            animationModel.setStart(animationModel.getPolylineList().get(animationModel.getIndex()));
////                                            end = polylineList.get(next);
//                                            animationModel.setEnd(animationModel.getPolylineList().get(animationModel.getNext()));
//                                        }
//
//                                        ValueAnimator valueAnimator = ValueAnimator.ofInt(0,1);
//                                        valueAnimator.setDuration(3000);
//                                        valueAnimator.setInterpolator(new LinearInterpolator());
//                                        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//                                            @Override
//                                            public void onAnimationUpdate(@NonNull ValueAnimator value) {
////                                                v = value.getAnimatedFraction();
//                                                animationModel.setV(value.getAnimatedFraction());
////                                                lat = v*end.latitude + (1-v) * start.latitude;
//                                                animationModel.setLat(animationModel.getV() * animationModel.getEnd().latitude +
//                                                        (1-animationModel.getV()) * animationModel.getStart().latitude);
////                                                lng = v*end.longitude + (1-v) * start.longitude;
//                                                animationModel.setLng(animationModel.getV() * animationModel.getEnd().longitude +
//                                                        (1-animationModel.getV()) * animationModel.getStart().longitude);
//                                                LatLng newPos = new LatLng(animationModel.getLat(),animationModel.getLng());
//                                                currentMarker.setPosition(newPos);
//                                                currentMarker.setAnchor(0.5f,0.5f);
//                                                currentMarker.setRotation(Common.getBearing(animationModel.getStart(),newPos));
//                                            }
//                                        });
//                                        valueAnimator.start();
//                                        if (animationModel.getIndex() < animationModel.getPolylineList().size() -2) //Reach destination
//                                            animationModel.getHandler().postDelayed(this,1500);
//                                        else if (animationModel.getIndex() < animationModel.getPolylineList().size() -1) // Done
//                                        {
//                                            animationModel.setRun(false);
//                                            Common.driverLocationSubscribe.put(key,animationModel);  //Update data
//                                        }
//                                    }
//                                }
//                            };
//
//                            //Run handler
//                            animationModel.getHandler().postDelayed(runnable,1500);
//
//
//                        }catch (Exception e)
//                        {
//                            Snackbar.make(getView(),e.getMessage(),Snackbar.LENGTH_LONG).show();
//                        }
//
//                    })
//            );
//        }
//
//    }
}
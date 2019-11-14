package com.example.treasurehunt;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.animation.IntEvaluator;
import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.graphics.Color.parseColor;


public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    ArrayList<Marker> markerArray;
    ArrayList<Marker> movement;
    ArrayList<GroundOverlay> movementNew;
    ArrayList<GroundOverlay> movementNew1;
    String root;
    File myDir;
    GroundOverlay currPosMarker;
    GroundOverlay currPosMarker1;
    private GoogleMap mMap;
    LocationManager locationManager;
    Button mark_button;
    Button stop_button;
    //private LocationListener listener;
    private StringBuilder data;
    private static final String TAG = "MainActivity";
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;
    private Button mstart;
    private double currLongitude;
    private double currLatitude;
    private TextView mspeed;
    private ListView mlist;
    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL =   100;
    private GoogleApiClient googleApiClient;
    //private GoogleMap mMap;
    private long FASTEST_INTERVAL = 200;
    String x = "",csv;
    boolean clicked=false;
    int click=0;
    int start=0;
    ArrayList<String>questionList;
    ArrayList<String>answerList;
    ArrayList<Integer>markerCountList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps2);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        movement = new ArrayList<Marker>();
        movementNew = new ArrayList<com.google.android.gms.maps.model.GroundOverlay>();
        movementNew1 = new ArrayList<com.google.android.gms.maps.model.GroundOverlay>();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
        checkLocation(); //check whether location service is enable or not in your  phone
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        //boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        mMap = googleMap;
        boolean success = googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        //Toast.makeText(getApplicationContext(),String.valueOf(success), Toast.LENGTH_SHORT).show();
        root = android.os.Environment.getExternalStorageDirectory().toString();
        myDir = new File(root + "/TreasureHunt");
        Intent second_intent_maps = getIntent();

        String fname = myDir + "/" + second_intent_maps.getStringExtra("ITEM_SELECTED");
//        Toast.makeText(getApplicationContext(),fname, Toast.LENGTH_SHORT).show();

        InputStream instream = null;
        try {
            instream = new FileInputStream(fname);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        InputStreamReader inputreader = new InputStreamReader(instream);
        BufferedReader reader= new BufferedReader(inputreader);
        List<LatLng> latLngList = new ArrayList<LatLng>();
        markerCountList = new ArrayList<Integer>();
        questionList = new ArrayList<>();
        answerList = new ArrayList<>();

        String line = "";

        try {
            reader.readLine();
        } catch (IOException e) {
            e.printStackTrace();
        }
        while(true) // Read until end of file
        {
            try {
                if (!((line = reader.readLine()) != null)) break;
            } catch (IOException e) {
                e.printStackTrace();
            }
            double lat = Double.parseDouble(line.split(",")[0]);
            double lon = Double.parseDouble(line.split(",")[1]);
            int markerCount = Integer.parseInt(line.split(",")[2]);
            String question = String.valueOf(line.split(",")[3]);
            String answer = String.valueOf(line.split(",")[4]);
            latLngList.add(new LatLng(lat, lon));
            markerCountList.add(markerCount);
            questionList.add(question);
            answerList.add(answer);
        }
        //       Toast.makeText(getApplicationContext(),String.valueOf(latLngList.size()), Toast.LENGTH_SHORT).show();

// Add them to map
        markerArray = new ArrayList<Marker>();
        for(int i=0;i<=latLngList.size() -1 ;i++)
        {
            markerArray.add(mMap.addMarker(new MarkerOptions()
                    .position(latLngList.get(i))
                    .title(String.valueOf(markerCountList.get(i)))
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.checkpoint)))); // Don't necessarily need title
            // Toast.makeText(getApplicationContext(),String.valueOf(pos), Toast.LENGTH_SHORT).show();

        }

        for(Marker mark : markerArray)
        {
            //          Toast.makeText(getApplicationContext(),"Marker hidden", Toast.LENGTH_SHORT).show();
            mark.setVisible(false);
        }

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(final Marker marker) {
                //if (marker.getTitle().equals("0")) // if marker source is clicked
                //Toast.makeText(MapsActivity2.this.getApplicationContext(),"Clicked the title "+  marker.getTitle(), Toast.LENGTH_SHORT).show();
                Toast.makeText(MapsActivity2.this.getApplicationContext(),questionList.get(Integer.parseInt(marker.getTitle())) +
                        answerList.get(Integer.parseInt(marker.getTitle()))
                        , Toast.LENGTH_SHORT).show();

                final Dialog builder = new Dialog(MapsActivity2.this);
                builder.setContentView(R.layout.dialog_layout_result);
                final String currentQuestion = questionList.get(Integer.parseInt(marker.getTitle()));
                final String currentAnswer = answerList.get(Integer.parseInt(marker.getTitle()));
                final String nextQuestion;

                if(Integer.parseInt(marker.getTitle()) == markerCountList.size() - 1)
                {
                    nextQuestion = "END";
                }
                else
                    nextQuestion = questionList.get(Integer.parseInt(marker.getTitle()) + 1);

                final TextView status=(TextView)builder.findViewById(R.id.status);
                final TextView quesCurrent=(TextView)builder.findViewById(R.id.quesCurrent);
                quesCurrent.setText(currentQuestion);
                final EditText ansCurrent=(EditText)builder.findViewById(R.id.ansCurrent);

                final Button button = (Button) builder.findViewById(R.id.submit);
                button.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        String answer=ansCurrent.getText().toString();

                        if(answer.equalsIgnoreCase(currentAnswer))
                        {
                            status.setText("Correct : Next Question");
                        }
                        else
                        {
                            status.setText("Incorrect Answer");
                        }

                        if(nextQuestion.equals("END")) {
                            quesCurrent.setText("GAME OVER");
                           // builder.dismiss();
                        }
                        else {
                            button.setText("START");
                            quesCurrent.setText(nextQuestion);
                        }
                        //builder.dismiss();
                    }});

                builder.getWindow().getAttributes().width = WindowManager.LayoutParams.FILL_PARENT;
                builder.getWindow().getAttributes().height = WindowManager.LayoutParams.WRAP_CONTENT;
                builder.show();

                return true;
            }
        });


        LatLng tmppos = new LatLng(0.0, 0.0);
        int radiusM = 3;// your radius in meters

        // draw circle
        int d = 500; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        //p.setColor(getResources().getColor(R.color.green));
        //p.setColor(Integer.parseInt("#4c6c79"));
        c.drawCircle(d/2, d/2, d/2, p);

        // generate BitmapDescriptor from circle Bitmap
        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

        currPosMarker = googleMap.addGroundOverlay(new GroundOverlayOptions().
                image(bmD).
                position(tmppos,radiusM*2,radiusM*2).
                transparency(0.4f));


        //Animating Circle Starts

        // The drawable to use for the circle
        GradientDrawable dr = new GradientDrawable();
        dr.setShape(GradientDrawable.OVAL);
        dr.setSize(500,500);
        //dr.setColor(0x555751FF);
        //dr.setColor(0xffa500);
        dr.setStroke(5, Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(dr.getIntrinsicWidth()
                , dr.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        dr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        dr.draw(canvas);

        // Radius of the circle
        final int radius = 20;

        // Add the circle to the map
        final GroundOverlay circle = googleMap.addGroundOverlay(new GroundOverlayOptions()
                .position(tmppos, 2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));

        currPosMarker1 = circle;



        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {
            startLocationUpdates();
            //      mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //      mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
   //         Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        //Toast.makeText(this, "checking for location", Toast.LENGTH_SHORT).show();
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());

        //mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
        //mLongitudeTextView.setText(String.valueOf(location.getLongitude() ));
        //mspeed.setText(location.getSpeed()+" m/s");

       // Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        //LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        //LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        LatLng pos = new LatLng(location.getLatitude(), location.getLongitude());

        currLatitude = location.getLatitude();
        currLongitude = location.getLongitude();

        double markLatitude;
        double markLongitude;

        double distanceValue;
        for(Marker mark: markerArray)
        {
            markLatitude = mark.getPosition().latitude;
            markLongitude = mark.getPosition().longitude;

            distanceValue = distance(currLatitude, currLongitude, markLatitude, markLongitude);

          //  Toast.makeText(this, "Distance Value is "+distanceValue, Toast.LENGTH_SHORT).show();

            if(distance(currLatitude, currLongitude, markLatitude, markLongitude) < 20)
            {
                mark.setVisible(true);
           //     Toast.makeText(this, "In distance with Title " + mark.getTitle(), Toast.LENGTH_SHORT).show();
            }
            else
            {
                mark.setVisible(false);
            }

        }
        /*int size;
        size = movementNew1.size();
        if(size>1)
        {
            for(GroundOverlay mark: movementNew1)
            {
                mark.setVisible(false);
            }
        }

        size = movementNew.size();
        if(size>1)
        {
            for(GroundOverlay mark: movementNew)
            {
                mark.remove();
            }
        }*/

        int radiusM = 3;// your radius in meters

        // draw circle
        int d = 500; // diameter
        Bitmap bm = Bitmap.createBitmap(d, d, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        //p.setColor(getResources().getColor(R.color.green));
        //p.setColor(Integer.parseInt("#4c6c79"));
        p.setColor(parseColor("#ffa500"));
        c.drawCircle(d/2, d/2, d/2, p);

        // generate BitmapDescriptor from circle Bitmap
        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);

        currPosMarker.remove();
        currPosMarker = (mMap.addGroundOverlay(new GroundOverlayOptions().
                image(bmD).
                position(pos,radiusM*2,radiusM*2).
                transparency(0.4f)));

        //Animating Circle Starts

        // The drawable to use for the circle
        GradientDrawable dr = new GradientDrawable();
        dr.setShape(GradientDrawable.OVAL);
        dr.setSize(500,500);
        dr.setColor(0x55ffa500);
        dr.setStroke(5, Color.TRANSPARENT);

        Bitmap bitmap = Bitmap.createBitmap(dr.getIntrinsicWidth()
                , dr.getIntrinsicHeight()
                , Bitmap.Config.ARGB_8888);

        // Convert the drawable to bitmap
        Canvas canvas = new Canvas(bitmap);
        dr.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        dr.draw(canvas);

        // Radius of the circle
        final int radius = 20;

        // Add the circle to the map
        final GroundOverlay circle = mMap.addGroundOverlay(new GroundOverlayOptions()
                .position(pos, 2 * radius).image(BitmapDescriptorFactory.fromBitmap(bitmap)));

        currPosMarker1.remove();
        currPosMarker1 = circle;
        //movementNew.add(circle);
// mapView is the GoogleMap



        ValueAnimator valueAnimator = new ValueAnimator();
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
       // valueAnimator.setRepeatMode(ValueAnimator.RESTART);
        valueAnimator.setIntValues(0, radius);
        valueAnimator.setDuration(1000);
        valueAnimator.setEvaluator(new IntEvaluator());
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float animatedFraction = valueAnimator.getAnimatedFraction();
                circle.setDimensions(animatedFraction * radius * 2);
            }
        });

        valueAnimator.start();


  /* movement.add(mMap.addMarker(new MarkerOptions()
                .position(pos)
                .title("Title!")));
*/
        //.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))); // Don't necessarily need title
        // Toast.makeText(getApplicationContext(),String.valueOf(pos), Toast.LENGTH_SHORT).show();

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(pos));
        float zoomLevel = 17.5f; //This goes up to 21
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pos, zoomLevel));



    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public void stop(View view)
    {
        clicked=true;
    }

    private double distance(double lat1, double lon1, double lat2, double lon2) {
        double theta = lon1 - lon2;
        double dist = Math.sin(deg2rad(lat1))
                * Math.sin(deg2rad(lat2))
                + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2))
                * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);
        dist = (dist * 60 * 1.1515 * 1.609344)*1000;
        return (dist);
    }

    private double deg2rad(double deg) {
        return (deg * Math.PI / 180.0);
    }

    private double rad2deg(double rad) {
        return (rad * 180.0 / Math.PI);
    }
}



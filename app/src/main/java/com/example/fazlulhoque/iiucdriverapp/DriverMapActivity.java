package com.example.fazlulhoque.iiucdriverapp;

import android.*;
import com.example.fazlulhoque.iiucdriverapp.Common.Common;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class DriverMapActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,OnMapReadyCallback, android.location.LocationListener,GoogleMap.OnInfoWindowClickListener{

    private DrawerLayout mDrawerlayout;
    private ActionBarDrawerToggle mToggle;

    private GoogleMap mMap;

    private Location mLastLocation;
    private LocationManager mLocationManager;
    public static final int REQUEST_LOCATION_CODE = 99;
    private Button mLogout;
    private String customerId = "";
    private Boolean isLoggingout = false;

    private boolean isGPSEnabled = false;
    private boolean isNetworkEnabled = false;
    private static final int RP_ACCESS_LOCATION = 1;
    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0;
    private static final long MIN_TIME_BW_UPDATES = 500;

    private List<LatLng> latLngs;
    HashMap<String, LatLng> hashMap;
    String getName;

    private String pokeCondition="";
    MarkerOptions[] pokeMarker;
    private String newStudentId="6ytt777t77t3456";
    ArrayList<String> allPokeID;
    String driverUndoPoke=" ";
    List<Marker> markerWithColors;

    FirebaseStorage firebaseStorage;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_map);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            checkLocationPermission();
        }

        //pokeMarker=new MarkerOptions[2];

        allPokeID=new ArrayList<>();
        pokeMarker=new MarkerOptions[2];
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.

        getName=getIntent().getExtras().getString("gender");

        getLocation();
        getAssignedPickupLocation();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

      /*  FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });*/

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

      /*  View navigationHeaderView =  navigationView.getHeaderView(0);
        final TextView userName = (TextView)navigationHeaderView.findViewById(R.id.userName);
        CircleImageView userImage = (CircleImageView)navigationHeaderView.findViewById(R.id.imageUser);

        userName.setText(Common.currentUser.getName());

        //But with userimage ,we just chaeck it with null or empty

        if(Common.currentUser.getImageUrl() !=null && !TextUtils.isEmpty(Common.currentUser.getImageUrl())){
            Picasso.with(this).load(Common.currentUser.getImageUrl()).into(userImage);
        }*/
    }
    private void signOut() {
        isLoggingout=true;
        disconnectDriver();
        FirebaseAuth.getInstance().signOut();
        Intent intent = new Intent(DriverMapActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
        return;
    }

    private String realtype;
    private void getAssignedPickupLocation()  {
        DatabaseReference getAssignedPickupLocationRef = FirebaseDatabase.getInstance().getReference().child("studentRequest");
        getAssignedPickupLocationRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    mMap.clear();

                    for (final DataSnapshot eachcustomers : dataSnapshot.getChildren()) {
                        final String studentid = eachcustomers.getKey();
                        final String driverid=FirebaseAuth.getInstance().getCurrentUser().getUid();

                        DatabaseReference studenttype = FirebaseDatabase.getInstance().getReference("studentRequest").child(studentid).child("type");


                        studenttype.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists())
                                {
                                    realtype=dataSnapshot.getValue(String.class);
                                    //  Log.d("realtype","realtypefind"+ realtype);

                                    if(getName.equals(realtype))
                                    {
                                        DatabaseReference undopoke=FirebaseDatabase.getInstance().getReference("undopoke").child(studentid);
                                        undopoke.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {
                                                    List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                                                    double LocationLat = 0;
                                                    double LocationLng = 0;

                                                    if (map.get(0) != null) {
                                                        LocationLat = Double.parseDouble(map.get(0).toString());
                                                    }
                                                    if (map.get(1) != null) {
                                                        LocationLng = Double.parseDouble(map.get(1).toString());
                                                    }
                                                    LatLng customerLatLng = new LatLng(LocationLat, LocationLng);

                                                    mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                                    //  pokeMarker[1]=new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).title(customerId).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                                }


                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                        final String driverid=FirebaseAuth.getInstance().getCurrentUser().getUid();
                                        final DatabaseReference pokeref=FirebaseDatabase.getInstance().getReference("poke").child(driverid).child(studentid);
                                        pokeref.addValueEventListener(new ValueEventListener() {
                                            @Override
                                            public void onDataChange(final DataSnapshot dataSnapshot) {
                                                if(dataSnapshot.exists())
                                                {


                                                    Log.d("markerDebugPKR","student id"+ studentid);
                                                    if (allPokeID.contains(studentid)){
                                                        // Toast.makeText(DriverMapsActivity.this, "Id in Array", Toast.LENGTH_SHORT).show();
                                                    }else{
                                                        allPokeID.add(studentid);
                                                        //allPokeID.add(driverid);
                                                        // Toast.makeText(DriverMapsActivity.this, "Id not in Array", Toast.LENGTH_SHORT).show();


                                                        AlertDialog.Builder notifyPoke= new AlertDialog.Builder(DriverMapActivity.this);
                                                        notifyPoke.setTitle("Is there space in bus?");
                                                        notifyPoke.setPositiveButton("Accept", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                DatabaseReference replyRef= pokeref.child("Reply");
                                                                replyRef.setValue("A");

                                                                dialogInterface.dismiss();
                                                                List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                                                                double LocationLat = 0;
                                                                double LocationLng = 0;

                                                                if (map.get(0) != null) {
                                                                    LocationLat = Double.parseDouble(map.get(0).toString());
                                                                }
                                                                if (map.get(1) != null) {
                                                                    LocationLng = Double.parseDouble(map.get(1).toString());
                                                                }
                                                                LatLng customerLatLng = new LatLng(LocationLat, LocationLng);
                                                                //  hashMap.put(driverId,customerLatLng);
                                                                pokeCondition="A";
                                                                // pokeMarker[0] =new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).title(customerId).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

                                                                mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).title(studentid).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                                                            }
                                                        });
                                                        notifyPoke.setNegativeButton("Deny", new DialogInterface.OnClickListener() {
                                                            @Override
                                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                                DatabaseReference replyRef= pokeref.child("Reply");
                                                                replyRef.setValue("D");
                                                                //pokeCondition="D";

                                                                dialogInterface.dismiss();
                                                                List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                                                                double LocationLat = 0;
                                                                double LocationLng = 0;

                                                                if (map.get(0) != null) {
                                                                    LocationLat = Double.parseDouble(map.get(0).toString());
                                                                }
                                                                if (map.get(1) != null) {
                                                                    LocationLng = Double.parseDouble(map.get(1).toString());
                                                                }
                                                                LatLng customerLatLng = new LatLng(LocationLat, LocationLng);
                                                                pokeCondition="D";
                                                                mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
                                                                //  pokeMarker[1]=new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).title(customerId).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));

                                                            }
                                                        });
                                                        notifyPoke.show();
                                                    }

                                     /*  AlertDialog alert=notifyPoke.create();
                                          alert.show();
                                                    String a[]={studentid.toString()};
                                                   newStudentId=studentid;*/

                                                }

                                            }

                                            @Override
                                            public void onCancelled(DatabaseError databaseError) {

                                            }
                                        });


                                    }



                                }


                                //  Log.d("realtype",realtype);
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });


                        //

                        Log.d("realtypefound","realtypefound"+ realtype);
                        List<Object> map = (List<Object>) eachcustomers.child("l").getValue();
                        double LocationLat = 0;
                        double LocationLng = 0;

                        if (map.get(0) != null) {
                            LocationLat = Double.parseDouble(map.get(0).toString());
                        }
                        if (map.get(1) != null) {
                            LocationLng = Double.parseDouble(map.get(1).toString());
                        }
                        // LatLng studentLatLng = new LatLng(LocationLat, LocationLng);
                        final LatLng customerLatLng = new LatLng(LocationLat, LocationLng);



                        if(allPokeID.contains(studentid))
                        {

                            if (pokeCondition.equals("A")){
                                // mMap.addMarker(pokeMarker[0]);


                                mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                            }
                            if (pokeCondition.equals("D")){
                                //  mMap.addMarker(pokeMarker[1]);


                              //  mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

                            }
                            //else
                            //  mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));



                        }
                        else {


                            mMap.addMarker(new MarkerOptions().position(new LatLng(customerLatLng.latitude, customerLatLng.longitude)).snippet(studentid).title("undopoke").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW)));
                        }

                    }


                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.setOnInfoWindowClickListener(this);
        }
        mMap.setOnInfoWindowClickListener(this);


    }



    public boolean checkLocationPermission() {

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION_CODE);
            }
            return false;
        } else
            return true;

    }


    @Override
    public void onLocationChanged(Location location) {
        Log.d("locUpdate", String.valueOf(location.getLatitude()));
        mLastLocation = location;

        String driverId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("DriverAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(driverId, new GeoLocation(mLastLocation.getLatitude(), mLastLocation.getLongitude()));
        if(getName.equals("male"))
        {
            ref.child(driverId).child("type").setValue(getName);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        }

        if(getName.equals("female"))
        {
            ref.child(driverId).child("type").setValue(getName);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
        if(getName.equals("teacher"))
        {
            ref.child(driverId).child("type").setValue(getName);

            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        }

    }




    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }


    public void getLocation() {
        mLocationManager = (LocationManager) DriverMapActivity.this.getSystemService(LOCATION_SERVICE);
        isGPSEnabled = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Location location;

        if (!isGPSEnabled && !isNetworkEnabled) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(DriverMapActivity.this);
            alertDialog.setTitle("GPS Settings");
            alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");
            alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    DriverMapActivity.this.startActivity(intent);
                }
            });

            alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            alertDialog.show();
        } else {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {

                if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                        ActivityCompat.shouldShowRequestPermissionRationale(this,
                                android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    String[] perm = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                } else {
                    String[] perm = {android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION};
                    ActivityCompat.requestPermissions(this, perm,
                            RP_ACCESS_LOCATION);
                }
            } else {
                if (isNetworkEnabled) {
                    mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                            MIN_TIME_BW_UPDATES,
                            MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) DriverMapActivity.this);

                    if (mLocationManager != null) {
                        location = mLocationManager
                                .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                        if (location != null) {
                            onLocationChanged(location);
                        }
                    }
                }

                if (isGPSEnabled) {
                    if (mLastLocation == null) {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                MIN_TIME_BW_UPDATES,
                                MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) DriverMapActivity.this);

                        if (mLocationManager != null) {
                            location = mLocationManager
                                    .getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                onLocationChanged(location);
                            }
                        }
                    }
                }
            }
        }
    }

    private void disconnectDriver() {


        if (ActivityCompat.checkSelfPermission(DriverMapActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DriverMapActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }else{
            mLocationManager.removeUpdates(DriverMapActivity.this);

        }

        mLocationManager.removeUpdates(DriverMapActivity.this);
        String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("DriverAvailable");
        GeoFire geoFire1 = new GeoFire(ref);
        geoFire1.removeLocation(userId);


    }



    @Override
    protected void onStop() {
        super.onStop();
        if(!isLoggingout)
        {
            disconnectDriver();
        }


    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.driver_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        /*if (id == R.id.action_settings) {
            return true;
        }*/

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

       /* if(id == R.id.nav_updateinfo){
            showDialogUpdateInfo();
        }
        else */
       if (id == R.id.nav_logout) {
            signOut();

        } else if (id == R.id.nav_help) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    /*private void showDialogUpdateInfo() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("UPDATE INFORMATION");
        dialog.setMessage("Please fill all information");

        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_info = inflater.inflate(R.layout.layout_update_information,null);

        final MaterialEditText edtName = layout_info.findViewById(R.id.edtName);
        final ImageView image_update = layout_info.findViewById(R.id.image_update);

        image_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chooseImage();
            }
        });

        dialog.setView(layout_info);

        //set button
        dialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                final android.app.Dialog waitingDialog = new SpotsDialog(DriverMapActivity.this);
                waitingDialog.show();

                String name = edtName.getText().toString();

                Map<String,Object> updateInfo = new HashMap<>();
                if(!TextUtils.isEmpty(name))
                    updateInfo.put("name",name);

                DatabaseReference userinformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                userinformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .updateChildren(updateInfo)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful())
                                    Toast.makeText(DriverMapActivity.this, "Information  Update !", Toast.LENGTH_SHORT).show();
                                else
                                    Toast.makeText(DriverMapActivity.this, "Information Update Failed !", Toast.LENGTH_SHORT).show();

                                waitingDialog.dismiss();
                            }
                        });
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }
    private void chooseImage() {
        Intent intent =new Intent();
        intent.setType("image*//*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"select picture: "),Common.PICK_IMAGE_REQUEST);
            *//*startActivityForResult(intent,Gallery_Pick);*//*
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null)
            *//*if(requestCode == Gallery_Pick && resultCode == RESULT_OK && data != null && data.getData() != null)*//*
        {
            Uri saveUri = data.getData();
            if(saveUri != null){
                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Upoloading ...");
                mDialog.show();

                String imageName = UUID.randomUUID().toString(); //rendom name image upload
                final StorageReference imageFolder = storageReference.child("image/"+imageName);
                imageFolder.putFile(saveUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        mDialog.dismiss();
                        Toast.makeText(DriverMapActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                        imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                Map<String,Object> imageUpdate = new HashMap<>();
                                imageUpdate.put("imageUrl",uri.toString());

                                DatabaseReference userinformation = FirebaseDatabase.getInstance().getReference(Common.user_driver_tbl);
                                userinformation.child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                        .updateChildren(imageUpdate)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful())
                                                    Toast.makeText(DriverMapActivity.this, "Uploaded !", Toast.LENGTH_SHORT).show();
                                                else
                                                    Toast.makeText(DriverMapActivity.this, "Upload Error !", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploaded"+progress+"%");
                            }
                        });
            }
        }
    }*/



    @Override
    public void onInfoWindowClick(Marker marker) {
        driverUndoPoke = marker.getSnippet();
        createUndoPoke();

    }

    public void createUndoPoke() {
        String  userId= FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref= FirebaseDatabase.getInstance().getReference("undopoke").child(driverUndoPoke);

        GeoFire geoFire=new GeoFire(ref);
        geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));

        //geoFire.setLocation(userId,new GeoLocation(mLastLocation.getLatitude(),mLastLocation.getLongitude()));


    }
}

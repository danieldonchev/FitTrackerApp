package com.daniel.FitTrackerApp.activities;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.daniel.FitTrackerApp.AppNetworkManager;
import com.daniel.FitTrackerApp.fragments.WeightLogFragment;
import com.daniel.FitTrackerApp.goal.GoalManager;
import com.daniel.FitTrackerApp.R;
import com.daniel.FitTrackerApp.authenticate.AbstractAuthentication;
import com.daniel.FitTrackerApp.dialogs.UpdateUserStatsDialog;
import com.daniel.FitTrackerApp.fragments.DailyStatsFragment;
import com.daniel.FitTrackerApp.fragments.EditSportActivity;
import com.daniel.FitTrackerApp.fragments.GoalAddFragment;
import com.daniel.FitTrackerApp.fragments.GoalsFragment;
import com.daniel.FitTrackerApp.fragments.SelectedActivityFromDB;
import com.daniel.FitTrackerApp.helpers.DBHelper;
import com.daniel.FitTrackerApp.interfaces.StopRecordingCallBacks;
import com.daniel.FitTrackerApp.fragments.RecordFragment;
import com.daniel.FitTrackerApp.fragments.SaveActivityFragment;
import com.daniel.FitTrackerApp.fragments.HistoryFragment;
import com.daniel.FitTrackerApp.helpers.PreferencesHelper;
import com.daniel.FitTrackerApp.preferences.SettingsFragment;
import com.daniel.FitTrackerApp.services.DownloadImageService;
import com.daniel.FitTrackerApp.sportactivity.SportActivity;
import com.daniel.FitTrackerApp.sportactivity.SportActivityTrackingService;
import com.daniel.FitTrackerApp.preferences.AudioSettingsFragment;
import com.daniel.FitTrackerApp.preferences.ProfileSettingsFragment;
import com.daniel.FitTrackerApp.synchronization.SyncHelper;
import com.daniel.FitTrackerApp.utils.AppUtils;
import com.daniel.FitTrackerApp.utils.IntentServiceResultReceiver;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.OptionalPendingResult;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.json.JSONObject;

import java.io.IOException;

import static com.daniel.FitTrackerApp.R.drawable.potato;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, StopRecordingCallBacks, IntentServiceResultReceiver.Receiver {
    private static final String PERSON_PHOTO_PATH = "photos";
    private static final String RECORD_FRAGMENT_TAG = "fragment_record";

    private NavigationView navigationView;
    private android.support.v4.app.FragmentManager fragmentManager;
    private Toolbar toolbar;
    private ImageView personPhotoImage;
    private Spinner photoOptions;
    private TextView personName;
    private GoogleApiClient mGoogleApiClient;
    private ActionBarDrawerToggle toggle;
    private DrawerLayout drawer;
    private FloatingActionButton fab, fabAddActivity, fabAddGoal;
    private CardView addActivityText, test1Text;
    private boolean isFABOpen;
    private Uri selectedImageUri;

    private boolean stoppedRecording;
    private SportActivity sportActivity;

    private IBinder trackerService;
    private ServiceConnection mConnection = new ServiceConnection() {

        public void onServiceConnected(ComponentName className, IBinder service) {
            trackerService = service;
            Fragment fragment =  getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if(fragment instanceof RecordFragment)
            {
                ((RecordFragment) fragment).onServiceConnected(className, service);
            }

        }

        public void onServiceDisconnected(ComponentName className) {
            Fragment fragment =  getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if(fragment instanceof RecordFragment)
            {
                ((RecordFragment)fragment).onServiceDisconnected(className);
            }
        }
    };

    public ServiceConnection getmConnection() {
        return mConnection;
    }

    public IBinder getTrackerService() {
        return trackerService;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        SyncHelper.requestManualSync(getApplicationContext(), false);
        fragmentManager = getSupportFragmentManager();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        fab = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        fabAddActivity = (FloatingActionButton) findViewById(R.id.fabAddActivity);
        fabAddGoal = (FloatingActionButton) findViewById(R.id.test1);
        addActivityText = (CardView) findViewById(R.id.add_activity_text);
        test1Text = (CardView) findViewById(R.id.test1_text);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);

        //setFirstInstallDefaults();
        checkGooglePlayAvailability();
        setNavDrawer();

        setPersonDetails();
        setView();
        isUpdated();

        PackageManager pm = getPackageManager();
        if (pm.hasSystemFeature(PackageManager.FEATURE_SENSOR_STEP_COUNTER)){
            sendBroadcast(new Intent("RestartService"));
        }
        setUpFABs();
}

    public void setCallbacks() {
        RecordFragment fragment = (RecordFragment) getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment != null) {
            if (fragment.trackingService != null) {
                fragment.trackingService.setCallbacks(MainActivity.this);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferencesHelper.getInstance().unbindService(getApplicationContext());
        PreferencesHelper.getInstance().unregisterListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferencesHelper.getInstance().bindService(getApplicationContext());
        PreferencesHelper.getInstance().registerListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setProfilePicFromDocs(selectedImageUri);
                } else {
                    Toast.makeText(getApplicationContext(), "Permission Denied",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions,
                        grantResults);
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.nav_appbar_menu, menu);
//        return true;
//    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case 502:{
                if(resultCode == RESULT_OK){
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        if (Build.VERSION.SDK_INT >= 23) {
                            requestPermissions(new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                            selectedImageUri = data.getData();
                        }
                    } else {
                        selectedImageUri = data.getData();
                        setProfilePicFromDocs(selectedImageUri);
                    }
                }
                photoOptions.setVisibility(View.GONE);
                break;
            }
            case 503:{
                if(resultCode == RESULT_OK){
                    Uri selectedImage = data.getData();
                    setProfilePicFromDocs(selectedImage);
                }
                photoOptions.setVisibility(View.GONE);
                break;
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof RecordFragment) {
            super.onBackPressed();
        }
        else if(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof ProfileSettingsFragment ||
                getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof AudioSettingsFragment ||
                getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof HistoryFragment ||
                getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof SaveActivityFragment)
        {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            {
                getSupportFragmentManager().popBackStack();
                setNavDrawerToggleOn();
            }
        }
        else {
            android.support.v4.app.Fragment fragment = new RecordFragment();
            fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, RECORD_FRAGMENT_TAG).commit();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if(toggle.onOptionsItemSelected(item))
        {
            DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            }
        }
        else if(item.getItemId() == android.R.id.home)
        {
            if(getSupportFragmentManager().getBackStackEntryCount() > 0)
            {
                getSupportFragmentManager().popBackStack();
                setNavDrawerToggleOn();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void stopRecording() {
        Fragment fragment = new SaveActivityFragment();
        ((SaveActivityFragment) fragment).setSportActivity(sportActivity);
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).addToBackStack(null).commit();
        fragmentManager.executePendingTransactions();
        getSupportActionBar().show();
    }

    @Override
    public void onReceiveSportActivity(SportActivity sportActivity)
    {
        this.sportActivity = sportActivity;
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sign_out_button: {
                    signOut();
                    break;
                }
            }
        }
    };

    public void setNavDrawer() {
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(this, drawer, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View header = navigationView.getHeaderView(0);
        personPhotoImage = (ImageView) header.findViewById(R.id.personPhoto);
        personName = (TextView) header.findViewById(R.id.personName);
        Button signOutButton = (Button) header.findViewById(R.id.sign_out_button);

        signOutButton.setOnClickListener(onClickListener);
        personPhotoImage.setImageBitmap(getPersonPhoto());

        photoOptions = (Spinner) header.findViewById(R.id.photoSpinner);

        personPhotoImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                photoOptions.setVisibility(View.VISIBLE);
                photoOptions.performClick();
            }
        });
        photoOptions.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(position == 1){
                    Intent takePicture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(takePicture, 502);
                } else if(position == 2){
                    Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    startActivityForResult(pickPhoto , 503);
                }
                photoOptions.setSelection(0);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        setNavDrawerToggleOn();
        switch (id) {
            case R.id.start: {
                if (!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof RecordFragment)) {
                    android.support.v4.app.Fragment fragment = new RecordFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, RECORD_FRAGMENT_TAG).commit();
                    fab.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.history: {
                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof HistoryFragment))
                {
                    Fragment fragment = new HistoryFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    fab.setVisibility(View.VISIBLE);
                }
                break;
            }
            case R.id.settings: {
                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof SettingsFragment))
                {
                    Fragment fragment = new SettingsFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    fab.setVisibility(View.GONE);
                }
                break;
            }
//            case R.id.peopleActivites: {
//                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof PeopleActivitiesMapFragment))
//                {
//                    Fragment fragment = new PeopleActivitiesMapFragment();
//                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//                    fab.setVisibility(View.GONE);
//                }
//                break;
//            }
//            case R.id.testFragment: {
//                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof TestFragment))
//                {
//                    Fragment fragment = new TestFragment();
//                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
//                    fab.setVisibility(View.GONE);
//                }
//                break;
//            }
            case R.id.dailyStats: {
                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof DailyStatsFragment))
                {
                    Fragment fragment = new DailyStatsFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    fab.setVisibility(View.GONE);
                }
                break;
            }
            case R.id.goals:{
                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof GoalsFragment))
                {
                    Fragment fragment = new GoalsFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    fab.setVisibility(View.VISIBLE);
                }
                break;
            }
            case R.id.weightLog:{
                if(!(getSupportFragmentManager().findFragmentById(R.id.content_frame) instanceof WeightLogFragment))
                {
                    Fragment fragment = new WeightLogFragment();
                    fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                    fab.setVisibility(View.GONE);
                }
                break;
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        if (resultCode == DownloadImageService.STATUS_FINISHED) {
            String name = PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext());
            Bitmap bitmap = AppUtils.getBitmapFromCache(this, name, PERSON_PHOTO_PATH);
            if (bitmap != null) {
                personPhotoImage.setImageBitmap(bitmap);
            } else {
                setDefaultPersonPhoto();
            }
        } else if (resultCode == DownloadImageService.STATUS_ERROR) {
            setDefaultPersonPhoto();
        }
    }

    private void downloadPersonPhoto(String url, String name, String path) {
        AppUtils.startImageDownload(this, url, name, path, this);
    }

    private void checkGooglePlayAvailability() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int code = api.isGooglePlayServicesAvailable(getApplicationContext());
        if (code == ConnectionResult.SUCCESS) {
            //all good
        } else {
            AlertDialog alertDialog = new AlertDialog.Builder(this).setMessage(
                    "You need to download Google Play Services in order to use this part of the application")
                    .create();
            alertDialog.show();
        }
    }

    private void setView() {
        if (getIntent().hasExtra(SportActivityTrackingService.STOPPED_RECORDING)) {
            Fragment fragment = new SaveActivityFragment();
            ((SaveActivityFragment)fragment).setSportActivityId(getIntent().getStringExtra(SportActivityTrackingService.SPORT_ACTIVITY_ID));
            fragmentManager.beginTransaction().add(R.id.content_frame, fragment).commit();
            fragmentManager.executePendingTransactions();
        }
            else if(getIntent().hasExtra(SelectedActivityFromDB.DELETED_ACTIVITY))
        {
            Fragment fragment = new HistoryFragment();
            fragmentManager.beginTransaction().add(R.id.content_frame, fragment).commit();
        }
        else {
            Fragment fragment = new RecordFragment();
            fragmentManager.beginTransaction().add(R.id.content_frame, fragment, RECORD_FRAGMENT_TAG).commit();
        }
    }

    private void signOut() {
        int signInType = PreferencesHelper.getInstance().getSignedInType(this);
        DBHelper.getInstance().updateAccountSettings(this,
                                                    PreferencesHelper.getInstance().getCurrentUserId(this),
                                                    PreferencesHelper.getInstance().getSettings(this));

        PreferencesHelper.getInstance().clear();
        if (signInType == AbstractAuthentication.SIGN_IN_FACEBOOK) {
            LoginManager.getInstance().logOut();
            startLoginActivity();
        } else if (signInType == AbstractAuthentication.SIGN_IN_GOOGLE) {
            buildGoogleApiClient(signOutCallback);
            mGoogleApiClient.connect();
        }
        else if(signInType == AbstractAuthentication.SING_IN_LOCAL)
        {
            startLoginActivity();
        }

        //setDefaultPersonDetails();
    }

    private void setPersonDetails() {
        setPersonNameEmai();
    }

    private void setPersonNameEmai() {
        personName.setText(PreferencesHelper.getInstance().getCurrentUserName(this));
    }

    private void setDefaultPersonDetails() {
        personName.setText("Namey McNameyson");
        setDefaultPersonPhoto();
    }

    private void setDefaultPersonPhoto() {
        personPhotoImage.setImageResource(potato);
    }

    private void startLoginActivity() {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    private void silentSignIn() {
        OptionalPendingResult<GoogleSignInResult> pendingResult = Auth.GoogleSignInApi.silentSignIn(mGoogleApiClient);
        if (pendingResult != null) {
            if (pendingResult.isDone()) {
                GoogleSignInResult result = pendingResult.get();
                if (result.isSuccess()) {
                    GoogleSignInAccount googleAccount = result.getSignInAccount();
                    downloadPersonPhoto(googleAccount.getPhotoUrl().toString(),
                            PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()),
                            PERSON_PHOTO_PATH);
                }
            }
        }
        mGoogleApiClient.disconnect();
    }

    GoogleApiClient.ConnectionCallbacks signOutCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            Auth.GoogleSignInApi.signOut(mGoogleApiClient);
            startLoginActivity();
            mGoogleApiClient.disconnect();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    GoogleApiClient.ConnectionCallbacks silentSignInCallback = new GoogleApiClient.ConnectionCallbacks() {
        @Override
        public void onConnected(@Nullable Bundle bundle) {
            silentSignIn();
        }

        @Override
        public void onConnectionSuspended(int i) {

        }
    };

    private synchronized void buildGoogleApiClient(GoogleApiClient.ConnectionCallbacks connectionCallbacks) {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestId()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    private void isUpdated()
    {
        if(!DBHelper.getInstance().isCurrentAccountUpdated(PreferencesHelper.getInstance().getCurrentUserEmail(this)))
        {
            //show update user stats dialog (height, weight, sex)
            UpdateUserStatsDialog userStatsDialog = new UpdateUserStatsDialog(this);
            userStatsDialog.show();
        } else {
            GoalManager.getInstance().load(getApplicationContext(), true);
        }
    }

    public void setNavDrawerToggleOn()
    {
        toggle.setDrawerIndicatorEnabled(true);
    }

    public void setNavDrawerToggleOff()
    {
        toggle.setDrawerIndicatorEnabled(false);
    }

    public void provideBackwardNavigation()
    {
        setNavDrawerToggleOff();
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private Bitmap getPersonPhoto() {
        int signInType = PreferencesHelper.getInstance().getSignedInType(this);

        Bitmap bitmap = AppUtils.getBitmapFromCache(this, PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()), PERSON_PHOTO_PATH);
        if (bitmap != null) {
            return bitmap;
        } else {
            if (signInType == AbstractAuthentication.SIGN_IN_GOOGLE) {
                buildGoogleApiClient(silentSignInCallback);
                mGoogleApiClient.connect();
            } else if (signInType == AbstractAuthentication.SIGN_IN_FACEBOOK) {
                String url = "https://graph.facebook.com/" + AccessToken.getCurrentAccessToken().getUserId() + "/picture?type=large";
                downloadPersonPhoto(url,
                                    PreferencesHelper.getInstance().getCurrentUserId(this),
                                    PERSON_PHOTO_PATH);
            }
            return BitmapFactory.decodeResource(this.getResources(), R.drawable.default_person_icon);
        }
    }

    public void sendChangedSettings(JSONObject jsonObject)
    {
        if(jsonObject.length() > 0)
        {
            AppNetworkManager.sendChangedSettings(getApplicationContext(), jsonObject, System.currentTimeMillis());
        }
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabAddActivity.setVisibility(View.VISIBLE);
        fabAddGoal.setVisibility(View.VISIBLE);

        addActivityText.setVisibility(View.VISIBLE);
        test1Text.setVisibility(View.VISIBLE);

        fabAddActivity.animate().translationY(-getResources().getDimension(R.dimen.fabDimen1));
        fabAddGoal.animate().translationY(-getResources().getDimension(R.dimen.fabDimen2));

        addActivityText.animate().translationY(-getResources().getDimension(R.dimen.fabDimen1));
        test1Text.animate().translationY(-getResources().getDimension(R.dimen.fabDimen2));
    }

    private void closeFABMenu(){
        isFABOpen = false;

        fabAddActivity.animate().translationY(0);
        fabAddGoal.animate().translationY(0);

        addActivityText.animate().translationY(0);
        test1Text.animate().translationY(0);

        fabAddActivity.setVisibility(View.GONE);
        fabAddGoal.setVisibility(View.GONE);

        addActivityText.setVisibility(View.GONE);
        test1Text.setVisibility(View.GONE);
    }

    private void setUpFABs(){
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
                if(!(fragment instanceof HistoryFragment) && !(fragment instanceof GoalsFragment)){
                    closeFABMenu();
                    fab.setVisibility(View.GONE);
                } else {
                    closeFABMenu();
                    fab.setVisibility(View.VISIBLE);
                }
            }
        });

        fab.setVisibility(View.GONE);
        closeFABMenu();
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isFABOpen){
                    showFABMenu();
                } else {
                    closeFABMenu();
                }
            }
        });

        fabAddActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new EditSportActivity()).addToBackStack(null).commit();
                if(isFABOpen){
                    closeFABMenu();
                }
            }
        });

        fabAddGoal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, new GoalAddFragment()).addToBackStack(null).commit();
                if(isFABOpen){
                    closeFABMenu();
                }
            }
        });
    }

    private void setProfilePicFromDocs(Uri uri){
        try {
            int orientation = 0;
            Cursor cursor = getContentResolver().query(uri, new String[] { MediaStore.Images.ImageColumns.ORIENTATION }, null, null, null);

            try {
                if (cursor.moveToFirst()) {
                    orientation = cursor.getInt(0);
                } else {
                    orientation = 0;
                }
            } finally {
                cursor.close();
            }
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            Float heightF = getResources().getDimension(R.dimen.profile_pic_height);
            Float widhtF = getResources().getDimension(R.dimen.profile_pic_width);
            int height = heightF.intValue();
            int width = widhtF.intValue();
            bitmap = Bitmap.createScaledBitmap(bitmap, width, height, false);
            switch(orientation) {

                case 90:
                    bitmap = AppUtils.rotateImage(bitmap, 90);
                    break;

                case 180:
                    bitmap = AppUtils.rotateImage(bitmap, 180);
                    break;

                case 270:
                    bitmap = AppUtils.rotateImage(bitmap, 270);
                    break;

                case 0:

                default:
                    break;
            }
            personPhotoImage.setImageBitmap(bitmap);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            AppNetworkManager.sendProfilePic(getApplicationContext(), stream.toByteArray());
            AppUtils.cacheBitmap(getApplicationContext(), bitmap,
                    PreferencesHelper.getInstance().getCurrentUserId(getApplicationContext()),
                    PERSON_PHOTO_PATH);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

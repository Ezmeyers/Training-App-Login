/*
* Resources Activity calls a list fragment which brings up the available resources (PDF, video) in a list fragment
*/

package edu.iupui.soic.biohealth.plhi.mhbs.activities;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;

import org.hisp.dhis.android.sdk.network.Credentials;

import java.util.ArrayList;
import java.util.List;

import edu.iupui.soic.biohealth.plhi.mhbs.R;
import edu.iupui.soic.biohealth.plhi.mhbs.documents.DocumentResources;
import edu.iupui.soic.biohealth.plhi.mhbs.documents.ResourceItemDownloader;
import edu.iupui.soic.biohealth.plhi.mhbs.fragments.CourseDetailsFragment;
import edu.iupui.soic.biohealth.plhi.mhbs.fragments.ItemFragment;
import edu.iupui.soic.biohealth.plhi.mhbs.fragments.PdfDetailsFragment;
import edu.iupui.soic.biohealth.plhi.mhbs.fragments.VideoDetailsFragment;

public class ResourcesActivity extends AppCompatActivity implements ItemFragment.OnListFragmentInteractionListener, DocumentResources.AsyncResponse, ResourceItemDownloader.DownloadResponse {
    private String ACTIVITY = "";
    private ImageButton btn_program;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL = 1;
    // may need to be cleared
    private ItemFragment itemFragment;
    private ProgressBar progressBar;
    boolean downloadOnly = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // set view to program portal buttons and ui elements
        setContentView(R.layout.activity_resources);

        //TODO: this is temporary to display course fragment for programmer testing
        ACTIVITY = getIntent().getStringExtra("resourceKey");
        if (ACTIVITY.equals("Courses")) {
            beginCourse();
        } else {
            setupUI();


            if (getIntent().getStringExtra("resourceKey") != null) {
                // Get the name of the activity which was called (Video or Pdf or course)
                ACTIVITY = getIntent().getStringExtra("resourceKey");
            } else {
                ACTIVITY = getIntent().getStringExtra("itemType");
            }
        }
    }


    private void beginCourse() {
        getSupportFragmentManager().beginTransaction().replace(R.id.whole_container, new CourseDetailsFragment()).commit();

    }

    // set up ui
    private void setupUI() {
        addListenerOnButton();

        // set the action bar to implement going back
        ActionBar myToolbar = this.getSupportActionBar();
        if (myToolbar != null) {
            myToolbar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME | ActionBar.DISPLAY_SHOW_TITLE);
            myToolbar.setDisplayHomeAsUpEnabled(true);
        }
    }


    public ItemFragment getItemFragment() {
        return this.itemFragment;
    }

    private void setItemFragment(ItemFragment itemFragment) {
        this.itemFragment = itemFragment;
    }

    // call in setupUi();
    public void addListenerOnButton() {
        final DocumentResources.AsyncResponse delegate = this;
        btn_program = (ImageButton) findViewById(R.id.btn_programs);
        btn_program.setOnClickListener(new ImageButton.OnClickListener() {

            @Override
            public void onClick(View view) {
                // disable while downloading
                btn_program.setClickable(false);

                // Downloading individual items
                new DocumentResources(delegate).execute(ACTIVITY);
                // display progress bar until receiving callback
                progressBar = (ProgressBar) findViewById(R.id.resourceDownloadBar);
                progressBar.setVisibility(View.VISIBLE);
            }

        });

    }

    // implemented from ItemFragment, the list has been populated,
    // on itemClick, we can attempt to download the requested content
    @Override
    public void onListFragmentInteraction(DocumentResources.ResourceItem item) {
        // if we can download
        if (checkRunTimePermissions()) {
            progressBar.setVisibility(View.VISIBLE);
            // Download resources
            ResourceItemDownloader resourceItemDownloader = new ResourceItemDownloader(this, item.id, this, ACTIVITY);
            resourceItemDownloader.setupFiles();
            resourceItemDownloader.tryToDownload();

        }
        // display progress bar while videos downloading
    }

    // fragment completed downloads or already has items. Display fragment.
    public void onFragmentComplete() {
        btn_program.setClickable(true);

        getSupportFragmentManager()
                .beginTransaction()
                .show(getItemFragment())
                .commit();
    }

    @Override
    public void onDownloadButtonClick(DocumentResources.ResourceItem item, boolean status) {
        //flag to only download but not display content
        downloadOnly = status;
        Log.d("Test", " "+ downloadOnly);
        onListFragmentInteraction(item);
    }


    // TODO: redo following android documentation
    private boolean checkRunTimePermissions() {
        int externalPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        List<String> listPermissionsNeeded = new ArrayList<>();

        if (externalPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toArray
                    (new String[listPermissionsNeeded.size()]), MY_PERMISSIONS_REQUEST_READ_EXTERNAL);
            return false;
        }
        return true;
    }


    @Override
    public void onBackPressed() {
        //TODO : if doc resources never run dont block
        if (DocumentResources.CURRENTLY_DOWNLOADING) {
            //block from backpress
        } else {
            NavUtils.navigateUpFromSameTask(this);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (DocumentResources.CURRENTLY_DOWNLOADING) {
            return false;
        }
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    super.onBackPressed();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }


    // return from downloading list content for Item fragment
    @Override
    public void processFinish(List<DocumentResources.ResourceItem> output) {
        progressBar.setVisibility(View.INVISIBLE);
        // start item fragment with list as bundle

            if (getItemFragment() == null) {
                setItemFragment(new ItemFragment());
                getItemFragment().setOutput(output);
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.res_fragment_container, getItemFragment())
                    .addToBackStack(null)
                    .commit();
    }

    public Credentials getCredentials() {
        String username = "";
        String password = "";
        SharedPreferences sharedPref = this.getSharedPreferences("credentials", 0);
        username = sharedPref.getString("username", "NULL");
        password = sharedPref.getString("password", "NULL");
        Credentials credentials = new Credentials(username, password);
        return credentials;
    }

    @Override
    public void onDownloadFinish(String fileName, String itemToDownload) {
        progressBar.setVisibility(View.INVISIBLE);

        // flag for case we only clicked download button
       if(!downloadOnly) {
           btn_program.setClickable(true);
           Fragment contentFragment;
           if (ACTIVITY.equals("Videos")) {
               contentFragment = new VideoDetailsFragment();
           } else {
               contentFragment = new PdfDetailsFragment();
           }


           Bundle b = new Bundle();
           b.putString("resourceDir", fileName);
           b.putString("itemToDownload", itemToDownload);
           contentFragment.setArguments(b);

           // now we can start our desired fragment.
           getSupportFragmentManager()
                   .beginTransaction()
                   .add(R.id.content_fragment_container, contentFragment)
                   .addToBackStack(null)
                   .commit();
       }else{
           //TODO: if item already downloaded, hide button ?
         View view = findViewById(R.id.res_fragment_container);
         Snackbar mySnackbar = Snackbar.make(view, "successfully downloaded", Snackbar.LENGTH_SHORT);
         mySnackbar.show();
       }
       // reset download button flag
        downloadOnly = false;

            // wait for callback setResourceStatus() to remove progressbar after download
    }
}
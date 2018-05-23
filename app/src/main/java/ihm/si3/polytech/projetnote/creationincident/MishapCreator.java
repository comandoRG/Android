package ihm.si3.polytech.projetnote.creationincident;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ihm.si3.polytech.projetnote.MainActivity;
import ihm.si3.polytech.projetnote.Manifest;
import java.util.Calendar;

import ihm.si3.polytech.projetnote.R;
import ihm.si3.polytech.projetnote.login.StoreUsers;
import ihm.si3.polytech.projetnote.utility.Mishap;
import ihm.si3.polytech.projetnote.utility.Priority;

import static android.app.Activity.RESULT_OK;

public class MishapCreator extends Fragment  {


    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final String ARG_SECTION_NUMBER = "1";
    private DatabaseReference databaseReference;
    private FusedLocationProviderClient mFusedLocationClient;

    Location mLocation;

    public MishapCreator() {


    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static MishapCreator newInstance() {
        MishapCreator fragment = new MishapCreator();
        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    // The onCreateView method is called when Fragment should create its View object hierarchy,
    // either dynamically or via XML layout inflation. 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        // Defines the xml file for the fragment
        return inflater.inflate(R.layout.recipe, parent, false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            ImageView imageView = getActivity().findViewById(R.id.imageView);
            imageView.setImageBitmap(imageBitmap);
        }
    }

    // This event is triggered soon after onCreateView().
    // Any view setup should occur here.  E.g., view lookups and attaching view listeners.
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        Button button = getActivity().findViewById(R.id.valider);
        Button buttonPicture = getActivity().findViewById(R.id.takePicture);
        Button buttonGPS = getActivity().findViewById(R.id.btnGPS);
        final Spinner spinner = getActivity().findViewById(R.id.SpinnerFeedbackType);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());

        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.

                            setLocationText(location);
                        }
                    });
        }




        buttonPicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }

            private void dispatchTakePictureIntent() {
                Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        });

        buttonGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                mFusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                // Got last known location. In some rare situations this can be null.
                                setLocationText(location);
                            }
                        });

            }
        });

        button.setOnClickListener(new View.OnClickListener() // do something on click
        {
            public void onClick(View v) {

                TextView title = getActivity().findViewById(R.id.title);
                TextView description = getActivity().findViewById(R.id.descriptionArticle);
                Mishap mishap = new Mishap();
                mishap.setTitle(title.getText().toString().trim());
                mishap.setDescription(description.getText().toString().trim());
                mishap.setPriority(Priority.valueOf(spinner.getSelectedItem().toString()));
                mishap.setAuthor(StoreUsers.getUserName());
                if(mLocation!=null) {
                    mishap.setxPos(mLocation.getLongitude());
                    mishap.setyPos(mLocation.getLatitude());
                }else{
                    mishap.setxPos((double) 0);
                    mishap.setyPos((double) 0);
                }
                mishap.setUrlPicture(StoreUsers.getUrlPicture());

                databaseReference = FirebaseDatabase.getInstance().getReference("mishap");
                String id = databaseReference.push().getKey();
                databaseReference.child(id).setValue(mishap);
                Toast.makeText(getContext(), "Information Save", Toast.LENGTH_LONG).show();

            }
        });
    }


    private void saveMishap() {

    }

    private void setLocationText(Location l){
        TextView textGPS= getActivity().findViewById(R.id.textGPS);
        if (l!=null){
            this.mLocation=l;
            textGPS.setText("Longitude : "+l.getLongitude()+"/Latitude : "+l.getLatitude());
        }else{
            textGPS.setText("Longitude : N /Latitude : N");
        }
    }






}
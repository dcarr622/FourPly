package perihelion.io.fourply.bathroom;

import android.Manifest;
import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.ButterKnife;
import perihelion.io.fourply.R;
import perihelion.io.fourply.imgur.imgurmodel.ImageResponse;
import perihelion.io.fourply.imgur.imgurmodel.Upload;
import perihelion.io.fourply.imgur.services.UploadService;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class AddBathroomFragment extends DialogFragment implements View.OnClickListener {

    private String id;
    private String image;
    private String TAG="AddBathroomFragment";

    private Upload upload;
    private File chosenFile;

    private float mLat;
    private float mLng;

    private TextView name;
    private TextView description;

    //@Bind(R.id.addbanner)
    //ImageView uploadImage;

    static final int REQUEST_IMAGE_CAPTURE = 1;

    public static AddBathroomFragment createInstance(float lat, float lng){
        AddBathroomFragment fragment = new AddBathroomFragment();
        Bundle args = new Bundle();
        args.putFloat("lat", lat);
        args.putFloat("lng", lng);
        fragment.setArguments(args);
        return fragment;
    }

    public AddBathroomFragment(){};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.bind(getActivity());
        Bundle args = getArguments();
        mLng = args.getFloat("lng");
        mLat = args.getFloat("lat");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_bathroom, null);

        view.findViewById(R.id.btn_cancel).setOnClickListener(this);
        view.findViewById(R.id.btn_ok).setOnClickListener(this);
        ImageView add = (ImageView) view.findViewById(R.id.addbanner);
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        name = (TextView) view.findViewById(R.id.field_name);
        description = (TextView) view.findViewById(R.id.field_description);
        return view;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_ok:
                createUpload(chosenFile);
                if (!name.getText().toString().isEmpty() && !description.getText().toString().isEmpty()) {
                    new UploadService(getActivity(), name.getText().toString(), description.getText().toString(), mLat, mLng).Execute(upload, new UiCallback());
                } else {
                    Toast.makeText(getActivity(), "Please Enter Text", Toast.LENGTH_LONG).show();
                }
                break;
            case R.id.btn_cancel:
                dismiss();
                break;
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String mCurrentPhotoPath;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(null);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void dispatchTakePictureIntent() {
        int permissionCheck = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

            } else {
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.CAMERA}, REQUEST_IMAGE_CAPTURE);
            }
        } else {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    chosenFile = createImageFile();
                } catch (IOException ex) {
                    // Error occurred while creating the File
                    Toast.makeText(getActivity(), "Failed to make file", Toast.LENGTH_LONG).show();
                }
                // Continue only if the File was successfully created
                if (chosenFile != null) {
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(chosenFile));
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_IMAGE_CAPTURE: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    Toast.makeText(getActivity(), "No Camera Permission Given", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == Activity.RESULT_OK) {
            ImageView view = ((ImageView) getView().findViewById(R.id.addbanner));
            view.setPadding(0, 0, 0, 0);
            view.setScaleType(ImageView.ScaleType.CENTER_CROP);
            if (chosenFile == null) {
                Toast.makeText(getActivity(), "Didn't get file from camera", Toast.LENGTH_LONG).show();
                return;
            }
            Picasso.with(getActivity())
                    .load(chosenFile)
                    .into(view);
        }
    }

    private void createUpload(File image) {
        upload = new Upload();

        upload.image = image;
        upload.title = "FourPly Upload";
        upload.description = "Don't forget to flush";
    }

    private class UiCallback implements Callback<ImageResponse> {

        @Override
        public void success(ImageResponse imageResponse, Response response) {
            Snackbar.make(getView(), "Success", Snackbar.LENGTH_SHORT).show();
            dismiss();
        }

        @Override
        public void failure(RetrofitError error) {
            //Assume we have no connection, since error is null
            if (error == null) {
                Snackbar.make(getView(), "No internet connection", Snackbar.LENGTH_SHORT).show();
            }
        }
    }
}
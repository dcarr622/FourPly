package perihelion.io.fourply;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.IOException;

/**
 * Created by vmagro on 1/23/16.
 */
public class ARGraffitiActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public native int processMat(long matAddrM, long matAddrOverlay);

    private static final String TAG = "ARGraffiti";
    private static final int PERMISSION_REQUEST_CAMERA = 42;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
        System.loadLibrary("argraffiti");
    }

    private FourplyCameraView mOpenCvCameraView = null;

    private Mat overlay = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_graffiti);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        try {
            overlay = Utils.loadResource(this, R.drawable.graffiti);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mOpenCvCameraView = (FourplyCameraView) findViewById(R.id.camera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    public void onResume() {
        super.onResume();
        // Here, thisActivity is the current activity
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // No explanation needed, we can request the permission.

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            mOpenCvCameraView.enableView();
        }
    }

    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mOpenCvCameraView.enableView();
                } else {
                    Toast.makeText(this, "Camera permission not granted", Toast.LENGTH_LONG).show();
                    finish();
                }
            }
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat input = inputFrame.rgba().clone();
        long start = System.currentTimeMillis();
        processMat(input.getNativeObjAddr(), overlay.getNativeObjAddr());
        long end = System.currentTimeMillis();
        Log.i(TAG, (end - start) + "ms");
        return input;
    }

}

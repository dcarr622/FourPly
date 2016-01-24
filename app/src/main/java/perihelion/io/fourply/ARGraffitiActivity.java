package perihelion.io.fourply;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by vmagro on 1/23/16.
 */
public class ARGraffitiActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    public native void processMat(long matAddrM, long matAddrOverlay);

    private static final String TAG = "ARGraffiti";
    private static final int PERMISSION_REQUEST_CAMERA = 42;
    private boolean mDrawTapRequested = false;

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
            FileInputStream in = openFileInput(getIntent().getStringExtra("id") + ".png");
            Bitmap graffitiBitmap = BitmapFactory.decodeStream(in);
            overlay = new Mat();
            Utils.bitmapToMat(graffitiBitmap, overlay);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        mOpenCvCameraView = (FourplyCameraView) findViewById(R.id.camera);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        findViewById(R.id.root).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDrawTapRequested = true;
            }
        });
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
        final Mat input = inputFrame.rgba().clone();
        long start = System.currentTimeMillis();
        ExecutorService executor = Executors.newCachedThreadPool();
        Callable<Object> task = new Callable<Object>() {
            public Object call() {
                processMat(input.getNativeObjAddr(), overlay.getNativeObjAddr());
                return null;
            }
        };
        Future<Object> future = executor.submit(task);
        try {
            future.get(500, TimeUnit.MILLISECONDS);
        } catch (TimeoutException | InterruptedException | ExecutionException ex) {
            // handle the timeout
            Log.w(TAG, "frame processing timed out");
            return input;
        } finally {
            future.cancel(true);
        }
        if (mDrawTapRequested) {
            mDrawTapRequested = false;
            try {
                FileOutputStream out = openFileOutput(getIntent().getStringExtra("id") + "bkg.png", Context.MODE_PRIVATE);
                Bitmap bitmap = Bitmap.createBitmap(input.cols(), input.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(input, bitmap);
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Intent drawIntent = new Intent(this, GraffitiActivity.class);
            drawIntent.putExtras(getIntent().getExtras());
            startActivity(drawIntent);
            finish();
        }
        if (overlay != null) {
            processMat(input.getNativeObjAddr(), overlay.getNativeObjAddr());
        }
        long end = System.currentTimeMillis();
        Log.i(TAG, (end - start) + "ms");
        return input;
    }

}

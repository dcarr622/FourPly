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
import org.opencv.core.Core;
import org.opencv.core.CvException;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.utils.Converters;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vmagro on 1/23/16.
 */
public class ARGraffitiActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ARGraffiti";
    private static final int PERMISSION_REQUEST_CAMERA = 42;

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private FourplyCameraView mOpenCvCameraView = null;

    private Mat overlay = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_graffiti);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Mat m = null;
        try {
            m = Utils.loadResource(this, R.drawable.stall);
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
        Mat input = inputFrame.rgba();
        Mat output = this.processMat(input);
        return output;
    }

    private static Point pointOnLineRaw(double rho, double theta, double distance) {
        final double a = Math.cos(theta);
        final double b = Math.sin(theta);
        final double x0 = a * rho;
        final double y0 = b * rho;
        final double x1 = Math.round(x0 + distance * (-b));
        final double y1 = Math.round(y0 + distance * a);
        return new Point(x1, y1);
    }

    private static Point pointOnLine(double theta, Point start, double distance) {
        theta = Math.PI / 2 - theta;
        double x1 = start.x + distance * Math.cos(theta);
        double x2 = start.x - distance * Math.cos(theta);
        double y1 = start.y - distance * Math.sin(theta);
        double y2 = start.y + distance * Math.sin(theta);
        if (y2 > y1) {
            return new Point(x2, y2);
        } else {
            return new Point(x1, y1);
        }
    }

    private static Point segIntersect(double r1, double t1, double r2, double t2, int width, int height) {
        double ct1 = Math.cos(t1);     //matrix element a
        double st1 = Math.sin(t1);     //b
        double ct2 = Math.cos(t2);     //c
        double st2 = Math.sin(t2);     //d
        double d = ct1 * st2 - st1 * ct2;        //determinative (rearranged matrix for inverse)
        if (d != 0.0f) {
            double x = ((st2 * r1 - st1 * r2) / d);
            double y = ((-ct2 * r1 + ct1 * r2) / d);
            if (x < 0 || x > width)
                return null;
            if (y < 0 || y > height)
                return null;
            return new Point(x, y);
        } else { //lines are parallel and will NEVER intersect!
            return null;
        }
    }

    private static class Corner {
        public Point p;
        public double[] horizontal;
        public double[] vertical;
    }

    private static List<Corner> findCorners(List<double[]> horizontalLines, List<double[]> verticalLines, int width, int height) {
        List<Corner> corners = new LinkedList<>();
        for (double[] horizontal : horizontalLines) {
            for (double[] vertical : verticalLines) {
                Point intersection = segIntersect(horizontal[0], horizontal[1], vertical[0], vertical[1], width, height);
                if (intersection != null) {
                    Corner c = new Corner();
                    c.p = intersection;
                    c.horizontal = horizontal;
                    c.vertical = vertical;
                    corners.add(c);
                }
            }
        }
        return corners;
    }

    private static double distance(Point a, Point b) {
        return Math.sqrt(Math.pow(a.x - b.x, 2) + Math.pow(a.y - b.y, 2));
    }

    private static Corner topLeftCorner(List<Corner> corners) {
        Collections.sort(corners, new Comparator<Corner>() {
            @Override
            public int compare(Corner lhs, Corner rhs) {
                double ldist = distance(new Point(0, 0), lhs.p);
                double rdist = distance(new Point(0, 0), rhs.p);
                if (ldist < rdist)
                    return -1;
                else if (ldist > rdist)
                    return 1;
                return 0;
            }
        });
        return corners.get(0);
    }

    private static Corner topRightCorner(List<Corner> corners, final int width) {
        Collections.sort(corners, new Comparator<Corner>() {
            @Override
            public int compare(Corner lhs, Corner rhs) {
                double ldist = distance(new Point(width, 0), lhs.p);
                double rdist = distance(new Point(width, 0), rhs.p);
                if (ldist < rdist)
                    return -1;
                else if (ldist > rdist)
                    return 1;
                return 0;
            }
        });
        return corners.get(0);
    }

    private static class GraffitiFrame {
        public Point topLeft;
        public Point topRight;
        public Point bottomLeft;
        public Point bottomRight;
        public boolean valid;
    }

    private static boolean drawGraffiti(Mat img, Mat overlay, GraffitiFrame frame) {
        List<Point> fromCorners = Arrays.asList(
                new Point(0, 0), new Point(overlay.width(), 0), new Point(0, overlay.height()), new Point(overlay.width(), overlay.height()));
        List<Point> toCorners = Arrays.asList(frame.topLeft, frame.topRight, frame.bottomLeft, frame.bottomRight);
        Mat from = Converters.vector_Point2f_to_Mat(fromCorners);
        Mat to = Converters.vector_Point2f_to_Mat(toCorners);
        Mat warp = Imgproc.getPerspectiveTransform(from, to);
        Mat warpedOverlay = new Mat();
        Imgproc.warpPerspective(overlay, warpedOverlay, warp, img.size());

        Imgproc.circle(img, frame.topLeft, 5, new Scalar(255, 0, 0), -1);
        Imgproc.circle(img, frame.topRight, 5, new Scalar(0, 255, 0), -1);
        Imgproc.circle(img, frame.bottomLeft, 5, new Scalar(0, 0, 255), -1);
        Imgproc.circle(img, frame.bottomRight, 5, new Scalar(255, 255, 0), -1);

        try {
            Core.add(img, warpedOverlay, img);
        } catch (CvException ex) {
            Log.e(TAG, "Couldn't add images");
            return false;
        }
        return true;
    }

    private GraffitiFrame lastFrame = new GraffitiFrame();
    private int lastFrameCounter = 0;
    final double angleThreshold = Math.PI / 6;
    final int cannyThreshold = 50;

    private Mat processMat(Mat m) {
        Mat gray = new Mat(m.rows(), m.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);
        Mat canny = new Mat();
        Imgproc.Canny(gray, canny, cannyThreshold, cannyThreshold * 3, 3, true);

        Mat houghLines = new Mat();
        Imgproc.HoughLines(canny, houghLines, 1, Math.PI / 180, 100);

        List<double[]> horizontalLines = new LinkedList<>();
        List<double[]> verticalLines = new LinkedList<>();
        for (int i = 0; i < houghLines.rows(); i++) {
            final double[] line = houghLines.get(i, 0);
            final double theta = line[1];

            // ~ horizontal
            if (Math.abs(theta - Math.PI / 2) < angleThreshold) {
                horizontalLines.add(line);
            }
            // ~ vertical
            if (Math.abs(theta) < angleThreshold || Math.abs(theta - Math.PI) < angleThreshold) {
                verticalLines.add(line);
            }
        }

//        for (double[] horizontal : horizontalLines) {
//            Imgproc.line(m, pointOnLineRaw(horizontal[0], horizontal[1], 1000), pointOnLineRaw(horizontal[0], horizontal[1], -1000), new Scalar(0, 0, 255), 1);
//        }
//        for (double[] vertical : verticalLines) {
//            Imgproc.line(m, pointOnLineRaw(vertical[0], vertical[1], 1000), pointOnLineRaw(vertical[0], vertical[1], -1000), new Scalar(0, 0, 255), 1);
//        }

        List<Corner> corners = findCorners(horizontalLines, verticalLines, m.cols(), m.rows());
        if (corners.size() != 0) {
            Corner topLeft = topLeftCorner(corners);
            Corner topRight = topRightCorner(corners, m.cols());

            double doorWidth = distance(topLeft.p, topRight.p);
            double graffitiHeight = doorWidth * (640 / 480);

            lastFrame.topLeft = topLeft.p;
            lastFrame.topRight = topRight.p;
            lastFrame.bottomLeft = pointOnLine(topLeft.vertical[1], topLeft.p, graffitiHeight);
            lastFrame.bottomRight = pointOnLine(topRight.vertical[1], topRight.p, graffitiHeight);
            lastFrame.valid = true;
            drawGraffiti(m, overlay, lastFrame);
            lastFrameCounter = 0;
        } else {
            Log.w(TAG, "Unable to find corners");
            //if it is within a small number of frames as a previous object, just assume it is in around the same location
            if (lastFrame.valid && lastFrameCounter < 5) {
                drawGraffiti(m, overlay, lastFrame);
                lastFrameCounter++;
            } else if (lastFrameCounter >= 5) {
                lastFrame.valid = false;
            }
        }
        gray.release();
        canny.release();
        houghLines.release();
        return m;
    }

}

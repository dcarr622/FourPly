package perihelion.io.fourply;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by vmagro on 1/23/16.
 */
public class ARGraffitiActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ARGraffiti";

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private CameraBridgeViewBase mOpenCvCameraView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ar_graffiti);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        Mat m = null;
        try {
            m = Utils.loadResource(this, R.drawable.stall);
        } catch (IOException e) {
            e.printStackTrace();
        }
        processMat(m);
//        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
//        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
//        mOpenCvCameraView.setCvCameraViewListener(this);
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
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        return inputFrame.rgba();
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
        final double x = start.x + distance * Math.cos(theta);
        final double y = start.y - distance * Math.sin(theta);
        return new Point(x, y);
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

    private static Corner topLeftCorner(List<Corner> corners) {
        Collections.sort(corners, new Comparator<Corner>() {
            @Override
            public int compare(Corner lhs, Corner rhs) {
                double ldist = Math.sqrt((lhs.p.x * lhs.p.x) + (lhs.p.y * lhs.p.y));
                double rdist = Math.sqrt((rhs.p.x * rhs.p.x) + (rhs.p.y * rhs.p.y));
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
                double ldist = Math.sqrt(((lhs.p.x - width) * (lhs.p.x - width)) + (lhs.p.y * lhs.p.y));
                double rdist = Math.sqrt(((rhs.p.x - width) * (rhs.p.x - width)) + (rhs.p.y * rhs.p.y));
                if (ldist < rdist)
                    return -1;
                else if (ldist > rdist)
                    return 1;
                return 0;
            }
        });
        return corners.get(0);
    }

    private void processMat(Mat m) {
        Mat gray = new Mat(m.rows(), m.cols(), CvType.CV_8UC1);
        Imgproc.cvtColor(m, gray, Imgproc.COLOR_RGB2GRAY);
        Mat blur = new Mat();
        Imgproc.bilateralFilter(gray, blur, 11, 17, 17);
        final int threshold = 50;
        Mat canny = new Mat();
        Imgproc.Canny(blur, canny, threshold, threshold * 3, 3, true);

        Mat houghLines = new Mat();
        Imgproc.HoughLines(canny, houghLines, 1, Math.PI / 180, 200);

        List<double[]> horizontalLines = new LinkedList<>();
        List<double[]> verticalLines = new LinkedList<>();
        final double angleThreshold = Math.PI / 6;
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

        for (double[] horizontal : horizontalLines) {
            Imgproc.line(m, pointOnLineRaw(horizontal[0], horizontal[1], 1000), pointOnLineRaw(horizontal[0], horizontal[1], -1000), new Scalar(0, 0, 255), 1);
        }
        for (double[] vertical : verticalLines) {
            Imgproc.line(m, pointOnLineRaw(vertical[0], vertical[1], 1000), pointOnLineRaw(vertical[0], vertical[1], -1000), new Scalar(0, 0, 255), 1);
        }

        List<Corner> corners = findCorners(horizontalLines, verticalLines, m.cols(), m.rows());
        if (corners.size() != 0) {
            Corner topLeft = topLeftCorner(corners);
            Corner topRight = topRightCorner(corners, m.cols());

            Imgproc.circle(m, topLeft.p, 5, new Scalar(255, 0, 0), -1);
            Imgproc.circle(m, topRight.p, 5, new Scalar(255, 0, 0), -1);
        } else {
            Log.w(TAG, "Unable to find corners");
        }

        ImageView imageView = (ImageView) findViewById(R.id.image);
        Bitmap bm = Bitmap.createBitmap(gray.cols(), gray.rows(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(m, bm);
        imageView.setImageBitmap(bm);
    }

}

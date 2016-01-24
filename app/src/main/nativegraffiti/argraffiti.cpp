//
// Created by Vinnie Magro on 1/23/16.
//
#include "argraffiti.h"
#include <jni.h>
#include "opencv2/opencv.hpp"
#include <list>

using namespace cv;

void processMat(Mat& m, Mat& overlay);
void drawGraffiti(Mat& m, Mat& overlay);

int main(int argc, char** argv )
{
    Mat overlay = imread("/Users/vmagro/Developer/android/FourPly/app/src/main/res/drawable-xxxhdpi/graffiti.png");
    Mat input = imread("/Users/vmagro/Developer/android/FourPly/app/src/main/res/drawable-xxxhdpi/stall.jpg");

    processMat(input, overlay);
    imshow("Result", input);
    waitKey(0);
}

typedef struct Line {
    double rho;
    double theta;

    Line() {
        rho = 0;
        theta = 0;
    }

    Line(Vec2f v) {
        this->rho = v[0];
        this->theta = v[1];
    }
} Line;


Point2f pointOnLineRaw(Line line, double distance) {
    Point pt1;
    double a = cos(line.theta), b = sin(line.theta);
    double x0 = a*line.rho, y0 = b*line.rho;
    pt1.x = cvRound(x0 + distance*(-b));
    pt1.y = cvRound(y0 + distance*(a));
    return pt1;
}

Point pointOnLine(Line line, Point start, double distance) {
    double theta = M_PI / 2 - line.theta;
    double x1 = start.x + distance * cos(theta);
    double x2 = start.x - distance * cos(theta);
    double y1 = start.y - distance * sin(theta);
    double y2 = start.y + distance * sin(theta);
    if (y2 > y1) {
        return Point(x2, y2);
    } else {
        return Point(x1, y1);
    }
}

Point segIntersect(Line l1, Line l2) {
    double ct1 = cos(l1.theta);
    double st1 = sin(l1.theta);
    double ct2 = cos(l2.theta);
    double st2 = sin(l2.theta);
    double d = ct1 * st2 - st1 * ct2;
    if (d != 0.0f) {
        double x = ((st2 * l1.rho - st1 * l2.rho) / d);
        double y = ((-ct2 * l1.rho + ct1 * l2.rho) / d);
        return Point(x, y);
    }
    return Point(-1, -1);
}

typedef struct Corner {
    Point p;
    Line vertical;

    Corner() {
    }

    Corner(Point pt, Line v) {
        this->p = pt;
        this->vertical = v;
    }
} Corner;

void findCorners(std::list<Corner>& corners, std::list<Line>& horizontalLines, std::list<Line>& verticalLines, int width, int height) {
    for (auto it = horizontalLines.begin(); it != horizontalLines.end(); ++it) {
        Line horizontal = *it;
        for (auto vit = verticalLines.begin(); vit != verticalLines.end(); ++vit) {
            Line vertical = *vit;
            Point intersection = segIntersect(horizontal, vertical);
            if (intersection.x >= 0 && intersection.x < width && intersection.y >= 0 && intersection.y < height) {
                corners.push_back(Corner(intersection, vertical));
            }
        }
    }
}

void topCorners(std::list<Corner>& corners, int width, Corner& topLeft, Corner& topRight) {
    double minTl = 99999999, minTr = 99999999;
    Point topRightOrigin = Point(width, 0);
    for (auto it = corners.begin(); it != corners.end(); ++it) {
        Corner c = *it;
        double tlDist = norm(c.p);
        double trDist = norm(c.p - topRightOrigin);
        if (tlDist < minTl) {
            minTl = tlDist;
            topLeft = c;
        }
        if (trDist < minTr) {
            minTr = trDist;
            topRight = c;
        }
    }
}

typedef struct GraffitiFrame {
    Point topLeft, topRight, bottomLeft, bottomRight;
    bool valid;
} GraffitiFrame;

const int cannyThreshold = 50;
const double angleThreshold = M_PI / 6;
Mat gray;
Mat canny;
Mat blurred;
GraffitiFrame lastFrame;
int lastFrameCounter = 0;
/**
 * Draw graffiti on top of the Mat m
 */
void processMat(Mat& m, Mat& overlay) {
    cvtColor(m, gray, COLOR_RGB2GRAY);
    bilateralFilter(gray, blurred, 11, 17, 17);
    Canny(blurred, canny, cannyThreshold, cannyThreshold * 3, 3, true);
    std::vector<Vec2f> lines;
    HoughLines(canny, lines, 1, M_PI / 180, 100);

    std::list<Line> horizontalLines;
    std::list<Line> verticalLines;
    for (size_t i = 0; i < lines.size(); i++) {
        Line line(lines[i]);
        // ~ horizontal
        if (fabs(line.theta - M_PI / 2) < angleThreshold)
            horizontalLines.push_back(line);
        // ~ vertical
        if (fabs(line.theta) < angleThreshold || fabs(line.theta - M_PI) < angleThreshold)
            verticalLines.push_back(line);
    }

    /* for (auto it = verticalLines.begin(); it != verticalLines.end(); ++it) { */
    /*     Line line = *it; */
    /*     cv::line(m, pointOnLineRaw(line, 1000), pointOnLineRaw(line, -1000), Scalar(0,0,255), 3, CV_AA); */
    /* } */

    std::list<Corner> corners;
    findCorners(corners, horizontalLines, verticalLines, m.cols, m.rows);

    if (corners.size() != 0) {
        Corner topLeft;
        Corner topRight;
        topCorners(corners, m.cols, topLeft, topRight);
        double doorWidth = norm(topLeft.p - topRight.p);
        double graffitiHeight = doorWidth * (4/3);

        lastFrame.topLeft = topLeft.p;
        lastFrame.topRight = topRight.p;
        lastFrame.bottomLeft = pointOnLine(topLeft.vertical, topLeft.p, graffitiHeight);
        lastFrame.bottomRight = pointOnLine(topRight.vertical, topRight.p, graffitiHeight);
        lastFrame.valid = true;

        drawGraffiti(m, overlay);
    } else {
        //if it is within a small number of frames as a previous object, just assume it is in around the same location
        if (lastFrame.valid && lastFrameCounter < 5) {
            drawGraffiti(m, overlay);
            lastFrameCounter++;
        } else if (lastFrameCounter >= 5) {
            lastFrame.valid = false;
        }
    }
}

Mat warped;
void drawGraffiti(Mat& m, Mat& overlay) {
    Point2f src_vertices[4];
    src_vertices[0] = Point(0, 0);
    src_vertices[1] = Point(overlay.cols, 0);
    src_vertices[2] = Point(0, overlay.rows);
    src_vertices[3] = Point(overlay.cols, overlay.rows);

    Point2f dst_vertices[4];
    dst_vertices[0] = lastFrame.topLeft;
    dst_vertices[1] = lastFrame.topRight;
    dst_vertices[2] = lastFrame.bottomLeft;
    dst_vertices[3] = lastFrame.bottomRight;

    Mat transform = getPerspectiveTransform(src_vertices, dst_vertices);
    warpPerspective(overlay, warped, transform, m.size());

    circle(m, lastFrame.topLeft, 5, Scalar(255, 0, 0), -1);
    circle(m, lastFrame.topRight, 5, Scalar(0, 255, 0), -1);
    circle(m, lastFrame.bottomLeft, 5, Scalar(0, 0, 255), -1);
    circle(m, lastFrame.bottomRight, 5, Scalar(0, 255, 255), -1);

    cv::add(m, warped, m);
}


extern "C" {

JNIEXPORT void JNICALL Java_perihelion_io_fourply_ARGraffitiActivity_processMat(JNIEnv*, jobject, jlong addrM, jlong addrOverlay);

JNIEXPORT void JNICALL Java_perihelion_io_fourply_ARGraffitiActivity_processMat(JNIEnv*, jobject, jlong addrM, jlong addrOverlay) {
    Mat& m = *(Mat*)addrM;
    Mat& overlay = *(Mat*)addrOverlay;

    int conv;
    jint retVal;

    processMat(m, overlay);
}

}
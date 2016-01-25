import cv2
import numpy as np


def perp(a):
    b = np.empty_like(a)
    b[0] = -a[1]
    b[1] = a[0]
    return b


# line segment a given by endpoints a1, a2
# line segment b given by endpoints b1, b2
# return
def seg_intersect(a1, a2, b1, b2):
    da = a2 - a1
    db = b2 - b1
    dp = a1 - b1
    dap = perp(da)
    denom = np.dot(dap, db)
    num = np.dot(dap, dp)
    if denom == 0:
        raise ValueError("Divide by 0")
    return (num / denom.astype(float)) * db + b1


def point_on_line_raw(rho, theta, distance):
    a = np.cos(theta)
    b = np.sin(theta)
    x0 = a * rho
    y0 = b * rho
    x1 = int(x0 + distance * (-b))
    y1 = int(y0 + distance * a)
    return x1, y1


def point_on_line(theta, start, distance):
    theta = np.pi / 2 - theta
    x = start[0] + distance * np.cos(theta)
    y = start[1] - distance * np.sin(theta)
    return int(x), int(y)


overlay = cv2.imread('/Users/vmagro/Desktop/fourply/graffiti.png', -1)
# img = cv2.imread('/Users/vmagro/Desktop/fourply/stall.jpg', cv2.IMREAD_COLOR)
# img = cv2.imread('/Users/vmagro/Desktop/fourply/IMG_0313.jpg', cv2.IMREAD_COLOR)
img = cv2.imread('/Users/vmagro/Desktop/fourply/whiteboard.jpg', cv2.IMREAD_COLOR)

cv2.imshow('raw', img.copy())

gray = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
gray = cv2.bilateralFilter(gray, 11, 17, 17)

threshold = 50
canny = cv2.Canny(gray, threshold, threshold * 3, apertureSize=3)

cv2.imshow('canny', canny)

_, contours, hierarchy = cv2.findContours(canny.copy(), cv2.RETR_TREE, cv2.CHAIN_APPROX_SIMPLE)

hough_lines = cv2.HoughLines(canny, 1, np.pi / 180, 75)
if hough_lines is None:
    print('Failed to get lines')

angle_threshold = np.pi / 6
horizontal = []
vertical = []
for line in hough_lines:
    rho, theta = line[0]
    x1, y1 = point_on_line_raw(rho, theta, 1000)
    x2, y2 = point_on_line_raw(rho, theta, -1000)

    # ~ horizontal line
    if np.abs(theta - (np.pi / 2)) < angle_threshold:
        horizontal.append((np.array([x1, y1]), np.array([x2, y2]), rho, theta))
        # cv2.line(img, (x1, y1), (x2, y2), (0, 0, 255), 2)
    # ~ vertical line
    if np.abs(theta) < angle_threshold or np.abs(theta - np.pi) < angle_threshold:
        vertical.append((np.array([x1, y1]), np.array([x2, y2]), rho, theta))
        # cv2.line(img, (x1, y1), (x2, y2), (0, 0, 255), 2)

corners = []
for horiz in horizontal:
    for vert in vertical:
        try:
            intersection = seg_intersect(horiz[0], horiz[1], vert[0], vert[1])
            if 0 <= intersection[0] < img.shape[1] and 0 <= intersection[1] < img.shape[0]:
                corners.append((intersection, horiz, vert))
        except ValueError:
            pass

if len(corners) == 0:
    print("No corners found")

else:
    top_left = sorted(corners, key=(lambda c: np.linalg.norm(c[0] - np.array([0, 0]))))[0]
    top_right = sorted(corners, key=(lambda c: np.linalg.norm(c[0] - np.array([img.shape[0], img.shape[1]]))))[0]

    tlc = top_left[0]
    tlc = (int(tlc[0]), int(tlc[1]))
    trc = top_right[0]
    trc = (int(trc[0]), int(trc[1]))

    cv2.circle(img, tlc, 5, (255, 0, 0), -1)
    cv2.circle(img, trc, 5, (255, 0, 0), -1)

    door_width = np.linalg.norm(top_right[0] - top_left[0])
    graffiti_height = door_width * 640 / 480
    # top_left[2] is the vertical line that makes up the intersection
    bottom_left = point_on_line(top_left[2][3], top_left[0], graffiti_height)
    bottom_right = point_on_line(top_right[2][3], top_right[0], graffiti_height)

    cv2.circle(img, bottom_left, 5, (255, 0, 0), -1)
    cv2.circle(img, bottom_right, 5, (255, 0, 0), -1)
    cv2.line(img, bottom_left, bottom_right, (255, 0, 0), 2)
    cv2.line(img, tlc, bottom_left, (255, 0, 0), 2)
    cv2.line(img, trc, bottom_right, (255, 0, 0), 2)
    cv2.line(img, tlc, trc, (255, 0, 0), 2)

    from_corners = np.array(
            [(0, 0), (overlay.shape[1], 0), (0, overlay.shape[0]), (overlay.shape[1], overlay.shape[0])],
            np.float32)
    dest_corners = np.array([tlc, trc, bottom_left, bottom_right], np.float32)
    warp = cv2.getPerspectiveTransform(from_corners, dest_corners)
    fixed_perspective = cv2.warpPerspective(overlay, warp, (img.shape[1], img.shape[0]))

    cv2.imshow('graffiti', fixed_perspective)

    fixed_perspective = cv2.cvtColor(fixed_perspective, cv2.COLOR_BGRA2BGR)

    img = cv2.add(img, fixed_perspective)
    cv2.imshow('result', img)

cv2.waitKey(0)

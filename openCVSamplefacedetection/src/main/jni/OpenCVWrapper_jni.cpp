
#include <OpenCVWrapper_jni.h>
#include <opencv2/core.hpp>
#include <opencv2/objdetect.hpp>
#include <android/bitmap.h>
#include <android/native_window_jni.h>
#include "opencv2/imgproc/imgproc.hpp"
#include "opencv2/highgui/highgui.hpp"

#include <string>
#include <vector>

#include <android/log.h>

#ifndef uint8_t
#define uint8_t unsigned char
#endif

#ifndef int32_t
#define int32_t int
#endif

#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "OpenCVNative", __VA_ARGS__)
#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, "OpenCVNative", __VA_ARGS__)

using namespace cv;

int LEFT_UP_X = 75;
int LEFT_UP_Y = 90;
int LEFT_DOWN_X = 0;
int LEFT_DOWN_Y = 180;
int RIGHT_UP_X = 280;
int RIGHT_UP_Y = 90;
int RIGHT_DOWN_X = 360;
int RIGHT_DOWN_Y = 180;

double CHECK_POS_SLOPES = 0.50;
double CHECK_NEG_SLOPES = -0.50;

int GAUSIAN_KERNEL_VALUE = 5;
double AUTO_CANNY_VALUE = 0.33;

double RHO = 1.0;
double THETA = (1 * CV_PI / 180);
int THERESHOLDVALUE = 20;
int MIN_LINE_LEN = 5;
int MAX_LINE_GAP = 5;

double textSize = 0.7;
double scaleMin = 1;

///警示用的顏色通常用在文字
cv::Scalar dangerColor = cv::Scalar(255,0,0,255);
cv::Scalar nomarlColor = cv::Scalar(0,0,0,255);
cv::Scalar rightSlopeTextColor = cv::Scalar(0,0,0,255);
cv::Scalar leftSlopeTextColor = cv::Scalar(0,0,0,255);

cv::Point rightSlopeStringPoint = cv::Point(610, 273);
cv::Point rightIntercpetValueStringPoint = cv::Point(610, 313);
cv::Point leftSlopeStringPoint = cv::Point(90, 273);
cv::Point leftIntercpetValueStringPoint = cv::Point(90, 313);
cv::Point channel1ValueStringPoint = cv::Point(90, 100);
cv::Point channel2ValueStringPoint = cv::Point(695, 107);
cv::Point computeChannel1ValueStringPoint = cv::Point(200, 100);
cv::Point computeChannel2ValueStringPoint = cv::Point(805, 107);

//-------------Line1Point2

cv::Point resultLeftBottomPointStringPoint = cv::Point(90, 402);

//-------------Line1Point1

cv::Point resultLeftTopPointStringPoint = cv::Point(90, 368);
cv::Point resultRightTopPointStringPoint = cv::Point(610, 368);
cv::Point resultRightBottomPointStringPoint = cv::Point(610, 402);

//FPS
cv::Point fpsStringPoint = cv::Point(405, 50);

cv::Point connectedCirclePoint = cv::Point(405, 100);

//

double negSlope = 0;
double negIntercept = 0;
double posSlope = 0;
double posIntercept = 0;

int currentLineCount = 0;
int* pointArray = new int[8];

//計算所需時間

long long BitmapToMatTime  = 0;
long long YellowToWhiteTime = 0;
long long grayScaleTime = 0;
long long gaussianBlurTime = 0;
long long medianTime = 0;
long long autoCannyTime = 0;
long long regionOfInterestTime = 0;
long long HoughLinesPTime = 0;
long long extrapolateTime = 0;
long long houghLinesTime = 0;
long long weightedImgTime = 0;
long long RGB2RGBAImgTime = 0;
long long createBitmapTime = 0;

void setLabel(cv::Mat& im, const std::string label, const cv::Point &point, cv::Scalar color, double frontScale)
{
    int fontface = cv::FONT_HERSHEY_SIMPLEX;
    int thickness = int(2.0 * scaleMin);

    cv::putText(im, label, point, fontface, frontScale, color, thickness, 8);
}

void scalePosition(cv::Point &point, double valueX, double valueY)
{
    point.x = int(double(point.x) * valueX);
    point.y = int(double(point.y) * valueY);
}

//計算所需時間

long long currentTimeInMilliseconds()
{

    std::vector<std::vector<cv::Point > > aa;
    struct timeval tv;
    gettimeofday(&tv, NULL);
    return ((tv.tv_sec * 1000) + (tv.tv_usec / 1000));
}

cv::Mat nBitmapToMat2 (JNIEnv * env, jclass, jobject bitmap, jboolean needUnPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;
    cv::Mat dst;

    try {
//        LOGD("nBitmapToMat");
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        dst.create(info.height, info.width, CV_8UC4);
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
//            LOGD("nBitmapToMat: RGBA_8888 -> CV_8UC4");
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(needUnPremultiplyAlpha) cvtColor(tmp, dst, CV_mRGBA2RGBA);
            else tmp.copyTo(dst);
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
//            LOGD("nBitmapToMat: RGB_565 -> CV_8UC4");
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            cv::cvtColor(tmp, dst, CV_BGR5652RGBA);
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return dst;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
//        LOGE("nBitmapToMat catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return dst;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
//        LOGE("nBitmapToMat catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nBitmapToMat}");
        return dst;
    }
}

void nMatToBitmap2
        (JNIEnv * env, jclass, cv::Mat src, jobject bitmap, jboolean needPremultiplyAlpha)
{
    AndroidBitmapInfo  info;
    void*              pixels = 0;

    try {
//        LOGD("nMatToBitmap");
        CV_Assert( AndroidBitmap_getInfo(env, bitmap, &info) >= 0 );
        CV_Assert( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 ||
                   info.format == ANDROID_BITMAP_FORMAT_RGB_565 );
        CV_Assert( src.dims == 2 && info.height == (uint32_t)src.rows && info.width == (uint32_t)src.cols );
        CV_Assert( src.type() == CV_8UC1 || src.type() == CV_8UC3 || src.type() == CV_8UC4 );
        CV_Assert( AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0 );
        CV_Assert( pixels );
        if( info.format == ANDROID_BITMAP_FORMAT_RGBA_8888 )
        {
            cv::Mat tmp(info.height, info.width, CV_8UC4, pixels);
            if(src.type() == CV_8UC1)
            {
//                LOGD("nMatToBitmap: CV_8UC1 -> RGBA_8888");
                cv::cvtColor(src, tmp, CV_GRAY2RGBA);
            } else if(src.type() == CV_8UC3){
//                LOGD("nMatToBitmap: CV_8UC3 -> RGBA_8888");
                cv::cvtColor(src, tmp, CV_RGB2RGBA);
            } else if(src.type() == CV_8UC4){
//                LOGD("nMatToBitmap: CV_8UC4 -> RGBA_8888");
                if(needPremultiplyAlpha) cvtColor(src, tmp, CV_RGBA2mRGBA);
                else src.copyTo(tmp);
            }
        } else {
            // info.format == ANDROID_BITMAP_FORMAT_RGB_565
            cv::Mat tmp(info.height, info.width, CV_8UC2, pixels);
            if(src.type() == CV_8UC1)
            {
//                LOGD("nMatToBitmap: CV_8UC1 -> RGB_565");
                cv::cvtColor(src, tmp, CV_GRAY2BGR565);
            } else if(src.type() == CV_8UC3){
//                LOGD("nMatToBitmap: CV_8UC3 -> RGB_565");
                cvtColor(src, tmp, CV_RGB2BGR565);
            } else if(src.type() == CV_8UC4){
//                LOGD("nMatToBitmap: CV_8UC4 -> RGB_565");
                cv::cvtColor(src, tmp, CV_RGBA2BGR565);
            }
        }
        AndroidBitmap_unlockPixels(env, bitmap);
        return;
    } catch(const cv::Exception& e) {
        AndroidBitmap_unlockPixels(env, bitmap);
//        LOGE("nMatToBitmap catched cv::Exception: %s", e.what());
        jclass je = env->FindClass("org/opencv/core/CvException");
        if(!je) je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, e.what());
        return;
    } catch (...) {
        AndroidBitmap_unlockPixels(env, bitmap);
//        LOGE("nMatToBitmap catched unknown exception (...)");
        jclass je = env->FindClass("java/lang/Exception");
        env->ThrowNew(je, "Unknown exception in JNI code {nMatToBitmap}");
        return;
    }
}

jobject createBitmap(JNIEnv * env, jclass, int imageWidth, int imageHeight) {
    jclass bitmapCls =(jclass)env->FindClass("android/graphics/Bitmap");
    jmethodID createBitmapFunction = env->GetStaticMethodID(bitmapCls, "createBitmap", "(IILandroid/graphics/Bitmap$Config;)Landroid/graphics/Bitmap;");
    jstring configName = env->NewStringUTF("ARGB_8888");
    jclass bitmapConfigClass = env->FindClass("android/graphics/Bitmap$Config");
    jmethodID valueOfBitmapConfigFunction = env->GetStaticMethodID(bitmapConfigClass, "valueOf", "(Ljava/lang/String;)Landroid/graphics/Bitmap$Config;");
    jobject bitmapConfig = env->CallStaticObjectMethod(bitmapConfigClass, valueOfBitmapConfigFunction, configName);
    return env->CallStaticObjectMethod(bitmapCls, createBitmapFunction, imageWidth, imageHeight, bitmapConfig);
}

cv::Mat yellowToWhiteImage(cv::Mat rgbaMat)
{
    //要注意 addWeighted 兩個cv::Mat的格式要一樣 ，不要然會出錯
    //例如：  cv::addWeighted(rgbMat, 1, grayMat, 1, 0, masked_replace_white);
    // grayMat 的格式為RGB rgbMat的就必需為 RGB

    cv::Mat rgbMat;
    cv::cvtColor(rgbaMat, rgbMat, CV_RGBA2RGB);

    return rgbMat;
}

cv::Mat grayscale(cv::Mat imageMat)
{
//    Applies the Grayscale transform
//    This will return an image with only one color channel
//    but NOTE: to see the returned image as grayscale
//    you should call plt.imshow(gray, cmap='gray')

    //cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    cv::Mat bgrGrayMat;
    cv::cvtColor(imageMat, bgrGrayMat, CV_BGR2GRAY);

    return bgrGrayMat;
}

cv::Mat gaussian_blur(cv::Mat img, int kernel_size)
{
    //Applies a Gaussian Noise kernel

    //cv2.GaussianBlur(img, (kernel_size, kernel_size), 0)
    cv::Mat gaussianBlurMat;
    cv::Size size = cv::Size(kernel_size, kernel_size);
    cv::GaussianBlur(img, gaussianBlurMat, size, 0);

    return gaussianBlurMat;
}

double medianTest(cv::Mat Input)
{
    Input = Input.reshape(0,1); // spread Input Mat to single row
    std::vector<double> vecFromMat;
    Input.copyTo(vecFromMat); // Copy Input Mat to vector vecFromMat
    std::nth_element(vecFromMat.begin(), vecFromMat.begin() + vecFromMat.size() / 2, vecFromMat.end());
    return vecFromMat[vecFromMat.size() / 2];
}

cv::Mat auto_canny(cv::Mat gaussianBlurMat, double sigma = 0.33)
{
    // compute the median of the single channel pixel intensities
    //    v = np.median(image)
    long long startCountMedianTime = currentTimeInMilliseconds();
    double medianValue = medianTest(gaussianBlurMat);
    long long endCountMedianTime = currentTimeInMilliseconds();

    __android_log_print(ANDROID_LOG_VERBOSE, "OpenCVNative", "median: %lld", (endCountMedianTime- startCountMedianTime));


    // apply automatic Canny edge detection using the computed median
    //    lower = int(max(0, (1.0 - sigma) * v))
    //    upper = int(min(255, (1.0 + sigma) * v))
    int lower = int(MAX(0, (1.0 - sigma) * medianValue));
    int upper = int(MIN(255, (1.0 + sigma) * medianValue));

    //    auto = canny(image, lower, upper)
    cv::Mat cimage;
    cv::Canny(gaussianBlurMat, cimage, lower, upper);

    return cimage;
}

cv::Mat region_of_interest(cv::Mat cimage, std::vector<std::vector<cv::Point > > vertices)
{
    //    Applies an image mask.
    //
    //    Only keeps the region of the image defined by the polygon
    //    formed from `vertices`. The rest of the image is set to black.
    //# defining a blank mask to start with
    //    mask = np.zeros_like(img)
    cv::Mat mask = cimage.zeros(cimage.rows, cimage.cols, cimage.type());

    //# defining a 3 channel or 1 channel color to fill the mask with depending on the input image
    //    if len(img.shape) > 2:
    //        channel_count = img.shape[2]  # i.e. 3 or 4 depending on your image
    //        ignore_mask_color = (255,) * channel_count
    //        else:
    //            ignore_mask_color = 255
    int cimageChannel = cimage.channels();

    cv::Scalar ignore_mask_color;

    if(cimageChannel == 1)
    {
        ignore_mask_color = cv::Scalar(255);
    }
    else if (cimageChannel == 2)
    {
        ignore_mask_color = cv::Scalar(255, 255);
    }
    else if (cimageChannel == 3)
    {
        ignore_mask_color = cv::Scalar(255, 255, 255);
    }
    else if (cimageChannel == 4)
    {
        ignore_mask_color = cv::Scalar(255, 255, 255, 255);
    }

    //filling pixels inside the polygon defined by "vertices" with the fill color
    //            cv2.fillPoly(mask, vertices, ignore_mask_color)
    cv::fillPoly(mask, vertices, ignore_mask_color);

    //returning the image only where mask pixels are nonzero
    //            masked_image = cv2.bitwise_and(img, mask)
    cv::Mat outMat;
    cv::bitwise_and(cimage, mask, outMat);

    return outMat;
}

void drawLinesP(cv::Mat &input, const std::vector<cv::Vec4i> &lines){
    for(int i=0; i<lines.size(); i++){
        cv::Vec4i l = lines[i];
        cv::line(input, cv::Point(l[0], l[1]), cv::Point(l[2], l[3]), cv::Scalar(255,0,0), 5);
    }
}

std::vector<std::vector<cv::Point > > getVertices()
{
    std::vector<cv::Point> pointArray = std::vector<cv::Point>();
    pointArray.push_back(cvPoint( LEFT_DOWN_X , LEFT_DOWN_Y )); //左下
    pointArray.push_back(cvPoint( RIGHT_DOWN_X, RIGHT_DOWN_Y)); //右下
    pointArray.push_back(cvPoint( RIGHT_UP_X, RIGHT_UP_Y)); //右上
    pointArray.push_back(cvPoint( LEFT_UP_X, LEFT_UP_Y)); //左上

    std::vector<std::vector<cv::Point > > vertices = std::vector<std::vector<cv::Point > >();
    vertices.push_back(pointArray);

    return vertices;
}

std::vector<double> apply_along_axis_similar(std::vector<cv::Vec4i> lines)
{
    std::vector<double> slopes;

    for(int i = 0 ; i < lines.size() ; i++)
    {
        cv::Vec4i point = lines[i];

        double x1 = point[0];
        double y1 = point[1];
        double x2 = point[2];
        double y2 = point[3];

        double value = (y2 - y1) / (x2 - x1);

        slopes.push_back(value);
    }

    return slopes;
}

void lines_linreg_by_OpenCV(std::vector<cv::Vec4i> lineArray, std::vector<int>& xEmptyContainer , std::vector<int>& yEmptyContainer , double& mValueParam , double& cValueParam)
{
    //Calculates the slope and intecept for line segments

    // python sourcde code for reference:
    //    def lines_linreg(lines_array):
    //    '''
    //    Calculates the slope and intecept for line segments
    //        '''
    //        try:
    //### Select the 0th and 2nd index which will provide the xval and reshape to extract x values
    //        x = np.reshape(lines_array[:, [0, 2]], (1, len(lines_array) * 2))[0]
    //### Select the 1st and 3rd index which will provide the yval and reshape to extract y values
    //        ### np.reshape:
    //        y = np.reshape(lines_array[:, [1, 3]], (1, len(lines_array) * 2))[0]
    //
    //        A = np.vstack([x, np.ones(len(x))]).T
    //        m, c = np.linalg.lstsq(A, y)[0]
    //        x = np.array(x)
    //        y = np.array(x * m + c).astype('int')
    //        except:
    //        print(x,y)
    //        return x, y, m, c

    //lines_array[:, [0, 2]], lines_array 在原始碼中進來的是一個2維陣列，第一個 "：" 意思是走遍整個第一維。[0, 2]是取值，取出第二維的陣列資料中Index為0跟2的資料。

    //np.ones(len(x)) 取得一個陣列，長度為 len(x)，每一個index的值都是1
    //len(x) 取得x陣列的長度
    //np.ones 可參考該網站 http://www.cnblogs.com/fortran/archive/2010/09/01/1814773.html

    //vstack合并垂直方向矩阵
    //Python範例
    // >>> a = np.array([1, 2, 3])
    // >>> b = np.array([2, 3, 4])
    // >>> np.vstack((a,b))
    // array([[1, 2, 3],
    //        [2, 3, 4]])
    //
    // >>>
    //
    // >>> a = np.array([[1], [2], [3]])
    // >>> b = np.array([[2], [3], [4]])
    // >>> np.vstack((a,b))
    // array([[1],
    //        [2],
    //        [3],
    //        [2],
    //        [3],
    //        [4]])
    //
    //後面的.T意思是轉換成矩陣
    //a = np.array([1, 2, 3])
    //b = np.array([2, 3, 4])
    //c = np.vstack((a,b));
    //d = c.T
    //
    //print("c value");
    //print(c);
    //print("d value");
    //print(d);
    //
    //c value
    //[[1 2 3]
    // [2 3 4]]
    //d value
    //[[1 2]
    // [2 3]
    // [3 4]]

    for(int i = 0 ; i < lineArray.size() ; i++)
    {
        cv::Vec4i line = lineArray[i];

        //Select the 0th and 2nd index which will provide the xval and reshape to extract x values
        xEmptyContainer.push_back(line[0]);
        xEmptyContainer.push_back(line[2]);

        //Select the 1st and 3rd index which will provide the yval and reshape to extract 7 values
        yEmptyContainer.push_back(line[1]);
        yEmptyContainer.push_back(line[3]);
    }


    std::vector<cv::Point> pointArray;

    for(int i = 0 ; i < lineArray.size() ; i++)
    {
        cv::Vec4i line = lineArray[i];

        pointArray.push_back(cv::Point2d(line[0], line[1]));
        pointArray.push_back(cv::Point2d(line[2], line[3]));
    }

    cv::Mat pointMatrix = cv::Mat(pointArray, true);

    cv::Mat mcVector;
    std::vector<double> value;
    cv::fitLine(pointMatrix, mcVector, CV_DIST_L2, 0, 0.01, 0.01);
    mcVector.copyTo(value);

    //m = line[1]/line[0]
    //c = line[3] - (m * line[2])
    mValueParam = value[1]/value[0];
    cValueParam = value[3] - (mValueParam * value[2]);

    yEmptyContainer.clear();

    for(int i = 0 ; i < xEmptyContainer.size() ; i++)
    {
        //y = mx + c
        int y = int(double(xEmptyContainer[i]) * mValueParam + cValueParam);
        yEmptyContainer.push_back(y);
    }
}

std::vector<bool> checkSlopesVectorValue(std::vector<double> valueArray, int type, double checkPoint)
{
    std::vector<bool> checkedArray;

    for(int i = 0 ; i < valueArray.size() ; i++)
    {
        double arrayValue = valueArray[i];

        bool result = false;

        if(type == 0)// >
        {
            result = arrayValue > checkPoint;
        }
        else if(type == 1)// <
        {
            result = arrayValue < checkPoint;
        }
        else // ==
        {
            result = arrayValue == checkPoint;
        }

        checkedArray.push_back(result);
    }

    return checkedArray;
}

std::vector<cv::Vec4i> fillterVectorValue(std::vector<cv::Vec4i> valueArray, std::vector<bool> fillter)
{
    std::vector<cv::Vec4i> checkedArray;

    for(int i = 0 ; i < valueArray.size() ; i++)
    {
        if(fillter[i])
        {
            checkedArray.push_back(valueArray[i]);
        }
    }

    return checkedArray;
}

void extrapolate(cv::Mat &img, std::vector<cv::Vec4i> lines)
{
    //python source code
//    '''
//    This function extrapolates the line segment consider the slope and intercept
//    for the left and right line segments.
//        '''
//        global pos_global_slopes
//        global pos_global_intercepts
//        global neg_global_slopes
//        global neg_global_intercepts
//        global top_y
//
//        max_height = img.shape[0]
//        max_width = img.shape[1]
//        if lines == None:
//            print("******************* No lines detected")
//            return
//            slopes = np.apply_along_axis(lambda row: (row[3] - row[1]) / (row[2] - row[0]), 2, lines)
//
//            pos_slopes = slopes > 0.50
//            pos_lines = lines[pos_slopes]
//
//            neg_slopes = slopes < -0.50
//            neg_lines = lines[neg_slopes]
//
//            if len(pos_lines) == 0 or len(neg_lines) == 0:
//                return;
//
//    pos_x, pos_y, pos_m, pos_c = lines_linreg(pos_lines)
//    pos_global_slopes = np.append(pos_global_slopes, [pos_m])
//    pos_global_intercepts = np.append(pos_global_intercepts, [pos_c])
//
//# print("Before adjusting slopes : %f intercept %f" % (pos_m, pos_c))
//    pos_m = pos_global_slopes[-20:].mean()
//    pos_c = pos_global_intercepts[-20:].mean()
//
//# print("------------")
//
//    neg_x, neg_y, neg_m, neg_c = lines_linreg(neg_lines)
//    neg_global_slopes = np.append(neg_global_slopes, [neg_m])
//    neg_global_intercepts = np.append(neg_global_intercepts, [neg_c])
//    neg_m = neg_global_slopes[-20:].mean()
//    neg_c = neg_global_intercepts[-20:].mean()
//
//    bottom_left_y = img.shape[0]
//    bottom_left_x = int((bottom_left_y - neg_c) / neg_m)
//
//    min_top_y = np.min([neg_y.min(), pos_y.min()])
//    top_y = np.append(top_y, [min_top_y])
//    min_top_y = int(top_y[-20:].mean())
//    top_left_y = min_top_y
//    top_left_x = int((top_left_y - neg_c) / neg_m)
//
//    top_right_y = min_top_y
//    top_right_x = int((top_right_y - pos_c) / pos_m)
//
//    bottom_right_y = img.shape[0]
//    bottom_right_x = int((bottom_right_y - pos_c) / pos_m)
//
//# Average
//    cv2.line(img, (bottom_left_x, bottom_left_y), (top_left_x, top_left_y), [0, 255, 0], 10)
//    cv2.line(img, (top_right_x, top_right_y), (bottom_right_x, bottom_right_y), [0, 255, 0], 10)


    //This function extrapolates the line segment consider the slope and intercept
    //for the left and right line segments.
    //global pos_global_slopes
    //global pos_global_intercepts
    //global neg_global_slopes
    //global neg_global_intercepts
    //global top_y
    //在python要存取全域變數，要使用 global <variable name> 指令才行, 但這裡是ObjectC++

    //max_height = img.shape[0]
    int max_height = img.rows;

    //max_width = img.shape[1]
    int max_width = img.cols;
    if (lines.size() == 0) // if lines == None:
    {
        printf("******************* No lines detected");

        negSlope = 0;
        negIntercept = 0;
        posSlope = 0;
        posIntercept = 0;

        return;
    }

    //slopes = np.apply_along_axis(lambda row: (row[3] - row[1]) / (row[2] - row[0]), 2, lines)
    std::vector<double> slopes = apply_along_axis_similar(lines);

//    pos_slopes = slopes > 0.50
    std::vector<bool> pos_slopes = checkSlopesVectorValue(slopes, 0, CHECK_POS_SLOPES);
//    pos_lines = lines[pos_slopes]
    std::vector<cv::Vec4i> pos_lines = fillterVectorValue(lines, pos_slopes);

//    neg_slopes = slopes < -0.50
    std::vector<bool> neg_slopes = checkSlopesVectorValue(slopes, 1, CHECK_NEG_SLOPES);
//    neg_lines = lines[neg_slopes]
    std::vector<cv::Vec4i> neg_lines = fillterVectorValue(lines, neg_slopes);

//    if len(pos_lines) == 0 or len(neg_lines) == 0: return;
//    if(pos_lines.size() == 0 || neg_lines.size() == 0)
//    {
//        point1 = CGPointMake(0, 0);
//        point2 = CGPointMake(0, 0);
//        point3 = CGPointMake(0, 0);
//        point4 = CGPointMake(0, 0);
//
//        negSlope = 0;
//        negIntercept = 0;
//        posSlope = 0;
//        posIntercept = 0;
//
//        return;
//    }

    //pos_x, pos_y, pos_m, pos_c = lines_linreg(pos_lines)
    std::vector<int> pos_x;
    std::vector<int> pos_y;
    double pos_m = 0;
    double pos_c = 0;
    lines_linreg_by_OpenCV(pos_lines, pos_x, pos_y, pos_m, pos_c);

//    //pos_global_slopes = np.append(pos_global_slopes, [pos_m])
//    pos_global_slopes.push_back(pos_m);
//    //pos_global_intercepts = np.append(pos_global_intercepts, [pos_c])
//    pos_global_intercepts.push_back(pos_c);
//
//    //# print("Before adjusting slopes : %f intercept %f" % (pos_m, pos_c))
//    //pos_m = pos_global_slopes[-20:].mean() //意思是取該陣列的倒數20筆資料為一個陣列(google關鍵字ndarray 切片)，並取平均值。
//    pos_m = meanDouble(pos_global_slopes);
//    //pos_c = pos_global_intercepts[-20:].mean() //意思是取該陣列的倒數20筆資料為一個陣列，並取平均值(google關鍵字ndarray 切片)，並取平均值。
//    pos_c = meanDouble(pos_global_intercepts);

    posSlope = pos_m;
    posIntercept = pos_c;

    //neg_x, neg_y, neg_m, neg_c = lines_linreg(neg_lines)
    std::vector<int> neg_x;
    std::vector<int> neg_y;
    double neg_m = 0;
    double neg_c = 0;
    lines_linreg_by_OpenCV(neg_lines, neg_x, neg_y, neg_m, neg_c);

//    //neg_global_slopes = np.append(neg_global_slopes, [neg_m])
//    neg_global_slopes.push_back(neg_m);
//    //neg_global_intercepts = np.append(neg_global_intercepts, [neg_c])
//    neg_global_intercepts.push_back(neg_c);
//
//    //neg_m = neg_global_slopes[-20:].mean()
//    neg_m = meanDouble(neg_global_slopes);
//    //neg_c = neg_global_intercepts[-20:].mean()
//    neg_c = meanDouble(neg_global_intercepts);

    negSlope = neg_m;
    negIntercept = neg_c;

    //bottom_left_y = img.shape[0]
    int bottom_left_y = LEFT_DOWN_Y;

    //bottom_left_x = int((bottom_left_y - neg_c) / neg_m)
    int bottom_left_x = int((bottom_left_y - neg_c) / neg_m);

    //min_top_y = np.min([neg_y.min(), pos_y.min()])

    int min_top_y = 0;

    if(pos_lines.size() > 0 && neg_lines.size() > 0)
    {
        int neg_y_MinValue = *min_element(neg_y.begin(), neg_y.end());
        int pos_y_MinValue = *min_element(pos_y.begin(), pos_y.end());
        min_top_y = MIN(neg_y_MinValue, pos_y_MinValue);
    }
    else if(pos_lines.size() > 0)
    {
        min_top_y = *min_element(pos_y.begin(), pos_y.end());
    }
    else if(neg_lines.size() > 0)
    {
        min_top_y = *min_element(neg_y.begin(), neg_y.end());
    }

    //top_y = np.append(top_y, [min_top_y])
//    top_y.push_back(min_top_y);

    //min_top_y = int(top_y[-20:].mean())
//    min_top_y = int(meanDouble(top_y));

    //top_left_y = min_top_y
    int top_left_y = LEFT_UP_Y;

    //top_left_x = int((top_left_y - neg_c) / neg_m)
    int top_left_x = int((top_left_y - neg_c) / neg_m);

    //top_right_y = min_top_y
    int top_right_y = RIGHT_UP_Y;

    //top_right_x = int((top_right_y - pos_c) / pos_m)
    int top_right_x = int((top_right_y - pos_c) / pos_m);

    //bottom_right_y = img.shape[0]
    int bottom_right_y = RIGHT_DOWN_Y;

    //bottom_right_x = int((bottom_right_y - pos_c) / pos_m)
    int bottom_right_x = int((bottom_right_y - pos_c) / pos_m);

//    printf("min_top_y: %d\n",min_top_y);
//    printf("top_left_y: %d\n",top_left_y);
//    printf("top_left_x: %d\n",top_left_x);
//    printf("top_right_y: %d\n",top_right_y);
//    printf("top_right_x: %d\n",top_right_x);
//    printf("bottom_right_y: %d\n",bottom_right_y);
//    printf("bottom_right_x: %d\n",bottom_right_x);
//    printf("bottom_left_y: %d\n",bottom_left_y);
//    printf("bottom_left_x: %d\n",bottom_left_x);

    pointArray[0]=0;
    pointArray[1]=0;
    pointArray[2]=0;
    pointArray[3]=0;
    pointArray[4]=0;
    pointArray[5]=0;
    pointArray[6]=0;
    pointArray[7]=0;

    pointArray[0] = bottom_left_x;
    pointArray[1] = bottom_left_y;

    pointArray[2] = top_left_x;
    pointArray[3] = top_left_y;

    pointArray[4] = top_right_x;
    pointArray[5] = top_right_y;

    pointArray[6] = bottom_right_x;
    pointArray[7] = bottom_right_y;

    //# Average
    //    cv2.line(img, (bottom_left_x, bottom_left_y), (top_left_x, top_left_y), [0, 255, 0], 10)
    if(cvIsNaN(neg_m) == false)
    {
        cv::line(img, cv::Point(bottom_left_x, bottom_left_y), cv::Point(top_left_x, top_left_y), cv::Scalar(0, 0, 255), 5);
    }

    if(cvIsNaN(pos_m) == false)
    {
        cv::line(img, cv::Point(top_right_x, top_right_y), cv::Point(bottom_right_x, bottom_right_y), cv::Scalar(0, 0, 255), 5);
    }
}

cv::Mat hough_lines(cv::Mat selected, double rho, double theta, int threshold, int min_line_len, int max_line_gap)
{
    // `img` should be the output of a Canny transform.
    //  Returns an image with hough lines drawn.

    //lines = cv2.HoughLinesP(img, rho, theta, threshold, np.array([]), minLineLength=min_line_len, maxLineGap=max_line_gap)
    long long startHoughLinesPTime = currentTimeInMilliseconds();
    std::vector<cv::Vec4i> lines;
    cv::HoughLinesP(selected, lines, rho, theta, threshold, min_line_len, max_line_gap);
    long long endHoughLinesPTime = currentTimeInMilliseconds();

    __android_log_print(ANDROID_LOG_VERBOSE, "OpenCVNative", "HoughLinesP: %lld", (endHoughLinesPTime- startHoughLinesPTime));

    //line_img = np.zeros((*img.shape, 3), dtype=np.uint8)
    cv::Mat line_img = cv::Mat(selected.rows, selected.cols, CV_8UC3);

    //  extrapolate(line_img, lines)
    long long startextrapolateTime = currentTimeInMilliseconds();
    extrapolate(line_img, lines);
    long long endextrapolateTime = currentTimeInMilliseconds();
    __android_log_print(ANDROID_LOG_VERBOSE, "OpenCVNative", "extrapolate: %lld", (endextrapolateTime- startextrapolateTime));
    //drawLinesP(line_img, lines)

    return line_img;
}

cv::Mat weighted_img(cv::Mat img, cv::Mat initial_img, double alpha, double beta, double gamma)
{
    //def weighted_img(img, initial_img, α=0.8, β=1., λ=0.):
    //`img` is the output of the hough_lines(), An image with lines drawn on it.
    // Should be a blank image (all black) with lines drawn on it.
    //`initial_img` should be the image before any processing.
    //The result image is computed as follows:
    //initial_img * α + img * β + λ
    //NOTE: initial_img and img must be the same shape!

    //cv2.addWeighted(initial_img, α, img, β, λ)
    cv::Mat output;
    cv::addWeighted(img, alpha, initial_img, beta, gamma, output);

    return output;
}

void count4point_extrapolate(std::vector<cv::Vec4i> lines, double* &resultArray)
{
    if (lines.size() == 0) // if lines == None:
    {
        printf("******************* No lines detected");

        return;
    }

    std::vector<double> slopes = apply_along_axis_similar(lines);

    std::vector<bool> pos_slopes = checkSlopesVectorValue(slopes, 0, CHECK_POS_SLOPES);
    std::vector<cv::Vec4i> pos_lines = fillterVectorValue(lines, pos_slopes);

    std::vector<bool> neg_slopes = checkSlopesVectorValue(slopes, 1, CHECK_NEG_SLOPES);
    std::vector<cv::Vec4i> neg_lines = fillterVectorValue(lines, neg_slopes);

    //    if len(pos_lines) == 0 or len(neg_lines) == 0: return;
    //    if(pos_lines.size() == 0 || neg_lines.size() == 0)
    //    {
    //        point1 = CGPointMake(0, 0);
    //        point2 = CGPointMake(0, 0);
    //        point3 = CGPointMake(0, 0);
    //        point4 = CGPointMake(0, 0);
    //
    //        negSlope = 0;
    //        negIntercept = 0;
    //        posSlope = 0;
    //        posIntercept = 0;
    //
    //        return;
    //    }

    std::vector<int> pos_x;
    std::vector<int> pos_y;
    double pos_m = 0;
    double pos_c = 0;
    lines_linreg_by_OpenCV(pos_lines, pos_x, pos_y, pos_m, pos_c);

    //    //pos_global_slopes = np.append(pos_global_slopes, [pos_m])
    //    pos_global_slopes.push_back(pos_m);
    //    //pos_global_intercepts = np.append(pos_global_intercepts, [pos_c])
    //    pos_global_intercepts.push_back(pos_c);
    //
    //    //# print("Before adjusting slopes : %f intercept %f" % (pos_m, pos_c))
    //    //pos_m = pos_global_slopes[-20:].mean() //意思是取該陣列的倒數20筆資料為一個陣列(google關鍵字ndarray 切片)，並取平均值。
    //    pos_m = meanDouble(pos_global_slopes);
    //    //pos_c = pos_global_intercepts[-20:].mean() //意思是取該陣列的倒數20筆資料為一個陣列，並取平均值(google關鍵字ndarray 切片)，並取平均值。
    //    pos_c = meanDouble(pos_global_intercepts);

    posSlope = pos_m;
    posIntercept = pos_c;

    //neg_x, neg_y, neg_m, neg_c = lines_linreg(neg_lines)
    std::vector<int> neg_x;
    std::vector<int> neg_y;
    double neg_m = 0;
    double neg_c = 0;
    lines_linreg_by_OpenCV(neg_lines, neg_x, neg_y, neg_m, neg_c);

    //    //neg_global_slopes = np.append(neg_global_slopes, [neg_m])
    //    neg_global_slopes.push_back(neg_m);
    //    //neg_global_intercepts = np.append(neg_global_intercepts, [neg_c])
    //    neg_global_intercepts.push_back(neg_c);
    //
    //    //neg_m = neg_global_slopes[-20:].mean()
    //    neg_m = meanDouble(neg_global_slopes);
    //    //neg_c = neg_global_intercepts[-20:].mean()
    //    neg_c = meanDouble(neg_global_intercepts);

    negSlope = neg_m;
    negIntercept = neg_c;

    //bottom_left_y = img.shape[0]
    int bottom_left_y = LEFT_DOWN_Y;

    //bottom_left_x = int((bottom_left_y - neg_c) / neg_m)
    int bottom_left_x = int((bottom_left_y - neg_c) / neg_m);

    //min_top_y = np.min([neg_y.min(), pos_y.min()])

    int min_top_y = 0;

    if(pos_lines.size() > 0 && neg_lines.size() > 0)
    {
        int neg_y_MinValue = *min_element(neg_y.begin(), neg_y.end());
        int pos_y_MinValue = *min_element(pos_y.begin(), pos_y.end());
        min_top_y = MIN(neg_y_MinValue, pos_y_MinValue);
    }
    else if(pos_lines.size() > 0)
    {
        min_top_y = *min_element(pos_y.begin(), pos_y.end());
    }
    else if(neg_lines.size() > 0)
    {
        min_top_y = *min_element(neg_y.begin(), neg_y.end());
    }

    //top_y = np.append(top_y, [min_top_y])
    //    top_y.push_back(min_top_y);

    //min_top_y = int(top_y[-20:].mean())
    //    min_top_y = int(meanDouble(top_y));

    //top_left_y = min_top_y
    int top_left_y = LEFT_UP_Y;

    //top_left_x = int((top_left_y - neg_c) / neg_m)
    int top_left_x = int((top_left_y - neg_c) / neg_m);

    //top_right_y = min_top_y
    int top_right_y = RIGHT_UP_Y;

    //top_right_x = int((top_right_y - pos_c) / pos_m)
    int top_right_x = int((top_right_y - pos_c) / pos_m);

    //bottom_right_y = img.shape[0]
    int bottom_right_y = RIGHT_DOWN_Y;

    //bottom_right_x = int((bottom_right_y - pos_c) / pos_m)
    int bottom_right_x = int((bottom_right_y - pos_c) / pos_m);

//    printf("min_top_y: %d\n",min_top_y);
//    printf("top_left_y: %d\n",top_left_y);
//    printf("top_left_x: %d\n",top_left_x);
//    printf("top_right_y: %d\n",top_right_y);
//    printf("top_right_x: %d\n",top_right_x);
//    printf("bottom_right_y: %d\n",bottom_right_y);
//    printf("bottom_right_x: %d\n",bottom_right_x);
//    printf("bottom_left_y: %d\n",bottom_left_y);
//    printf("bottom_left_x: %d\n",bottom_left_x);

    if(neg_lines.size() > 0)
    {
        resultArray[0] = top_left_x;
        resultArray[1] = top_left_y;
        resultArray[2] = bottom_left_x;
        resultArray[3] = bottom_left_y;

        resultArray[4] = neg_m;
        resultArray[5] = neg_c;
    }

    if(pos_lines.size() > 0)
    {
        resultArray[6] = top_right_x;
        resultArray[7] = top_right_y;
        resultArray[8] = bottom_right_x;
        resultArray[9] = bottom_right_y;

        resultArray[10] = pos_m;
        resultArray[11] = pos_c;
    }
}

void count4point_houghLines(cv::Mat selected, double rho, double theta, int threshold, int min_line_len, int max_line_gap, double* &resultArray)
{
    std::vector<cv::Vec4i> lines;
    cv::HoughLinesP(selected, lines, rho, theta, threshold, min_line_len, max_line_gap);

    count4point_extrapolate(lines, resultArray);
}

void count4Points(cv::Mat imageMat, double* &resultArray)
{
    cv::Mat image = yellowToWhiteImage(imageMat);

    cv::Mat converted;
    cv::cvtColor(image, converted, CV_BGRA2RGB);

    int maxWidth = image.cols;

    std::vector<std::vector<cv::Point> > vertices = getVertices();

    //    gray_scaled = grayscale(sample_image)
    cv::Mat gray_scaled = grayscale(converted);

    //    blurred_img = gaussian_blur(gray_scaled, 5)
    cv::Mat blurred_img = gaussian_blur(gray_scaled, GAUSIAN_KERNEL_VALUE);

    //#cimage = canny(blurred_img, 50, 250)
    //# Use auto_canny instead.
    //cimage = auto_canny(blurred_img)
    cv::Mat cimage = auto_canny(blurred_img, AUTO_CANNY_VALUE);

    //tmp = np.copy(cimage)

    //region_of_interest
    cv::Mat selected = region_of_interest(cimage, vertices);

    count4point_houghLines(selected, RHO, THETA, THERESHOLDVALUE, MIN_LINE_LEN, MAX_LINE_GAP, resultArray);
}

cv::Mat drawROIArea(cv::Mat sourceMat)
{
    //這樣才不會有殘影，但為什麼會有殘影？？
    cv::Mat roiAreaMat = sourceMat.zeros(sourceMat.rows, sourceMat.cols, sourceMat.type());// CV_8UC4

    std::vector<std::vector<cv::Point> > vertices = getVertices();

    cv::Scalar roiColor = cv::Scalar(0,72,0,64);

    cv::fillPoly(roiAreaMat, vertices, roiColor);

    return roiAreaMat;
}

int changeParamsModeFunction(cv::Mat &ImageMat, int whichStep)
{
    cv::Mat image = yellowToWhiteImage(ImageMat);

        cv::Mat converted;
        cv::cvtColor(image, converted, CV_BGRA2RGB);

        std::vector<std::vector<cv::Point> > vertices = getVertices();

        cv::Mat gray_scaled = grayscale(converted);

        cv::Mat blurred_img = gaussian_blur(gray_scaled, GAUSIAN_KERNEL_VALUE);

        if(whichStep == 0)//只做完高斯模糊(0)
        {
            blurred_img.copyTo(ImageMat);

            return 0;
        }

        cv::Mat cimage = auto_canny(blurred_img, AUTO_CANNY_VALUE);

        if(whichStep == 1)//只做完 auto_canny(1)
        {
            cimage.copyTo(ImageMat);

            return 1;
        }

        //region_of_interest
        cv::Mat selected = region_of_interest(cimage, vertices);

        std::vector<cv::Vec4i> lines;
        cv::HoughLinesP(selected, lines, RHO, THETA, THERESHOLDVALUE, MIN_LINE_LEN, MAX_LINE_GAP);

        cv::Mat showLinesImage;
        cv::cvtColor(cimage, showLinesImage, CV_GRAY2RGB);

        cv::Mat showROIArea = drawROIArea(showLinesImage);

        drawLinesP(showLinesImage, lines);

        showLinesImage = weighted_img(showLinesImage, showROIArea, 0.8, 1.0, 0.0);

        //只做完修改 RHO(2), THETA(3), THRESHOLD(4), MIN_LINE_LEN(5), MAX_LINE_GAP(6)
        if(whichStep == 2 || whichStep == 3 || whichStep == 4 || whichStep == 5 || whichStep == 6)
        {
            showLinesImage.copyTo(ImageMat);
        }

        extrapolate(showLinesImage, lines);

        //只做完修改 checkPosSlopes(7), checkNegSlopes(8)
        if(whichStep == 7  || whichStep == 8 )
        {
            showLinesImage.copyTo(ImageMat);
        }

        cv::Mat imageRGBMat;
        cv::cvtColor(ImageMat, imageRGBMat, CV_RGBA2RGB);

        cv::Mat result = weighted_img(showLinesImage, imageRGBMat, 0.8, 1.0, 0.0);
        cv::Mat resultRGBA;
        cv::cvtColor(result, resultRGBA, CV_RGB2RGBA);

        //[imageList  addObject:MatToUIImage(result)]  8;

        cv::Mat testMat;
        cv::Mat testArea = drawROIArea(resultRGBA);
        cv::addWeighted(testArea, 0.8, resultRGBA, 0.8, 0, testMat);
        //[imageList  addObject:MatToUIImage(testMat)]  9;

        testMat.copyTo(ImageMat);



        return 9;
}

extern "C"
{
    JNIEXPORT jint JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_changeParamsModeUse(
            JNIEnv *env, jclass, jlong jMatObject, jint whichStep) {

        cv::Mat *ImageMat = (cv::Mat*) jMatObject;

        return changeParamsModeFunction(*ImageMat, whichStep);
    }

    JNIEXPORT jdoubleArray JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_getSlopesInterceptGroup(JNIEnv *env, jclass)
    {
        double* resultArray = new double[4];
        resultArray[0] = 0;
        resultArray[1] = 0;
        resultArray[2] = 0;
        resultArray[3] = 0;

        resultArray[0] = posSlope;
        resultArray[1] = posIntercept;
        resultArray[2] = negSlope;
        resultArray[3] = negIntercept;

        //转换为JNI数组后返回
        jdoubleArray outRes = env->NewDoubleArray(4); //分配数组所需要的空间
        (*env).SetDoubleArrayRegion(outRes, 0, 4, resultArray);//将数据拷贝到jdoubleArray所在空间

        delete [] resultArray;

        return outRes;
    }

    JNIEXPORT jint JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_getLineCount(JNIEnv *env, jclass)
    {
        return currentLineCount;
    }

    JNIEXPORT jintArray JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_getPointArray(JNIEnv *env, jclass)
    {
        //转换为JNI数组后返回
        jintArray outRes = env->NewIntArray(8); //分配数组所需要的空间
        (*env).SetIntArrayRegion(outRes, 0, 8, pointArray);//将数据拷贝到jdoubleArray所在空间

        return outRes;
    }

    JNIEXPORT jobjectArray JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_getAllStepImage(JNIEnv *env, jclass, jlong jMatObject)
    {
        cv::Mat *ImageMat = (cv::Mat*) jMatObject;

        //宣告回傳的容器
        std::vector<cv::Mat> matArray;

        cv::Mat image = yellowToWhiteImage(*ImageMat);

        cv::Mat converted;
        cv::cvtColor(image, converted, CV_BGRA2RGB);
        matArray.push_back(converted);//[imageList  addObject:MatToUIImage(converted)]; 1

        int maxWidth = image.cols;

        std::vector<std::vector<cv::Point> > vertices = getVertices();

        //    gray_scaled = grayscale(sample_image)
        cv::Mat gray_scaled = grayscale(converted);
        matArray.push_back(gray_scaled); //[imageList  addObject:MatToUIImage(gray_scaled)] 2;

        //    blurred_img = gaussian_blur(gray_scaled, 5)
        cv::Mat blurred_img = gaussian_blur(gray_scaled, GAUSIAN_KERNEL_VALUE);
        matArray.push_back(blurred_img); //[imageList  addObject:MatToUIImage(blurred_img)] 3;

        //#cimage = canny(blurred_img, 50, 250)
        //# Use auto_canny instead.
        //cimage = auto_canny(blurred_img)
        cv::Mat cimage = auto_canny(blurred_img, AUTO_CANNY_VALUE);
        matArray.push_back(cimage);//[imageList  addObject:MatToUIImage(cimage)]  4;

        //region_of_interest
        cv::Mat selected = region_of_interest(cimage, vertices);
        matArray.push_back(selected);//[imageList  addObject:MatToUIImage(selected)]  5;


        std::vector<cv::Vec4i> lines;
        cv::HoughLinesP(selected, lines, RHO, THETA, THERESHOLDVALUE, MIN_LINE_LEN, MAX_LINE_GAP);

        currentLineCount = lines.size();

        cv::Mat showLinesImage = cv::Mat(selected.rows, selected.cols, CV_8UC3);
        drawLinesP(showLinesImage, lines);
        matArray.push_back(showLinesImage);//[imageList  addObject:MatToUIImage(showLinesImage)] 6;

        extrapolate(selected, lines);
        matArray.push_back(selected); //[imageList  addObject:MatToUIImage(lineImage)] 7;

        cv::Mat imageRGBMat;
        cv::cvtColor(*ImageMat, imageRGBMat, CV_RGBA2RGB);

        cv::Mat result = weighted_img(showLinesImage, imageRGBMat, 0.8, 1.0, 0.0);
        cv::Mat resultRGBA;
        cv::cvtColor(result, resultRGBA, CV_RGB2RGBA);
        matArray.push_back(result);//[imageList  addObject:MatToUIImage(result)]  8;

        cv::Mat testMat;
        cv::Mat testArea = drawROIArea(resultRGBA);
        cv::addWeighted(testArea, 0.8, resultRGBA, 0.8, 0, testMat);
        matArray.push_back(testMat);//[imageList  addObject:MatToUIImage(testMat)]  9;

        //return 回傳的容器
        jclass matclass = env->FindClass("org/opencv/core/Mat");
        jmethodID jMatCons = env->GetMethodID(matclass,"<init>","()V");
        jmethodID getPtrMethod = env->GetMethodID(matclass, "getNativeObjAddr", "()J");

        // Call back constructor to allocate a new instance
        jobjectArray newMatArr = env->NewObjectArray(matArray.size(), matclass, 0);

        for (int i=0; i< matArray.size(); i++){
            jobject jMat = env->NewObject(matclass, jMatCons);
            Mat & native_image= *(Mat*)env->CallLongMethod(jMat, getPtrMethod);
            native_image=matArray[i];

            env->SetObjectArrayElement(newMatArr, i, jMat);
        }

        return newMatArr;
    }

    //JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_drawConnectedDevice(JNIEnv *env, jclass, jlong jMatObject)
    //{
    //    cv::Mat *ImageMat = (cv::Mat*) jMatObject;
    //    cv::circle(ImageMat, connectedCirclePoint, 50, cv::Scalar(0, 255, 0, 255))
    //}

    //JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_drawDisconnectedDevice(JNIEnv *env, jclass, jlong jMatObject)
    //    {
    //        cv::Mat *ImageMat = (cv::Mat*) jMatObject;
    //        cv::circle(ImageMat, connectedCirclePoint, 50, cv::Scalar(255, 0, 0, 255))
    //    }

    JNIEXPORT jdoubleArray JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_count4Points(JNIEnv *env, jclass, jlong jMatObject)
    {
        cv::Mat *ImageMat = (cv::Mat*) jMatObject;

        double* resultArray = new double[12];
        resultArray[0] = 0;
        resultArray[1] = 0;
        resultArray[2] = 0;
        resultArray[3] = 0;
        resultArray[4] = 0;
        resultArray[5] = 0;
        resultArray[6] = 0;
        resultArray[7] = 0;
        resultArray[8] = 0;
        resultArray[9] = 0;
        resultArray[10] = 0;
        resultArray[11] = 0;

        count4Points(*ImageMat, resultArray);

        //转换为JNI数组后返回
        jdoubleArray outRes = env->NewDoubleArray(12); //分配数组所需要的空间
        (*env).SetDoubleArrayRegion(outRes, 0, 12, resultArray);//将数据拷贝到jdoubleArray所在空间

        delete [] resultArray;

        return outRes;
    }

    JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_drawChangeParamsModeReferenceLine(JNIEnv *env, jclass, jlong jMatObject, jint baseLineWidth
        , jboolean isLeftRefernceLine, jint defaultLeftBottomX, jint defaultLeftBottomY, jint defaultLeftTopX, jint defaultLeftTopY
        , jboolean isRightRefernceLine, jint defaultRightTopX, jint defaultRightTopY, jint defaultRightBottomX, jint defaultRightBottomY){

        cv::Mat *ImageMat = (cv::Mat*) jMatObject;

        if(isLeftRefernceLine == true)
        {
            //left
            //橘色的線
            cv::line(*ImageMat, cv::Point(defaultLeftBottomX, defaultLeftBottomY), cv::Point(defaultLeftTopX, defaultLeftTopY), cv::Scalar(255, 50, 0, 255), baseLineWidth);
        }

        if(isRightRefernceLine == true)
        {
            //right
            //紅色的線
            cv::line(*ImageMat, cv::Point(defaultRightTopX, defaultRightTopY), cv::Point(defaultRightBottomX, defaultRightBottomY), cv::Scalar(255, 0, 0, 255), baseLineWidth);
        }
    }

    JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_drawResult(JNIEnv *env, jclass, jlong jMatObject, jint baseLineWidth, jint detectLineWidth
        , jdouble rightSlopeValue, jint rightTopX, jint rightTopY, jint rightBottomX, jint rightBottomY
        , jdouble leftSlopeValue, jint leftTopX, jint leftTopY, jint leftBottomX, jint leftBottomY
        , jboolean isLeftRefernceLine, jint defaultLeftBottomX, jint defaultLeftBottomY, jint defaultLeftTopX, jint defaultLeftTopY
        , jboolean isRightRefernceLine, jint defaultRightTopX, jint defaultRightTopY, jint defaultRightBottomX, jint defaultRightBottomY
        , jdouble defaultRightSlope
        , jdouble defaultRightIntercept, jdouble rightIntercpetValue
        , jdouble defaultLeftSlope
        , jdouble defaultLeftIntercept, jdouble leftIntercpetValue
        , jdouble channel1Value, jdouble channel2Value
        , jint afterComputeChannel1, jint afterComputeChannel2
        , jdouble fps)
    {
        cv::Mat *ImageMat = (cv::Mat*) jMatObject;

        cv::Mat roiAndLinesLayer = drawROIArea(*ImageMat);

        cv::Point rightTopPoint = cv::Point(rightTopX, rightTopY);
        cv::Point rightBottomPoint = cv::Point(rightBottomX, rightBottomY);

        cv::Scalar blueColor = cv::Scalar(0, 0, 255, 255);

        if(cvIsNaN(rightSlopeValue) == false)
        {
            //右 深藍色的線
            cv::line(roiAndLinesLayer, rightTopPoint , rightBottomPoint, blueColor, detectLineWidth);
        }

        cv::Point leftBottomPoint = cv::Point(leftBottomX, leftBottomY);
        cv::Point leftTopPoint = cv::Point(leftTopX, leftTopY);
        cv::Scalar lightblueColor = cv::Scalar(0, 125, 125, 255);

        if(cvIsNaN(leftSlopeValue) == false)
        {
            //左 淺藍色的線
            cv::line(roiAndLinesLayer, leftBottomPoint, leftTopPoint, lightblueColor, detectLineWidth);
        }

        if(isLeftRefernceLine == true)
        {
            //left
            //橘色的線
            cv::line(roiAndLinesLayer, cv::Point(defaultLeftBottomX, defaultLeftBottomY), cv::Point(defaultLeftTopX, defaultLeftTopY), cv::Scalar(255, 50, 0, 255), baseLineWidth);
        }

        if(isRightRefernceLine == true)
        {
            //right
            //紅色的線
            cv::line(roiAndLinesLayer, cv::Point(defaultRightTopX, defaultRightTopY), cv::Point(defaultRightBottomX, defaultRightBottomY), cv::Scalar(255, 0, 0, 255), baseLineWidth);
        }


        cv::Mat imageAddRoiAndLinesLayerMat;
        cv::addWeighted(*ImageMat, 1, roiAndLinesLayer, 1, 0, imageAddRoiAndLinesLayerMat);
        imageAddRoiAndLinesLayerMat.copyTo(*ImageMat);
    }

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROILeftUpX(
        JNIEnv *env, jobject obj, jint leftUpX)
{
    LEFT_UP_X = leftUpX;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROILeftUpY(
        JNIEnv *env, jobject obj, jint leftUpY)
{
    LEFT_UP_Y = leftUpY;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROILeftDownX(
        JNIEnv *env, jobject obj, jint leftDownX)
{
    LEFT_DOWN_X = leftDownX;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROILeftDownY(
        JNIEnv *env, jobject obj, jint leftDownY)
{
    LEFT_DOWN_Y = leftDownY;
}


JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROIRightUpX(
        JNIEnv *env, jobject obj, jint rightUpX)
{
    RIGHT_UP_X = rightUpX;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROIRightUpY(
        JNIEnv *env, jobject obj, jint rightUpY)
{
    RIGHT_UP_Y = rightUpY;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROIRightDownX(
        JNIEnv *env, jobject obj, jint rightDownX)
{
    RIGHT_DOWN_X = rightDownX;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setROIRightDownY(
        JNIEnv *env, jobject obj, jint rightDownY)
{
    RIGHT_DOWN_Y = rightDownY;
}

//霍夫找線的參數
JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setCheckPosSlopes(
        JNIEnv *env, jobject obj, jdouble checkPosSlopes)
{
    CHECK_POS_SLOPES = checkPosSlopes;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setCheckNegSlopes(
        JNIEnv *env, jobject obj, jdouble checkNegSlopes)
{
    CHECK_NEG_SLOPES = checkNegSlopes;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setGausianKernelValue(
        JNIEnv *env, jobject obj, jint gausianKernelValue)
{
    GAUSIAN_KERNEL_VALUE = gausianKernelValue;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setAutoCannyValue(
        JNIEnv *env, jobject obj, jdouble autoCannyValue)
{
    AUTO_CANNY_VALUE = autoCannyValue;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setRhoValue(
        JNIEnv *env, jobject obj, jdouble rho)
{
    RHO = rho;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setThetaValue(
        JNIEnv *env, jobject obj, jint thetaValue)
{
    THETA = (thetaValue * CV_PI / 180);
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setTheresHoldValue(
        JNIEnv *env, jobject obj, jint theresHoldValue)
{
    THERESHOLDVALUE = theresHoldValue;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setMinLineLen(
        JNIEnv *env, jobject obj, jint minLineLen)
{
    MIN_LINE_LEN = minLineLen;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setMaxLineGap(
        JNIEnv *env, jobject obj, jint maxLineGap)
{
    MAX_LINE_GAP = maxLineGap;
}

//設定要左右斜率的顏色
JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_rightSlopeTextColorUseNormalColor(JNIEnv *env, jobject obj)
{
    rightSlopeTextColor = nomarlColor;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_rightSlopeTextColorUseDangerColor(JNIEnv *env, jobject obj)
{
    rightSlopeTextColor = dangerColor;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_leftSlopeTextColorUseNormalColor(JNIEnv *env, jobject obj)
{
    leftSlopeTextColor = nomarlColor;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_leftSlopeTextColorUsedDangerColor(JNIEnv *env, jobject obj)
{
    leftSlopeTextColor = dangerColor;
}

//變更字體大小及字的位置
JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setTextSize(JNIEnv *env, jobject obj, jdouble jTextSize)
{
    textSize = jTextSize;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_setScaleMin(JNIEnv *env, jobject obj, jdouble jScaleMinValue)
{
    scaleMin = jScaleMinValue;
}

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_scaleTextPosition(JNIEnv *env, jobject obj, jdouble jScaleXValue, jdouble jScaleYValue)
{
    scalePosition(rightSlopeStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(rightIntercpetValueStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(leftSlopeStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(leftIntercpetValueStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(channel1ValueStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(channel2ValueStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(computeChannel1ValueStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(computeChannel2ValueStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(resultLeftBottomPointStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(resultLeftTopPointStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(resultRightTopPointStringPoint, jScaleXValue, jScaleYValue);
    scalePosition(resultRightBottomPointStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(fpsStringPoint, jScaleXValue, jScaleYValue);

    scalePosition(connectedCirclePoint, jScaleXValue, jScaleYValue);
}

    //[self scalePosition:accelerometerXPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];
    //[self scalePosition:accelerometerYPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];
    //[self scalePosition:accelerometerZPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];
    //[self scalePosition:gyroXPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];
    //[self scalePosition:gyroYPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];
    //[self scalePosition:gyroZPoint scaleValueX:scaleValueX scaleValueY:scaleValueY];

//for demo

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_yellowToWhite(
        JNIEnv *env, jclass, jobject bitmap) {
    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    cv::Mat outputData = yellowToWhiteImage(imgData);

    jobject outputBitmap = createBitmap(env, 0, outputData.cols, outputData.rows);
    nMatToBitmap2(env, 0, outputData, outputBitmap, false);

    return outputBitmap;
}

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_grayscale(
        JNIEnv *env, jclass, jobject bitmap) {
    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    cv::Mat outputData = grayscale(imgData);

    jobject outputBitmap = createBitmap(env, 0, outputData.cols, outputData.rows);
    nMatToBitmap2(env, 0, outputData, outputBitmap, false);

    return outputBitmap;
}

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_gaussianBlur(
        JNIEnv *env, jclass, jobject bitmap) {
    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    cv::Mat outputData = gaussian_blur(imgData, 5);

    jobject outputBitmap = createBitmap(env, 0, outputData.cols, outputData.rows);
    nMatToBitmap2(env, 0, outputData, outputBitmap, false);

    return outputBitmap;
}

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_autoCanny(
        JNIEnv *env, jclass, jobject bitmap) {

    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    cv::Mat converted = yellowToWhiteImage(imgData);

    //sample_image = np.copy(converted)
    cv::Mat sample_image = converted.clone();

    std::vector<std::vector<cv::Point > > vertices = getVertices();

    //    gray_scaled = grayscale(sample_image)
    cv::Mat gray_scaled = grayscale(sample_image);

    //    blurred_img = gaussian_blur(gray_scaled, 5)
    cv::Mat blurred_img = gaussian_blur(gray_scaled, 5);

    //#cimage = canny(blurred_img, 50, 250)
    //# Use auto_canny instead.
    //cimage = auto_canny(blurred_img)
    cv::Mat cimage = auto_canny(blurred_img);

    jobject outputBitmap = createBitmap(env, 0, cimage.cols, cimage.rows);
    nMatToBitmap2(env, 0, cimage, outputBitmap, false);

    return outputBitmap;
}

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_regionOfInterest(
        JNIEnv *env, jclass, jobject bitmap) {
    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    //converted = yellow_to_white(image)
    cv::Mat converted = yellowToWhiteImage(imgData);

    //sample_image = np.copy(converted)
    cv::Mat sample_image = converted.clone();

    std::vector<std::vector<cv::Point > > vertices = getVertices();

    //    gray_scaled = grayscale(sample_image)
    cv::Mat gray_scaled = grayscale(sample_image);

    //    blurred_img = gaussian_blur(gray_scaled, 5)
    cv::Mat blurred_img = gaussian_blur(gray_scaled, 5);

    //#cimage = canny(blurred_img, 50, 250)
    //# Use auto_canny instead.
    //cimage = auto_canny(blurred_img)
    cv::Mat cimage = auto_canny(blurred_img);

    //tmp = np.copy(cimage)
//    cv::Mat tmp = cimage.clone();

    //region_of_interest
    cv::Mat selected = region_of_interest(cimage, vertices);

    jobject outputBitmap = createBitmap(env, 0, selected.cols, selected.rows);
    nMatToBitmap2(env, 0, selected, outputBitmap, false);

    return outputBitmap;
}

JNIEXPORT jobject JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_houghLines(
        JNIEnv *env, jclass, jobject bitmap) {
    cv::Mat imgData = nBitmapToMat2(env, 0, bitmap, false);

    //converted = yellow_to_white(image)
    cv::Mat converted = yellowToWhiteImage(imgData);

    //sample_image = np.copy(converted)
    cv::Mat sample_image = converted.clone();

    std::vector<std::vector<cv::Point > > vertices = getVertices();

    //    gray_scaled = grayscale(sample_image)
    cv::Mat gray_scaled = grayscale(sample_image);

    //    blurred_img = gaussian_blur(gray_scaled, 5)
    cv::Mat blurred_img = gaussian_blur(gray_scaled, 5);

    //#cimage = canny(blurred_img, 50, 250)
    //# Use auto_canny instead.
    //cimage = auto_canny(blurred_img)
    cv::Mat cimage = auto_canny(blurred_img);

    //tmp = np.copy(cimage)
//    cv::Mat tmp = cimage.clone();

    //region_of_interest
    cv::Mat selected = region_of_interest(cimage, vertices);

    //hough_linesew
    cv::Mat lineImage = hough_lines(selected, 1.0, (1 * CV_PI / 180), 20, 5, 5);

    jobject outputBitmap = createBitmap(env, 0, lineImage.cols, lineImage.rows);
    nMatToBitmap2(env, 0, lineImage, outputBitmap, false);

    return outputBitmap;
}

//for demo

JNIEXPORT void JNICALL Java_org_opencv_samples_facedetect_OpenCVWrapper_detectLane(
        JNIEnv *env, jobject obj, jint srcWidth, jint srcHeight,
        jobject srcBuffer, jobject dstSurface) {

}

//計算所需時間

JNIEXPORT jlong Java_org_opencv_samples_facedetect_OpenCVWrapper_getBitmapToMatTime(
        JNIEnv *env, jobject obj)
{
    return BitmapToMatTime;
}

//long long BitmapToMatTime  = 0;
//long long YellowToWhiteTime = 0;
//long long grayScaleTime = 0;
//long long gaussianBlurTime = 0;
//long long medianTime = 0;
//long long autoCannyTime = 0;
//long long regionOfInterestTime = 0;
//long long HoughLinesPTime = 0;
//long long extrapolateTime = 0;
//long long houghLinesTime = 0;
//long long weightedImgTime = 0;
//long long RGB2RGBAImgTime = 0;
//long long createBitmapTime = 0;

//計算所需時間

}


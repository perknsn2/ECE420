#include "opencvmex.hpp"
#include "opencv2/opencv.hpp"
#include "opencv2/nonfree/nonfree.hpp"
#include "opencv2/features2d/features2d.hpp"

using namespace cv;

//////////////////////////////////////////////////////////////////////////////
// Check inputs
//////////////////////////////////////////////////////////////////////////////
void checkInputs(int nrhs, const mxArray *prhs[])
{
    if (nrhs != 2)
    {
        mexErrMsgTxt("Incorrect number of inputs. Function expects 2 inputs.");
    }
    
    if (!mxIsUint8(prhs[0]))
    {       
        mexErrMsgTxt("Input image must be uint8.");
    }
}

///////////////////////////////////////////////////////////////////////////
// Main entry point to a MEX function
///////////////////////////////////////////////////////////////////////////
void mexFunction(int nlhs, mxArray *plhs[], int nrhs, const mxArray *prhs[])
{  
    
    checkInputs(nrhs, prhs);

    // inputs
    cv::Ptr<cv::Mat> img1 = ocvMxArrayToImage_uint8(prhs[0], true);
    cv::Ptr<cv::Mat> img2 = ocvMxArrayToImage_uint8(prhs[1], true);
        
    // initialize OpenCV's 2D feature module
    initModule_features2d();
    
    initModule_nonfree();
    
    // get pointer to the detection algorithm  
    Ptr<FeatureDetector> siftDetector = 
            Algorithm::create<FeatureDetector>("Feature2D.SIFT");    
    // check if error occurs
    if( siftDetector.empty() )
        CV_Error(CV_StsNotImplemented, "OpenCV was built without SIFT detect support");
    
    // get pointer to the extraction algorithm
    Ptr<DescriptorExtractor> siftExtractor =
        Algorithm::create<DescriptorExtractor>("Feature2D.SIFT");
        // check if error occurs
    if( siftExtractor.empty() )
        CV_Error(CV_StsNotImplemented, "OpenCV was built without SIFT extract support");
    
    //get pointer to the matching algorithm 
    // suggesting matcher for SIFT: BFMatcher (brute force)
//    Ptr<BFMatcher> siftMatcher = 
//            Algorithm::create<BFMatcher>("Feature2D.NORM_L2");
    BFMatcher siftMatcher(NORM_L1, true);
//    if( matcher.empty() )
//        CV_Error(CV_StsNotImplemented, "OpenCV was built without SIFT match support");
    
    // prepare space for returned keypoints
    vector<KeyPoint> keypoints1;
    vector<KeyPoint> keypoints2;
    
    // invoke the detector
    siftDetector->detect(*img1, keypoints1); 
    siftDetector->detect(*img2, keypoints2);
        
    // populate the outputs
    //plhs[0] = ocvKeyPointsToStruct(keypoints1); 
    
     Mat descriptors1;
     Mat descriptors2;
     siftExtractor->compute(*img1, keypoints1, descriptors1);
     siftExtractor->compute(*img2, keypoints2, descriptors2);
     
     vector<DMatch> matches;
     
     siftMatcher.match(descriptors1, descriptors2, matches);
    
    Mat outImg1;
    drawKeypoints(*img1, keypoints1, outImg1, Scalar(0,0,255));
    
    Mat outImg2;
    drawKeypoints(*img2, keypoints2, outImg2, Scalar(0,0,255));
    
    Mat matchImg;
    if((((float)matches.size()/(float)keypoints1.size()) > 0.7) || (((float)matches.size()/(float)keypoints2.size()) > 0.7))
    { 
        drawMatches(*img1, keypoints1, *img2, keypoints2, matches, matchImg, Scalar(0,0,255), Scalar(0,255,0));
    }
    else
    {
        vector<DMatch> noMatch;
        drawMatches(*img1, keypoints1, *img2, keypoints2, noMatch, matchImg, Scalar(0,0,255), Scalar(0,255,0));
    }

    
    plhs[0] = ocvMxArrayFromImage_uint8(outImg1);
    plhs[1] = ocvMxArrayFromImage_uint8(outImg2);
    plhs[2] = ocvMxArrayFromImage_uint8(matchImg);
}


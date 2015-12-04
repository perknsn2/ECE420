package com.example.caleb.buildingdetector;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private static final String TAG = "MainActivity";

    static {
        System.loadLibrary("opencv_java3");
    }

    private Uri fileUri;
    private MenuItem mItemStream;
    private MenuItem mItemSnapshot;
    private Bitmap mBitmap;
    private Mat mMat;
    private Features2d mFeatures2d;
    private FeatureDetector mFeatureDetector;
    private MatOfKeyPoint mMatofKeyPoint;
    private DescriptorMatcher mDescriptorMatcher;
    private Mat dbImgMat;
    private List<Mat> mDescriptorList;
    private ImageView mImageView;
    private TextView mTextView;

//    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
//        @Override
//        public void onManagerConnected(int status) {
//            switch (status){
//                case LoaderCallbackInterface.SUCCESS:
//                {
//                    Log.d(TAG, "OpenCV loaded Successfully");
//
//                    dbImgMat = new Mat();
//                } break;
//
//                case LoaderCallbackInterface.MARKET_ERROR:
//                {
//                    Log.d(TAG, "Google Play service is not accessible");
//                    AlertDialog MarketErrorMessage = new AlertDialog.Builder(mAppContext).create();
//                    MarketErrorMessage.setTitle("OpenCV Manager");
//                    MarketErrorMessage.setMessage("Google Play service is not accessible!");
//                    MarketErrorMessage.setCancelable(false);
//                    MarketErrorMessage.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            ((Activity) mAppContext).finish();
//                        }
//                    });
//                    MarketErrorMessage.show();
//                } break;
//
//                default:
//                {
//                    super.onManagerConnected(status);
//                } break;
//            }
//        }
//    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        /** So using the async loader doesn't load the library in time so I switched to static
         * loading.  Not sure which is better... wait. If open cv isn't installed this may cause a
         * problem. Not worth checking out now though.
         */
//        Log.d(TAG, "Load OpenCV Library");
//        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mOpenCVCallBack)) {
//            Log.e(TAG, "Failed to load OpenCV library");
//        }

        Bitmap dbImgBitmap;
        mDescriptorList = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == 0)
                dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.oscilloscope);
            else if (i == 1)
                dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.multimeter);
            else if (i == 2)
                dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.function_gen);
            else if (i == 3)
                dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard);
            else if (i == 4)
                dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mouse);
            else
                dbImgBitmap = null;

            dbImgMat = new Mat();

            Utils.bitmapToMat(dbImgBitmap, dbImgMat);

            Mat dbBwMat = new Mat();
            org.opencv.imgproc.Imgproc.cvtColor(dbImgMat, dbBwMat, Imgproc.COLOR_BGR2GRAY);

            mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);
            MatOfKeyPoint dbKeypoints = new MatOfKeyPoint();
            mFeatureDetector.detect(dbBwMat, dbKeypoints);

            Mat dbImgDesc = new Mat();

            DescriptorExtractor siftExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
            siftExtractor.compute(dbBwMat, dbKeypoints, dbImgDesc);
            mDescriptorList.add(dbImgDesc);
        }

        mDescriptorMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);

//        mDescriptorMatcher.add(mDescriptorList);

        /** Creating Camera intent **/
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = CameraSupport.getOutputMediaFileUri(CameraSupport.MEDIA_TYPE_IMAGE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);

//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);


    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu");

        mItemStream = menu.add("Stream");
        mItemSnapshot = menu.add("Snapshot");
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected");
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        else if (item == mItemSnapshot){
            Log.d(TAG, "Snapshot");
        }
        else if (item == mItemStream){
            Log.d(TAG, "Stream");
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Image captured and saved to fileUri specified in the Intent
//                Toast.makeText(this, "Image saved to:\n" +
//                        fileUri, Toast.LENGTH_LONG).show();

                setContentView(R.layout.content_main);

                mImageView = (ImageView) findViewById(R.id.imageView);
                mTextView = (TextView) findViewById(R.id.imageDescription);

                try {
                    // Get image bitmap
                    BitmapFactory.Options options = new BitmapFactory.Options();

                    //Bitmap is type ARGB_8888
                    Bitmap mBitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(fileUri), null, options);

                    mMat = new Mat();

                    Utils.bitmapToMat(mBitmap, mMat);

                    Mat newMat = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(mMat, newMat, Imgproc.COLOR_BGR2GRAY);

                    mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);

                    mMatofKeyPoint = new MatOfKeyPoint();

                    mFeatureDetector.detect(newMat, mMatofKeyPoint);

                    Mat imgDesc = new Mat();

                    DescriptorExtractor siftExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                    siftExtractor.compute(newMat, mMatofKeyPoint, imgDesc);

                    int bestMatchIdx = -1;
                    Mat bestMatchDesc = new Mat();
                    MatOfDMatch bestMatch = new MatOfDMatch();
                    float avgMinSizeofMatches = Float.MAX_VALUE;
                    for (int i = 0; i < mDescriptorList.size(); i++) {
                        float avgSizeOfMatches = 0;
                        MatOfDMatch curMatch = new MatOfDMatch();
                        mDescriptorMatcher.match(imgDesc, mDescriptorList.get(i), curMatch);
                        avgSizeOfMatches = matchHelp.average(curMatch);
                        if (avgSizeOfMatches <= avgMinSizeofMatches) {
                            avgMinSizeofMatches = avgSizeOfMatches;
                            bestMatchIdx = i;
                            bestMatch = curMatch;           //holds matches of best matched image
                            bestMatchDesc = mDescriptorList.get(i);
                        }
                    }


//                    List<DMatch> matchList = imgMatches.toList();
//                    float average = 0;
//                    float min = matchList.get(0).distance;
//                    float max = min;
//                    for(int i = 0; i<matchList.size(); i++)
//                    {
//                        float dist = matchList.get(i).distance;
//                        if(dist<min)
//                            min = dist;
//                        else if(dist>max)
//                            max = dist;
//                        average += dist;
//                    }
//                    average /= matchList.size();
//                    int numGoodMatch = 0;
//                    for(int i = 0; i<matchList.size(); i++)
//                    {
//                        if(matchList.get(i).distance < (min + 0.85*(average-min)))
//                            numGoodMatch++;
//                    }
//                    List<KeyPoint> dbImgKeyList = dbKeypoints.toList();
//                    List<KeyPoint> imgKeyList = mMatofKeyPoint.toList();
//
//                    double numDbKeys = (double)dbImgKeyList.size();
//                    double numImgKeys = (double)imgKeyList.size();
//
                    Mat imgMat = new Mat();
                    Bitmap dbImgBitmap = null;
                    String imageDescription = "The image is a ";

                    if (0 == bestMatchIdx) {
                        dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.oscilloscope);
                        imageDescription += "oscilloscope";
                    } else if (1 == bestMatchIdx) {
                        dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.multimeter);
                        imageDescription += "multimeter";
                    } else if (2 == bestMatchIdx) {
                        dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.function_gen);
                        imageDescription += "function generator";
                    } else if (3 == bestMatchIdx) {
                        dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.keyboard);
                        imageDescription += "keyboard";
                    } else if (4 == bestMatchIdx) {
                        dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.mouse);
                        imageDescription += "mouse";
                    }

                    Utils.bitmapToMat(dbImgBitmap, imgMat);

                    Mat grayImgMat = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(imgMat, grayImgMat, Imgproc.COLOR_BGR2GRAY);

                    MatOfKeyPoint dbKeypoints = new MatOfKeyPoint();
                    mFeatureDetector.detect(grayImgMat, dbKeypoints);
                    List<MatOfDMatch> threshMatch = new ArrayList<MatOfDMatch>();

                    mDescriptorMatcher.knnMatch(imgDesc, bestMatchDesc, threshMatch, 2);
                    int numMatch = threshMatch.size();
                    int goodMatch = 0;
                    for(int i = 0; i<numMatch; i++)
                    {
                        List<DMatch> neighbors = threshMatch.get(i).toList();
                        if(neighbors.size()>=2)
                        {
                            if((neighbors.get(0).distance/neighbors.get(1).distance < 0.8) ||
                                    (neighbors.get(1).distance/neighbors.get(0).distance < 0.8))
                            {
                                goodMatch++;
                            }
                        }
                    }

                    Mat outImg = new Mat();
                    Scalar blue = new Scalar(0,0,255);
                    Scalar green = new Scalar(0,255,0);
                    MatOfByte matchMask = new MatOfByte();
                    if(((float)goodMatch/(float)numMatch) > 0.35) {
                        Features2d.drawMatches(newMat, mMatofKeyPoint, grayImgMat, dbKeypoints, bestMatch, outImg, blue, green, matchMask, 0);
                        mTextView.setText(imageDescription);
                    }
                    else
                    {
                        Features2d.drawKeypoints(newMat, mMatofKeyPoint, outImg);
                    }

//                    Features2d.drawKeypoints(dbBwMat,dbKeypoints,outImg, blue, 0);

                    /**   Testing code **/
//                    Bitmap newBitmap = Bitmap.createBitmap(newMat.width(), newMat.height(), Bitmap.Config.ARGB_8888);
                    Bitmap newBitmap = Bitmap.createBitmap(outImg.width(), outImg.height(), Bitmap.Config.ARGB_8888);
//                    Utils.matToBitmap(newMat, newBitmap);
                    Utils.matToBitmap(outImg, newBitmap);
                    //Write Image to screen
                    mImageView.setImageBitmap(newBitmap);
//                    mImageView.setImageBitmap(dbImgBitmap);

                } catch (IOException e) {
                    e.printStackTrace();
                }

            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        } else if (requestCode == CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // Video captured and saved to fileUri specified in the Intent
                Toast.makeText(this, "Video saved to:\n" +
                        fileUri, Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the video capture
            } else {
                // Video capture failed, advise user
            }
        }
    }
}

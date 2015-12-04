package com.example.caleb.buildingdetector;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
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

import com.example.caleb.buildingdetector.CameraSupport;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.example.caleb.buildingdetector.R.layout.content_main;

public class MainActivity extends AppCompatActivity {

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private static final int CAPTURE_VIDEO_ACTIVITY_REQUEST_CODE = 200;
    private static final String TAG = "MainActivity";
    private Uri fileUri;

    private MenuItem mItemStream;
    private MenuItem mItemSnapshot;

    private Bitmap mBitmap;
    private Mat mMat;
    private Features2d mFeatures2d;
    private FeatureDetector mFeatureDetector;
    private MatOfKeyPoint mMatofKeyPoint;

    private ImageView mImageView;

    private BaseLoaderCallback mOpenCVCallBack = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status){
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.d(TAG, "OpenCV loaded Successfully");

                    // Creating Camera intent
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                    fileUri = CameraSupport.getOutputMediaFileUri(CameraSupport.MEDIA_TYPE_IMAGE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

                    startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                } break;

                case LoaderCallbackInterface.MARKET_ERROR:
                {
                    Log.d(TAG, "Google Play service is not accessible");
                    AlertDialog MarketErrorMessage = new AlertDialog.Builder(mAppContext).create();
                    MarketErrorMessage.setTitle("OpenCV Manager");
                    MarketErrorMessage.setMessage("Google Play service is not accessible!");
                    MarketErrorMessage.setCancelable(false);
                    MarketErrorMessage.setButton(AlertDialog.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ((Activity) mAppContext).finish();
                        }
                    });
                    MarketErrorMessage.show();
                } break;

                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");

        super.onCreate(savedInstanceState);

        Log.d(TAG, "Load OpenCV Library");
        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_8, this, mOpenCVCallBack)) {
            Log.e(TAG, "Failed to load OpenCV library");
        }

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

                try {
                    // Get image bitmap
                    BitmapFactory.Options options = new BitmapFactory.Options();
//                    options.outHeight = mImageView.getHeight();
//                    options.outWidth = mImageView.getWidth();

                    //Bitmap is type ARGB_8888
                    Bitmap mBitmap = BitmapFactory.decodeStream(this.getContentResolver().openInputStream(fileUri), null, options);
                    Bitmap dbImgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.oscilloscope);

                    mMat = new Mat();
                    Mat dbImgMat = new Mat();

                    Utils.bitmapToMat(mBitmap, mMat);
                    Utils.bitmapToMat(dbImgBitmap, dbImgMat);

                    Mat newMat = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(mMat, newMat, Imgproc.COLOR_BGR2GRAY);
                    Mat dbBwMat = new Mat();
                    org.opencv.imgproc.Imgproc.cvtColor(dbImgMat, dbBwMat, Imgproc.COLOR_BGR2GRAY);

                    mFeatureDetector = FeatureDetector.create(FeatureDetector.ORB);

                    mMatofKeyPoint = new MatOfKeyPoint();
                    MatOfKeyPoint dbKeypoints = new MatOfKeyPoint();

                    mFeatureDetector.detect(newMat, mMatofKeyPoint);
                    mFeatureDetector.detect(dbBwMat, dbKeypoints);

                    Mat imgDesc = new Mat();
                    Mat dbImgDesc = new Mat();

                    DescriptorExtractor siftExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
                    siftExtractor.compute(newMat, mMatofKeyPoint, imgDesc);
                    siftExtractor.compute(dbBwMat, dbKeypoints, dbImgDesc);

                    DescriptorMatcher siftMatcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_L1);

                    MatOfDMatch imgMatches = new MatOfDMatch();
                    siftMatcher.match(imgDesc, dbImgDesc, imgMatches);

                    List<DMatch> matchList = imgMatches.toList();
                    float average = 0;
                    float min = matchList.get(0).distance;
                    float max = min;
                    for(int i = 0; i<matchList.size(); i++)
                    {
                        float dist = matchList.get(i).distance;
                        if(dist<min)
                            min = dist;
                        else if(dist>max)
                            max = dist;
                        average += dist;
                    }
                    average /= matchList.size();
                    int numGoodMatch = 0;
                    for(int i = 0; i<matchList.size(); i++)
                    {
                        if(matchList.get(i).distance < (min + 0.85*(average-min)))
                            numGoodMatch++;
                    }
                    List<KeyPoint> dbImgKeyList = dbKeypoints.toList();
                    List<KeyPoint> imgKeyList = mMatofKeyPoint.toList();

                    double numDbKeys = (double)dbImgKeyList.size();
                    double numImgKeys = (double)imgKeyList.size();

                    Mat outImg = new Mat();
                    Scalar blue = new Scalar(0,0,255);
                    Scalar green = new Scalar(0,255,0);
                    MatOfByte matchMask = new MatOfByte();
                    if(((double)numGoodMatch/numDbKeys > 0.35) || ((double)numGoodMatch/numImgKeys > 0.35))
                        Features2d.drawMatches(newMat, mMatofKeyPoint, dbBwMat, dbKeypoints, imgMatches, outImg, blue, green, matchMask, 0);
                    else
                    {
                        MatOfDMatch noMatch = new MatOfDMatch();
                        Features2d.drawMatches(newMat, mMatofKeyPoint, dbBwMat, dbKeypoints, noMatch, outImg, blue, green, matchMask, 0);
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

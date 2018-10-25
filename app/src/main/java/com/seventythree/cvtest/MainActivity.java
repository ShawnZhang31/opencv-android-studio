package com.seventythree.cvtest;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String                             TAG = "MainActivity";
    private CameraBridgeViewBase                mCameraBridgeViewBase;
    private BaseLoaderCallback _baseLoaderCallback = new BaseLoaderCallback(this)
    {
        @Override
        public void onManagerConnected(int status)
        {
            switch (status)
            {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV库加载成功");
                    // opencv初始化之后加载ndk打包的模块
                    System.loadLibrary("native-lib");
                    mCameraBridgeViewBase.enableView();
                }break;
                default:
                {
                    super.onManagerConnected(status);
                }break;
            }
        }
    };

//    // Used to load the 'native-lib' library on application startup.
//    static {
//        System.loadLibrary("native-lib");
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 设置windows保持常量
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        // 在Android 6.0 +上可以
        ActivityCompat.requestPermissions(MainActivity.this,
                new  String[]{Manifest.permission.CAMERA},
                1);

        mCameraBridgeViewBase = (CameraBridgeViewBase) findViewById(R.id.camera_surface);
        mCameraBridgeViewBase.setVisibility(SurfaceView.VISIBLE);
        mCameraBridgeViewBase.setCvCameraViewListener(this);

    }

    /**
     * 重写onPause
     */
    @Override
    protected void onPause() {
        super.onPause();
        disableCamera();
    }

    /**
     * 重写onResume
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug())
        {
            Log.d(TAG, "应用内无法找到OpenCV库，使用OpenCV Manager进行初始化!");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, _baseLoaderCallback);
        }
        else
        {
            Log.d(TAG, "使用应用内的OpenCV库进行初始化");
            _baseLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    /**
     * 重修onDestroy
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        disableCamera();
    }

    /**
     * 摄像头授权回调
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode)
        {
            case 1:
            {
                // 如果用户取消授权，则result数组为空
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // 授权成功可以做一些你爱做的事了ˇˍˇ
                }
                else
                {
                    // 授权失败了
                    Toast.makeText(MainActivity.this, "一定要授权才能使用呀", Toast.LENGTH_SHORT).show();
                }
                return;
            }
            // 其他的case大家根据自己的实际需求写吧
        }
    }


    /**
     * 禁用摄像头
     */
    public void disableCamera()
    {
        if (mCameraBridgeViewBase !=null)
            mCameraBridgeViewBase.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame)
    {
        Mat matGray = inputFrame.gray();
        CanndyDetect(matGray.getNativeObjAddr());
        return matGray;
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String stringFromJNI();

    /**
     * 对图像进行Canndy边缘检测
     * @param matAddr 灰度图像的Mat地址
     */
    public native void  CanndyDetect(long matAddr);
}

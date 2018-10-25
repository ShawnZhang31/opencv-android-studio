# OpenCV Android Studio
在Android Studio工程中使用Native的方式集成OpenCV
## 环境
- Android Studio 3.2.1
- NDK R16C
- OpenCV 3.3.0
- CMAKE 3.6.4
## 注意
1. 关于NDK的安装  
 在国内由于网络的原因有时候通过Android Studio下载NDK的时候会造成下载不得部分文件缺失，这样NDK安装时候成功就不知道，在编译的时候回出现莫名其妙的错误(**我就被这种错误深深的坑过**)。所以安装NDK最好的办法是在Android官网上下载完整的安装包再解压到自己的电脑上。并在Android Studio中设置NDK的目录。
## 工程配置
1. 更新Android SDK并安装NDK
2. 下载OpenCV SDK的Android版本   
 去OpenCV官网下载OpenCV的Android版本:https://opencv.org/
3. 创建一个新的Android Studio工程  
   - 选择**Include C++ Support**
   - 选择一个Empty Activity
   - 在C++ Support中勾选 **-fexception**和 **-frtti**
4. 导入OpenCV Library Module   
   -  New -> Import Module
   -  选择$(OpenCV for android SDK 所在目录)/sdk/java
   - 一路next即可
5. 修改OpenCV Library Module的build.gradle和你的app的build.gradle一致   
例如：我的工程的的app的build.gradle的如下:
```gradle
 compileSdkVersion 28
    defaultConfig {
        minSdkVersion 18
        targetSdkVersion 28
    }
```
则应该将opencv library的build.gradle也修改为与app的build.gradle一致。下面是我修改之后
```gradle
android {
    compileSdkVersion 28 //与app的build.gradle一致
//    buildToolsVersion "28.0.3" 这一行可以注视点

    defaultConfig {
        minSdkVersion 18 //与app的build.gradle一致
        targetSdkVersion 28 //与app的build.gradle一致
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.txt'
        }
    }
}
```
6. 在 app module dependency中添加OpenCV Module  
File -> Project structure -> Module app -> Dependencies tab -> New module dependency -> choose OpenCV library module
7. 在工程的app/src/main目录下创建名为**jniLibs**的文件夹，并将$(OpenCV for android SDK 所在目录)/sdk/native/libs下面的文件夹拷贝到该文件夹路径下   
为了是最终打包的APK尽可能的小，可以只拷贝对应ABI的文件.
8. 将(OpenCV for android SDK 所在目录)/sdk/native/jni/include文件夹拷贝到app/src/main/cpp文件下    
   - 网上也有人说不用拷贝，在 **CMakeLists.txt** 设置就行了，但是我实际这样操作话会出现莫名其妙的错误，目前还不知道错误原因，等弄明白了再更新
   - 如果在创建Android Studio工程的时候选择**Include C++ Support**，则在app/src/main文件夹下面会自动出现cpp文件夹。如果是自己手工添加C++ Support的话，存放C++文件的文件夹是自己定义的，总之就是将include文件放在你存放C++文件的文件夹下.
9. 配置**CMakeLists.txt**文件  
 配置**CMakeLists.txt**有两种方法，选择一种适合你自己的   
   - 第一种：这种方式比较简单，一般不会出现什么错误，但是打包的*.so文件会稍微大一点，初学者推荐始终这种配置方式
      ```CMake
      cmake_minimum_required(VERSION 3.4.1)

      # OpenCV 配置
      include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
      add_library( lib_opencv SHARED IMPORTED )
      set_target_properties(lib_opencv PROPERTIES IMPORTED_LOCATION ${CMAKE_CURRENT_SOURCE_DIR}/src/main/jniLibs/${ANDROID_ABI}/libopencv_java3.so)

      # Creates and names a library, sets it as either STATIC
      # or SHARED, and provides the relative paths to its source code.
      # You can define multiple libraries, and CMake builds it for you.
      # Gradle automatically packages shared libraries with your APK.
      
      add_library( # Sets the name of the library.
        native-lib

        # Sets the library as a shared library.
        SHARED

        # Provides a relative path to your source file(s).
        # Associated headers in the same location as their source
        # file are automatically included.
        src/main/cpp/native-lib.cpp )



            # Searches for a specified prebuilt library and stores the path as a
            # variable. Because system libraries are included in the search path by
            # default, you only need to specify the name of the public NDK library
            # you want to add. CMake verifies that the library exists before
            # completing its build.

            find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log )

      # Specifies libraries CMake should link to your target library. You
      # can link multiple libraries, such as libraries you define in the
      # build script, prebuilt third-party libraries, or system libraries.
      
      target_link_libraries( # Specifies the target library.
        native-lib

        # OpenCV lib
        lib_opencv

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib} )
      ```
   - 第二种：将opencv中的*.so文件一个一个指定打包。这种方法可以最大限度的减少打包的*.so文件的大小。但是对于OpenCV有一定的要求，初学者不建议采用这种配置方式。
      ```CMake
      # NDK的最小版本
      cmake_minimum_required(VERSION 3.4.1)
      
      # 显示CMake Build的输出信息
      set(CMAKE_VERBOSE_MAKEFILE on)
      
      set(libs "${CMAKE_SOURCE_DIR}/src/main/jniLibs")
      include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include)
      
      add_library(libopencv_java3 SHARED IMPORTED )
      set_target_properties(libopencv_java3 PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_java3.so")
      
      add_library(libopencv_calib3d STATIC IMPORTED )
      set_target_properties(libopencv_calib3d PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_calib3d.a")
      
      add_library(libopencv_core STATIC IMPORTED )
      set_target_properties(libopencv_core PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_core.a")
      
      add_library(libopencv_features2d STATIC IMPORTED )
      set_target_properties(libopencv_features2d PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_features2d.a")
      
      add_library(libopencv_flann STATIC IMPORTED )
      set_target_properties(libopencv_flann PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_flann.a")
      
      add_library(libopencv_highgui STATIC IMPORTED )
      set_target_properties(libopencv_highgui PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_highgui.a")
      
      add_library(libopencv_imgcodecs STATIC IMPORTED )
      set_target_properties(libopencv_imgcodecs PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_imgcodecs.a")
      
      add_library(libopencv_imgproc STATIC IMPORTED )
      set_target_properties(libopencv_imgproc PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_imgproc.a")
      
      add_library(libopencv_ml STATIC IMPORTED )
      set_target_properties(libopencv_ml PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_ml.a")
      
      add_library(libopencv_objdetect STATIC IMPORTED )
      set_target_properties(libopencv_objdetect PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_objdetect.a")
      
      add_library(libopencv_photo STATIC IMPORTED )
      set_target_properties(libopencv_photo PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_photo.a")
      
      add_library(libopencv_shape STATIC IMPORTED )
      set_target_properties(libopencv_shape PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_shape.a")
      
      add_library(libopencv_stitching STATIC IMPORTED )
      set_target_properties(libopencv_stitching PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_stitching.a")
      
      add_library(libopencv_superres STATIC IMPORTED )
      set_target_properties(libopencv_superres PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_superres.a")
      
      add_library(libopencv_video STATIC IMPORTED )
      set_target_properties(libopencv_video PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_video.a")
      
      add_library(libopencv_videoio STATIC IMPORTED )
      set_target_properties(libopencv_videoio PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_videoio.a")
      
      add_library(libopencv_videostab STATIC IMPORTED )
      set_target_properties(libopencv_videostab PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_videostab.a")
      
      add_library(libopencv_ts STATIC IMPORTED )
      set_target_properties(libopencv_ts PROPERTIES
          IMPORTED_LOCATION "${libs}/${ANDROID_ABI}/libopencv_ts.a")
      
      
      add_library( # Sets the name of the library.
                   native-lib
      
                   # Sets the library as a shared library.
                   SHARED
      
                   # Provides a relative path to your source file(s).
                   # Associated headers in the same location as their source
                   # file are automatically included.
                   src/main/cpp/native-lib.cpp )
      
      find_library( # Sets the name of the path variable.
                    log-lib
      
                    # Specifies the name of the NDK library that
                    # you want CMake to locate.
                    log)
      
      target_link_libraries(native-lib android log
          libopencv_java3 libopencv_calib3d libopencv_core libopencv_features2d libopencv_flann libopencv_highgui libopencv_imgcodecs
          libopencv_imgproc libopencv_ml libopencv_objdetect libopencv_photo       libopencv_shape libopencv_stitching libopencv_superres
          libopencv_video libopencv_videoio libopencv_videostab
          ${log-lib}
          )
      ``` 
10. 如何指定编译的ABI可以在app的build.gradle中指定   
```Gradle
apply plugin: 'com.android.application'

android {
    compileSdkVersion 28
    defaultConfig {
        applicationId "com.xxxxx.xxxx"
        minSdkVersion 18
        targetSdkVersion 28
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
        externalNativeBuild {
            cmake {
                cppFlags "-std=c++11 -frtti -fexceptions" //CMake编译支持
                abiFilters  'armeabi-v7a', 'x86', 'x86_64', 'arm64-v8a', 'armeabi' //指定需要编译的ABI
            }
        }
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
    externalNativeBuild {
        cmake {
            path "CMakeLists.txt"
        }
    }
    sourceSets{
        main{
            jniLibs.srcDirs = ['src/main/jniLibs']
        }
    }
}

dependencies {
    implementation fileTree(include: ['*.jar'], dir: 'libs')
    implementation 'com.android.support:appcompat-v7:28.0.0'
    implementation 'com.android.support.constraint:constraint-layout:1.1.3'
    testImplementation 'junit:junit:4.12'
    androidTestImplementation 'com.android.support.test:runner:1.0.2'
    androidTestImplementation 'com.android.support.test.espresso:espresso-core:3.0.2'
    implementation project(':openCVLibrary330')
}
```
## Demo检查
要检查我们是否配置成功，可以先Build一下，如果一切正常则配置成功。否则根据提示修正错误即可。如果一切正常，我们来做一个从工程：打开摄像头并使用OpenCV提供Canndy算子来检测摄像头画面中对象轮廓。效果图如下：   
[效果图](README_Files/device.png)   
1. 为app的AndroidManifest.xml文件添加摄像头访问权限  
   ```xml
    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-feature android:name="android.hardware.camera"/>
    <uses-feature android:name="android.hardware.camera.autofocus"/>
    <uses-feature android:name="android.hardware.camera.front"/>
    <uses-feature android:name="android.hardware.camera.front.autofocus"/>
   ```
2. 修改activity_main.xml文件添加摄像头画面SurfaceView  
   ```xml
   <?xml version="1.0" encoding="utf-8"?>
   <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    tools:context=".MainActivity">

    <org.opencv.android.JavaCameraView
        android:id="@+id/camera_surface"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
   </RelativeLayout>
   ```
3. 修改app/src/main/cpp下的native-lib.cpp文件，添加Canndy处理方法
   ```cpp
   #include <jni.h>
   #include <string>
   #include <opencv2/opencv.hpp>
   #include <opencv2/imgproc/imgproc.hpp>
   #include <opencv2/features2d/features2d.hpp>

   using namespace std;
   using namespace cv;

   extern "C" JNIEXPORT jstring JNICALL Java_com_seventythree_cvtest_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
   }

   /**
    * @brief 使用Canndy算子检测图像中的对象轮廓
    * @param matAddrGray, Mat图像的内存地址
    */
   extern "C" JNIEXPORT void JNICALL Java_com_seventythree_cvtest_MainActivity_CanndyDetect(
        JNIEnv *env,
        jobject thiz,
        jlong matAddrGray) {
       Mat &grayMat = *(Mat *) matAddrGray;
       Canny(grayMat, grayMat, 50, 100);
   }
   ```
4. 修改MainActivity.java
```Java
package com.你自己的包名;

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

```
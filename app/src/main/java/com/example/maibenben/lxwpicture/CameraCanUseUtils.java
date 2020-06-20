package com.example.maibenben.lxwpicture;

import android.content.Intent;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;

/**
 * Created by MAIBENBEN on 2020/6/7.
 */
public class CameraCanUseUtils {

    /**
     * 测试当前摄像头能否被使用
     * @return
     */
    public static boolean isCameraCanUse() {
        boolean canUse = true;//
        Camera mCamera = null;
        try {
            mCamera = Camera.open(0);
            mCamera.setDisplayOrientation(90);
        } catch (Exception e) {
            canUse = false;
        }
        if (canUse) {
            mCamera.release();
            mCamera = null;
        }
        //Timber.v("isCameraCanuse="+canUse);
        return canUse;
    }


}
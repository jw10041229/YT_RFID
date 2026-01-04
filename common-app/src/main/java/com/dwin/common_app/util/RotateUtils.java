package com.dwin.common_app.util;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileOutputStream;
import java.io.IOException;

public class RotateUtils {

    private static final String TAG = "RotateUtils";

    // 读取系统文件中的当前旋转角度
    public static String getCurrentRotationFromFile() {
        String rotation = "0"; // 默认值
        try (BufferedReader reader = new BufferedReader(new FileReader("/sys/storage/orientation"))) {
            String fileValue = reader.readLine(); // 读取文件
            Log.d(TAG, "Raw rotation value read from file: " + fileValue);

            if (fileValue.trim().isEmpty() || "null".equals(fileValue.trim())) {
                Log.w(TAG, "Rotation value is empty or null, using default 270");
                rotation = "0";
            } else {
                rotation = fileValue.trim();
                Log.d(TAG, "Rotation value read from file: " + rotation);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading /sys/storage/orientation", e);
        }
        Log.d(TAG, "Final rotation value: " + rotation);  // 确保最终返回的值
        return rotation;
    }

    // 写入旋转值到 /sys/storage/orientation 文件
    public static boolean writeOrientationFile(String content) {
        try (FileOutputStream fos = new FileOutputStream("/sys/storage/orientation")) {
            fos.write(content.getBytes());
            fos.flush();
            Log.d(TAG, "Successfully wrote to /sys/storage/orientation: " + content);
            return true;
        } catch (IOException e) {
            Log.e(TAG, "Failed to write to /sys/storage/orientation", e);
            return false;
        }
    }

    // 获取旋转角度对应的位置
    public static int getRotationPosition(String rotation, String[] mRotate) {
        for (int i = 0; i < mRotate.length; i++) {
            if (mRotate[i].equals(rotation)) {
                return i;
            }
        }
        Log.w(TAG, "Rotation value " + rotation + " not found, returning default position 0");
        return 0; // 默认返回 0
    }

    // 格式化旋转角度
    public static String formatRotation(String rotation) {
        return rotation.replace("R", "").trim();
    }

    // 根据新的旋转角度设置对应的值
    public static String getOrientationValue(String rotation) {
        switch (rotation) {
            case "0":
                return "R0";
            case "90":
                return "R90";
            case "180":
                return "R180";
            case "270":
                return "R270";
            default:
                Log.w(TAG, "Unexpected rotation value: " + rotation);
                return "R0";
        }
    }
}

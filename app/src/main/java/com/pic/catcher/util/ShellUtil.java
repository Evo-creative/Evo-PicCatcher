package com.pic.catcher.util;

import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;

import rikka.shizuku.Shizuku;

public class ShellUtil {
    private static final String TAG = "ShellUtil";

    public static boolean isShizukuAvailable() {
        try {
            return Shizuku.pingBinder();
        } catch (Throwable e) {
            return false;
        }
    }

    public static boolean hasShizukuPermission() {
        if (isShizukuAvailable()) {
            try {
                return Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
            } catch (Throwable e) {
                return false;
            }
        }
        return false;
    }

    public static boolean isSui() {
        try {
            if (isShizukuAvailable()) {
                // 如果已授权，UID 为 0 说明是 Sui
                if (hasShizukuPermission()) {
                    return Shizuku.getUid() == 0;
                }
                // 如果未授权，Sui 的版本号通常很大 (例如 12000+)
                // 而原生 Shizuku 版本号目前在 13 左右
                return Shizuku.getVersion() >= 1000;
            }
        } catch (Throwable ignored) {}
        return false;
    }

    public static boolean hasRootPermission() {
        try {
            Process process = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("id\n");
            os.writeBytes("exit\n");
            os.flush();
            int exitCode = process.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    public static ShellResult runCommand(String command) {
        if (isShizukuAvailable() && hasShizukuPermission()) {
            return runShizukuCommand(command);
        } else {
            return runSuCommand(command);
        }
    }

    private static ShellResult runShizukuCommand(String command) {
        try {
            // 由于 Shizuku.newProcess 在 SDK 13 是 private 的，我们需要通过反射调用 IShizukuService
            java.lang.reflect.Method getServiceMethod = Shizuku.class.getDeclaredMethod("requireService");
            getServiceMethod.setAccessible(true);
            Object service = getServiceMethod.invoke(null);
            
            if (service == null) return new ShellResult(-1, "", "IShizukuService is null");

            java.lang.reflect.Method newProcessMethod = service.getClass().getMethod(
                    "newProcess", String[].class, String[].class, String.class);
            
            // 调用服务端方法，返回远程进程的 IBinder
            android.os.IBinder binder = (android.os.IBinder) newProcessMethod.invoke(service, new String[]{"sh", "-c", command}, null, null);
            
            // 使用反射实例化包级私有的 ShizukuRemoteProcess
            java.lang.reflect.Constructor<?> constructor = Class.forName("rikka.shizuku.ShizukuRemoteProcess")
                    .getDeclaredConstructor(android.os.IBinder.class);
            constructor.setAccessible(true);
            Process process = (Process) constructor.newInstance(binder);

            return readProcess(process);
        } catch (Exception e) {
            Log.e(TAG, "Shizuku/Sui command failed: " + command, e);
            return new ShellResult(-1, "", e.getMessage());
        }
    }

    private static ShellResult runSuCommand(String command) {
        Process process = null;
        DataOutputStream os = null;
        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(command + "\n");
            os.writeBytes("exit\n");
            os.flush();
            return readProcess(process);
        } catch (Exception e) {
            Log.e(TAG, "SU command failed: " + command, e);
            return new ShellResult(-1, "", e.getMessage());
        } finally {
            try {
                if (os != null) os.close();
                if (process != null) process.destroy();
            } catch (Exception ignored) {}
        }
    }

    private static ShellResult readProcess(Process process) throws Exception {
        BufferedReader successReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();
        String line;
        
        while ((line = successReader.readLine()) != null) {
            successMsg.append(line).append("\n");
        }
        while ((line = errorReader.readLine()) != null) {
            errorMsg.append(line).append("\n");
        }
        
        int exitCode = process.waitFor();
        return new ShellResult(exitCode, successMsg.toString().trim(), errorMsg.toString().trim());
    }

    public static class ShellResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;

        public ShellResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
        }

        public boolean isSuccess() {
            return exitCode == 0;
        }
    }
}

package com.pic.catcher.util;

import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class RootUtil {
    private static final String TAG = "RootUtil";
    private static Boolean cachedRootStatus = null;
    
    private static Process shellProcess = null;
    private static BufferedWriter shellWriter = null;
    private static ShellOutputReader outputReader = null;
    private static final ReentrantLock shellLock = new ReentrantLock();

    public enum Status {
        UNKNOWN, AUTHORIZED, DENIED, NOT_FOUND
    }

    public static class CheckResult {
        public final Status status;
        public final String suName;
        public CheckResult(Status status, String suName) {
            this.status = status;
            this.suName = suName;
        }
    }

    /**
     * 启动/检查 Root 权限。长连接模式，通常只在启动时触发一次 Magisk Toast。
     */
    public static CheckResult checkRootStatus() {
        boolean authorized = ensureShellSession();
        cachedRootStatus = authorized;
        // 关键：在建立 Shell 会话后探测，结果更准
        String suName = probeSuManagerName();
        return new CheckResult(authorized ? Status.AUTHORIZED : Status.DENIED, suName);
    }

    private static boolean ensureShellSession() {
        if (shellProcess != null && shellProcess.isAlive()) return true;
        
        shellLock.lock();
        try {
            if (shellProcess != null && shellProcess.isAlive()) return true;
            closeShellSession(); 
            
            Log.d(TAG, "Starting persistent Root session...");
            shellProcess = Runtime.getRuntime().exec("su");
            shellWriter = new BufferedWriter(new OutputStreamWriter(shellProcess.getOutputStream()));
            outputReader = new ShellOutputReader(shellProcess.getInputStream());
            outputReader.start();
            
            // 验证授权
            String marker = "AUTH_CHECK_" + System.currentTimeMillis();
            shellWriter.write("id\necho " + marker + "\n");
            shellWriter.flush();
            
            String result = outputReader.readUntil(marker, 25000);
            return result != null && result.contains("uid=0");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start su session", e);
            closeShellSession();
            return false;
        } finally {
            shellLock.unlock();
        }
    }

    private static void closeShellSession() {
        try {
            if (shellWriter != null) {
                shellWriter.write("exit\n");
                shellWriter.flush();
                shellWriter.close();
            }
            if (outputReader != null) outputReader.stop();
            if (shellProcess != null) shellProcess.destroy();
        } catch (Exception ignored) {}
        shellWriter = null; outputReader = null; shellProcess = null;
    }

    /**
     * 执行命令并获取结果 - 复用持久会话，不会引起重复的 Magisk Toast 提示
     */
    public static RootResult runCommand(String command) {
        if (!ensureShellSession()) return new RootResult(-1, "", "SU unavailable");
        
        shellLock.lock();
        try {
            String marker = "CMD_END_" + System.currentTimeMillis();
            // 分别打印标记和退出码，确保解析准确
            shellWriter.write(command + "\necho " + marker + "\necho $?\n");
            shellWriter.flush();
            
            String output = outputReader.readUntil(marker, 45000);
            if (output == null) return new RootResult(-1, "", "Timeout");
            
            // Marker 后的下一行即为退出码
            String exitCodeLine = outputReader.readLine(2000);
            int exitCode = -1;
            if (exitCodeLine != null) {
                try {
                    exitCode = Integer.parseInt(exitCodeLine.trim());
                } catch (Exception ignored) {}
            }
            
            return new RootResult(exitCode, output.trim(), "");
        } catch (Exception e) {
            Log.e(TAG, "runCommand error", e);
            closeShellSession();
            return new RootResult(-1, "", e.getMessage());
        } finally {
            shellLock.unlock();
        }
    }

    public static String probeSuManagerName() {
        // 1. 尝试文件路径探测 (最快，无需权限)
        if (new java.io.File("/data/adb/magisk").exists()) return "Magisk";
        if (new java.io.File("/data/adb/ksu").exists()) return "KernelSU";
        if (new java.io.File("/data/adb/apatch").exists()) return "APatch";

        // 2. 尝试通过 su 命令探测
        try {
            Process p = Runtime.getRuntime().exec("which su");
            String path = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream())).readLine();
            if (path != null) {
                if (path.contains("magisk")) return "Magisk";
                if (path.contains("ksu")) return "KernelSU";
                if (path.contains("apatch")) return "APatch";
            }
        } catch (Exception ignored) {}

        // 3. 如果已有会话，通过 su -v 获取版本信息
        if (hasRootPermission()) {
            RootResult result = runCommand("su -v");
            if (result.isSuccess()) {
                String out = result.stdout.toUpperCase();
                if (out.contains("MAGISK")) return "Magisk";
                if (out.contains("KSU") || out.contains("KERNELSU")) return "KernelSU";
                if (out.contains("APATCH")) return "APatch";
            }
        }

        return "Root";
    }

    public static boolean hasRootPermission() {
        return cachedRootStatus != null && cachedRootStatus;
    }

    private static class ShellOutputReader {
        private final BufferedReader reader;
        private final StringBuilder buffer = new StringBuilder();
        private Thread thread;
        private volatile boolean running = true;

        public ShellOutputReader(java.io.InputStream is) {
            this.reader = new BufferedReader(new InputStreamReader(is));
        }

        public void start() {
            thread = new Thread(() -> {
                try {
                    String line;
                    while (running && (line = reader.readLine()) != null) {
                        synchronized (buffer) {
                            buffer.append(line).append("\n");
                            buffer.notifyAll();
                        }
                    }
                } catch (IOException ignored) {}
            }, "ShellReader");
            thread.start();
        }

        public void stop() {
            running = false;
            if (thread != null) thread.interrupt();
        }

        public String readUntil(String marker, long timeoutMillis) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            synchronized (buffer) {
                while (System.currentTimeMillis() < deadline) {
                    int index = buffer.indexOf(marker);
                    if (index != -1) {
                        String result = buffer.substring(0, index);
                        buffer.delete(0, index + marker.length());
                        if (buffer.length() > 0 && buffer.charAt(0) == '\n') buffer.deleteCharAt(0);
                        return result;
                    }
                    try {
                        buffer.wait(500);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
            return null;
        }

        public String readLine(long timeoutMillis) {
            long deadline = System.currentTimeMillis() + timeoutMillis;
            synchronized (buffer) {
                while (System.currentTimeMillis() < deadline) {
                    int index = buffer.indexOf("\n");
                    if (index != -1) {
                        String line = buffer.substring(0, index);
                        buffer.delete(0, index + 1);
                        return line;
                    }
                    try {
                        buffer.wait(200);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
            return null;
        }
    }

    public static class RootResult {
        public final int exitCode;
        public final String stdout;
        public final String stderr;
        public RootResult(int exitCode, String stdout, String stderr) {
            this.exitCode = exitCode; this.stdout = stdout; this.stderr = stderr;
        }
        public boolean isSuccess() { return exitCode == 0; }
    }
}

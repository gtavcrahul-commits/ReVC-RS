package com.revc.game;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.libsdl.app.SDLActivity;

import com.revc.game.core.REVC;

import java.io.File;

public class MainActivity extends SDLActivity {

    private static final String TAG = "reVC";
    private static final int REQ_STORAGE = 1001;

    @Override
    protected String[] getLibraries() {
        // Order matters: dependencies first, main game library last.
        // The last entry (reVC) determines getMainSharedObject() -> libreVC.so
        return new String[] {
                "SDL2",
                "openal",
                "mpg123",
                "reVC"
        };
    }

    @Override
    protected String getMainFunction() {
        return "SDL_main";
    }

    @Override
    public void loadLibraries() {
        super.loadLibraries();
        // After all .so files are loaded, point the native game at its asset directory.
        // We use this app's external files dir, which is accessible without
        // MANAGE_EXTERNAL_STORAGE: /storage/emulated/0/Android/data/com.revc.game/files/
        try {
            File dir = getExternalFilesDir(null);
            if (dir == null) {
                dir = getFilesDir();
            }
            String path = dir.getAbsolutePath();
            if (!path.endsWith("/")) path += "/";
            Log.i(TAG, "Setting game storage root: " + path);
            REVC.setGamePath(path);
        } catch (Throwable t) {
            Log.e(TAG, "setGamePath failed", t);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Best-effort runtime permission request for legacy storage on
        // Android <= 12. On Android 13+ the user can also grant
        // MANAGE_EXTERNAL_STORAGE via Settings if they want to use /sdcard
        // directly; we use the per-app external files dir which doesn't
        // need any permission at all on modern Android.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
            String[] perms = new String[] {
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            };
            boolean needRequest = false;
            for (String p : perms) {
                if (ContextCompat.checkSelfPermission(this, p)
                        != PackageManager.PERMISSION_GRANTED) {
                    needRequest = true;
                    break;
                }
            }
            if (needRequest) {
                ActivityCompat.requestPermissions(this, perms, REQ_STORAGE);
            }
        }

        super.onCreate(savedInstanceState);
    }
}

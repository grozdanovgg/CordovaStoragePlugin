package org.apache.cordova.sdcard;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import android.app.ActivityManager;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Path;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;

import static android.support.v4.content.ContextCompat.startActivity;

public class SdCard extends CordovaPlugin {
  Context context = null;

  public SdCard() {

  }

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    try {
      if (action.equals("get")) {
        List<Path> result = get();
        Gson gson = new Gson();
        String json = gson.toJson(result);
        callbackContext.success(json);
        return true;
      } else if (action.equals("getSpace")) {
        HashMap<String, List<Long>> result = getSpace();
        Gson gson = new Gson();
        String json = gson.toJson(result);
        callbackContext.success(json);
        return true;
      }
      return false;
    } catch (Exception ex) {
      callbackContext.error(ex.getMessage());
    }
    return true;
  }

  public class Path {
    public  String path;

    public Path(String _path){
      path = _path;
    }
  }

  private List<Path> getStorageInfo(){
    List<Path> pathsToReturn = new ArrayList<Path>();

    Context context = this.cordova.getActivity().getApplicationContext();

    File[] allDirectories = context.getExternalFilesDirs(null);
    File internalDirectory = context.getFilesDir();
    File externalStorage = new File("");
    File storage = new File("/storage");
    final List<String> out = new ArrayList<String>();

    String reg = "(?i).*(vold|media_rw).*(sdcard|vfat|ntfs|exfat|fat32|ext3|ext4).*rw.*";
    String s = "";
    try {
      final Process process = new ProcessBuilder().command("mount").redirectErrorStream(true).start();
      process.waitFor();
      final InputStream is = process.getInputStream();
      final byte[] buffer = new byte[1024];
      while (is.read(buffer) != -1) {
        s = s + new String(buffer);
      }
      is.close();

      final String[] lines = s.split("\n");
      for (String line : lines) {
        if (!line.toLowerCase(Locale.US).contains("asec")) {
          if (line.matches(reg)) {
            String[] parts = line.split(" ");
            for (String part : parts) {
              if (part.startsWith("/"))
                if (!part.toLowerCase(Locale.US).contains("vold"))
                  out.add(part);
            }
          }
        }
      }

      if(storage.exists()) {
        File[] files = storage.listFiles();

        for (File file : files) {
          if (file.exists() &&
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
            Environment.isExternalStorageRemovable(file)) {
            externalStorage = file;
            break;
          }
        }
      }

      pathsToReturn.add(new Path(internalDirectory.getCanonicalPath()));

      if(out.size() == 1 && new File(out.get(0)).canWrite()) {
        pathsToReturn.add(new Path(out.get(0) + "/Android/data/" + context.getPackageName() + "/files"));
      } if (out.size() > 1) {

        Boolean pathsAreOk = true;
        for(String path : out){
          for(String pathAll : out){
            if(!path.equals(pathAll) || !(new File(path).canWrite()) ){
              pathsAreOk = false;
            }
          }
        }
        if(pathsAreOk){
          pathsToReturn.add(new Path(out.get(0) + "/Android/data/" + context.getPackageName() + "/files"));
        }

      }
      if (pathsToReturn.size() < 2 &&
        allDirectories.length > 1 &&
        (new File(allDirectories[1].getCanonicalPath()).canWrite())){
        pathsToReturn.add(new Path(allDirectories[1].getCanonicalPath()));
      }
      if (pathsToReturn.size() < 2 &&
        externalStorage.getCanonicalPath().length() > 0 &&
        (new File(externalStorage.getCanonicalPath()).canWrite())){
        pathsToReturn.add(new Path(externalStorage.getCanonicalPath() + "/Android/data/" + context.getPackageName() + "/files"));
      }

    } catch (final Exception e) {
      try {
        pathsToReturn.add(new Path(internalDirectory.getCanonicalPath()));
        return pathsToReturn;
      } catch (IOException e1) {
        return pathsToReturn;
      }
    }

    return pathsToReturn;
  }
  public List<Path> get() {
    List<Path> pathsToReturn = getStorageInfo();
    return pathsToReturn;
  }

  public HashMap<String, List<Long>> getSpace() {
    HashMap<String, List<Long>> result = new HashMap<String, List<Long>>();
    List<Path> pathsToReturn = getStorageInfo();
      for (int i = 0; i < pathsToReturn.size(); i++) {
        List<Long> sizeList = new ArrayList<Long>();
        Path path =  pathsToReturn.get(i);

        sizeList.add(new File(pathsToReturn.get(i).path).getTotalSpace());
        sizeList.add(new File(pathsToReturn.get(i).path).getUsableSpace());
        result.put(path.path, sizeList);
      }

    return result;
  }
}



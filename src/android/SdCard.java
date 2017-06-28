package org.apache.cordova.sdcard;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;

public class SdCard extends CordovaPlugin {
	public SdCard() {
		
	}
	
	@Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        try {
            if (action.equals("get")) {
                File[] result = get();
				String dirResult = getDir();
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
	
	
	public File[] get() {
		Context context = this.cordova.getActivity().getApplicationContext(); 
		return context.getExternalFilesDirs(null);
	}
	
	public HashMap<String, List<Long>> getSpace() {
		HashMap<String, List<Long>> result = new HashMap<String, List<Long>>();
		Context context = this.cordova.getActivity().getApplicationContext(); 
		File[] files = context.getExternalFilesDirs(null);
		for (File file : files) {
			List<Long> sizeList = new ArrayList<Long>();
			sizeList.add(file.getTotalSpace());
			sizeList.add(file.getUsableSpace());
			result.put(file.getPath(), sizeList);
		}

		return result;
	}
	
	public String getDir() {
		return System.getenv("EXTERNAL_SDCARD_STORAGE");
	}
}
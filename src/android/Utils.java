package ru.cordova.android.bars;

import android.app.Activity;
import android.content.res.Resources;
import android.os.Handler;
import android.util.DisplayMetrics;

import org.json.JSONException;
import org.json.JSONObject;
import org.apache.cordova.PluginResult;
import org.apache.cordova.CallbackContext;

import java.util.concurrent.atomic.AtomicInteger;

public class Utils {
  private static Object clearInterval;


  public static int dpToPx(Resources resources, int dp) {
    DisplayMetrics displayMetrics = resources.getDisplayMetrics();
    return Math.round(dp / displayMetrics.density);
  }

  public static void sendResult(CallbackContext cb, JSONObject info, boolean keepCallback) {
    if (cb != null) {
      PluginResult result = new PluginResult(PluginResult.Status.OK, info);
      result.setKeepCallback(keepCallback);
      cb.sendPluginResult(result);
    }
  }

  public void setTimeout(Activity activity, Runnable runnable, int delay) {
    new Thread(() -> {
      try {
        Thread.sleep(delay);
        activity.runOnUiThread(runnable);
      }
      catch (Exception e){
        System.err.println(e);
      }
    }).start();
  }

  public static class ReturnSetInterval {
    private Handler handler;
    private Runnable runnable;

    protected ReturnSetInterval(Handler handler, Runnable runnable) {
      this.handler = handler;
      this.runnable = runnable;
    }
    public void closeInterval() {
      handler.removeCallbacks(runnable);
    }
  }

  public interface  SetIntervalCb {
     boolean run();
  }
  public static ReturnSetInterval setInterval(Utils.SetIntervalCb cb, int delay, int limit) {
    Handler handler = new Handler();
    AtomicInteger totalLimit = new AtomicInteger(0);
    Runnable runnable = new Runnable() {
      @Override
      public void run() {
        ReturnSetInterval returnSetInterval = new ReturnSetInterval(handler, this);

       boolean isClose =  cb.run();
       if(isClose | (limit != 0 & totalLimit.get() > limit)){
         totalLimit.set(0);
         handler.removeCallbacks(this);
       }else{
         if(limit != 0) { totalLimit.set(totalLimit.get() + delay); }
         handler.postDelayed(this, delay);
       }

      }
    };

    handler.postDelayed(runnable, delay);
    return new ReturnSetInterval(handler, runnable);
  }

}


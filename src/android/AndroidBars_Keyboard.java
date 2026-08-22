package ru.cordova.android.bars;

import android.content.Context;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.LOG;
import org.json.JSONException;

public class AndroidBars_Keyboard extends CordovaPlugin{
  private static final String TAG = "AndroidBars";

  protected static final String KEYBOARD_SHOW = "show";
  protected static final String KEYBOARD_HIDE = "hide";


  private AppCompatActivity activity;
  private Window window;
  private CallbackContext callbackContext = null;

  public void initialize(final CordovaInterface cordova, CordovaWebView webView){
    LOG.d(TAG, "initialization Keyboard");
    super.initialize(cordova, webView);
    activity = this.cordova.getActivity();
    window = activity.getWindow();

  }

  public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext){
    LOG.d(TAG, "execute (action): " + action);
    this.callbackContext = callbackContext;
    InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
    View view = window.getDecorView().getRootView();
    switch(action){
      case KEYBOARD_SHOW:
        imm.showSoftInput(view, 0);
        callbackContext.success();
        return true;

      case KEYBOARD_HIDE:
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        callbackContext.success();
        return true;

      default: return false;
    }
  }

  private WindowInsetsControllerCompat getInsetsController(){
    return WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
  }
  private WindowInsetsCompat getWindowInsetsCompat(){
    return ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
  }

  private void showKeyboard(){
    int idKeyboard = WindowInsetsCompat.Type.ime();
    WindowInsetsControllerCompat insetsController = getInsetsController();
    insetsController.show(idKeyboard);
  }

  private void hideKeyboard(){
    int idKeyboard = WindowInsetsCompat.Type.ime();
    WindowInsetsControllerCompat insetsController = getInsetsController();
    insetsController.hide(idKeyboard);
  }

  private Insets getInsetsKeyboard(){
    int idKeyboard = WindowInsetsCompat.Type.ime();
    WindowInsetsCompat wInsetsCompat = getWindowInsetsCompat();
    Insets insetsIgnoringVisibility = wInsetsCompat.getInsetsIgnoringVisibility(idKeyboard);
    return wInsetsCompat.getInsets(idKeyboard);
  }
}

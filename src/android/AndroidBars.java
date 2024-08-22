package ru.cordova.android.bars;


import android.graphics.Color;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;

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
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class AndroidBars extends CordovaPlugin{
  private static final String TAG = "AndroidBars";

  private AppCompatActivity activity;
  private Window window;

  private static final String ACTION_ON = "on";
  private static final String ACTION_OFF = "off";
  private static final String ACTION_BG_COLOR_ALL = "bgColorAll";
  private static final String ACTION_BG_COLOR_NAV_BAR = "bgColorNavBar";
  private static final String ACTION_BG_COLOR_STATUS_BAR = "bgColorStatusBar";
  private static final String ACTION_FULL_SCREEN = "setFullScreen";
  private static final String ACTION_TOGGLE_COLOR_ICONS = "setDarkIcon";
  private static final String ACTION_ACTIVE_IMMERSIVE_MODE = "setActiveImmersiveMode";


  private List<String> listStringState = Arrays.asList(ACTION_BG_COLOR_ALL, ACTION_BG_COLOR_STATUS_BAR, ACTION_BG_COLOR_NAV_BAR);
  private JSONObject stateValueString = new JSONObject();

  private List<String> listBooleanState = Arrays.asList(ACTION_FULL_SCREEN, ACTION_TOGGLE_COLOR_ICONS, ACTION_ACTIVE_IMMERSIVE_MODE);
  private JSONObject stateValueBoolean = new JSONObject();
  //В будущем пожно пополнять listNameEvents
  private List<String> listNameEvents = Arrays.asList("watchKeyboard");
  Map<String, CallbackContext> listCurrentEvents = new HashMap<>();
  Map<String, JSONObject> storeInfoByActiveEvents = new HashMap<>();
  private JSONObject stateControlForSetInterval = new JSONObject();

  private int DELAY_SPLASH = 2;
  private int TIMEOUT_DELAY = 2;
  private double TIME_REACTION_AFTER_SPLASH = 1.2;//уменьшая увеличиваем задержку отработки свойств после сплешь скрина
  private int heightKeyboard = 0;
  private int limitIntervalMillisecond = 2000;
  private CallbackContext callbackContext = null;

//  private CallbackContext watchSizingBars = null;

  /**
   * Sets the context of the Command. This can then be used to do things like
   * get file paths associated with the Activity.
   *
   * @param cordova The context of the main Activity.
   * @param webView The CordovaWebView Cordova is running in.
   */
  @Override
  public void initialize(final CordovaInterface cordova, CordovaWebView webView){
    LOG.d(TAG, "initialization");
    super.initialize(cordova, webView);
    activity = this.cordova.getActivity();
    window = activity.getWindow();
    DELAY_SPLASH = preferences.getInteger("SplashScreenDelay", 10);
    TIMEOUT_DELAY = (int) (DELAY_SPLASH / TIME_REACTION_AFTER_SPLASH);// /  1.2

    activity.runOnUiThread(() -> {
      //Reset Request after Splash
      Utils.setTimeout(() -> {
        TIMEOUT_DELAY = 3;
      }, (int) (TIMEOUT_DELAY * 1.5));


      String defaultColor = "#000000";
      try{
        for(String key : listStringState) stateValueString.put(key, defaultColor);

      }catch(JSONException e){
        throw new RuntimeException(e);
      }

      boolean defaultStatus = false;
      try{
        for(String key : listBooleanState) stateValueBoolean.put(key, defaultStatus);
      }catch(JSONException e){
        throw new RuntimeException(e);
      }


//      WindowManager.LayoutParams params = window.getAttributes();
//      params.screenBrightness = 1F;
//      window.setAttributes(params);


      ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView().getRootView(), (view, wInsets) -> {

        int idKeyboard = WindowInsetsCompat.Type.ime();
        boolean isVisibleKeyboard = wInsets.isVisible(idKeyboard);
        int imeHeight = wInsets.getInsets(idKeyboard).bottom;

        if(listCurrentEvents.get("watchKeyboard") != null){
          //Active Event
          CallbackContext cbKeyboard = listCurrentEvents.get("watchKeyboard");
          JSONObject keyboardInfoState = storeInfoByActiveEvents.get("watchKeyboard");
          JSONObject payloadInfo = new JSONObject();
          try{
            int heightKeyboardState = (int) keyboardInfoState.get("height");

            if(imeHeight != 0){
              payloadInfo.put("action", "open");
              payloadInfo.put("height", imeHeight);
            } else {
              payloadInfo.put("action", "close");
              payloadInfo.put("height", 0);
            }

            if(heightKeyboardState != payloadInfo.getInt("height")){
              keyboardInfoState.put("action", payloadInfo.getString("action"));
              keyboardInfoState.put("height", payloadInfo.getInt("height"));
              watchKeyboard(cbKeyboard, payloadInfo);
            }
          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }

        LOG.d(TAG, "SetOnApplyWindowInsetsListener");
        Insets iGestures = wInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
        Insets iBars = wInsets.getInsets(WindowInsetsCompat.Type.systemBars());


//        v.setPadding(iBars.left, iBars.top, iBars.right, iBars.bottom);

//        try {
//          String colorNav = colorsState.getString(ACTION_BG_COLOR_NAV_BAR);
//          int f = window.getStatusBarColor();
//          int d = Color.parseColor(colorNav);
//          if(f != d ){
//            setBgColorNavBar(colorNav);
//          }
//        } catch (JSONException e) {
//          throw new RuntimeException(e);
//        }
//


        return ViewCompat.onApplyWindowInsets(view, wInsets);// ViewCompat.onApplyWindowInsets(v, wInsets);// WindowInsetsCompat.CONSUMED;//wInsets;//ViewCompat.onApplyWindowInsets(window.getDecorView().getRootView(), wInsets);
      });


    });
  }


  @Override
  public void pluginInitialize(){
    LOG.d(TAG, "pluginInitialize");
    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE);
  }

  @Override
  public void onStart(){
    LOG.d(TAG, "onStart");
  }

  @Override
  public void onDestroy(){
    LOG.d(TAG, "onDestroy");
    ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView(), null);
  }


  /**
   * Executes the request and returns PluginResult.
   *
   * @param action          The action to execute.
   * @param args            JSONArry of arguments for the plugin.
   * @param callbackContext The callback id used when calling back into JavaScript.
   * @return True if the action was valid, false otherwise.
   */

  @Override
  public boolean execute(final String action, final CordovaArgs args, final CallbackContext callbackContext){
    this.callbackContext = callbackContext;
    try{
      stateControlForSetInterval.put(action, true);
    }catch(JSONException e){
      throw new RuntimeException(e);
    }
    LOG.d(TAG, "isFlag: FLAG_FORCE_NOT_FULLSCREEN: " + isFlag(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
    LOG.d(TAG, "isFlag: FLAG_TRANSLUCENT_STATUS: " + isFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS));
    LOG.d(TAG, "isFlag: FLAG_TRANSLUCENT_NAVIGATION: " + isFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION));
    LOG.d(TAG, "isFlag: FLAG_LAYOUT_NO_LIMITS: " + isFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: " + isFlag(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_FULLSCREEN: " + isFlag(View.SYSTEM_UI_FLAG_FULLSCREEN));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LAYOUT_STABLE : " + isFlag(View.SYSTEM_UI_FLAG_LAYOUT_STABLE));
    LOG.d(TAG, "isFlag: BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE : " + isFlag(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE));
    LOG.d(TAG, "isFlag: BEHAVIOR_SHOW_BARS_BY_TOUCH : " + isFlag(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : " + isFlag(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
    LOG.d(TAG, "execute (action): " + action);

    /*
     * Такая мысль: Возможно execute отрабатывает быстро и т.к. setTimeout из класса она перезатирает
     * */
    boolean isCurrentDarkIc = !((activity.getWindow().getDecorView().getSystemUiVisibility() & View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR) != 0);
    LOG.d("WERQ", "isCurrentDarkIcon (execute): " + isCurrentDarkIc);


    switch(action){
      case ACTION_BG_COLOR_ALL:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              String hexSystemBars = args.getString(0);
              stateValueString.put(ACTION_BG_COLOR_ALL, hexSystemBars);
              setBgColorAll(hexSystemBars);
            }catch(JSONException ignore){
              LOG.e(TAG, "Invalid hexString argument, use f.i. '#777777'");
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_BG_COLOR_STATUS_BAR:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              String hexStatusBars = args.getString(0);
              stateValueString.put(ACTION_BG_COLOR_STATUS_BAR, hexStatusBars);
              setBgColorStatusBar(hexStatusBars);
            }catch(JSONException ignore){
              LOG.e(TAG, "Invalid" + ACTION_BG_COLOR_STATUS_BAR);
            }

          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_BG_COLOR_NAV_BAR:

        Utils.setTimeout(() -> {
          cordova.getActivity().runOnUiThread(() -> {
            try{
              String hexColorNav = args.getString(0);
              stateValueString.put(ACTION_BG_COLOR_NAV_BAR, hexColorNav);
              setBgColorNavBar(hexColorNav);
            }catch(JSONException ignore){
              LOG.e(TAG, "Invalid hexString argument, use f.i. '#777777'");
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_TOGGLE_COLOR_ICONS:
        Utils.setTimeout(() -> {
          cordova.getActivity().runOnUiThread(() -> {
            try{
              boolean isRequestUI = stateControlForSetInterval.getBoolean(ACTION_TOGGLE_COLOR_ICONS);
              boolean isDarkIcon = args.getBoolean(0);
              stateValueBoolean.put(ACTION_TOGGLE_COLOR_ICONS, isDarkIcon);
              /*
                boolean isAppearanceLight = wInsetsController.isAppearanceLightStatusBars();
                Поведение isAppearanceLightStatusBars в данной ситеации не соответвует тому что на телефоне.
                Не синхрона. показывает предыдущее значение. Устанавливаем тупо лимит.
               */

              Utils.ReturnSetInterval controlInterval = Utils.setInterval(() -> {
                setDarkIcon(isDarkIcon);
                return false;
              }, 10, limitIntervalMillisecond);
            }catch(JSONException e){
              throw new RuntimeException(e);
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_FULL_SCREEN:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              boolean isFullScreen = args.getBoolean(0);
              stateValueBoolean.put(ACTION_FULL_SCREEN, isFullScreen);
              setFullScreen(isFullScreen);
            }catch(JSONException e){
              throw new RuntimeException(e);
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_ACTIVE_IMMERSIVE_MODE:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              boolean isActiveImmersiveMode = args.getBoolean(0);
              stateValueBoolean.put(ACTION_ACTIVE_IMMERSIVE_MODE, isActiveImmersiveMode);
              setActiveImmersiveMode(isActiveImmersiveMode);
            }catch(JSONException e){
              throw new RuntimeException(e);
            }
          });
        }, TIMEOUT_DELAY);
        return true;
      case ACTION_ON:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              String nameEvent = args.getString(0);
              boolean isRealEvent = listNameEvents.contains(nameEvent);
              if(isRealEvent){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                  if(listCurrentEvents.get(nameEvent) == null){
                    listCurrentEvents.put(nameEvent, callbackContext);
                    initializationStateByEvent(nameEvent);
                  }
                }
              }
            }catch(JSONException e){
              throw new RuntimeException(e);
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_OFF:
        Utils.setTimeout(() -> {
          activity.runOnUiThread(() -> {
            try{
              String nameEvent = args.getString(0);
              boolean isRealEvent = listNameEvents.contains(nameEvent);
              if(isRealEvent){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                  if(listCurrentEvents.get(nameEvent) != null){
                    listCurrentEvents.remove(nameEvent);
                    removeStateByEvent(nameEvent);
                  }
                }
              }
            }catch(JSONException e){
              throw new RuntimeException(e);
            }
          });
        }, TIMEOUT_DELAY);
        return true;

      default:
        return false;
    }
  }


  public void setFullScreen(boolean isFull){
    //Не засовывать в setOnApplyWindowInsetsListener будет цикл
    int idGestures = WindowInsetsCompat.Type.systemGestures();
    WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), !isFull);
  }

  public void setActiveImmersiveMode(boolean isMode){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    int idStatusBar = WindowInsetsCompat.Type.statusBars();

//    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    wInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH);
    if(isMode){
      wInsetsController.hide(idStatusBar);
    } else {
      wInsetsController.show(idStatusBar);
    }
  }

  public void setDarkIcon(boolean isDarkIcon){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
//    wInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH);
    /*  На фоне так же ставиться и убираеться  setSystemUiFlag(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
     * */

//    WindowInsetsControllerCompat.removeOnControllableInsetsChangedListener();
//    wInsetsController.applyThemesSystemBarAppearance
    wInsetsController.setAppearanceLightStatusBars(isDarkIcon);
    wInsetsController.setAppearanceLightNavigationBars(isDarkIcon);
  }

  private boolean isCurrentDarkIcon(){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    //Ориентировать будет только на sustem bar. Меняються меняем цвет и в nav одновременно.
    return !wInsetsController.isAppearanceLightStatusBars();
  }

  public void setBgColorAll(String color){
    if(color.isEmpty()) return;
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(Color.parseColor(color));
    window.setNavigationBarColor(Color.parseColor(color));
  }

  public void setBgColorStatusBar(String color){
    if(color.isEmpty()) return;
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setStatusBarColor(Color.parseColor(color));
  }

  public void setBgColorNavBar(String color){
    if(color.isEmpty()) return;
    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
    window.setNavigationBarColor(Color.parseColor(color));
  }

  private WindowInsetsControllerCompat getInsetsController(){
    return WindowCompat.getInsetsController(activity.getWindow(), activity.getWindow().getDecorView());
  }

  private void setApply(String action){
//    this.CURRENT_ACTION = action;
    ViewCompat.dispatchApplyWindowInsets(window.getDecorView(), getWindowInsetsCompat());
  }

  private WindowInsetsCompat getWindowInsetsCompat(){
    return ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
  }


  private WindowInsets getWindowInsets(){
    return activity.getWindow().getDecorView().getRootWindowInsets();
  }

  private void showBars(){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    if(wInsetsController != null){
      wInsetsController.show(WindowInsetsCompat.Type.systemBars());
    }
  }

  private void hideBars(){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    if(wInsetsController != null){
      wInsetsController.hide(WindowInsetsCompat.Type.systemBars());
    }
  }

  boolean getVisibleBars(){
    WindowInsetsCompat wInsets = getWindowInsetsCompat();
    boolean isVisibleNavBar = wInsets.isVisible(WindowInsetsCompat.Type.navigationBars());
    boolean isVisibleStatusBar = wInsets.isVisible(WindowInsetsCompat.Type.statusBars());
    return isVisibleNavBar & isVisibleStatusBar;
  }

  private void configClickShowingBars(String mode){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    if(mode.equals("TIME_HIDE")){
      wInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
    }
    if(mode.equals("STATIC_HIDE")){
      wInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH);
    }
  }

  private JSONObject getHeightKeyboard() throws JSONException{

    WindowInsets insets = getWindowInsets();
    JSONObject payload = new JSONObject();


    return payload;
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

  private boolean isFlag(int flag){
    return !((window.getAttributes().flags & flag) == 0);
  }

  private boolean isDarkColorIcon(){
    WindowInsetsControllerCompat insetsController = getInsetsController();
    return insetsController.isAppearanceLightStatusBars();
  }

  private void watchKeyboard(CallbackContext cb, JSONObject info){
    LOG.d("watchKeyboard", "");
    Utils.sendResult(cb, info, true);
  }

  private interface Interface_P{
    //Тупо пример. Оказываеться прям в классе в java пишут
  }

  private void initializationStateByEvent(String nameEvent){
    //Инициализация данных по эвентам
    if(nameEvent.equals("watchKeyboard")){
      JSONObject keyboardPayload = new JSONObject();
      try{
        keyboardPayload.put("action", "close");
        keyboardPayload.put("height", 0);
        storeInfoByActiveEvents.put("watchKeyboard", keyboardPayload);
      }catch(JSONException e){
        throw new RuntimeException(e);
      }

    }
  }

  private void removeStateByEvent(String nameEvent){
    if(nameEvent.equals("watchKeyboard")){

    }
  }

  private class Event{
    String name;
    CallbackContext cb;

    Event(String name, CallbackContext cb){
      this.name = name;
      this.cb = cb;
    }
  }


  private class Events{
    String name = "";
    CallbackContext cb = null;

    Events(String name, CallbackContext cb){
      this.name = name;
      this.cb = cb;
    }

    public boolean add(){
      return true;
    }

    public boolean remove(){
      return true;
    }
  }

  //  private int calculateStatusBarColor() {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
////      (WindowInsets.Type.statusBars()) == 0
//      return calculateBarColor(
//              WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, window.getStatusBarColor(),
//              APPEARANCE_LIGHT_STATUS_BARS, true);
//    } else {
//      return 0;
//    }
//
//  }

//  private int calculateNavigationBarColor() {
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
////      (WindowInsets.Type.navigationBars()) == 0)
//      return calculateBarColor(
//              WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, window.getNavigationBarColor(),
//              APPEARANCE_LIGHT_NAVIGATION_BARS, true);
//    } else {
//      return 0;
//    }
//  }

//  public int calculateBarColor(int translucentFlag, int barColor, int lightAppearanceFlag, boolean isTransparent) {
//    int appearance = 0;
//    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
//      WindowInsetsController insetsController = window.getInsetsController();
//      appearance = insetsController.getSystemBarsAppearance();
//    }
//    int flags = window.getAttributes().flags;
//    int semiTransparentBarColor = R.color.system_bar_background_semi_transparent;
//    if ((flags & translucentFlag) != 0) {
//      return semiTransparentBarColor;
//    } else if ((flags & FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS) == 0) {
//      return Color.BLACK;
//    } else if (isTransparent && Color.alpha(barColor) == 0) {
//      boolean light = (appearance & lightAppearanceFlag) != 0;
//      return light ? SCRIM_LIGHT : semiTransparentBarColor;
//    } else {
//      return barColor;
//    }
//  }

}

//      window.getDecorView().setOnSystemUiVisibilityChangeListener
//              (new View.OnSystemUiVisibilityChangeListener() {
//                @Override
//                public void onSystemUiVisibilityChange(int visibility) {
//                  // Note that system bars will only be "visible" if none of the
//                  // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
//                    LOG.d("SetOnApplyWindowInsetsListener", "ACTIVE1111");
//                  if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                    // The system bars are visible.
//                  } else {
//                    // The system bars are NOT visible.
//                  }
//                }
//              });
//windowInsets.isVisible(WindowInsetsCompat.Type.navigationBars())

     /*
                Растягивает фон за навигацию и статус бар  deprecated api 30
                   window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE| View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
                   или
                   window.setDecorFitsSystemWindows(false);
            */
            /*
                Полупрозрачность
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
                или
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                или
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            */


//    ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView(), (v, windowInsets) -> {
//      Insets insetsSystemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
//
//      v.setPadding(
//              insetsSystemBars.left,
//              insetsSystemBars.top,
//              insetsSystemBars.right,
//              insetsSystemBars.bottom
//      );
//
//
//
//      LOG.d("SYSTEM_BARS", "insetsSystemBars");
//      return WindowInsetsCompat.CONSUMED;// WindowInsetsCompat.CONSUMED;
//    });


package ru.cordova.android.bars;

import android.content.res.Resources;
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
import java.util.List;


public class AndroidBars extends CordovaPlugin{
  private static final String TAG = "AndroidBars";

  private AppCompatActivity activity;
  private Window window;
  private Events eventsBars = new Events();

  private static final String ACTION_ON = "on";
  private static final String ACTION_OFF = "off";
  private static final String ACTION_BG_COLOR_ALL = "bgColorAll";
  private static final String ACTION_BG_COLOR_NAV_BAR = "bgColorNavBar";
  private static final String ACTION_BG_COLOR_STATUS_BAR = "bgColorStatusBar";
  private static final String ACTION_FULL_SCREEN = "setFullScreen";
  private static final String ACTION_IS_FULL_SCREEN = "isFullScreen";
  private static final String ACTION_TOGGLE_COLOR_ICONS = "setDarkIcon";
  private static final String ACTION_ACTIVE_IMMERSIVE_MODE = "setActiveImmersiveMode";
  private static final String ACTION_GET_HEIGHT_SYSTEM_BARS = "getHeightSystemBars";


  private List<String> listStringState = Arrays.asList(ACTION_BG_COLOR_ALL, ACTION_BG_COLOR_STATUS_BAR, ACTION_BG_COLOR_NAV_BAR);
  private JSONObject stateValueString = new JSONObject();

  private List<String> listBooleanState = Arrays.asList(ACTION_FULL_SCREEN, ACTION_TOGGLE_COLOR_ICONS, ACTION_ACTIVE_IMMERSIVE_MODE);
  private JSONObject stateValueBoolean = new JSONObject();


  private int DELAY_SPLASH = 2;
  private int TIMEOUT_DELAY = 2;
  //На 29 нор 100.
  private double TIME_REACTION_AFTER_SPLASH = 100;//1.2;//уменьшая увеличиваем задержку отработки свойств после сплешь скрина
  private int limitIntervalMillisecond = 3000;
  private CallbackContext callbackContext = null;

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
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
      TIME_REACTION_AFTER_SPLASH = 10;
    }

    TIMEOUT_DELAY = (int) (DELAY_SPLASH / TIME_REACTION_AFTER_SPLASH);// /  1.2

    activity.runOnUiThread(() -> {
      String defaultColor = "#000000";
      window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
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


      try{
        boolean isFullScreenInit = preferences.getBoolean("AndroidBarsFullScreen", true);
        stateValueBoolean.put(ACTION_FULL_SCREEN, isFullScreenInit);
        setFullScreen(isFullScreenInit);
      }catch(JSONException e){
        throw new RuntimeException(e);
      }

      new Utils().setTimeout(activity, () -> {
        TIMEOUT_DELAY = 3;
      }, (int) (TIMEOUT_DELAY * 1.5));


      ViewCompat.setOnApplyWindowInsetsListener(window.getDecorView().getRootView(), (view, wInsets) -> {
        Insets iGestures = wInsets.getInsets(WindowInsetsCompat.Type.systemGestures());
        Insets iBars = wInsets.getInsets(WindowInsetsCompat.Type.systemBars());

        if(eventsBars.listCurrentEvents.get("watchKeyboard") != null){
          watchKeyboard(wInsets);
        }

        LOG.d(TAG, "SetOnApplyWindowInsetsListener");
//      v.setPadding(iBars.left, iBars.top, iBars.right, iBars.bottom);
        return ViewCompat.onApplyWindowInsets(view, wInsets);// ViewCompat.onApplyWindowInsets(v, wInsets);// WindowInsetsCompat.CONSUMED;//wInsets;//ViewCompat.onApplyWindowInsets(window.getDecorView().getRootView(), wInsets);
      });
    });
  }

  @Override
  public void pluginInitialize(){
    LOG.d(TAG, "pluginInitialize");
    window.clearFlags(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);
//    window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE);
  }

  @Override
  public void onStart(){
    LOG.d(TAG, "onStart");
    LOG.d(TAG, "isFlag: FLAG_FORCE_NOT_FULLSCREEN: " + isFlag(WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN));
    LOG.d(TAG, "isFlag: FLAG_TRANSLUCENT_STATUS: " + isFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS));
    LOG.d(TAG, "isFlag: FLAG_TRANSLUCENT_NAVIGATION: " + isFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION));
    LOG.d(TAG, "isFlag: FLAG_LAYOUT_NO_LIMITS: " + isFlag(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS));
    LOG.d(TAG, "isFlag: FLAG_FULLSCREEN: " + isFlag(WindowManager.LayoutParams.FLAG_FULLSCREEN));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN: " + isFlag(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_FULLSCREEN: " + isFlag(View.SYSTEM_UI_FLAG_FULLSCREEN));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LAYOUT_STABLE : " + isFlag(View.SYSTEM_UI_FLAG_LAYOUT_STABLE));
    LOG.d(TAG, "isFlag: BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE : " + isFlag(WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE));
    LOG.d(TAG, "isFlag: BEHAVIOR_SHOW_BARS_BY_TOUCH : " + isFlag(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH));
    LOG.d(TAG, "isFlag: SYSTEM_UI_FLAG_LIGHT_STATUS_BAR : " + isFlag(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR));
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
    LOG.d(TAG, "execute (action): " + action);
    this.callbackContext = callbackContext;
    /*
     *   INFO: setInterval требуеться в начале инициализации приложения т.к. некоторые api упорно мешают пепереключать цвета
     *   меняя на свои. Ориентироваться на статус и деативировать interval не выйдет т.к. в моменте времени он снова изменится
     *   поэтому долбим до осечки.
     * */

    switch(action){
      case ACTION_BG_COLOR_ALL:
        new Utils().setTimeout(activity, () -> {
          try{
            String hexSystemBars = args.getString(0);
            stateValueString.put(ACTION_BG_COLOR_ALL, hexSystemBars);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
              if(TIMEOUT_DELAY == 0){
                setBgColorAll(hexSystemBars);
              } else {
                Utils.setInterval(() -> {
                  setBgColorAll(hexSystemBars);
                  return false;
                }, 300, limitIntervalMillisecond);
              }
            } else {
              setBgColorAll(hexSystemBars);
            }

          }catch(JSONException ignore){
            LOG.e(TAG, "Invalid hexString argument, use f.i. '#777777'");
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_BG_COLOR_STATUS_BAR:
        new Utils().setTimeout(activity, () -> {
          try{
            String hexStatusBars = args.getString(0);
            stateValueString.put(ACTION_BG_COLOR_STATUS_BAR, hexStatusBars);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
              if(TIMEOUT_DELAY == 0){
                setBgColorStatusBar(hexStatusBars);
              } else {
                Utils.setInterval(() -> {
                  setBgColorStatusBar(hexStatusBars);
                  return false;
                }, 300, limitIntervalMillisecond);
              }
            } else {
              setBgColorStatusBar(hexStatusBars);
            }
          }catch(JSONException ignore){
            LOG.e(TAG, "Invalid" + ACTION_BG_COLOR_STATUS_BAR);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_BG_COLOR_NAV_BAR:
        new Utils().setTimeout(activity, () -> {
          try{
            String hexColorNav = args.getString(0);
            stateValueString.put(ACTION_BG_COLOR_NAV_BAR, hexColorNav);
            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
              if(TIMEOUT_DELAY == 0){
                setBgColorNavBar(hexColorNav);
              } else {
                Utils.setInterval(() -> {
                  setBgColorNavBar(hexColorNav);
                  return false;
                }, 300, limitIntervalMillisecond);
              }
            } else {
              setBgColorNavBar(hexColorNav);
            }

          }catch(JSONException ignore){
            LOG.e(TAG, "Invalid hexString argument, use f.i. '#777777'");
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_TOGGLE_COLOR_ICONS:
        new Utils().setTimeout(activity, () -> {
          try{
            boolean isDarkIcon = args.getBoolean(0);
            String statusDarkNavIcon = args.getString(1);//"null" | "active" | "noActive"
            stateValueBoolean.put(ACTION_TOGGLE_COLOR_ICONS, isDarkIcon);
              /*
                boolean isAppearanceLight = wInsetsController.isAppearanceLightStatusBars();
                Поведение isAppearanceLightStatusBars в данной ситеации не соответвует тому что на телефоне.
                Не синхрона. показывает предыдущее значение. Устанавливаем тупо лимит.
               */

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
              if(TIMEOUT_DELAY == 0){
                setDarkIcon(isDarkIcon, statusDarkNavIcon);
              } else {
                Utils.setInterval(() -> {
                  setDarkIcon(isDarkIcon, statusDarkNavIcon);
                  return false;
                }, 300, limitIntervalMillisecond);
              }
            } else {
              setDarkIcon(isDarkIcon, statusDarkNavIcon);
            }

          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_FULL_SCREEN:
        new Utils().setTimeout(activity, () -> {
          try{
            boolean isFullScreen = args.getBoolean(0);
            stateValueBoolean.put(ACTION_FULL_SCREEN, isFullScreen);

            if(Build.VERSION.SDK_INT > Build.VERSION_CODES.S){
              if(TIMEOUT_DELAY == 0){
                setFullScreen(isFullScreen);
              } else {
                Utils.setInterval(() -> {
                  setFullScreen(isFullScreen);
                  return false;
                }, 300, limitIntervalMillisecond);
              }
            } else {
              setFullScreen(isFullScreen);
            }
          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_ACTIVE_IMMERSIVE_MODE:
        new Utils().setTimeout(activity, () -> {
          try{
            boolean isActiveImmersiveMode = args.getBoolean(0);
            stateValueBoolean.put(ACTION_ACTIVE_IMMERSIVE_MODE, isActiveImmersiveMode);
            setActiveImmersiveMode(isActiveImmersiveMode);
          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_GET_HEIGHT_SYSTEM_BARS:
        new Utils().setTimeout(activity, () -> {
          try{
            Utils.sendResult(callbackContext, getHeightsBars(), false);
          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_IS_FULL_SCREEN:
        new Utils().setTimeout(activity, () -> {
          try{
            boolean is = isFullScreen();
            JSONObject payload = new JSONObject();
            payload.put("is", is);
            Utils.sendResult(callbackContext, payload, false);
          }catch(JSONException e){
            throw new RuntimeException(e);
          }

        }, TIMEOUT_DELAY);
        return true;

      case ACTION_ON:
        new Utils().setTimeout(activity, () -> {
          try{
            String nameEvent = args.getString(0);
            boolean isRealEvent = eventsBars.listNameEvents.contains(nameEvent);
            if(isRealEvent){
              if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                if(eventsBars.listCurrentEvents.get(nameEvent) == null){
                  eventsBars.listCurrentEvents.put(nameEvent, callbackContext);
                  eventsBars.initializationStateByEvent(nameEvent);
                }
              }
            }
          }catch(JSONException e){
            throw new RuntimeException(e);
          }
        }, TIMEOUT_DELAY);
        return true;

      case ACTION_OFF:
        new Utils().setTimeout(activity, () -> {
          try{
            String nameEvent = args.getString(0);
            boolean isRealEvent = eventsBars.listNameEvents.contains(nameEvent);
            if(isRealEvent){
              if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
                if(eventsBars.listCurrentEvents.get(nameEvent) != null){
                  eventsBars.listCurrentEvents.remove(nameEvent);
                  eventsBars.removeStateByEvent(nameEvent);
                }
              }
            }
          }catch(JSONException e){
            throw new RuntimeException(e);
          }

        }, TIMEOUT_DELAY);
        return true;

      default:
        return false;
    }
  }


  public void setFullScreen(boolean isFull){
    if(!isFull){
      window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_VISIBLE);
    }

    WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), !isFull);
  }

  public void setActiveImmersiveMode(boolean isMode) throws JSONException{
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    int idSystemBars = WindowInsetsCompat.Type.systemBars();
//    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    wInsetsController.setSystemBarsBehavior(WindowInsetsControllerCompat.BEHAVIOR_SHOW_BARS_BY_TOUCH);
    boolean isFullScreen = stateValueBoolean.getBoolean(ACTION_FULL_SCREEN);

    if(isMode){
      if(!isFullScreen){
        setFullScreen(true);
      }
      wInsetsController.hide(idSystemBars);
    } else {
      if(!isFullScreen){
        setFullScreen(false);
      }
      wInsetsController.show(idSystemBars);
    }
  }

  public void setDarkIcon(boolean isDarkIcon, String statusDarkNavIcon){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    wInsetsController.setAppearanceLightStatusBars(isDarkIcon);
    if(statusDarkNavIcon.equals("null")){
      wInsetsController.setAppearanceLightNavigationBars(isDarkIcon);
    } else {
      boolean isDarkNavIcon = statusDarkNavIcon.equals("active");
      wInsetsController.setAppearanceLightNavigationBars(isDarkNavIcon);
    }
  }

  public boolean isFullScreen() throws JSONException{
    return stateValueBoolean.getBoolean(ACTION_FULL_SCREEN);
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

  private WindowInsetsCompat getWindowInsetsCompat(){
    return ViewCompat.getRootWindowInsets(activity.getWindow().getDecorView());
  }

  private WindowInsets getWindowInsets(){
    return activity.getWindow().getDecorView().getRootWindowInsets();
  }

  private boolean isFlag(int flag){
    return !((window.getAttributes().flags & flag) == 0);
  }

  private void watchKeyboard(WindowInsetsCompat wInsets){
    String nameEvent = "watchKeyboard";
    CallbackContext cb = eventsBars.listCurrentEvents.get(nameEvent);
    JSONObject keyboardInfoState = eventsBars.storeInfoByActiveEvents.get(nameEvent);
    JSONObject payloadInfo = new JSONObject();
    try{
      int heightKeyboardState = (int) keyboardInfoState.get("height");
      int imeHeight = wInsets.getInsets(WindowInsetsCompat.Type.ime()).bottom;


      JSONObject hData = getHeightsBars();
      payloadInfo.put("isFullScreen", stateValueBoolean.getBoolean(ACTION_FULL_SCREEN));
      payloadInfo.put("heightStatus", hData.getInt("heightStatus"));
      payloadInfo.put("heightNav", hData.getInt("heightNav"));

      if(imeHeight != 0){
        Resources resources = window.getDecorView().getResources();
        payloadInfo.put("isShow", true);
        payloadInfo.put("height", Utils.dpToPx(resources, imeHeight));
      } else {
        payloadInfo.put("isShow", false);
        payloadInfo.put("height", 0);
      }

      if(heightKeyboardState != payloadInfo.getInt("height")){
        keyboardInfoState.put("isShow", payloadInfo.getBoolean("isShow"));
        keyboardInfoState.put("height", payloadInfo.getInt("height"));
        Utils.sendResult(cb, payloadInfo, true);
      }
    }catch(JSONException e){
      throw new RuntimeException(e);
    }
  }

  private int getHeightStatusBar(WindowInsetsCompat wInsets){
    Resources resources = window.getDecorView().getResources();
    int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
    if (resourceId > 0) {
      int h = resources.getDimensionPixelSize(resourceId);
      return h;
    }
    return 0;
    //    return wInsets.getInsets(WindowInsetsCompat.Type.statusBars()).top;
  }

  private int getHeightNavBar(WindowInsetsCompat wInsets){
    // Замустив не в вертикальной ориентации я должен гадать left или right или bottom что бы получить размер навигации
    int g = wInsets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;

    // Так что берём по старинке
    Resources resources = window.getDecorView().getResources();
    int resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android");
    if (resourceId > 0) {
      int h = resources.getDimensionPixelSize(resourceId);
      return h;
    }
    return 0;
  }

  private JSONObject getHeightsBars() throws JSONException{
    WindowInsetsCompat wInsetsCompat = getWindowInsetsCompat();
    int heightStatusBar = getHeightStatusBar(wInsetsCompat);
    int heightNavBar = getHeightNavBar(wInsetsCompat);
    JSONObject payloadInfo = new JSONObject();
    Resources resources = window.getDecorView().getResources();
    payloadInfo.put("heightStatus", Utils.dpToPx(resources, heightStatusBar));
    payloadInfo.put("heightNav", Utils.dpToPx(resources, heightNavBar));
    return payloadInfo;
  }


  //  -----------------------------------------------------------------------------------------
  private void setApply(String action){
    ViewCompat.dispatchApplyWindowInsets(window.getDecorView(), getWindowInsetsCompat());
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


  private boolean isCurrentDarkIcon(){
    WindowInsetsControllerCompat wInsetsController = getInsetsController();
    //Ориентировать будет только на sustem bar. Меняються меняем цвет и в nav одновременно.
    return !wInsetsController.isAppearanceLightStatusBars();
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

  private interface Interface_P{
    //Тупо пример. Оказываеться прям в классе в java пишут
  }


//  private class Event{
//    String name;
//    CallbackContext cb;
//
//    Event(String name, CallbackContext cb){
//      this.name = name;
//      this.cb = cb;
//    }
//  }

}


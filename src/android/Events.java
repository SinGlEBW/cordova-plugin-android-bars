package ru.cordova.android.bars;

import org.apache.cordova.CallbackContext;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events{
  public List<String> listNameEvents = Arrays.asList("watchKeyboard");
  public Map<String, JSONObject> storeInfoByActiveEvents = new HashMap<>();
  public Map<String, CallbackContext> listCurrentEvents = new HashMap<>();

  public void initializationStateByEvent(String nameEvent){
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
    if(nameEvent.equals("watchHeightBars")){
      JSONObject barsPayload = new JSONObject();
      try{
        barsPayload.put("heightStatus", 0);
        barsPayload.put("heightNav", 0);
        storeInfoByActiveEvents.put("watchHeightBars", barsPayload);
      }catch(JSONException e){
        throw new RuntimeException(e);
      }
    }
  }

  public void removeStateByEvent(String nameEvent){
    if(nameEvent.equals("watchKeyboard")){

    }
  }
}

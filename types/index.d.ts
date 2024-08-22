/// <reference types="cordova" />

type NameEvents_OR = 'watchKeyboard' | 'watchHeightBars';
interface AndroidBars {
  setFullScreen(isFull):void;
  bgColorAll(hex: string):void;
  bgColorStatusBar(hex: string):void;
  bgColorNavBar(hex: string):void;
  setDarkIcon(isDarkIcon: boolean):void;
  setActiveImmersiveMode(isMode: boolean):void;
  on(name:NameEvents_OR, cb: (data: any) => void ):void;
  off(name:NameEvents_OR):void;
}

interface CordovaPlugins {
  AndroidBars: AndroidBars
}


/// <reference types="cordova" />


type NameEvents_OR = 'watchKeyboard';

interface AllCbEvents{
  watchKeyboard(data: {
    isShow: boolean, 
    height: number, 
    heightStatus: number, 
    heightNav: number, 
    isFullScreen: boolean
  }):void
}

interface AndroidBars {
  setFullScreen(isFull):void;
  isFullScreen(cb: ({is:  boolean}) => void):void;
  bgColorAll(hex: string):void;
  bgColorStatusBar(hex: string):void;
  bgColorNavBar(hex: string):void;
  setDarkIcon(isDarkIcon: boolean, isDarkNavIcon?:boolean):void;
  setActiveImmersiveMode(isMode: boolean):void;
  on<Name extends NameEvents_OR>(name:Name, cb: AllCbEvents[Name] ):void;
  off(name:NameEvents_OR):void;
  getHeightSystemBars(cb:AllCbEvents['watchKeyboard']):void
}

interface CordovaPlugins {
  AndroidBars: AndroidBars
}


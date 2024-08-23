---
title: AndroidBars
description: Control the device systemBars.
---


# cordova-plugin-android-bars



### Installation


    npm i cordova-plugin-android-bars

It is also possible to install via repo url directly

    cordova plugin add https://github.com/SinGlEBW/cordova-plugin-android-bars.git



```html
<meta name="viewport" content="initial-scale=1, width=device-width, viewport-fit=cover">
```

#### config.xml

   If desired, set the default settings. Usually, the plugin is loaded to manage the dynamic settings of the theme, so there is not much point in static parameters. But if you want, you are provided   with many parameters in the amount of ONE :)  Its default value is true

        <preference name="AndroidBarsFullScreen" value="true" />
      

### Android Quirks
List methods
```js
  const { AndroidBars } = window?.cordova?.plugins;
    //The color is transmitted in hex format.
    //(#RGB) | (#RRGGBB) | (#RRGGBBAA)//In secret, you can # omit
    AndroidBars.bgColorStatusBar(hex)
    AndroidBars.bgColorNavBar(hex)
    AndroidBars.bgColorAll(hex)

    AndroidBars.setDarkIcon(is) // sets dark or light tones on icons in bars
    AndroidBars.setFullScreen(is) //controls the size of the background. by default, it is not fullScreen (ps. if other plugins do not disrupt the operation)
    AndroidBars.setActiveImmersiveMode(is)//enables or disables the status and navigation panels. When enabled, the default state will be returned, which can be initially set via setFullScreen.
    AndroidBars.getHeightSystemBats(({heightStatus, heightNav}) => {

    })
    AndroidBars.on('watchKeyboard', ({
        isShow, height, heightStatus, heightNav, isFullScreen
    }) => {     })

    AndroidBars.off('watchKeyboard')
    AndroidBars.isFullScreen(({is}) => {});

```


var exec = require("cordova/exec");

let classInJava = "AndroidBars_Keyboard";

let AndroidBars_Keyboard = {
  hide: function () {
    exec(null, null, classInJava, "hide", []);
  },

  show: function () {
    exec(null, null, classInJava, "show", []);
  },
};

module.exports = AndroidBars_Keyboard;

var exec = require("cordova/exec");


function validateHex (hex = '') {
  const itemsHex = hex.split('');

  const hexAF = ['A', 'B', 'C', 'D', 'E', 'F'];
  let validHex = [];
  for (let i = 0; i < itemsHex.length; i++) {
    const itemHex = itemsHex[i];
    if(!i){
      validHex.push('#')
    }
    if(!Number.isNaN(Number(itemHex)) || hexAF.includes(itemHex.toUpperCase())){
      validHex.push(itemHex)
    }else{
      continue
    }
  }

 
  if(validHex.length > 9){
    validHex.length = 9
    validHex = [
      validHex[0], validHex[8], validHex[7], 
      validHex[1], validHex[2], validHex[3], 
      validHex[4], validHex[5], validHex[6]
    ];
  }
  let i = [1,2,3,5,6,8]
  if(i.includes(validHex.length)){
    console.error('Неверно заполнена строка hex')
    return '#000000';
  }
  if(validHex.length == 4){
    validHex = [validHex[0], validHex[1], validHex[1], validHex[2], validHex[2], validHex[3], validHex[3]];
  }
  
  return validHex.join('');
}


let classInJava = "AndroidBars";


let AndroidBars = {
  setFullScreen: function (isFull) {
    exec(null, null, classInJava, "setFullScreen", [isFull]);
  },
  isFullScreen: function (cb) {
    exec(cb, null, classInJava, "isFullScreen", []);
  },
  bgColorAll: function (hex = '') {
    const color = validateHex(hex)
    exec(null, null, classInJava, "bgColorAll", [color]);
  },
  bgColorStatusBar: function (hex = '') {
    const color = validateHex(hex)
    console.log('color', color);
    exec(null, null, classInJava, "bgColorStatusBar", [color]);
  },
  bgColorNavBar: function (hex = '') {
    const color = validateHex(hex)
    exec(null, null, classInJava, "bgColorNavBar", [color]);
  },
  setDarkIcon: function (isDarkIcon) {
    exec(null, null, classInJava, "setDarkIcon", [isDarkIcon]);
  },
  getHeightSystemBars: function (cb) {
    exec(cb, null, classInJava, "getHeightSystemBars", []);
  },
  setActiveImmersiveMode: function (isMode) {
    exec(null, null, classInJava, "setActiveImmersiveMode", [isMode]);
  },

  on: function (name, cb) {
    exec(cb, null, classInJava, "on", [name]);
  },
  off: function (name) {
    exec(null, null, classInJava, "off", [name]);
  }
};



module.exports = AndroidBars;

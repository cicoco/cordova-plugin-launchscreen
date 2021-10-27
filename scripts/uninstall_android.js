const fs = require("fs");
let commonFuncs = require("./common");


module.exports = function (ctx) {
    var destination = commonFuncs.getFilePath(
      "./platforms/android/app/src/main/java/",
      "MainActivity.java"
    );
    var _packName = destination.substring(
      "platforms/android/app/src/main/java/".length
    );
    _packName = _packName.replace(new RegExp("/", "gm"), ".");
  
    // 替换java
    fs.readFile("plugins/cordova-plugin-launchscreen/src/android/MainActivity_old.java", "utf8", function (err, data) {
      if (err) {
        console.log("android revert file failed");
        return;
      }
      var result = data.replace(
        new RegExp("package com.cicoco.placeholder;", "gm"),
        "package " + _packName + ";"
      );
  
      fs.writeFile(
        destination + "/MainActivity.java",
        result,
        "utf8",
        function (err) {
          if (err) {
            console.log("android revert MainActivity failed");
          } else {
            console.log("android revert MainActivity success");
          }
        }
      );
    });
  };
  
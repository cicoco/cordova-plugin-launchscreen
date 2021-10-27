const fs = require("fs");
let commonFuncs = require("./common");

module.exports = function (ctx) {
  var projName = commonFuncs.getXcodeProjName("./platforms/ios/");
  console.log("projName:" + projName);
  fs.copyFile(
    "plugins/cordova-plugin-launchscreen/src/ios/MainViewController_new.m",
    "platforms/ios/" + projName + "/Classes/MainViewController.m",
    function (err) {
      if (err) {
        console.log("ios copy MainViewController failed");
      } else {
        console.log("ios copy MainViewController success");
      }
    }
  );
};

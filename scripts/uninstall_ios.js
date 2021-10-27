const fs = require("fs");
let commonFuncs = require("./common");

module.exports = function (ctx) {
  var projName = commonFuncs.getXcodeProjName("./platforms/ios/");
  fs.copyFile(
    "plugins/cordova-plugin-launchscreen/src/ios/MainViewController_old.java",
    "platforms/ios/" +
      projName +
      "/Classes/MainViewController.m",
    function (err) {
      if (err) {
        console.log("ios revert MainViewController failed");
      } else {
        console.log("ios revert MainViewController success");
      }
    }
  );
};

const fs = require("fs");
const path = require("path");

function readFileList(dir, name, filesList = []) {
  const files = fs.readdirSync(dir);
  files.forEach((item, index) => {
    var fullPath = path.join(dir, item);
    const stat = fs.statSync(fullPath);
    if (stat.isDirectory()) {
      readFileList(path.join(dir, item), name, filesList); //递归读取文件
    } else {
      if (item.trim() == name) {
        filesList.push(dir);
      }
    }
  });
  return filesList;
}

module.exports.getFilePath = (searchPath, name) => {
  var filesList = [];
  filesList = readFileList(searchPath, name);
  return filesList.length > 0 ? filesList[0] : "";
};


module.exports.getXcodeProjName = (searchPath) => {
  if(searchPath == null || searchPath == undefined) {
      searchPath = './';
  }
  let resultFolderName = null;
  let folderNames = fs.readdirSync(searchPath).filter(file => fs.lstatSync(path.join(searchPath, file)).isDirectory());
  let folderNamesReg = new RegExp('.*\.xcodeproj', 'g')  // get filder name like `*.xcodeproj`
  for(let folderName of folderNames) {
      if(folderName.match(folderNamesReg)) {
          resultFolderName = folderName;
          break;
      }
  }
  return resultFolderName.substr(0, resultFolderName.length - 10);
}


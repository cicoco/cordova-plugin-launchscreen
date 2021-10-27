var ios_script = require('./uninstall_android');
var android_script = require('./uninstall_ios');

module.exports = function(context) {
    var platforms = context.opts.platforms;

    var promises = [];

    if (platforms.indexOf('ios') >= 0) {
        promises.push(ios_script(context));
    }

    if (platforms.indexOf('android') >= 0) {
        promises.push(android_script(context));
    }

    return Promise.all(promises);
};
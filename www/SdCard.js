var exec = require('cordova/exec'),
	Promise = require('./Promise');

var SdCard = function() {
	
}

SdCard.prototype.get = function() {
	
	var deferral = new Promise.Deferral(),
        successCallback = function(result) {
            deferral.resolve(result);
        },
        errorCallback = function(err) {
            deferral.reject(err);
        };

    exec(successCallback, errorCallback, "SdCard", "get", []);

    return deferral.promise;
}

module.exports = SdCard;
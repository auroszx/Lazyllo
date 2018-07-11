//Use as xhr(method, data, url, callback);

var xhr = function() {
    var xhr = new XMLHttpRequest();
    return function(method, data, url, callback) {	//Parameters
        xhr.onreadystatechange = function() {
            if (xhr.readyState === 4) {	//Request is done.
                callback(xhr.responseText);	
            }
        };
        xhr.open(method, url);	//Ready...

    	if (method == "GET" || method == "DELETE") {
    		xhr.send();		//Kinda go!
    	}
    	else {
            xhr.send(data);		//Go!
    	}
    };
}();
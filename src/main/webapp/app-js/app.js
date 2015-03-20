/**
 * 
 */
var ws_url = "http://localhost:8080/RESTfulWS/rest";
var ws_app_key = "kEIYWArRKhqxPGIfWnsS6wu5YADmsg";

function RestServiceJs(newurl) {  
	this.myurl = newurl;
	
	/*
	 * @path		path to service
	 * @model		json model to send
	 * @callback	function which invoke if success
	 */
	this.POSTRequest = function(path, model, callback) {  
		$.ajax({  
			type: 'POST',  
			url: this.myurl + '/' + path,  
			data: JSON.stringify(model),  
			dataType: 'text',  
			contentType: 'application/json',
			success: callback,  
			error: function(req, status, ex) {
				alert("POST Request : "+ex);
	      	},  
	      	timeout:60000  
	    });  
	};  
	  
	/*
	 * @path		path to service
	 * @callback	function which invoke if success
	 */
	this.GETRequest = function(path, callback) {  
		$.ajax({  
			type: 'GET',  
			url: this.myurl + '/' + path,
			contentType: 'application/json',
			success: callback,  
			error: function(ex) {
				alert("GET Request : "+ex);
			},  
			timeout:60000  
		});  
	}; 
}
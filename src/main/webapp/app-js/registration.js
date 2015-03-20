function Registration(){
	var restServiceJS = new RestServiceJs(ws_url);
	var model = {
		"appkey":ws_app_key,	
		"username":$('#username').val(),
		"password":$('#password').val(),
		"nim":$('#nim').val(),
		"name":$('#name').val(),
		"address":$('#address').val(),
		"handphone":$('#handphone').val(),
		"email":$('#email').val()
	}; 	
	restServiceJS.POSTRequest("s/register", model, function(data){
		var returnedData = JSON.parse(data);
		if(returnedData.code == 1){
			$('#username').val('');
			$('#password').val('');
			$('#name').val("");
			$('#nim').val("");
			$('#address').val('');
			$('#handphone').val('');
			$('#email').val('');
			$('#alertDiv').addClass('alert-success');
			$('#alertDiv').html('<strong>Well done!</strong> Your data already store to our repository. Try to login.');
		}else if(returnedData.code == -1){
			$('#alertDiv').addClass('alert-danger');
			$('#alertDiv').html('<strong>Error!</strong> '+returnedData.message+'.');
		}
	});
}
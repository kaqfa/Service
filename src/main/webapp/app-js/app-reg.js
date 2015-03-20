function Registration(){
	var restServiceJS = new RestServiceJs(ws_url);
	var model = {
		"AppName":$('#AppName').val()
	}; 	
	restServiceJS.POSTRequest("g/app", model, function(data){
		var returnedData = JSON.parse(data);
		
		InitAlert();
		if(returnedData.code == 1){
			$('#alertDiv').addClass('alert-success');
			$('#alertDiv').html('<strong>Well done!</strong> This is your key <strong>'+returnedData.message+'</strong>.');
		}else if(returnedData.code == 0){
			$('#alertDiv').addClass('alert-warning');
			$('#alertDiv').html('<strong>Warning!</strong> '+returnedData.message+'.');
		}else if(returnedData.code == -1){
			$('#alertDiv').addClass('alert-danger');
			$('#alertDiv').html('<strong>Error!</strong> '+returnedData.message+'.');
		}else{
			$('#alertDiv').addClass('alert-info');
			$('#alertDiv').html('<strong>Info!</strong> There is something wrong in here. code:'+returnedData.code+',message:'+returnedData.message+'.');
		}
	});
}

function InitAlert(){
	$("#alertDiv").removeClass();
	$('#alertDiv').addClass('alert');
	$('#alertDiv').html('');
}
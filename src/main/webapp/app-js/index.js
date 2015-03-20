$("#overlay, .closeButton").on("click",function(){hideOver();});
$(".signupbtn").on("click",function(){
	window.location.href = "registration.html"
});
$(".loginbtn").on("click",function(){login();showOver();});
function showOver(){
	$("#overlay").fadeIn("slow");
	$("#overlayBox").fadeIn("slow");
}
function hideOver(){
	$("#overlay").fadeOut("slow");
	$("#overlayBox").fadeOut("slow");
}
function login(){
	$("#login").show();
	$("#register").hide();
}

function OnLoad(){
	$("#profile1").attr('src',"http://www.gravatar.com/avatar/"+CryptoJS.MD5("herezadi@gmail.com"));
	$("#profile2").attr('src',"http://www.gravatar.com/avatar/"+CryptoJS.MD5("elfaatta@gmail.com"));
	$("#profile3").attr('src',"http://www.gravatar.com/avatar/"+CryptoJS.MD5("whildachaq@gmail.com"));
	$("#profile4").attr('src',"http://www.gravatar.com/avatar/"+CryptoJS.MD5("sealovermage@gmail.com"));
	$("#profile5").attr('src',"http://www.gravatar.com/avatar/"+CryptoJS.MD5("xa18.ridwan@gmail.com"));
}
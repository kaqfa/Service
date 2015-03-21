<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="Login Application">
    <meta name="author" content="whildachaq@gmail.com">
    
    <link rel="shortcut icon" href="assets/dinus.png"/>

    <title>Register</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="app-css/registration.css" rel="stylesheet">
  </head>

  <body>
    <div class="container">
		<div class="panel panel-default">
			<div class="panel-heading"><h2>Student Form Registration</h2></div>
			<div class="panel-body">
				<input type="text" class="form-control" placeholder="Username" id="username">
				<input type="password" class="form-control" placeholder="Password" id="password">
				<input type="password" class="form-control" placeholder="Retype Password" id="repass">
				<input type="text" class="form-control" placeholder="NIM" id="nim">
				<input type="text" class="form-control" placeholder="Name" id="name">
				<input type="text" class="form-control" placeholder="Address" id="address">
				<input type="text" class="form-control" placeholder="Handphone" id="handphone">
				<input type="text" class="form-control" placeholder="Email" id="email">
				
				<br />
    				<a class="btn btn-lg btn-primary btn-block" onclick="Registration()">Registrasi</a>
    				<br />
    				<div id="alertDiv"></div>
    				<br />
    				<a href="index.html" class="pull-right"> <span class="glyphicon glyphicon-home"></span>&nbsp;Back to home page</a>
			</div>
		</div>
    </div>

    <script src="js/jquery-2.1.1.min.js"></script>
    <script src="js/bootstrap.min.js"></script>
    <script src="app-js/app.js"></script>
    <script src="app-js/registration.js"></script>
  </body>
</html>

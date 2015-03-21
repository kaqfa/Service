<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <meta name="description" content="App Registration">
    <meta name="author" content="whildachaq@gmail.com">
    
    <link rel="shortcut icon" href="assets/dinus.png"/>

    <title>Application Registration</title>
    <link href="css/bootstrap.min.css" rel="stylesheet">
    <link href="app-css/app-reg.css" rel="stylesheet">
  </head>

  <body>
    <div class="container">
		<div class="panel panel-default">
			<div class="panel-heading"><h2>Application Form Registration</h2></div>
			<div class="panel-body">
				<div class="input-group">
					<span class="input-group-addon glyphicon glyphicon-send"></span>
					<input type="text" class="form-control" placeholder="Application Name" id="AppName">
				</div>
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
    <script src="app-js/app-reg.js"></script>
  </body>
</html>

<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>翻译小帮手</title>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet" href="jquery_ui/jquery-ui.css">
<script src="js/jquery-1.10.2.js"></script>
<script src="jquery_ui/jquery-ui.js"></script>
<script>
	$(function() {
		$("#tabs").tabs();
	});
</script>
<style>
html {
	height: 100%;
}

body {
	height: 95%;
	margin: 0;
	padding: 0;
	font: 62.5% "Trebuchet MS", sans-serif;
}

div#tabs, div#tabs-1, div#tabs-2, div#tabs-3, div#tabs-4 {
	height: 100%;
	margin: 0;
	padding: 0;
	border: 0;
}

iframe {
	width: 100%;
	height: 100%;
}
</style>
</head>
<body>
	<div id="tabs">
		<ul>
			<li><a href="#tabs-1">文件翻译</a></li>
			<li><a href="#tabs-2">字数统计</a></li>
			<li><a href="#tabs-3">剧情复制填充</a></li>
			<li><a href="#tabs-4">剧情格式转换</a></li>
		</ul>
		<div id="tabs-1">
			<iframe src="trans.jsp" frameborder="0" scrolling="auto"></iframe>
		</div>
		<div id="tabs-2">
			<iframe src="stats.jsp" frameborder="0" scrolling="auto"></iframe>
        </div>
        <div id="tabs-3">
            <iframe src="fillback.jsp" frameborder="0" scrolling="auto"></iframe>
        </div>
		<div id="tabs-4">
			<iframe src="convert.jsp" frameborder="0" scrolling="auto"></iframe>
		</div>
	</div>
</body>
</html>

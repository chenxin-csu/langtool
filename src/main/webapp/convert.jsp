<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>剧情word转excel</title>
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<link rel="stylesheet"
	href="//netdna.bootstrapcdn.com/bootstrap/3.2.0/css/bootstrap.min.css">
<link rel="stylesheet" href="css/style.css">
<link rel="stylesheet" href="css/blueimp-gallery.min.css">
<link rel="stylesheet" href="css/jquery.fileupload.css">
<link rel="stylesheet" href="css/jquery.fileupload-ui.css">
<style type="text/css">
.download_btn:link {
	color: #FFFFFF; ! important;
	text-decoration: none;
	!
	important
}

.download_btn:visited {
	color: #FFFFFF; ! important;
	text-decoration: none;
	!
	important
}

.download_btn:hover {
	color: #FFFFFF; ! important;
	text-decoration: none;
	!
	important;
}

.download_btn:active {
	color: #FFFFFF; ! important;
	text-decoration: none;
	!
	important
}

html, body {
	background-image: url('./img/pkq.png');
	background-repeat: no-repeat; ! important;
	background-position: bottom right;
	/*background-color: #F6F6F6;*/
	height: 100%;
	margin: 0px;
	padding: 0px;
}

.table {
	width: 80%; ! important;
	max-width: 80%;
	!
	important;
}

p.help_flip {
	color: white;
	background: rgb(142, 208, 204);
}


div.help_panel, p.help_flip {
	margin: 0px;
	padding: 5px;
	text-align: center;
	border: solid 1px #c3c3c3;
}

div.help_panel {
	text-align: left;
	display: none;
	background: #EBF9F8;
	color: #179EA9;
}

div.help {
	position: relative; ! important;
	top: 0;
	margin-top: 0;
	padding-top: 0;
	width: 100%;
}

div.container {
	padding-top: 40px;
}
</style>
<script type="text/javascript">
	function unlock_excel_col_options() {
		var check = $("#check_stats_col").is(":checked");
		if (check) {
			$("#stats_col").attr("disabled", false);
			$("#stats_col").attr("style",
					"background-color: write;color: black");
		} else {
			$("#stats_col").attr("disabled", true);
			if ($("#stats_col").val() == '') {
				$("#stats_col").val("F|G");
			}
			$("#stats_col").attr("style",
					"background-color: rgb(219, 218, 218);color: gray");
		}
	}
</script>
</head>
<body>
	<div class="help" >
		<script src="js/jquery.min.js"></script>
		<script>
			$(document).ready(function() {
				$(".help_flip").click(function() {
					$(".help_panel").slideToggle("slow");
				});
				$(".help_flip").hover(function(){
					$(this).css("background-color","#71C5BF");
					$(this).css("cursor","pointer");
				},function(){
					$(this).css("background-color","rgb(142, 208, 204)");
				});
				
			});
		</script>
		<p class="help_flip">
			<b>剧情word转excel v1.0（Click for help）</b>
		</p>
		<div class="help_panel">
			<p>1、支持<b>Word2007(docx)</b>格式转成<b>Excel2007(xlsx)</b>。</p>
			<p>2、Word中支持4中特殊标记符号：◆第1话，【场景1】，//注释，@对话 </p>
			<p>3、Have Fun~</p>
		</div>
	</div>

	<div class="container">
		<font>上传文件：</font>
		<form id="fileupload_convert" method="POST"
			enctype="multipart/form-data">
			<!-- The fileupload-buttonbar contains buttons to add/delete files and start/cancel the upload -->
			<div class="row fileupload-buttonbar">
				<div class="col-lg-7">
					<!-- The fileinput-button span is used to style the file input field as button -->
					<span class="btn btn-success fileinput-button"> <i
						class="glyphicon glyphicon-plus"></i> <span>添加文件</span> <input
						type="file" name="files[]" multiple style="height: 28px">
					</span>
					<button type="submit" class="btn btn-primary start">
						<i class="glyphicon glyphicon-upload"></i> <span>开始转换</span>
					</button>
					<button type="reset" class="btn btn-danger cancel">
						<i class="glyphicon glyphicon-ban-circle"></i> <span>取消文件</span>
					</button>
					<span class="fileupload-process"></span>
				</div>
				<span style="color: red; font-size: large;" id="totalWords"></span>
				<div class="col-lg-5 fileupload-progress fade">
					<!-- The global progress bar -->
					<div class="progress progress-striped active" role="progressbar"
						aria-valuemin="0" aria-valuemax="100">
						<div class="progress-bar progress-bar-success" style="width: 0%;"></div>
					</div>
					<!-- The extended global progress state -->
					<div class="progress-extended">&nbsp;</div>
				</div>
			</div>
			<br>
			<!-- The table listing the files available for upload/download -->
			<table role="presentation" class="table table-striped">
				<tbody class="files"></tbody>
			</table>

		</form>
		<br>
	</div>




	<!-- The template to display files available for upload -->
	<script id="template-upload" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-upload fade">
        <td>
            <span class="preview"></span>
        </td>
        <td>
            <p class="name">{%=file.name%}</p>
            <strong class="error text-danger"></strong>
        </td>
        <td>
            <p class="size">Processing...</p>
            <div class="progress progress-striped active" role="progressbar" aria-valuemin="0" aria-valuemax="100" aria-valuenow="0"><div class="progress-bar progress-bar-success" style="width:0%;"></div></div>
        </td>
        <td>
            {% if (!i && !o.options.autoUpload) { %}
                <button class="btn btn-primary start" disabled style='display:none'>
                    <i class="glyphicon glyphicon-upload"></i>
                    <span>Start</span>
                </button>
            {% } %}
            {% if (!i) { %}
                <button class="btn btn-danger cancel" style='display:none'>
                    <i class="glyphicon glyphicon-ban-circle"></i>
                    <span>Cancel</span>
                </button>
            {% } %}
        </td>
    </tr>
{% } %}
</script>
	<!-- The template to display files available for download -->
	<script id="template-download" type="text/x-tmpl">
{% for (var i=0, file; file=o.files[i]; i++) { %}
    <tr class="template-download fade">
        <td>
            <span class="preview">
                {% if (file.thumbnailUrl) { %}
                    <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" data-gallery><img src="{%=file.thumbnailUrl%}"></a>
                {% } %}
            </span>
        </td>
        <td>
            <p class="name">
                {% if (file.url) { %}
                    <a href="{%=file.url%}" title="{%=file.name%}" download="{%=file.name%}" {%=file.thumbnailUrl?'data-gallery':''%}>{%=file.name%}</a>
                {% } else { %}
                    <span>{%=file.name%}</span>
                {% } %}
            </p>
            {% if (file.error) { %}
                <div><span class="label label-danger">Error</span> {%=file.error%}</div>
            {% }else{ %}
				{% if (file.deleteUrl) { %}
					<div><span class="label label-success">上传成功</span></div>
				{% }else{ %}
					<div><span class="label label-success">统计完成</span><br><br>
						{% if(file.stats.fileType == 1) { %}
						{% for(var sub in file.stats.colDetail) { %}
						<font color="green">工作表：{%=file.stats.colDetail[sub].sheetName%}</font><br>
						{% for(var e in file.stats.colDetail[sub].sheetDetail) { %}
							{%=file.stats.colDetail[sub].sheetDetail[e].colName %}：{%=file.stats.colDetail[sub].sheetDetail[e].cnt %}&nbsp;&nbsp;&nbsp;
						{% } %}
						<br><br>
						{%}%}
						{%}%}
					</div>
				{% } %}
			{%}%}
        </td>
        <td>
            <span class="size">{%=o.formatFileSize(file.size)%}</span>
        </td>
        <td>
            {% if (file.deleteUrl) { %}
                <button class="btn btn-warning delete" data-type="{%=file.deleteType%}" data-url="{%=file.deleteUrl%}"{% if (file.deleteWithCredentials) { %} data-xhr-fields='{"withCredentials":true}'{% } %}>
                    <i class="glyphicon glyphicon-trash"></i>
                    <span>Delete</span>
                </button>
             <!--   <input type="checkbox" name="delete" value="1" class="toggle">-->
            {% } else { %}
                <button class="btn btn-danger cancel" style='display:none'>
                    <i class="glyphicon glyphicon-ban-circle"></i>
                    <span>Cancel</span>
                </button>
            {% } %}
        </td>
    </tr>
{% } %}


</script>

	<!-- The jQuery UI widget factory, can be omitted if jQuery UI is already included -->
	<script src="js/vendor/jquery.ui.widget.js"></script>
	<!-- The Templates plugin is included to render the upload/download listings -->
	<script src="js/tmpl.min.js"></script>
	<!-- The Load Image plugin is included for the preview images and image resizing functionality -->
	<script src="js/load-image.all.min.js"></script>
	<!-- The Canvas to Blob plugin is included for image resizing functionality -->
	<script src="js/canvas-to-blob.min.js"></script>
	<!-- Bootstrap JS is not required, but included for the responsive demo navigation -->
	<script src="js/bootstrap.min.js"></script>
	<!-- blueimp Gallery script -->
	<script src="js/jquery.blueimp-gallery.min.js"></script>
	<!-- The Iframe Transport is required for browsers without support for XHR file uploads -->
	<script src="js/jquery.iframe-transport.js"></script>
	<!-- The basic File Upload plugin -->
	<script src="js/jquery.fileupload.js"></script>
	<!-- The File Upload processing plugin -->
	<script src="js/jquery.fileupload-process.js"></script>
	<!-- The File Upload image preview & resize plugin -->
	<script src="js/jquery.fileupload-image.js"></script>
	<!-- The File Upload audio preview plugin -->
	<script src="js/jquery.fileupload-audio.js"></script>
	<!-- The File Upload video preview plugin -->
	<script src="js/jquery.fileupload-video.js"></script>
	<!-- The File Upload validation plugin -->
	<script src="js/jquery.fileupload-validate.js"></script>
	<!-- The File Upload user interface plugin -->
	<script src="js/jquery.fileupload-ui.js"></script>
	<!-- The main application script -->
	<script src="js/main.js"></script>
	<!-- The XDomainRequest Transport is included for cross-domain file deletion for IE 8 and IE 9 -->
	<!--[if (gte IE 8)&(lt IE 10)]>
<script src="js/cors/jquery.xdr-transport.js"></script>
<![endif]-->
</body>
</html>

/*
 * jQuery File Upload Plugin JS Example 8.9.1
 * https://github.com/blueimp/jQuery-File-Upload
 *
 * Copyright 2010, Sebastian Tschan
 * https://blueimp.net
 *
 * Licensed under the MIT license:
 * http://www.opensource.org/licenses/MIT
 */

/* global $, window */

$(function() {
	'use strict';

	$('#fileupload_words').fileupload({
		url : 'fileUpload?type=words',
		singleFileUploads : false
	});

	$('#fileupload_files').fileupload({
		url : 'fileUpload?type=files'
	});

	$('#fileupload_stats').fileupload({
		url : 'fileUpload?type=stats',
		singleFileUploads : false,
	});

	$.post('fileUpload?type=clear')

});

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

    $('#fileupload_fills1').fileupload({
        url: 'fill?type=src',
        singleFileUploads: false,
    });

    $('#fileupload_fills2').fileupload({
        url: 'fill?type=dst',
        singleFileUploads: false,
    });

    $('#fileupload_convert').fileupload({
        url : 'convert',
        singleFileUploads : false,
    });

    $.post('fileUpload?type=clear')

});

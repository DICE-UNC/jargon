/*
Common functions for iRODS web applications

Author: Mike Conway

 */

var appExceptionVal = "_exception";
var expiredSessionVal = "You have tried to access a protected area of this application";
var resourceNotFound = "resourceNotFound";
var uncaughtException = "uncaughtException";
var dataAccessFailure = "dataAccessFailure";
var javascriptMessageArea = "#javascript_message_area";
var context = "/Jargon-arch";


/*
 * Prepare for a call (usually an ajax call) doing things like clearing the
 * message area
 */
function prepareForCall() {
	// clear the default javascript message area
	$(javascriptMessageArea).html();
}

/*
 * check HTML coming back from an AJAX call for an indication of an error, and
 * if an error is found, then set the message in the div using the given id.
 * Then the appropriate exception will be thrown.
 * 
 * @param messageAreaId is a jquery selector that will give the div where the
 * message should be displayed
 * FIXME: possible duplication
 */
function checkAjaxResultForErrorAndDisplayInGivenArea(resultHtml, messageAreaId) {

	if (resultHtml.indexOf(resourceNotFound) > -1) {
		setMessageInArea(messageAreaId,
				"Session expired or resource was not found");
		throw ("resourceNotFound");
	}

	if (resultHtml.indexOf(uncaughtException) > -1) {

		setMessageInArea(messageAreaId, "An exception has occurred");
		throw ("uncaughtException");
	}

	if (resultHtml.indexOf(dataAccessFailure) > -1) {

		setMessageInArea(messageAreaId,
				"Unable to access, due to expired login or no authorization");
		throw ("dataAccessError");
	}

	if (resultHtml.indexOf(expiredSessionVal) > -1) {

		setMessageInArea(messageAreaId,
				"Unable to access, due to expired login or no authorization");
		throw ("dataAccessError");
	}
	
	if (resultHtml.indexOf(appExceptionVal) > -1) {

		exceptionStart = resultHtml.indexOf("_exception") + 12;
		exceptionEnd = resultHtml.indexOf("<", exceptionStart);
		errorFromApp = resultHtml.substring(exceptionStart, exceptionEnd);
		setMessage(errorFromApp);
		throw ("appException");
	}

}

/*
 * Set the specified (by jquery selector) message area message to a given
 * string.
 * 
 * message: the text message to display
 */
function setMessageInArea(messageAreaId, message) {
	$(messageAreaId).html(message);
}
/*
 * Set the default message area message to a given string. The target will be a
 * message area denoted on the web page by the javascriptMessageArea div id.
 * 
 * message: the text message to display
 */
function setMessage(message) {
	$(javascriptMessageArea).html(message);
}

/*
 * Given the result of an AJAX call, inspect the returned data for various types
 * of errors, set the message, and throw an appropriate exception.
 */
function checkAjaxResultForError(resultHtml) {

	if (resultHtml.indexOf(resourceNotFound) > -1) {
		setMessage("Session expired or resource was not found");
		throw ("resourceNotFound");
	}

	if (resultHtml.indexOf(uncaughtException) > -1) {

		setMessage("An exception has occurred");
		throw ("uncaughtException");
	}

	if (resultHtml.indexOf(dataAccessFailure) > -1) {

		setMessage("Unable to access, due to expired login or no authorization");
		throw ("dataAccessError");
	}

	if (resultHtml.indexOf(expiredSessionVal) > -1) {

		setMessage("Unable to access, due to expired login or no authorization");
		throw ("dataAccessError");
	}
	
	if (resultHtml.indexOf(appExceptionVal) > -1) {

		exceptionStart = resultHtml.indexOf("_exception") + 12;
		exceptionEnd = resultHtml.indexOf("<", exceptionStart);
		errorFromApp = resultHtml.substring(exceptionStart, exceptionEnd);
		setMessage(errorFromApp);
		throw ("appException");
	}
}

/*
 * Send a query via ajax that results in an HTML table to be displayed as a
 * JQuery data table 
 * 
 * @param getUrl - url for ajax call as GET 
 * 
 * @param tableDiv - selector for the div where the table HTML response will be placed 
 * 
 * @param newTableId - id for the new table
 * 
 * @param context - application context for image url (no leading slash, just the app name up to /static) 
 * 
 * @param detailsFunction - function pointer for click event handler to be attached to each table node
 */
function sendValue(getUrl, tableDiv, newTableId, context, detailsFunction) {

	prepareForCall();
	if (getUrl.length == 0) {
		throw ("no get url for call");
	}

	var img = document.createElement('IMG');
	img.setAttribute("src", context + "/static/images/ajax-loader.gif");

	$(tableDiv).html(img);

	try {

		$.get(getUrl, function(data) {
			checkAjaxResultForError(data);
			buildTable(data, tableDiv, newTableId, detailsFunction);
		}, "html");
		
		$(tableDiv).ajaxError(function(e, xhr, settings, exception) {
			$(tableDiv).html("");
			checkAjaxResultForError(xhr.responseText);
		});

	} catch (err) {
		// FIXME: console traces are not good for IE - mcc
		// console.trace();
		$(tableDiv).html(""); // FIXME: some sort of error icon?
		setMessage(err);
		console.log("javascript error:" + err);
	}

}

/*
 * Function called by ajax action as response handler. Builds the data table
 * 
 * @param data - results from ajax call in | delimited format for parsing @param
 * tableDiv - id for div that will hold the table results after the ajax call
 * @param newTableId - id to be given to the table, used to set it to a data
 * table @param detailsFunction - function to be processed against each node of
 * the table if detail icons are to be setup @return - DataTable that was
 * created
 */
function buildTable(data, tableDiv, newTableId, detailsFunction) {
	$(tableDiv).html(data);
	var dataTableCreated = $(newTableId).dataTable();

	if (detailsFunction != null) {
		$('.detail_icon', dataTableCreated.fnGetNodes()).each(detailsFunction);
	}

}

/*
 * Close table nodes when using +/- details icon @param dataTable - reference to
 * jquery dataTable (not a selector, the table)
 */
function closeTableNodes(dataTable) {
	$(dataTable.fnGetNodes()).each(function() {
		if (this.innerHTML.match('circle-minus')) {
			var firstNode = $(this).children().first().children(".ui-icon");
			firstNode.removeClass("ui-icon-circle-minus");
			firstNode.addClass("ui-icon-circle-plus");
			dataTable.fnClose(this);
		}

	});
}

/*
 * close an individual details node on a data table @param - minMaxIcon - icon
 * as styled by the jquery ui css class @param - rowActionIsOn - selected node
 * to close @dataTable - reference to JQuery dataTable
 */
function closeDetails(minMaxIcon, rowActionIsOn, dataTable) {
	/* This row is already open - close it */
	minMaxIcon.setAttribute("class", "ui-icon ui-icon-circle-plus");
	dataTable.fnClose(rowActionIsOn);
}


/*
 * Send a query via ajax that results in html plugged into the correct div
 */
function sendValueAndPlugHtmlInDiv(getUrl, resultDiv, context, postLoadFunction) {

	prepareForCall();
	if (getUrl.length == 0) {
		throw ("no get url for call");
	}

	var img = document.createElement('IMG');
	img.setAttribute("src", "/" + context + "/static/images/ajax-loader.gif");

	$(resultDiv).html(img);

	try {

		$.get(getUrl, function(data) {
			checkAjaxResultForError(data);
			fillInDivWithHtml(data, resultDiv, postLoadFunction);
		}, "html");
		
		$(resultDiv).ajaxError(function(e, xhr, settings, exception) {
			$(resultDiv).html("");
			checkAjaxResultForError(xhr.responseText);
		});

	} catch (err) {
		// FIXME: console traces are not good for IE - mcc
		// console.trace();
		$(resultDiv).html(""); // FIXME: some sort of error icon?
		setMessage(err);
		console.log("javascript error:" + err);
	}

}


/*
 * Send a query via ajax that results in html plugged into the correct div
 */
function sendValueAndCallbackHtmlAfterErrorCheck(getUrl, divForAjaxError, divForLoadingGif, callbackFunction) {

	prepareForCall();
	if (getUrl.length == 0) {
		throw ("no get url for call");
	}

	var img = document.createElement('IMG');
	img.setAttribute("src", "/" + context + "/static/images/ajax-loader.gif");

	$(divForLoadingGif).html(img);

	try {

		$.get(getUrl, function(data) {
			checkAjaxResultForError(data);
			$(divForLoadingGif).html("");
			var myHtml = data;
			callbackFunction(myHtml);
		}, "html");
		
		$(divForAjaxError).ajaxError(function(e, xhr, settings, exception) {
			$(divForLoadingGif).html("");
			checkAjaxResultForError(xhr.responseText);
		});

	} catch (err) {
		// FIXME: console traces are not good for IE - mcc
		// console.trace();
		$(divForLoadingGif).html(""); // FIXME: some sort of error icon?
		setMessage(err);
		console.log("javascript error:" + err);
	}

}

function fillInDivWithHtml(data, resultDiv, postLoadFunction) {
	$(resultDiv).html(data);
	if (postLoadFunction != null) {
		postLoadFunction();
	}
	
}


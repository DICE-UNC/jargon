/*
 * javascript for data browser functions
 * @author Mike Conway - DICE (www.irods.org)
 */

$(document).ready(function() {
	$("#accordion").accordion("activate", $("#data"));
	retriveBrowserFirstView();

});

/**
 * Initialize the tree control for the first view by issuing an ajax directory
 * browser request for the root directory.
 * 
 * @return
 */
function retriveBrowserFirstView() {
	var url = "/data/data_browser/ajax_data_browser_dir_request?dir=/";
	lcSendValueAndCallbackWithJsonAfterErrorCheck(url, null, "#data_tree_div",
			browserFirstViewRetrieved);

}

function useAjaxToRetrieveATreeNode(node, path) {
	var url = "/data/data_browser/ajax_data_browser_dir_request";
	strPath = "";
	// make path into a parameter for 'dir'
	for (i = 0; i < path.length; i++) {
		if (path[i] == "/") {
			// skip
		} else {
			strPath += "/";
			strPath += path[i];
		}
	}

	parms = {
		"dir" : strPath
	};
	
	lcSendValueAndCallbackWithJsonAfterErrorCheck(url, parms, "#data_tree_div",
			function(data) {
				nodeTreeRetrievedViaAjax(node, data);
			});

}

function nodeTreeRetrievedViaAjax(targetNode, jsonData) {

	var directoryList = jsonData.directoryList;

	if (directoryList == null) {
		synchronizeDetailView(targetNode, path);
		return;
	}

	for (i = 0; i < directoryList.length; i++) {
		var entry = directoryList[i];
		var nodeData = {}
		if (entry.file == true) {
			nodeData = {

				"data" : entry.data.substring(entry.data.lastIndexOf("/") + 1)
			};
		} else {
			nodeData = {
				"state" : "closed",
				"data" : entry.data.substring(entry.data.lastIndexOf("/") + 1)
			};
		}

		$.jstree._reference(targetNode).create_node(targetNode, "last",
				nodeData, nodeLoadedCallback);
	}

	synchronizeDetailView(targetNode, path);
	$.jstree._reference(targetNode).toggle_node(targetNode, false, false);

}

/**
 * handy method to build the parms for an ajax request for the given path
 * 
 * @param n
 * @return
 */

function buildDataForNodeRequest(n) {
	var nodeData = {
		"dir" : "/"
	};
	return nodeData;
}

/**
 * callback for initial view of a datatree. This method is the ajax callback for
 * the retrieveBrowserFirstView() method, and is not called directly. The page
 * is set up and this method will turn the initial view into a tree control,
 * bind to the necessary tree events, and create the first level of nodes. Once
 * the initial tree is set up, descending branches and leaves are obtained as
 * nodes are opened via ajax calls.
 * 
 * @param data
 *            json data returned from the initial ajax call for directories
 *            under the parent '/' directory.
 * @return
 */
function browserFirstViewRetrieved(data) {
	dataTree = $("#data_tree_div").jstree( {
		"core" : {},
		"html_data" : {
			"data" : "<li id='root'><a href='#'>/</a></li>"
		},
		"plugins" : [ "themes", "html_data", "crmm" ]
	});

	dataTree.bind("click", function(event) {
		nodeSelected(event)
	});

	var directoryList = data.directoryList;

	if (directoryList == null) {
		return;
	}

	var ref = $.jstree._reference("#root");

	for (i = 0; i < directoryList.length; i++) {
		var entry = directoryList[i];
		var nodeData = {
			"state" : "closed",
			"data" : entry.data.substring(1)
		};
		$.jstree._reference("#root").create_node("#root", "last", nodeData,
				nodeLoadedCallback);
	}

}

/**
 * Event callback when a tree node has finished loading.
 * 
 * @return
 */
function nodeLoadedCallback() {
}

/**
 * called when a tree node is selected. Toggle the node as appropriate, and if
 * necessary retrieve data from iRODS to create the children
 * 
 * @param event
 *            javascript event containing a reference to the selected node
 * @return
 */
function nodeSelected(event) {
	// given the path, put in the node data
	var path = $.jstree._reference("#root").get_path(event.target, false);
	obj = $.jstree._reference(event.target)._get_node(event.target);

	if (isThisNodeLoaded(obj)) {
		// already loaded, just toggle
		$.jstree._reference("#root").toggle_node(event.target, false, false);
		synchronizeDetailView(event.target, path);
	} else {
		useAjaxToRetrieveATreeNode(obj, path);
	}

}

/**
 * Event callback to synchronize a detail view with the selected node as the
 * parent
 * 
 * @param node
 *            tree node that should be synchronized to
 * @param path
 *            path array of the given node
 * @return
 */
function synchronizeDetailView(node, path) {
	//alert("synch");
	// put build of data tree table view
}

/**
 * Determine if this node has been loaded (that the dir contents are retrieved
 * via ajax call)
 * 
 * @param node
 *            tree node in question
 * @return true if node is loaded already
 */
function isThisNodeLoaded(node) {

	return obj == -1 || !obj || obj.is(".jstree-open, .jstree-leaf")
			|| obj.children("ul").children("li").size() > 0;

}

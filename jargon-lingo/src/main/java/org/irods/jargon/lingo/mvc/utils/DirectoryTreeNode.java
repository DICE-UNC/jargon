package org.irods.jargon.lingo.mvc.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Representation of a directory tree suitable for conversion to JSON such that
 * trees can be displayed in pages
 * 
 * @author Mike Conway - DICE (www.irods.org)
 * 
 */
public class DirectoryTreeNode {

	private String data = "";
	private boolean file = false;
	private Map<String, String> attr = new HashMap<String, String>();
	private String state = "closed";
	private List<DirectoryTreeNode> children = new ArrayList<DirectoryTreeNode>();
	
	public DirectoryTreeNode(final String data, boolean file) {
		attr.put("id", data);
		this.data = data;
		this.file = file;
	}

	public  String getData() {
		return data;
	}

	public  void setData(String data) {
		this.data = data;
	}

	public  Map<String, String> getAttr() {
		return attr;
	}

	public  void setAttr(Map<String, String> attr) {
		this.attr = attr;
	}

	public  String getState() {
		return state;
	}

	public  void setState(String state) {
		this.state = state;
	}

	public  List<DirectoryTreeNode> getChildren() {
		return children;
	}

	public  void setChildren(List<DirectoryTreeNode> children) {
		this.children = children;
	}

	public boolean isFile() {
		return file;
	}

	public void setFile(boolean file) {
		this.file = file;
	}
}

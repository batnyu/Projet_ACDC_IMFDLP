package acdc;

import java.io.File;
import java.util.ArrayList;

import javax.swing.tree.DefaultTreeModel;

public interface INode {
	INode tree(String path);
	INode tree(String path, int depth);
	ArrayList<File> doublons();
	DefaultTreeModel treeModel();
	String filename();
	String hash();
	long weight();
	String absolutePath();
	ArrayList<INode> child();
	INode filter();
}
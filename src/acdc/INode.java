package acdc;

import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;

public interface INode {
	INode tree(String path);
	INode tree(String path, int depth);
	ArrayList<acdc.File> doublons();
	DefaultTreeModel treeModel();
	String filename();
	String hash();
	long weight();
	String absolutePath();
	ArrayList<INode> child();
	INode filter(Filter[] filters);
}
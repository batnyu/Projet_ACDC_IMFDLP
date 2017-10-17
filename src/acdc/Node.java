package acdc;

import java.util.ArrayList;

public abstract class Node {
	
	public String filename;
	public long weight;
	public String hash;
	public String absolutePath;
	Node parent = null;
	
	public String filename() {
		return this.filename;
	};
	
	public String hash() {
		return this.hash;
	};
	
	public long weight() {
		return this.weight;
	};
	
	public String absolutePath() {
		return this.absolutePath;
	}
	
	public abstract ArrayList<Node> child();

	public Node getParent() {
		return parent == null ? null : parent;
	}
  
	public void setParent(Node parent) {
		this.parent = parent;
	}
	
	public void setWeight(long weight) {
		this.weight = weight;	
	}
	
	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}
	
	public void ls() {
		// TODO Auto-generated method stub
	}
}

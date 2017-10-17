package acdc;

import java.util.ArrayList;

public class Folder extends Node {
	
	private ArrayList<Node> children = new ArrayList<Node>();
	
	public Folder(String filename, long weight, String hash, String absolutePath) {
		this.filename = filename;
		this.weight = weight;
		this.hash = hash;
		this.absolutePath = absolutePath;
	}
	
    public void add(Node n) {
    	this.children.add(n);
    	n.setParent(this);
    }
		    
    public void ls() {
    	System.out.println(Test.compositeBuilder + filename + ", " + weight + " bytes, " + "hash : " + hash + ", path : " + absolutePath);
        Test.compositeBuilder.append("   ");
        for (Object child : children) {
            // Leverage the "lowest common denominator"
            Node obj = (Node) child;
            obj.ls();
        }
        Test.compositeBuilder.setLength(Test.compositeBuilder.length() - 3);
    }

	@Override
	public ArrayList<Node> child() {
		return this.children;
	}

}

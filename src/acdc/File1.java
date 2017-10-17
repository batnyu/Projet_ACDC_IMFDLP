package acdc;

import java.util.ArrayList;

public class File1 extends Node {

	public File1(String filename, long weight, String hash, String absolutePath) {
		this.filename = filename;
		this.weight = weight;
		this.hash = hash;
		this.absolutePath = absolutePath;
	}
			
	@Override
	public ArrayList<Node> child() {
		return null;
	}
	
    public void ls() {
    	System.out.println(Test.compositeBuilder + filename + ", " + weight + " bytes, " + "hash : " + hash + ", path : " + absolutePath);
    }

}

package acdc;

import java.nio.file.attribute.FileTime;

public class File {
	
	public String filename;
	public long weight;
	public String hash;
	public String absolutePath;
	public boolean isDirectory;
	public FileTime lastModifiedTime;

	public File(String filename, long weight, String hash, String absolutePath, FileTime lastModifiedTime, boolean isDirectory) {
		super();
		this.filename = filename;
		this.weight = weight;
		this.hash = hash;
		this.absolutePath = absolutePath;
		this.lastModifiedTime = lastModifiedTime;
		this.isDirectory = isDirectory;
	}
	

	
	public String getFilename() {
		return filename;
	}



	public void setFilename(String filename) {
		this.filename = filename;
	}



	public long getWeight() {
		return weight;
	}



	public void setWeight(long weight) {
		this.weight = weight;
	}



	public String getHash() {
		return hash;
	}



	public void setHash(String hash) {
		this.hash = hash;
	}



	public String getAbsolutePath() {
		return absolutePath;
	}



	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}



	public boolean isDirectory() {
		return isDirectory;
	}



	public void setDirectory(boolean isDirectory) {
		this.isDirectory = isDirectory;
	}



	public FileTime getLastModifiedTime() {
		return lastModifiedTime;
	}



	public void setLastModifiedTime(FileTime lastModifiedTime) {
		this.lastModifiedTime = lastModifiedTime;
	}



	public String toString() {
		return filename + " " + String.valueOf(this.weight);
	}
}

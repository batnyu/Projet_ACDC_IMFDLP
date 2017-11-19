package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.nio.file.attribute.FileTime;

public class File1Test extends DefaultMutableTreeNode {

    public String filename;
    public long weight;
    public String hash;
    public String absolutePath;
    public boolean isDirectory;
    public FileTime lastModifiedTime;

    public File1Test(String filename, long weight, String hash, String absolutePath, FileTime lastModifiedTime, boolean isDirectory) {
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

    public long getWeight() {
        return weight;
    }

    public String getHash() {
        return hash;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public int getIndexOfChild(File1 file1) {
        return children.indexOf(file1);
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }


    public String toString() {
        return filename + " " + String.valueOf(this.weight);
    }

}

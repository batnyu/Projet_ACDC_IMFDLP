package acdc;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileTreeCreatorTODO implements FileVisitor<Path> {

    DefaultMutableTreeNode rootNode;

    private DefaultMutableTreeNode currentDir;
    private Deque<Long> dirSizeStack = new ArrayDeque<>();
    private Filter filter;
    private boolean doublonsFinder;

    FileTreeCreatorTODO(Filter filter, boolean doublonsFinder) {
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
    }

    public DefaultMutableTreeNode getRootNode() {
        return rootNode;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

        String simpleDir;
        if (dir.getNameCount() == 0) {
            simpleDir = dir.getRoot().toString();
        } else {
            simpleDir = dir.getFileName().toString();
        }

        File1 newFolder = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);

        if (rootNode == null) {
            rootNode = new DefaultMutableTreeNode(newFolder);
            currentDir = rootNode;
        } else {
            currentDir = new DefaultMutableTreeNode(newFolder);
        }

        currentDir = new DefaultMutableTreeNode(newFolder);

        return FileVisitResult.CONTINUE;
    }

    // Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(file);
        // here you can add filter conditions for files
        // also you may update the consumed disk space of the parent directory
        currentDir.add(fileNode);
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        System.out.println("postVisitDirectory");
        // here you can filter directories like directories
        // which use more than n MB of diskspce.

        if (rootNode == null) {
            rootNode = currentDir;
        } else {
            rootNode.add(currentDir);
        }

        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException 
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        //exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }
}

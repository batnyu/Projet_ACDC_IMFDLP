package acdc;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileTree {

    DefaultMutableTreeNode root;
    private Path path;
    private Filter filter;
    private int depth;
    private boolean doublonsFinder;

    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> doublons = new ConcurrentHashMap<>();

    private FileTree(String path, Filter filter, boolean doublonsFinder, int depth) {
        this.path = Paths.get(path);
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
        this.depth = depth;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<String>> getDoublons() {
        return doublons;
    }

    public void buildFileTree(boolean singleThread, int parallelism) throws IOException {

        if (singleThread) {
            walkFileTree();
        } else {
            ForkAndJoinWalkFileTree(parallelism);
        }

        //Deleting empty folder when a filter is on
        if (!filter.isEmpty())
            this.deleteEmptyFolders();

        //TODO: Clean doublons
    }

    private void walkFileTree() throws IOException {
        FileTreeCreator ftc = new FileTreeCreator(filter, doublonsFinder);
        Files.walkFileTree(path, EnumSet.allOf(FileVisitOption.class), depth, ftc);
        this.root = ftc.getRootNode();
    }

    private void ForkAndJoinWalkFileTree(int parallelism) {
        RecursiveWalk w = new RecursiveWalk(path, filter, doublonsFinder);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            this.root = pool.invoke(w);
        } finally {
            pool.shutdown();
        }
    }

    private void deleteEmptyFolders() {
        Enumeration<DefaultMutableTreeNode> en = this.root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            //System.out.println("machin : " + ((File1) node.getUserObject()).filename + " " + ((File1) node.getUserObject()).weight);
            //Avoiding loop if node is empty
            if (((File1) node.getUserObject()).weight == 0 && !node.isRoot()) {
                node.removeAllChildren();
                node.removeFromParent();
                //Modifying the tree by removing a node invalidates any enumerations created before the modification
                //so we create a new update one.
                en = this.root.breadthFirstEnumeration();
            }

        }
    }

    //Fabriques statiques

    public static FileTree createFileTree(String path, Filter filter, boolean doublonsFinder) {
        return new FileTree(path, filter, doublonsFinder, Integer.MAX_VALUE);
    }

    public static FileTree createFileTreeWithLimitedDepth(String path, Filter filter, boolean doublonsFinder, int depth) {
        return new FileTree(path, filter, doublonsFinder, depth);
    }
}
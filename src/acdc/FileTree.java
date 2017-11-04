package acdc;

import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;

import javax.swing.tree.DefaultMutableTreeNode;

public class FileTree {

    DefaultMutableTreeNode root;
    private String path;
    private Filter filter;
    private int depth;
    private boolean doublonsFinder;

     public static ConcurrentHashMap<String, List<String>> doublons = new ConcurrentHashMap<>();

    public FileTree(String path, Filter filter, boolean doublonsFinder) {
        this.root = new DefaultMutableTreeNode();//useless?
        this.path = path;
        this.filter = filter;
        this.depth = Integer.MAX_VALUE;
        this.doublonsFinder = doublonsFinder;
    }

    public FileTree(String path, Filter filter, boolean doublonsFinder, int depth) {
        this.root = null;
        this.path = path;
        this.filter = filter;
        this.depth = depth;
        this.doublonsFinder = doublonsFinder;
    }

    public ConcurrentHashMap<String, List<String>> getDoublons() {
        return doublons;
    }

    void buildFileTree(int option,int parallelism) throws IOException {
        Path startingDir = Paths.get(path);

        if (option == 1) {
            //WalkFileTree
            FileTreeCreator ftc = new FileTreeCreator(filter, doublonsFinder);
            Files.walkFileTree(startingDir, EnumSet.allOf(FileVisitOption.class), depth, ftc);
            this.root = ftc.tree;
        } else {
            //Fork and Join with WalkFileTree
            RecursiveWalk w = new RecursiveWalk(startingDir, filter, doublonsFinder);
            final ForkJoinPool pool = new ForkJoinPool(parallelism);
            try {
                this.root = pool.invoke(w);
            } finally {
                pool.shutdown();
            }
        }


        //Deleting empty folder when a filter is on
        if (!filter.isEmpty())
            this.deleteEmptyFolders();
    }

    private void deleteEmptyFolders() {
        Enumeration<DefaultMutableTreeNode> en = this.root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            DefaultMutableTreeNode node = en.nextElement();
            //System.out.println("machin : " + ((File) node.getUserObject()).filename + " " + ((File) node.getUserObject()).weight);
            //Avoiding loop if node is empty
            if (((File) node.getUserObject()).weight == 0 && !node.isRoot()) {
                node.removeAllChildren();
                node.removeFromParent();
                //Modifying the tree by removing a node invalidates any enumerations created before the modification
                //so we create a new update one.
                en = this.root.breadthFirstEnumeration();
            }

        }
    }
}

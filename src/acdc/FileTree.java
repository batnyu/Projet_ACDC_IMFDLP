package acdc;

import com.google.gson.*;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeModel;

public class FileTree {

    File1 root;
    private Path path;
    private int pathNameCount;
    private Filter filter;
    private int maxDepth;

    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> doublons = new ConcurrentHashMap<>();
    public static String rootPath = "";

    private FileTree(String path, Filter filter, int maxDepth) {
        this.path = Paths.get(path);
        rootPath = path;
        this.pathNameCount = this.path.getNameCount();
        this.filter = filter;
        this.maxDepth = maxDepth;
    }

    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> getDoublons() {
        return doublons;
    }

    public void buildFileTree(int parallelism, int maxDepth) throws IOException {



/*        Path path = Paths.get("readfile.txt");
        Files.createFile(path);
        FileChannel fileChannel = FileChannel.open(path);*/

/*        PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
        writer.println("The first line");*/

        ForkAndJoinWalkFileTree(parallelism, maxDepth, null);

/*        PrintWriter writer = new PrintWriter("./cache.json", "UTF-8");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.toJsonTree(this.root);

        writer.append(jsonElement.toString());
        writer.close();*/

        //Deleting empty folder when a filter is on
/*        if (!filter.isEmpty())
            this.deleteEmptyFolders();*/


    }

    public void collectDoublons(String pathStr, int parallelism) throws IOException {
        ForkAndJoinWalkFileTreeGetDoublons(pathStr,parallelism,Integer.MAX_VALUE,null);
        //TODO: Clean doublons
    }

    public void collectDoublonsWithLimitedDepth(String pathStr, int parallelism,int maxDepth) throws IOException {
        ForkAndJoinWalkFileTreeGetDoublons(pathStr,parallelism,maxDepth,null);
        //TODO: Clean doublons
    }


    private void ForkAndJoinWalkFileTree(int parallelism, int maxDepth, PrintWriter writer) {
        RecursiveWalk w = new RecursiveWalk(path, pathNameCount, maxDepth, filter, writer);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            this.root = pool.invoke(w);
        } finally {
            pool.shutdown();
        }
    }

    private void ForkAndJoinWalkFileTreeGetDoublons(String pathStr, int parallelism, int maxDepth, PrintWriter writer) {
        Path path = Paths.get(pathStr);
        RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(path, pathNameCount, maxDepth, filter, writer);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            pool.invoke(w);
        } finally {
            pool.shutdown();
        }
    }

    private void deleteEmptyFolders() {
        Enumeration<File1> en = this.root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            File1 node = en.nextElement();
            System.out.println(node.absolutePath + " " + node.weight);
            //System.out.println("machin : " + ((File1) node.getUserObject()).filename + " " + ((File1) node.getUserObject()).weight);
            //Avoiding loop if node is empty
            if (node.weight == 0 && !node.isRoot()) {
                System.out.println(node.absolutePath);
                node.removeAllChildren();
                node.removeFromParent();
                //Modifying the tree by removing a node invalidates any enumerations created before the modification
                //so we create a new update one.
                en = this.root.breadthFirstEnumeration();
            }

        }
    }

    //Fabriques statiques

    public static FileTree createFileTree(String path, Filter filter) {
        return new FileTree(path, filter, Integer.MAX_VALUE);
    }

    public static FileTree createFileTreeWithLimitedDepth(String path, Filter filter, int maxDepth) {
        return new FileTree(path, filter, maxDepth);
    }
}
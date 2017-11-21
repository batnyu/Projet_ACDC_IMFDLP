package acdc;

import javax.swing.tree.TreeModel;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

public class FileTree implements IMFDLP {

    File1 root;

    /**
     * ConcurrentHashmap with string as key and ConcurrentLinkedQueue<File> as values.
     * Using of Concurrent classes because of several threads writing in this.
     */
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = new ConcurrentHashMap<>();


    public static String rootPath = "";

    public FileTree() {
    }

    /**
     * Building a tree structure with the Fork And Join Framework (Multi-threading)
     * and WalkFileTree to walk the tree.
     *
     * @param path the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param pathNameCount number of levels of the path
     * @param maxDepth to limit the depth of the tree.
     * @param writer not implemented
     * @return
     */
    private File1 createTreeWithForkAndJoinWalkFileTree(Path path, Filter filter, int parallelism, int pathNameCount, int maxDepth, PrintWriter writer) {
        File1 root = null;

        RecursiveCreateTree w = new RecursiveCreateTree(path, pathNameCount, maxDepth, filter, writer);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            root = pool.invoke(w);
        } finally {
            pool.shutdown();
            return root;
        }
    }

    /**
     * Collects duplicates files from the pathStr to the end of the tree
     *
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException
     */
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicates(
            String pathStr, Filter filter, int parallelism) throws IOException {
        manageException(pathStr, filter);
        duplicates.clear();
        collectDuplicatesWithForkAndJoinWalkFileTree(pathStr, filter, parallelism, Integer.MAX_VALUE, null);
        cleanDuplicates();
        return duplicates;
    }

    /**
     * Collects duplicates files from the pathStr to the maximum depth
     *
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param maxDepth the maximum depth from the root path
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException
     */
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicatesWithLimitedDepth(
            String pathStr, Filter filter, int parallelism, int maxDepth) throws IOException {
        manageException(pathStr, filter);
        duplicates.clear();
        collectDuplicatesWithForkAndJoinWalkFileTree(pathStr, filter, parallelism, maxDepth, null);
        cleanDuplicates();
        return duplicates;
    }

    /**
     * Throws some exception (fail-fast)
     *
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     */
    private void manageException(String pathStr, Filter filter) {
        if (filter == null) {
            throw new NullPointerException("Filtre null");
        } else if (pathStr == null) {
            throw new NullPointerException("Path null");
        }
    }

    /**
     * Collecting the duplicates with the Fork And Join Framework (Multi-threading)
     * and WalkFileTree to walk the tree.
     *
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param maxDepth to limit the depth of the tree.
     * @param writer not implemented
     */
    private void collectDuplicatesWithForkAndJoinWalkFileTree(String pathStr, Filter filter, int parallelism, int maxDepth, PrintWriter writer) {
        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(path, pathNameCount, maxDepth, filter, writer);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            pool.invoke(w);
        } finally {
            pool.shutdown();
        }
    }

    /**
     * Clean entries in duplicates with only one value (No duplicate files)
     */
    private void cleanDuplicates() {
        duplicates.entrySet().removeIf(entry -> entry.getValue().size() == 1);
    }

    /**
     * Used to get a tree from the pathStr to the end of the tree.
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return Treemodel for JTree.
     */
    @Override
    public TreeModel tree(String pathStr, Filter filter, int parallelism) {
       /*        Path path = Paths.get("readfile.txt");
        Files.createFile(path);
        FileChannel fileChannel = FileChannel.open(path);*/

/*        PrintWriter writer = new PrintWriter("the-file-name.txt", "UTF-8");
        writer.println("The first line");*/

        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        File1 root = createTreeWithForkAndJoinWalkFileTree(
                path, filter, parallelism, pathNameCount, Integer.MAX_VALUE, null);
        TreeModel model = new FileTreeModel(root);
        return model;

/*        PrintWriter writer = new PrintWriter("./cache.json", "UTF-8");

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonElement jsonElement = gson.toJsonTree(this.root);

        writer.append(jsonElement.toString());
        writer.close();*/

        //Deleting empty folder when a filter is on
/*        if (!filter.isEmpty())
            this.deleteEmptyFolders();*/
    }

    /**
     * Used to get a tree from the pathStr to the depth specified.
     * @param pathStr the root path of the tree.
     * @param filter the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param depth to limit the depth of the tree.
     * @return Treemodel for JTree.
     */
    @Override
    public TreeModel tree(String pathStr, Filter filter, int parallelism, int depth) {
        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        return (TreeModel) createTreeWithForkAndJoinWalkFileTree(
                path, filter, parallelism, pathNameCount, depth, null);
    }

    /**
     * @return the hashmap containing the duplicate files
     */
    @Override
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> getDoublons() {
        return duplicates;
    }

    @Override
    public String filename() {
        return this.root.getFilename();
    }

    @Override
    public long weight() {
        return this.root.getWeight();
    }

    @Override
    public String absolutePath() {
        return this.root.getAbsolutePath();
    }

    /**
     * Used to delete a file from the file system.
     * @param path of the file to delete
     */
    public void deleteFile(Path path) {
        try {
            Files.delete(path);
        } catch (NoSuchFileException e) {
            System.err.format("%s: no such file or directory%n", path);
        } catch (DirectoryNotEmptyException e) {
            System.err.format("%s not empty%n", path);
        } catch (IOException e) {
            //File permission problems are caught here.
            e.printStackTrace();
        }
    }

    /**
     * This method removes the empty folders from the tree.
     * Not used anymore because, it's now done directly in RecursiveCreateTree
     * by not adding the empty folders in the tree.
     */
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
}
package acdc.Core;

import acdc.Core.Utils.Filter;
import acdc.Services.ErrorLogging;
import acdc.TreeDataModel.File1;
import acdc.TreeDataModel.FileTreeModel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.swing.tree.TreeModel;
import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;

/**
 * <b>FileTree is the implementation of IMFDLP</b>
 *
 * <p>
 * It's the Core of the application with all the features.
 *
 * @author Baptiste
 * @version 1.0
 */
public class FileTree implements IMFDLP {

    /**
     * ConcurrentHashmap with string as key and ConcurrentLinkedQueue<File> as values.
     * Using of Concurrent classes because of several threads writing in this.
     */
    public static ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = new ConcurrentHashMap<>();

    public static String rootPath = "";

    private FileTree() {
    }

    /**
     * Static fabric to instantiate the FileTree class.
     *
     * @return a FileTree.
     */
    public static FileTree creerFileTree() {
        return new FileTree();
    }

    /**
     * Building a tree structure with the Fork And Join Framework (Multi-threading)
     * and WalkFileTree to walk the tree.
     *
     * @param path          the root path of the tree.
     * @param filter        the filter you want to apply to the tree.
     * @param parallelism   the level of parallelism.
     * @param pathNameCount number of levels of the path
     * @param maxDepth      to limit the depth of the tree.
     * @return a structure representing the tree (File1)
     */
    public File1 createTreeWithForkAndJoinWalkFileTree(Path path, Filter filter, int parallelism, int pathNameCount, int maxDepth) {
        File1 root;

        RecursiveCreateTree w = new RecursiveCreateTree(path, pathNameCount, maxDepth, filter);
        final ForkJoinPool pool = new ForkJoinPool(parallelism);
        try {
            root = pool.invoke(w);
        } finally {
            pool.shutdown();
        }
        return root;
    }

    /**
     * Collects duplicates files from the pathStr to the end of the tree
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException when access to protected file for example
     */
    @Override
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicates(
            String pathStr, Filter filter, int parallelism) throws IOException {
        manageException(pathStr, filter);
        duplicates.clear();
        collectDuplicatesWithForkAndJoinWalkFileTree(pathStr, filter, parallelism, Integer.MAX_VALUE);
        cleanDuplicates();
        return duplicates;
    }

    /**
     * Collects duplicates files from the pathStr to the maximum depth
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param maxDepth    the maximum depth from the root path
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException when access to protected file for example
     */
    @Override
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicatesWithLimitedDepth(
            String pathStr, Filter filter, int parallelism, int maxDepth) throws IOException {
        manageException(pathStr, filter);
        duplicates.clear();
        collectDuplicatesWithForkAndJoinWalkFileTree(pathStr, filter, parallelism, maxDepth);
        cleanDuplicates();
        return duplicates;
    }

    /**
     * Collecting the duplicates with the Fork And Join Framework (Multi-threading)
     * and WalkFileTree to walk the tree.
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param maxDepth    to limit the depth of the tree.
     */
    private void collectDuplicatesWithForkAndJoinWalkFileTree(String pathStr, Filter filter, int parallelism, int maxDepth) {
        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(path, pathNameCount, maxDepth, filter);
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
     * Throws some exception (fail-fast try)
     *
     * @param pathStr the root path of the tree.
     * @param filter  the filter you want to apply to the tree.
     */
    private void manageException(String pathStr, Filter filter) {
        if (filter == null) {
            ErrorLogging.getInstance().addLog("Filtre null");
            throw new NullPointerException("Filtre null");
        } else if (pathStr == null) {
            ErrorLogging.getInstance().addLog("Path null");
            throw new NullPointerException("Path null");
        }
    }

    /**
     * Used to get a tree from the pathStr to the end of the tree.
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return Treemodel for JTree.
     */
    @Override
    public TreeModel tree(String pathStr, Filter filter, int parallelism) {

        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        File1 root = createTreeWithForkAndJoinWalkFileTree(
                path, filter, parallelism, pathNameCount, Integer.MAX_VALUE);

        //These commented lines build a json cache of all the tree.
        // I didn't have the time to implement the update of this cache.
/*        PrintWriter writer = null;
        try {
            writer = new PrintWriter("cacheTree.json", "UTF-8");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        //JsonElement jsonElement = gson.toJsonTree(root);
        String treeJson = gson.toJson(root);

        System.out.println(treeJson);
        writer.append(treeJson);
        writer.close();*/

        return new FileTreeModel(root);

    }

    /**
     * Used to get a tree from the pathStr to the depth specified.
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param depth       to limit the depth of the tree.
     * @return Treemodel for JTree.
     */
    @Override
    public TreeModel tree(String pathStr, Filter filter, int parallelism, int depth) {
        Path path = Paths.get(pathStr);
        int pathNameCount = path.getNameCount();

        File1 root = createTreeWithForkAndJoinWalkFileTree(
                path, filter, parallelism, pathNameCount, depth);
        return new FileTreeModel(root);
    }

    /**
     * @return the hashmap containing the current duplicate files
     */
    public ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> getDoublons() {
        return duplicates;
    }

    /**
     * Displaying the duplicate files
     *
     * @param duplicates to display
     */
    public void displayDuplicates(Map<String, ConcurrentLinkedQueue<File>> duplicates) {
        StringBuilder result = new StringBuilder();
        int compteur = 0;

        for (Map.Entry<String, ConcurrentLinkedQueue<File>> entry : duplicates.entrySet()) {
            //Useless condition if you clean duplicates with the cleanDuplicates method of FileTree class.
            if (entry.getValue().size() > 1) {
                result.append("hash : ").append(entry.getKey()).append("\n");
                for (File file : entry.getValue()) {
                    result.append(file.getAbsolutePath()).append("\n");
                }
                result.append("\n");
                compteur++;
            }
        }
        System.out.println("\n--- DUPLICATES ("+ compteur +") ---\n");
        System.out.print(result.toString());
    }

    /**
     * Used to delete a file from the file system.
     *
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
    private void deleteEmptyFolders(File1 root) {
        Enumeration<File1> en = root.breadthFirstEnumeration();

        while (en.hasMoreElements()) {
            File1 node = en.nextElement();
            System.out.println(node.absolutePath + " " + node.weight);
            //Avoiding loop if node is empty
            if (node.weight == 0 && !node.isRoot()) {
                System.out.println(node.absolutePath);
                node.removeAllChildren();
                node.removeFromParent();
                //Modifying the tree by removing a node invalidates any enumerations created before the modification
                //so we create a new update one.
                en = root.breadthFirstEnumeration();
            }

        }
    }

    /**
     * String representation of the file tree
     */
    @Override
    public void display(File1 root) {
        System.out.println("\n--- TREE ---\n");
        String result = "";
        Enumeration<File1> en = root.preorderEnumeration();
        while (en.hasMoreElements()) {
            File1 node = en.nextElement();
            String nodeValue = node.toString();
            boolean isDirectory = node.isDirectory;
            String indent = "";
            while (node.getParent() != null) {
                indent += "    ";
                node = node.getParent();
            }
            if(isDirectory){
                indent += "+";
            } else {
                indent += "-";
            }
            result += indent + nodeValue + "\n";
        }
        System.out.println(result);
    }


}
package acdc.Core;

import acdc.Core.Utils.Filter;

import javax.swing.tree.TreeModel;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <b>API Il me faut de la place!</b>
 *
 * @author Baptiste
 * @version 1.0
 */
public interface IMFDLP {

    /**
     * Used to get a tree from the pathStr to the end of the tree.
     *
     * @param path        the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return Treemodel for JTree.
     */
    TreeModel tree(String path, Filter filter, int parallelism);

    /**
     * Used to get a tree from the pathStr to the depth specified.
     *
     * @param path        the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param depth       to limit the depth of the tree.
     * @return Treemodel for JTree.
     */
    TreeModel tree(String path, Filter filter, int parallelism, int depth);

    /**
     * Collects duplicates files from the pathStr to the end of the tree following the filter.
     * Uses Concurrent class because writing in Hashmap from multiple threads.
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException when access to file
     */
    ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicates(
            String pathStr, Filter filter, int parallelism) throws IOException;

    /**
     * Collects duplicates files from the pathStr to the maximum depth following the filter.
     * Uses Concurrent class because writing in Hashmap from multiple threads.
     *
     * @param pathStr     the root path of the tree.
     * @param filter      the filter you want to apply to the tree.
     * @param parallelism the level of parallelism.
     * @param maxDepth    the maximum depth from the root path
     * @return a ConcurrentHashmap with key string (hash) and Files as values
     * @throws IOException
     */
    ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> collectDuplicatesWithLimitedDepth(
            String pathStr, Filter filter, int parallelism, int maxDepth) throws IOException;
}
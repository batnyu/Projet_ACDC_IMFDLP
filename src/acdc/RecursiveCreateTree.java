package acdc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 * <b>RecursiveCreateTree is the class allowing you to get a tree.</b>
 * <p>
 *     It uses the Fork/Join Framework to split the work.
 *     It extends from RecursiveTask that allow to return a result.
 *     I use it to return the buil
 *     For each folder, a class is instantiated.
 *     You can choose the level of parallelism you want (how many worker threads to use).
 * </p>
 * <p>
 *     To walk the file system tree, it uses WalkFileTree from Files.
 *     When an IOException occurs, it adds the error message in the ErrorLogging class.
 * </p>
 * <p>
 *     When Files.walkFileTree visit files, it uses the filter to add only
 *     the matching files.
 * </p>
 * <p>
 *     During Files.walkFileTree, it adds the file size and adds it to the parent folder recursively.
 * </p>
 * <p>
 *     Since it's in multithread with multiple instance of WalkFileTree,
 *     the maxDepth coming with WalkFileTree doesn't work.
 *     So, I use the pathNameCount of the current dir and I compare it to the original path requested.
 *     With this, I join the folders and add the files only if the current depth is smaller than the max depth.
 *     But I continue creating instances of RecursiveCreateTree in preVisitDirectory() in order to get
 *     the size of the folders of the tree.
 * </p>
 *
 * @author Baptiste
 * @version 1.0
 */
public class RecursiveCreateTree extends RecursiveTask<File1> {

    private Filter filter;
    private PrintWriter writer;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;
    private long folderSize;

    private File1 tree;
    private File1 currentDir;

    public RecursiveCreateTree(Path dir, int pathNameCount, int maxDepth, Filter filter, PrintWriter writer) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.writer = writer;
    }

    @Override
    protected File1 compute() {
        final List<RecursiveCreateTree> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //Create another instance for each folder in dir
                    if (!dir.equals(RecursiveCreateTree.this.dir)) {
                        // Look at the number of levels of the current dir
                        RecursiveCreateTree w = new RecursiveCreateTree(dir, pathNameCount, maxDepth, filter, writer);
                        w.fork();
                        walks.add(w);
                        //System.out.println("SUBFOLDER  : " + dir + "\t" + Thread.currentThread());
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        //Creating the dir node
                        //System.out.println("FOLDER : " + dir + "\t" + Thread.currentThread());
                        String simpleDir;

                        if (dir.getNameCount() == 0) {
                            simpleDir = dir.getRoot().toString();
                        } else {
                            simpleDir = dir.getFileName().toString();
                        }

                        tree = new File1(simpleDir, 0, dir.toString(), attrs.lastModifiedTime(), true);
                        currentDir = tree;

                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        //System.out.println(Thread.currentThread() + "\t" + file);

                        if (filter.accept(file)) {
                            if (isBelowMaxDepth(file)) {

                                // I was trying to build a cache with a json file for the tree.
                                // But it's useless since I don't use hooks for checking the file system.
                                // So, I always need to go through the file system at the beginning of the app.
                                // I choose to separate the finding of the duplicates and the building of the tree.
                                // So, I only cache the hash of the files to avoid re-hashing the same files over and over.

/*                                String pattern = Pattern.quote(System.getProperty("file.separator"));
                                String[] levels = file.toString().split(pattern);
                                System.out.println(file.toString());

                                System.out.println("rootpath = " + FileTree.rootPath);
                                String[] rootPath = FileTree.rootPath.split(pattern);
                                int debut = rootPath.length;
                                int machin = file.getNameCount() - file.getParent().getNameCount();

                                CacheUpdate cacheUpdate = new CacheUpdate(rootPath, attrs.lastModifiedTime().toMillis());
                                cacheUpdate.readJsonStream();*/

                                File1 newFile = new File1(file.getFileName().toString(), attrs.size(), file.toString(), attrs.lastModifiedTime(), false);
                                currentDir.add(newFile);

                            }
                            folderSize += attrs.size();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) {
                    ErrorLogging.getInstance().addLog(exc.toString());
                    //exc.printStackTrace();
                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        long somme = folderSize;
        for (RecursiveCreateTree w : walks) {
            //Loop through subfolders and adding them to the parent
            if (isBelowMaxDepth(w.dir)) {
                if (filterIsActiveAndFolderIsNotEmptyOrFilterIsNotActive(w)) {
                    tree.add(w.join());
                }
            }
            //Adding the size of the subfolders to join with the size of the files.
            somme = somme + (w.join()).getWeight();
        }

        //Setting the parent folder size.
        tree.setWeight(somme);

        return tree;
    }

    /**
     * Used to omit adding the empty folders if the filter is not empty.
     *
     * @param w instance of RecursiveCreateTree
     * @return a boolean
     */
    private boolean filterIsActiveAndFolderIsNotEmptyOrFilterIsNotActive(RecursiveCreateTree w) {
        return (w.join()).getWeight() != 0 && !filter.isEmpty() || filter.isEmpty();
    }

    /**
     * Used to limit the depth of the search.
     *
     * @param file the path of the file.
     * @return true if the current depth is smaller than the max depth.
     */
    private boolean isBelowMaxDepth(Path file) {
        return file.getNameCount() - pathNameCount <= maxDepth;
    }
}



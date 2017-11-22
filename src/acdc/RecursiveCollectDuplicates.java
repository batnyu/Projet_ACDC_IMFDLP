package acdc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;

import static acdc.FileTree.duplicates;

/**
 * <b>RecursiveCollectDuplicates is the class allowing you to collect the duplicates.</b>
 * <p>
 * It uses the Fork/Join Framework to split the work.
 * It extends from RecursiveAction that return nothing.
 * Since I just add hash to the static duplicates field in FileTree class, I don't need to return anything.
 * For each folder, a class is instantiated.
 * You can choose the level of parallelism you want (how many worker threads to use).
 * </p>
 * <p>
 * To walk the file system tree, it uses WalkFileTree from Files.
 * When an IOException occurs, it adds the error message in the ErrorLogging class.
 * </p>
 * <p>
 * When Files.walkFileTree visit files, it uses the filter to collect only the duplicates of
 * the matching files.
 * </p>
 * <p>
 * Since it's in multithread with multiple instance of WalkFileTree,
 * the maxDepth coming with WalkFileTree doesn't work.
 * So, I use the pathNameCount of the current dir and I compare it to the original path requested.
 * With this, I join the folders and add the files only if the current depth is smaller than the max depth.
 * </p>
 *
 * @author Baptiste
 * @version 1.0
 */
public class RecursiveCollectDuplicates extends RecursiveAction {

    private Filter filter;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;

    public RecursiveCollectDuplicates(Path dir, int pathNameCount, int maxDepth, Filter filter) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
    }

    @Override
    protected void compute() {
        final List<RecursiveCollectDuplicates> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //Create another instance for each folder in dir
                    if (!dir.equals(RecursiveCollectDuplicates.this.dir)) {
                        if (isBelowMaxDepth(dir)) {
                            // Look at the number of levels of the current dir
                            RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(dir, pathNameCount, maxDepth, filter);
                            w.fork();
                            walks.add(w);
                        }
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        //System.out.println(Thread.currentThread() + "\t" + file);

                        Path fileCache = Paths.get("cache.txt");

                        if (isBelowMaxDepth(file)) {
                            if (filter.accept(file)) {
                                //Recherche dans cache duplicates
                                try {
                                    Files.createFile(fileCache);
                                } catch (FileAlreadyExistsException ignored) {
                                }

                                //Search for the file in cache
                                String cachedStr = searchUsingBufferedReader(
                                        fileCache.toString(),
                                        file.toString() + "!!!" +
                                                attrs.size() + "!!!" +
                                                attrs.lastModifiedTime().toMillis());

                                collectDuplicates(file, attrs.size(), cachedStr, attrs.lastModifiedTime(), fileCache);
                            }

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

        for (RecursiveCollectDuplicates w : walks) {
            //Loop through subfolders and adding them to the parent
            if (isBelowMaxDepth(w.dir)) {
                w.join();
            }
        }
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

    /**
     * Hashing the file if not already in the cache or getting back the hash from the file
     * and adding it to the static HashMap of the FileTree class.
     *
     * @param file the path of the current file
     * @param size the size of the current file
     * @param cachedStr the result of the search in the cache
     * @param fileTime the last modified time of the current file.
     * @param fileCache the path towards the cache file.
     */
    private void collectDuplicates(Path file, long size, String cachedStr, FileTime fileTime, Path fileCache) {
        String uniqueFileHash;
        try {
            //If the file is not cached
            if (cachedStr == null) {
                //quick but errors can happen (constant complexity)
                uniqueFileHash = Hash.sampleHashFile(file.toString()) + size;
                //very long but no error
                //uniqueFileHash = Hash.md5OfFile(file.toFile());

                //Write to file if not find
                try (BufferedWriter writer = Files.newBufferedWriter(fileCache, StandardOpenOption.APPEND)) {
                    writer.write(
                            file.toString() + "!!!" +
                                    size + "!!!" +
                                    fileTime.toMillis() + "!!!" +
                                    uniqueFileHash + "!!!" +
                                    System.lineSeparator());
                } catch (IOException ioe) {
                    System.err.format("IOException: %s%n", ioe);
                }

            } else {
                String[] splitted = cachedStr.split("!!!");
                uniqueFileHash = splitted[3];
            }

            duplicates.computeIfAbsent(uniqueFileHash, k -> new ConcurrentLinkedQueue<>())
                    .add(file.toFile());

            //Other way to write it.
/*            ConcurrentLinkedQueue<File> list = duplicates.get(uniqueFileHash);
            if (list == null) {
                System.out.println("hash not there");
                list = new ConcurrentLinkedQueue<>();
                duplicates.put(uniqueFileHash, list);
            }
            list.add(file.toFile());*/


        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Search for a string in a file and return the string.
     *
     * @param filePath the path of the file to search
     * @param searchQuery the string to search.
     * @return null if not found or the string found.
     * @throws IOException
     */
    public String searchUsingBufferedReader(String filePath, String searchQuery) throws IOException {
        searchQuery = searchQuery.trim();
        BufferedReader br = null;

        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line;
            while ((line = br.readLine()) != null) {
                //System.out.println("line = " + line);
                //System.out.println("searchQuery = " + searchQuery);
                if (line.contains(searchQuery)) {
                    return line;
                } else {
                }
            }
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (Exception e) {
                System.err.println("Exception while closing bufferedreader " + e.toString());
            }
        }

        return null;
    }
}



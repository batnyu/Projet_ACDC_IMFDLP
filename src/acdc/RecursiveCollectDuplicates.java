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

public class RecursiveCollectDuplicates extends RecursiveAction {

    private Filter filter;
    private PrintWriter writer;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;

    public RecursiveCollectDuplicates(Path dir, int pathNameCount, int maxDepth, Filter filter, PrintWriter writer) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.writer = writer;
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
                        // Look at the number of levels of the current dir
                        RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(dir, pathNameCount, maxDepth, filter, writer);
                        w.fork();
                        walks.add(w);
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

                        if (filter.accept(file)) {
                            if (isBelowMaxDepth(file)) {

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

    private boolean isBelowMaxDepth(Path file) {
        return file.getNameCount() - pathNameCount <= maxDepth;
    }

    private String collectDuplicates(Path file, long size, String cachedStr, FileTime fileTime, Path fileCache) {
        String uniqueFileHash = null;
        try {
            //If the file is not cached
            if (cachedStr == null) {
                //quick but errors can happen
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
        return uniqueFileHash;
    }

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



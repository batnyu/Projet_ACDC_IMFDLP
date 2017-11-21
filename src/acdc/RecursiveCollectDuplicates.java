package acdc;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveAction;

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

                        String uniqueFileHash = "hash";

                        if (filter.accept(file)) {
                            if (isBelowMaxDepth(file)) {
                                collectDuplicates(file,attrs.size());
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
                    exc.printStackTrace();
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

    private String collectDuplicates(Path file, long size) {
        String uniqueFileHash = null;
        try {
            //quick but errors can happen
            uniqueFileHash = Hash.sampleHashFile(file.toString()) + size;
            //very long but no error
            //uniqueFileHash = Hash.md5OfFile(file.toFile());

            FileTree.duplicates.computeIfAbsent(uniqueFileHash, k -> new ConcurrentLinkedQueue<>())
                    .add(file.toFile());

    /*      List<String> list = duplicates.get(uniqueFileHash);
            if (list == null) {
                list = new LinkedList<>();
                duplicates.put(uniqueFileHash,list);
            }
            list.add(file.toAbsolutePath().toString());*/
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueFileHash;
    }
}



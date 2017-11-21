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
import java.util.concurrent.RecursiveTask;

public class RecursiveCollectDuplicates extends RecursiveAction {

    private Filter filter;
    private boolean doublonsFinder;
    private PrintWriter writer;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;

    public RecursiveCollectDuplicates(Path dir, int pathNameCount, int maxDepth, Filter filter, boolean doublonsFinder, PrintWriter writer) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
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
                        RecursiveCollectDuplicates w = new RecursiveCollectDuplicates(dir, pathNameCount, maxDepth, filter, doublonsFinder, writer);
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
                                collectDuplicates(file);
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

    private String collectDuplicates(Path file) {
        //TODO : Thread pour la collecte des doublons
        String uniqueFileHash = null;
        try {

            uniqueFileHash = Hash.sampleHashFile(file.toString());

            FileTree.doublons.computeIfAbsent(uniqueFileHash, k -> new ConcurrentLinkedQueue<>())
                    .add(file.toFile());

    /*      List<String> list = doublons.get(uniqueFileHash);
            if (list == null) {
                list = new LinkedList<>();
                doublons.put(uniqueFileHash,list);
            }
            list.add(file.toAbsolutePath().toString());*/
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return uniqueFileHash;
    }
}



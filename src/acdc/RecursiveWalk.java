package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;

public class RecursiveWalk extends RecursiveTask<DefaultMutableTreeNode> {

    private Filter filter;
    private boolean doublonsFinder;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;
    private long folderSize;

    private DefaultMutableTreeNode tree;
    private DefaultMutableTreeNode currentDir;

    public RecursiveWalk(Path dir, int pathNameCount, int maxDepth, Filter filter, boolean doublonsFinder) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
    }

    @Override
    protected DefaultMutableTreeNode compute() {
        final List<RecursiveWalk> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //Create another instance for each folder in dir
                    if (!dir.equals(RecursiveWalk.this.dir)) {
                        // Look at the number of levels of the current dir
                        RecursiveWalk w = new RecursiveWalk(dir, pathNameCount, maxDepth, filter, doublonsFinder);
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

                        File1 newFolder = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
                        tree = new DefaultMutableTreeNode(newFolder);
                        currentDir = tree;
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        //System.out.println(Thread.currentThread() + "\t" + file);

                        String uniqueFileHash = "hash";

                        if (filter.accept(file)) {
                            if (doublonsFinder) {
                                uniqueFileHash = collectDuplicates(file);
                            }
                            //Adding all the files in the current DIR
                            File1 newFile = new File1(file.getFileName().toString(), attrs.size(), uniqueFileHash, file.toString(), attrs.lastModifiedTime(), false);
                            if (isBelowMaxDepth(file)) {
                                currentDir.add(new DefaultMutableTreeNode(newFile));
                            }
                            folderSize += attrs.size();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    //Setting the size of all the files in the folder
                    ((File1) currentDir.getUserObject()).setWeight(folderSize);
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

        long somme = folderSize;
        for (RecursiveWalk w : walks) {
            //Loop through subfolders and adding them to the parent
            if (isBelowMaxDepth(w.dir))
                tree.add(w.join());
            //Adding the size of the subfolders to join with the size of the files.
            somme = somme + ((File1) (w.join()).getUserObject()).getWeight();
        }
        //Setting the parent folder size.
        ((File1) tree.getUserObject()).setWeight(somme);
        return tree;
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
                    .add(file.toAbsolutePath().toString());

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



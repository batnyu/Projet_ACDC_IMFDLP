package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.RecursiveTask;

public class RecursiveWalk extends RecursiveTask<DefaultMutableTreeNode> {
    private static final long serialVersionUID = 6913234076030245489L;

    private Filter filter;
    private boolean doublonsFinder;

    private Map<String, List<String>> doublons = new HashMap<>();

    private final Path dir;
    private long folderSize;

    private DefaultMutableTreeNode tree;
    private DefaultMutableTreeNode currentDir;

    public RecursiveWalk(Path dir, Filter filter, boolean doublonsFinder) {
        this.dir = dir;
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
    }

    public Map<String, List<String>> getDoublons() {
        return doublons;
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
                        RecursiveWalk w = new RecursiveWalk(dir, filter, doublonsFinder);
                        w.fork();
                        walks.add(w);
                        System.out.println("SUBFOLDER  : " + dir + "\t" + Thread.currentThread());
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        //Creating the dir node
                        System.out.println("FOLDER : " + dir + "\t" + Thread.currentThread());
                        String simpleDir;

                        if (dir.getNameCount() == 0) {
                            simpleDir = dir.getRoot().toString();
                        } else {
                            simpleDir = dir.getFileName().toString();
                        }

                        File newFolder = new acdc.File(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
                        tree = new DefaultMutableTreeNode(newFolder);
                        currentDir = tree;
                        return FileVisitResult.CONTINUE;
                    }
                }

                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (attrs.isRegularFile()) {
                        System.out.println(file + "\t" + Thread.currentThread());

                        String uniqueFileHash = "hash";

                        if (filter.accept(file)) {
                            if (doublonsFinder) {
                                uniqueFileHash = collectDuplicates(file, attrs);
                            }
                            //Adding all the files in the current DIR
                            File newFile = new acdc.File(file.getFileName().toString(), attrs.size(), uniqueFileHash, file.toString(), attrs.lastModifiedTime(), false);
                            currentDir.add(new DefaultMutableTreeNode(newFile));
                            folderSize += attrs.size();
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    //Setting the size of all the files in the folder
                    ((File) currentDir.getUserObject()).setWeight(folderSize);
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
            tree.add(w.join());
            //Adding the size of the subfolders to join with the size of the files.
            somme = somme + ((File) (w.join()).getUserObject()).getWeight();
        }
        //Setting the parent folder size.
        ((File) tree.getUserObject()).setWeight(somme);
        return tree;
    }

    private String collectDuplicates(Path file, BasicFileAttributes attr) {
        //TODO : Thread pour la collecte des doublons
        String uniqueFileHash = null;
        try {

            uniqueFileHash = Hash.sampleHashFile(file.toString());

            this.doublons.computeIfAbsent(uniqueFileHash, k -> new LinkedList<>())
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



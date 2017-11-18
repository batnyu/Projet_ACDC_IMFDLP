package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedQueue;

public class FileTreeCreator implements FileVisitor<Path> {

    DefaultMutableTreeNode tree;

    private DefaultMutableTreeNode currentDir;
    private Deque<Long> dirSizeStack = new ArrayDeque<>();
    private Filter filter;
    private boolean doublonsFinder;

    FileTreeCreator(Filter filter, boolean doublonsFinder) {
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
    }

    public DefaultMutableTreeNode getRootNode() {
        return tree;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        //System.out.format("Directory: %s%n", dir);

        dirSizeStack.push((long) 0);

        String simpleDir;

        //to counter bug when getFileName() is called on root
        if (dir.getNameCount() == 0) {
            simpleDir = dir.getRoot().toString();
        } else {
            simpleDir = dir.getFileName().toString();
        }

        File1 newFolder = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);

        if (tree == null) {
            tree = new DefaultMutableTreeNode(newFolder);

            currentDir = tree;
        } else {
            DefaultMutableTreeNode newDir = new DefaultMutableTreeNode(newFolder);

            currentDir.add(newDir);
            currentDir = newDir;
        }

        //System.out.println("currentDir : " + currentDir.filename);

        return FileVisitResult.CONTINUE;
    }

    // Print information about
    // each type of file.
    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attr) throws IOException {
        if(Files.isReadable(file)){

        if (attr.isSymbolicLink()) {
            //System.out.format("Symbolic link: %s ", file);
        } else if (attr.isRegularFile()) {
            //System.out.format("Regular file: %s \n", file);

            String uniqueFileHash = null;

            if (filter.accept(file)) {
                if(doublonsFinder){
                    uniqueFileHash = collectDuplicates(file);
                }

                File1 newFile = new File1(file.getFileName().toString(), attr.size(), uniqueFileHash, file.toString(), attr.lastModifiedTime(), false);
                dirSizeStack.push(dirSizeStack.pop() + attr.size());
                currentDir.add(new DefaultMutableTreeNode(newFile));
            }

        } else {
            //System.out.format("Other: %s ", file);
        }
        }

        //System.out.println("(" + attr.size() + "bytes)");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        long size = dirSizeStack.pop();

        ((File1) currentDir.getUserObject()).setWeight(size);

        if (!dirSizeStack.isEmpty()) // add this dir size to parent's size
            dirSizeStack.push(dirSizeStack.pop() + size);

        //climb up after visiting the folder
        currentDir = (DefaultMutableTreeNode) currentDir.getParent();

        //System.out.println("currentDir : " + currentDir.filename);

        return FileVisitResult.CONTINUE;
    }

    // If there is some error accessing
    // the file, let the user know.
    // If you don't override this method
    // and an error occurs, an IOException 
    // is thrown.
    @Override
    public FileVisitResult visitFileFailed(Path file,
                                           IOException exc) {
        //exc.printStackTrace();
        return FileVisitResult.CONTINUE;
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

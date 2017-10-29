package acdc;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class FileTreeCreator implements FileVisitor<Path> {

    DefaultMutableTreeNode tree;

    private DefaultMutableTreeNode currentDir;
    private Deque<Long> dirSizeStack = new ArrayDeque<>();
    private Filter filter;
    private boolean doublonsFinder;

    Map<String, List<String>> doublons = new HashMap<>();

    FileTreeCreator(Filter filter, boolean doublonsFinder) {
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
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

        File newFolder = new File(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);

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

        if (attr.isSymbolicLink()) {
            //System.out.format("Symbolic link: %s ", file);
        } else if (attr.isRegularFile()) {
            System.out.format("Regular file: %s \n", file);

            String uniqueFileHash = null;

            if (filter.accept(file)) {
                if(doublonsFinder){
                    uniqueFileHash = collectDuplicates(file,attr);
                }

                File newFile = new File(file.getFileName().toString(), attr.size(), uniqueFileHash, file.toString(), attr.lastModifiedTime(), false);
                dirSizeStack.push(dirSizeStack.pop() + attr.size());
                currentDir.add(new DefaultMutableTreeNode(newFile));
            }

        } else {
            //System.out.format("Other: %s ", file);
        }
        //System.out.println("(" + attr.size() + "bytes)");
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        long size = dirSizeStack.pop();

        ((File) currentDir.getUserObject()).setWeight(size);

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
        exc.printStackTrace();
        return FileVisitResult.CONTINUE;
    }


    public String collectDuplicates(Path file,BasicFileAttributes attr) {
        //TODO : Thread pour la collecte des doublons
        String uniqueFileHash = null;
        try {

            uniqueFileHash = sampleHashFile(file);

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

    private static final int SAMPLE_SIZE = 4000;

    private static String sampleHashFile(Path path) throws IOException, NoSuchAlgorithmException {

        final long totalBytes = new java.io.File(path.toString()).length();

        try(InputStream inputStream = new FileInputStream(path.toString())) {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            DigestInputStream digestInputStream = new DigestInputStream(inputStream, digest);

            // if the file is too short to take 3 samples, hash the entire file
            if (totalBytes < SAMPLE_SIZE * 3) {
                byte[] bytes = new byte[(int) totalBytes];
                digestInputStream.read(bytes);
            } else {
                byte[] bytes = new byte[SAMPLE_SIZE * 3];
                long numBytesBetweenSamples = (totalBytes - SAMPLE_SIZE * 3) / 2;

                // read first, middle and last bytes
                for (int n = 0; n < 3; n++) {
                    digestInputStream.read(bytes, n * SAMPLE_SIZE, SAMPLE_SIZE);
                    digestInputStream.skip(numBytesBetweenSamples);
                }
            }
            return new BigInteger(1, digest.digest()).toString(16);
        }
    }
}

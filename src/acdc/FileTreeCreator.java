package acdc;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
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
            //System.out.format("Regular file: %s ", file);

            if (filter.accept(file)) {
                if(doublonsFinder){
                    collectDoublons(file,attr);
                }

                File newFile = new File(file.getFileName().toString(), attr.size(), "hash", file.toString(), attr.lastModifiedTime(), false);
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

    private void collectDoublons(Path file, BasicFileAttributes attr) {
        //TODO : Thread pour la collecte des doublons
        byte fileData[] = new byte[(int) attr.size()];
        //byte fileData[] = new byte[(int) attr.size() + Math.abs((int)attr.lastModifiedTime().toMillis()/45555)];
        String uniqueFileHash = new BigInteger(1, messageDigest.digest(fileData)).toString(16);
        this.doublons.computeIfAbsent(uniqueFileHash, k -> new LinkedList<>())
                .add(file.toAbsolutePath().toString());

/*      List<String> list = doublons.get(uniqueFileHash);
        if (list == null) {
            list = new LinkedList<>();
            doublons.put(uniqueFileHash,list);
        }
        list.add(file.toAbsolutePath().toString());*/
    }

    private static MessageDigest messageDigest;

    static {
        try {
            messageDigest = MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("cannot initialize SHA-512 hash function", e);
        }
    }

    //	public String buildHash(Path file) {
//		MessageDigest md = MessageDigest.getInstance("MD5");
//		try (InputStream is = Files.newInputStream(file);
//		     DigestInputStream dis = new DigestInputStream(is, md)) 
//		{
//		  /* Read decorated stream (dis) to EOF as normal... */
//		}
//		byte[] digest = md.digest();
//		
//		return digest.toString();
//	}
}

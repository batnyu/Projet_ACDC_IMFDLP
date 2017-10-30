package acdc;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MultiThreadedFileTreeWalk {
    private static class RecursiveWalk extends RecursiveAction {
        private static final long serialVersionUID = 6913234076030245489L;
        private final Path dir;


        DefaultMutableTreeNode tree;

        private DefaultMutableTreeNode currentDir;
        private Deque<Long> dirSizeStack = new ArrayDeque<>();
        private Filter filter;
        private boolean doublonsFinder;

        Map<String, List<String>> doublons = new HashMap<>();


        public RecursiveWalk(Path dir) {
            this.dir = dir;
        }

        @Override
        protected void compute() {
            final List<RecursiveWalk> walks = new ArrayList<>();
            try {
                Files.walkFileTree(dir, new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                        if (!dir.equals(RecursiveWalk.this.dir)) {
                            RecursiveWalk w = new RecursiveWalk(dir);
                            w.fork();
                            walks.add(w);

                            return FileVisitResult.SKIP_SUBTREE;
                        } else {

                            //System.out.format("Directory: %s%n", dir);

                            dirSizeStack.push((long) 0);

                            String simpleDir;

                            //to counter bug when getFileName() is called on root
                            if (dir.getNameCount() == 0) {
                                simpleDir = dir.getRoot().toString();
                            } else {
                                simpleDir = dir.getFileName().toString();
                            }

                            acdc.File newFolder = new acdc.File(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);

                            if (tree == null) {
                                tree = new DefaultMutableTreeNode(newFolder);

                                currentDir = tree;
                            } else {
                                DefaultMutableTreeNode newDir = new DefaultMutableTreeNode(newFolder);

                                currentDir.add(newDir);
                                currentDir = newDir;
                            }
                            return FileVisitResult.CONTINUE;
                        }
                    }

                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        System.out.println(file + "\t" + Thread.currentThread());

                        if (attrs.isSymbolicLink()) {
                            //System.out.format("Symbolic link: %s ", file);
                        } else if (attrs.isRegularFile()) {
                            System.out.format("Regular file: %s \n", file);

                            String uniqueFileHash = null;

                            if (filter.accept(file)) {
                                if(doublonsFinder){
                                    //uniqueFileHash = collectDuplicates(file,attrs);
                                }

                                acdc.File newFile = new acdc.File(file.getFileName().toString(), attrs.size(), uniqueFileHash, file.toString(), attrs.lastModifiedTime(), false);
                                dirSizeStack.push(dirSizeStack.pop() + attrs.size());
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

                        ((acdc.File) currentDir.getUserObject()).setWeight(size);

                        if (!dirSizeStack.isEmpty()) // add this dir size to parent's size
                            dirSizeStack.push(dirSizeStack.pop() + size);

                        //climb up after visiting the folder
                        currentDir = (DefaultMutableTreeNode) currentDir.getParent();

                        //System.out.println("currentDir : " + currentDir.filename);

                        return FileVisitResult.CONTINUE;
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (RecursiveWalk w : walks) {
                w.join();
            }
        }
    }

    public static void main(String[] args) throws IOException {
        RecursiveWalk w = new RecursiveWalk(Paths.get("D:" + File.separator).toRealPath());
        ForkJoinPool p = new ForkJoinPool();
        p.invoke(w);
    }

/*    public String collectDuplicates(Path file, BasicFileAttributes attr) {
        //TODO : Thread pour la collecte des doublons
        String uniqueFileHash = null;
        try {

            uniqueFileHash = sampleHashFile(file);

            this.doublons.computeIfAbsent(uniqueFileHash, k -> new LinkedList<>())
                    .add(file.toAbsolutePath().toString());

    *//*      List<String> list = doublons.get(uniqueFileHash);
            if (list == null) {
                list = new LinkedList<>();
                doublons.put(uniqueFileHash,list);
            }
            list.add(file.toAbsolutePath().toString());*//*
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
    }*/
}

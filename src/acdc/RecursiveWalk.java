package acdc;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;

import javax.swing.tree.DefaultMutableTreeNode;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Pattern;

import static com.jayway.jsonpath.Option.DEFAULT_PATH_LEAF_TO_NULL;
import static sun.plugin.javascript.navig.JSType.Option;

public class RecursiveWalk extends RecursiveTask<File1> {

    private Filter filter;
    private boolean doublonsFinder;
    private PrintWriter writer;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;
    private long folderSize;


    private File1 tree;
    private File1 currentDir;

    public RecursiveWalk(Path dir, int pathNameCount, int maxDepth, Filter filter, boolean doublonsFinder, PrintWriter writer) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.doublonsFinder = doublonsFinder;
        this.writer = writer;
    }

    @Override
    protected File1 compute() {
        final List<RecursiveWalk> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //Create another instance for each folder in dir
                    if (!dir.equals(RecursiveWalk.this.dir)) {
                        // Look at the number of levels of the current dir
                        RecursiveWalk w = new RecursiveWalk(dir, pathNameCount, maxDepth, filter, doublonsFinder, writer);
                        w.fork();
                        walks.add(w);
                        //System.out.println("SUBFOLDER  : " + dir + "\t" + Thread.currentThread());
                        return FileVisitResult.SKIP_SUBTREE;
                    } else {
                        //Creating the dir node
                        //System.out.println("FOLDER : " + dir + "\t" + Thread.currentThread());
                        if (!doublonsFinder) {
                            String simpleDir;

                            if (dir.getNameCount() == 0) {
                                simpleDir = dir.getRoot().toString();
                            } else {
                                simpleDir = dir.getFileName().toString();
                            }

                            //File1 newFolder = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
                            tree = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
                            currentDir = tree;
                        }
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
                                //TODO: ADD TO JSON FILE

/*                                String pattern = Pattern.quote(System.getProperty("file.separator"));
                                String[] levels = file.toString().split(pattern);
                                System.out.println(file.toString());

                                System.out.println("rootpath = " + FileTree.rootPath);
                                String[] rootPath = FileTree.rootPath.split(pattern);
                                int debut = rootPath.length;
                                int machin = file.getNameCount() - file.getParent().getNameCount();

                                CacheUpdate cacheUpdate = new CacheUpdate(rootPath, attrs.lastModifiedTime().toMillis());
                                cacheUpdate.readJsonStream();*/

/*                                String json = "cache.json";
                                Configuration conf = Configuration.defaultConfiguration().addOptions(DEFAULT_PATH_LEAF_TO_NULL);
                                Object document = conf.jsonProvider().parse(json);


                                System.out.println(debut);
                                System.out.println(machin);
                                System.out.println(levels.length);

                                StringBuilder jsonPath = new StringBuilder("$");
                                for (int i = debut; i < debut + machin; i++) {
                                    jsonPath.append(".children");
                                    jsonPath.append("[?(@.filename == '" + levels[i] + "')]");
                                }
                                jsonPath.append(".lastModifiedTime.value");
                                System.out.println(jsonPath.toString());

                                String timestamp = JsonPath.read(document, jsonPath.toString());
                                System.out.println(timestamp);*/

                                //$.children[?(@.filename == "19268_1333773742162_6130659_n - Copie.jpg")].lastModifiedTime.value


                                if (!doublonsFinder) {
                                    File1 newFile = new File1(file.getFileName().toString(), attrs.size(), uniqueFileHash, file.toString(), attrs.lastModifiedTime(), false);
                                    currentDir.add(newFile);
                                } else {
                                    collectDuplicates(file,attrs.size());
                                }

                            }
                            if (!doublonsFinder) {
                                folderSize += attrs.size();
                            }
                        }
                    }
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    //USELESS
                    //Setting the size of all the files in the folder
                    //currentDir.setWeight(folderSize);
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
            if (isBelowMaxDepth(w.dir)) {
                if (!doublonsFinder) {
                    if (filterIsActiveAndFolderIsNotEmptyOrfilterIsNotActive(w)) {
                        tree.add(w.join());
                    }
                } else {
                    w.join();
            }
        }
        //Adding the size of the subfolders to join with the size of the files.
            if(!doublonsFinder)
                somme = somme + (w.join()).getWeight();
    }
    //Setting the parent folder size.
        if(!doublonsFinder)
            tree.setWeight(somme);

        return tree;
}

    private boolean filterIsActiveAndFolderIsNotEmptyOrfilterIsNotActive(RecursiveWalk w) {
        return (w.join()).getWeight() != 0 && !filter.isEmpty() || filter.isEmpty();
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return uniqueFileHash;
    }
}



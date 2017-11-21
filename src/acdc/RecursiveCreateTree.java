package acdc;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.RecursiveTask;

public class RecursiveCreateTree extends RecursiveTask<File1> {

    private Filter filter;
    private PrintWriter writer;

    private final Path dir;
    private int pathNameCount;
    private int maxDepth;
    private long folderSize;


    private File1 tree;
    private File1 currentDir;

    public RecursiveCreateTree(Path dir, int pathNameCount, int maxDepth, Filter filter, PrintWriter writer) {
        this.dir = dir;
        this.pathNameCount = pathNameCount;
        this.maxDepth = maxDepth;
        this.filter = filter;
        this.writer = writer;
    }

    @Override
    protected File1 compute() {
        final List<RecursiveCreateTree> walks = new ArrayList<>();
        try {
            Files.walkFileTree(dir, EnumSet.allOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                    //Create another instance for each folder in dir
                    if (!dir.equals(RecursiveCreateTree.this.dir)) {
                        // Look at the number of levels of the current dir
                        RecursiveCreateTree w = new RecursiveCreateTree(dir, pathNameCount, maxDepth, filter, writer);
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

                        //File1 newFolder = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
                        tree = new File1(simpleDir, 0, "hash", dir.toString(), attrs.lastModifiedTime(), true);
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

                                File1 newFile = new File1(file.getFileName().toString(), attrs.size(), uniqueFileHash, file.toString(), attrs.lastModifiedTime(), false);
                                currentDir.add(newFile);

                            }
                            folderSize += attrs.size();
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

        long somme = folderSize;
        for (RecursiveCreateTree w : walks) {
            //Loop through subfolders and adding them to the parent
            if (isBelowMaxDepth(w.dir)) {
                if (filterIsActiveAndFolderIsNotEmptyOrFilterIsNotActive(w)) {
                    tree.add(w.join());
                }
            }
            //Adding the size of the subfolders to join with the size of the files.
            somme = somme + (w.join()).getWeight();
        }

        //Setting the parent folder size.
        tree.setWeight(somme);

        return tree;
    }

    private boolean filterIsActiveAndFolderIsNotEmptyOrFilterIsNotActive(RecursiveCreateTree w) {
        return (w.join()).getWeight() != 0 && !filter.isEmpty() || filter.isEmpty();
    }

    private boolean isBelowMaxDepth(Path file) {
        return file.getNameCount() - pathNameCount <= maxDepth;
    }
}



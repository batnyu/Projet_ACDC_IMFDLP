package acdc;

import acdc.Core.FileTree;
import acdc.Core.Utils.Filter;
import acdc.Services.ErrorLogging;
import acdc.Services.Settings;
import acdc.TreeDataModel.File1;

import javax.swing.tree.TreeModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Main {

    public static void main(String[] args) throws Exception {
        String path = "dossierTest";
        Settings.getInstance().setPathCacheHash("cacheHash.txt");

        int parallelism = 1;

        Filter filter = Filter.createFilter();
        FileTree fileTree = FileTree.creerFileTree();

        String param = "";

        for (String arg : args) {
            if(arg.contains("=")){
                param = getParam(arg);
            }
            if (arg.contains("-dir=")) {
                path = param.replace("\\",File.separator);;
                System.out.println("\nDirectory : '" + param + "'");

            } else if (arg.contains("-regex=")) {

                filter.setPattern(param);
                System.out.println("\nFilter : all the files containing '" + param + "'");

            } else if (arg.contains("-help")) {

                help();

            } else if (arg.contains("-gtWeight")) {

                filter.GtWeight(Integer.parseInt(param));
                System.out.println("\nFilter : all the files with a weight greater than '" + param + "'");

            } else if (arg.contains("-lwWeight")) {

                filter.LwWeight(Integer.parseInt(param));
                System.out.println("\nFilter : all the files with a weight lower than '" + param + "'");

            } else if (arg.contains("-lwWeight")) {

                filter.LwWeight(Integer.parseInt(param));
                System.out.println("\nFilter : all the files with a weight lower than '" + param + "'");

            } else if (arg.contains("-parallelism=")) {
                parallelism = Integer.parseInt(param);
                System.out.println("Parallelism : " + param);

            } else if (arg.contains("-tree=")) {

                TreeModel model = fileTree.tree(path, filter, parallelism, Integer.parseInt(param));
                fileTree.display(((File1) model.getRoot()));

            } else if (arg.contains("-tree")) {

                TreeModel model = fileTree.tree(path, filter, parallelism);
                fileTree.display(((File1) model.getRoot()));

            } else if (arg.contains("-duplicates=")) {
                ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicatesWithLimitedDepth(path, filter, parallelism, Integer.parseInt(param));
                fileTree.displayDuplicates(duplicates);

            } else if (arg.contains("-duplicates")) {
                ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicates(path, filter, parallelism);
                fileTree.displayDuplicates(duplicates);

            } else if (arg.contains("-errors")) {
                ArrayList<String> errorLogs = ErrorLogging.getInstance().getLogs();
                System.out.println("Errors (" + errorLogs.size() +")");
                for (String log : errorLogs) {
                    System.out.println(log);
                }

            } else {

                System.out.println("'" + arg + "' is not accepted as an option.");

            }
        }
    }

    public static String getParam(String arg) throws Exception {
        String[] filterStr = arg.split("=");
        if (filterStr.length != 2) {
            throw new Exception("No params for '" + arg + "' option");
        }
        return filterStr[1];
    }

    public static void help()
    {
        System.out.println("#### Commands available (please, respect order): \n");

        System.out.println("# -dir=\"directory\" -> Choose the target directory.");
        System.out.println("# -regex=REGEX -> Add a regex filter to the tree and duplicates commands.");
        System.out.println("# -gtWeight=nbBytes -> Filter to accept only files greater than the number passed.");
        System.out.println("# -lwWeight=nbBytes -> Filter to accept only files lower than the number passed.");
        System.out.println("# -parallelism=number -> Use a level of parallelism (multi-threading) for the tree and duplicates commands.");
        System.out.println("# -tree -> Build and display the tree.");
        System.out.println("# -tree=depth -> Build and display the tree with a limited depth.");
        System.out.println("# -duplicates -> Collect and display duplicates.");
        System.out.println("# -duplicates=depth -> Collect and display duplicates with a limited depth.");
        System.out.println("# -errors -> Display all the errors that happened during tree and duplicates commands.");


    }
}

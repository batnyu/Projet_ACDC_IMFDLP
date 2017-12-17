package acdc;

import acdc.Core.FileTree;
import acdc.Core.Utils.Filter;
import acdc.Services.ErrorLogging;
import acdc.Services.Settings;
import acdc.TreeDataModel.FileTreeCellRenderer;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class MainWithJTree extends JFrame {

    public MainWithJTree() throws IOException {
        long startTime = System.currentTimeMillis();

        //String path2 = "C:" + File.separator;
        //String path2 = "D:" + File.separator;
        String path2 = "dossierTest";


///////// Set the path of the cacheHash.txt file /////////
        Settings.getInstance().setPathCacheHash("cacheHash.txt");

///////// FILTER CREATION /////////
        Filter filter = Filter.createFilter();
        //filter.setPattern("mkv");
        //filter.addRefusedFiles("msdia80.dll");
        //filter.addRefusedExtension("mkv");
        //filter.addExtension("mkv");
        //filter.addExtension("txt");
        //filter.setName("msdia80.dll");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(36423);
        //filter.GtWeight(61735);

///////// GETTING A TREE /////////
        FileTree fileTree = FileTree.creerFileTree();
        //File1 root = fileTree.createTreeWithForkAndJoinWalkFileTree(Paths.get(path2), filter, 2, Paths.get(path2).getNameCount(), Integer.MAX_VALUE);
        TreeModel model = fileTree.tree(path2, filter, 2);
        //TreeModel model = fileTree.tree(path2,filter,2,2);

        //Example of getting the root of the tree
        //System.out.println("rootPath = " + ((File1) model.getRoot()).getAbsolutePath());

        JTree tree = new JTree(model);
        tree.setCellRenderer(new FileTreeCellRenderer());

///////// Preparing the event listener to right clic on node to collect duplicates from there /////////
        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if (selRow != -1) {
                    if (e.getClickCount() == 1) {
                        tree.setSelectionRow(selRow);
                        System.out.println(selPath);
                    } else if (e.getClickCount() == 2) {
                    }
                }
            }
        };
        tree.addMouseListener(ml);

        JScrollPane treeView = new JScrollPane(tree);
        this.add(treeView);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("Il me faut de la place !");
        this.pack();
        this.setBounds(0, 0, 600, 600);
        this.setVisible(true);

///////// COLLECT DUPLICATES /////////
        ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicates(path2, filter, 1);
        //ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicatesWithLimitedDepth(path2,filter,1,2);

///////// DISPLAY DUPLICATES /////////
        fileTree.displayDuplicates(duplicates);

///////// COLLECT ERRORS /////////
        System.out.println("Erreurs : ");
        ArrayList<String> errorLogs = ErrorLogging.getInstance().getLogs();
        for (String log : errorLogs) {
            System.out.println(log);
        }

///////// Displaying elapsed time /////////
        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("elapsedTime : " + elapsedTime + " ms");
        System.out.println((double) elapsedTime / 1000 / 60 + " minutes");


    }

    public static void main(String[] args) throws IOException {
        MainWithJTree window = new MainWithJTree();
    }

}


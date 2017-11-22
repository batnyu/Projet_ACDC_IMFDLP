package acdc;


import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class Test extends JFrame {

    public Test() throws IOException {
        //String path2 = "D:" + File.separator;
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test";
        String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Pictures";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste";
        //String path2 = "C:" + File.separator;
        //String path2 = "C:\\Program Files (x86)\\Steam\\SteamApps";
        //String path2 = "C:\\Program Files (x86)";

        Filter filter = new Filter();
        //filter.setPattern("app");
        //filter.addRefusedFiles("taco.vtf");
        //filter.addRefusedExtension("txt");
        //filter.addExtension("mkv");
        //filter.addExtension("txt");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(36423);
        //filter.GtWeight(61735);

        long startTime = System.currentTimeMillis();

        FileTree fileTree = new FileTree();
        TreeModel model = fileTree.tree(path2, filter,1);
        //TreeModel model = fileTree.tree(path2,filter,2,2);

/*        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("elapsedTime : " + elapsedTime + " ms");
        System.out.println((double)elapsedTime/1000/60 + " minutes");*/

        //Exemple récupération de la racine
        //System.out.println("rootPath = " + ((File1) model.getRoot()).getAbsolutePath());

        JTree tree = new JTree(model);
        tree.setCellRenderer(new FileTreeCellRenderer());

        MouseListener ml = new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                int selRow = tree.getRowForLocation(e.getX(), e.getY());
                TreePath selPath = tree.getPathForLocation(e.getX(), e.getY());
                if(selRow != -1) {
                    if(e.getClickCount() == 1) {
                        tree.setSelectionRow(selRow);
                        System.out.println(selPath);
                    }
                    else if(e.getClickCount() == 2) {
                    }
                }
            }
        };
        tree.addMouseListener(ml);

        JScrollPane treeView = new JScrollPane(tree);
        this.add(treeView);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("JTree Example");
        this.pack();
        this.setBounds(0, 0, 600, 600);
        this.setVisible(true);

        ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicates(path2, filter,1);
        //ConcurrentHashMap<String, ConcurrentLinkedQueue<File>> duplicates = fileTree.collectDuplicatesWithLimitedDepth(path2,filter,1,2);

        displayDuplicates(duplicates);

        //Récupération des erreurs
        System.out.println("Erreurs : ");
        ArrayList<String> errorLogs = ErrorLogging.getInstance().getLogs();
        for (String log: errorLogs) {
            System.out.println(log);
        }

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("elapsedTime : " + elapsedTime + " ms");
        System.out.println((double)elapsedTime/1000/60 + " minutes");

    }

    private void displayDuplicates(Map<String, ConcurrentLinkedQueue<File>> doublons) {
        System.out.println("\n\n--- DOUBLONS ---\n");
        int compteur=0;


        for (Map.Entry<String, ConcurrentLinkedQueue<File>> entry : doublons.entrySet()) {
            //Useless condition if you clean duplicates with the cleanDuplicates method of FileTree class.
            if(entry.getValue().size() > 1){
                System.out.println("hash : " + entry.getKey());
                for (File file : entry.getValue()) {
                    System.out.println(file.getAbsolutePath());
                }
                System.out.println("");
                compteur++;
            }
        }
        System.out.println("nb : " + compteur);
    }

    public static void main(String[] args) throws IOException {

        Test window = new Test();
    }

}


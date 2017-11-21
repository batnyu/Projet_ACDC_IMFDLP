package acdc;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

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
        //filter.addExtension("mkv");
        //filter.addExtension("txt");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(36423);
        //filter.GtWeight(61735);

        long startTime = System.currentTimeMillis();

        FileTree fileTree = FileTree.createFileTree(path2, filter);
        //FileTree fileTree = FileTree.createFileTreeWithLimitedDepth(path2, filter, 2);
        fileTree.buildFileTree(1,2);
        //fileTree.collectDoublons(path2,2);

        fileTree.collectDoublonsWithLimitedDepth(path2,1,2);
        displayDuplicates(fileTree);

        fileTree.collectDoublonsWithLimitedDepth(path2,1,2);
        displayDuplicates(fileTree);

        //TEST OF HASH
/*        try {
            //System.out.println(Hash.md5OfFile(new File1("D:\\Downloads T\\Fallout.New.Vegas.Ultimate.Edition-PROPHET\\ppt-fvue.iso")));

            System.out.println(Hash.sampleHashFile("D:\\Downloads T\\Fallout.New.Vegas.Ultimate.Edition-PROPHET\\ppt-fvue.iso"));
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        long stopTime = System.currentTimeMillis();
        long elapsedTime = stopTime - startTime;
        System.out.println("elapsedTime : " + elapsedTime + " ms");
        System.out.println((double)elapsedTime/1000/60 + " minutes");


        JTree test = new JTree(new FileTreeModel(fileTree.root));
        test.setCellRenderer(new FileTreeCellRenderer());

        JScrollPane treeView = new JScrollPane(test);
        this.add(treeView);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("JTree Example");
        this.pack();
        this.setVisible(true);
    }

    private void displayDuplicates(FileTree fileTree) {
        System.out.println("\n\n--- DOUBLONS ---\n");
        Map<String, ConcurrentLinkedQueue<File>> doublons = fileTree.getDoublons();
        int compteur=0;


        for (Map.Entry<String, ConcurrentLinkedQueue<File>> entry : doublons.entrySet()) {
            //Useless conditions if you clean duplicates.
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
        window.setBounds(0, 0, 600, 600);
        window.setVisible(true);
    }

}


package acdc;


import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import javax.swing.*;

public class Test extends JFrame {

    public Test() throws IOException {
        //String path2 = "D:" + File1.separator;
        //String path2 = "C:" + File1.separator + "Users" + File1.separator + "Baptiste" + File1.separator + "Desktop" + File1.separator + "test";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Pictures";
        //String path2 = "C:" + File1.separator + "Users" + File1.separator + "Baptiste";
        String path2 = "C:" + File.separator;
        //String path2 = "C:\\Program Files (x86)\\Steam\\SteamApps";

        Filter filter = new Filter();
        //filter.addExtension("jpg");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(36423);
        //filter.GtWeight(61735);

        long startTime = System.currentTimeMillis();

        FileTree fileTree = FileTree.createFileTree(path2, filter, false);
        fileTree.buildFileTree(true,2);


        System.out.println("\n\n--- DOUBLONS ---\n");
        Map<String, ConcurrentLinkedQueue<String>> doublons = fileTree.getDoublons();
        int compteur=0;


        for (Map.Entry<String, ConcurrentLinkedQueue<String>> entry : doublons.entrySet()) {
            if(entry.getValue().size() > 1){
                System.out.println("hash : " + entry.getKey());
                for (String file : entry.getValue()) {
                    System.out.println(file);
                }
                System.out.println("");
                compteur++;
            }
        }
        System.out.println("nb : " + compteur);

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


        JTree test = new JTree(fileTree.root);
        test.setCellRenderer(new FileTreeCellRenderer());

        JScrollPane treeView = new JScrollPane(test);
        this.add(treeView);
        this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        this.setTitle("JTree Example");
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        Test window = new Test();
        window.setBounds(0, 0, 600, 600);
        window.setVisible(true);
    }

}


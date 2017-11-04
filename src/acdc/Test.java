package acdc;


import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import javax.swing.*;

public class Test extends JFrame {

    public Test() throws IOException {
        //String path2 = "D:" + File.separator;
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Pictures";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste";
        String path2 = "C:" + File.separator;

        Filter filter = new Filter();
        //filter.addExtension("jpg");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(36423);
        //filter.GtWeight(61735);

        long startTime = System.currentTimeMillis();

        FileTree fileTree = new FileTree(path2, filter, true);
        fileTree.buildFileTree(2,3);


        System.out.println("\n\n--- DOUBLONS ---\n");
        Map<String, List<String>> doublons = fileTree.getDoublons();
        int compteur=0;


        for (Map.Entry<String, List<String>> entry : doublons.entrySet()) {
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
            //System.out.println(Hash.md5OfFile(new File("D:\\Downloads T\\Fallout.New.Vegas.Ultimate.Edition-PROPHET\\ppt-fvue.iso")));

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


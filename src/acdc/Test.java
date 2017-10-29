package acdc;


import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.*;

public class Test extends JFrame {

    public Test() throws IOException {
        String path2 = "D:" + File.separator;
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "pictures";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste";

        Filter filter = new Filter();
        //filter.addExtension("jpg");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(61735);
        //filter.GtWeight(61735);

        FileTree fileTree = new FileTree(path2, filter, true);
        fileTree.buildFileTree();

        Map<String, List<String>> doublons = fileTree.getDoublons();

        for (Map.Entry<String, List<String>> entry : doublons.entrySet()) {
            if(entry.getValue().size() > 1){
                System.out.println(entry.getKey());
                for (String file : entry.getValue()) {
                    System.out.println(file);
                }
                System.out.println("");
            }

        }

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


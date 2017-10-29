package acdc;


import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;

public class Test extends JFrame {

    public Test() throws IOException {
        //String path2 = "D:" + File.separator;
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "Desktop" + File.separator + "test";
        String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste" + File.separator + "pictures";
        //String path2 = "C:" + File.separator + "Users" + File.separator + "Baptiste";

        Filter filter = new Filter();
        filter.addExtension("jpg");
        //filter.setName("Cdd");
        //filter.setLastModifiedTime("14/05/2013");
        //filter.equalsWeight(61735));
        //filter.LwWeight(61735);
        filter.GtWeight(61735);

        FileTree fileTree = new FileTree(path2, filter, true);
        fileTree.buildFileTree();

        Map<String, List<String>> doublons = fileTree.getDoublons();

        for (List<String> list : doublons.values()) {
            if (list.size() > 1) {
                System.out.println("ICI\n");
                for (String file : list) {
                    System.out.println(file);
                }
            }
        }

        JTree test = new JTree(fileTree.root);
        test.setCellRenderer(new FileTreeCellRenderer());
//		for (int i = 0; i < test.getRowCount(); i++) {
//			test.expandRow(i);
//			System.out.println(i);
//		}

        JScrollPane treeView = new JScrollPane(test);
        this.add(treeView);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setTitle("JTree Example");
        this.pack();
        this.setVisible(true);
    }

    public static void main(String[] args) throws IOException {

        Test fenetre = new Test();
        fenetre.setBounds(0, 0, 600, 600);
        fenetre.setVisible(true);
    }

}


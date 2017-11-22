package acdc.TreeDataModel;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;

/**
 * <b>FileTreeCellRenderer is a custom implementation of DefaultTreeCellRenderer</b>
 *
 * <p>
 * It is used to modify the icon of empty file that are showed as leaf.
 * It also changes the root icon to a computer.
 * Here, you can change the style of the JTree.
 *
 * @author Baptiste
 * @version 1.0
 */
public class FileTreeCellRenderer extends DefaultTreeCellRenderer {

    public Component getTreeCellRendererComponent(JTree tree,
                                                  Object value, boolean sel, boolean expanded, boolean leaf,
                                                  int row, boolean hasFocus) {
        JLabel renderer = (JLabel) super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);

        if (value instanceof File1) {

            File1 node = (File1) value;

            if (node.isRoot()) {

                renderer.setIcon(UIManager.getIcon("FileView.computerIcon"));

            } else {
                if (node.isDirectory()) {
                    if (expanded) {
                        renderer.setIcon(openIcon);
                    } else {
                        renderer.setIcon(closedIcon);
                    }
                } else {
                    renderer.setIcon(leafIcon);
                }
            }
        }
        return renderer;
    }

}

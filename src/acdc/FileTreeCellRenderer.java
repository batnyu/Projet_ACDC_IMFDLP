package acdc;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

class FileTreeCellRenderer extends DefaultTreeCellRenderer {
	  
    public Component getTreeCellRendererComponent(JTree tree,
            Object value, boolean sel, boolean expanded, boolean leaf,
            int row, boolean hasFocus)
    {
        JLabel renderer = (JLabel)super.getTreeCellRendererComponent(
                tree, value, sel, expanded, leaf, row, hasFocus);
                
        if (value instanceof DefaultMutableTreeNode) {
        	
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            
            if (node.getUserObject() instanceof String) {
            	
            	renderer.setIcon(UIManager.getIcon("FileView.computerIcon"));
            
            } else if (node.getUserObject() instanceof File1) {
            	File1 contact = (File1) node.getUserObject();
            	if (contact.isDirectory()) {
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

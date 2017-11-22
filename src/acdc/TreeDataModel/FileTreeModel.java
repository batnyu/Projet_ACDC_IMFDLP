package acdc.TreeDataModel;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.util.Vector;

/**
 * <b>FileTreeModel is a custom implementation of TreeModel</b>
 *
 * <p>
 * It is useful to create my own data structure that is represented by the File1 class.
 * I did this to be able to cache my structure without difficulties with gson.toJsonTree().
 * But I didn't manage to update and add to this cache. I just had the time to implement the read of it.
 * He could be very useful to use this technique since you can use gson.fromJsonTree()
 * to get the tree structure back and put it in a JTree without listing your file system each time.
 * But it require hooks on the file system to be relevant.
 *
 * @author Baptiste
 * @version 1.0
 */
public class FileTreeModel implements TreeModel {
    private Vector<TreeModelListener> treeModelListeners = new Vector<TreeModelListener>();
    private File1 rootFile;

    public FileTreeModel(File1 root) {
        rootFile = root;
    }

///////////////////// Fire events //////////////////////////////////////////////

    /**
     * The only event raised by this model is TreeStructureChanged with the
     * root as path, i.e. the whole tree has changed.
     *
     * @param oldRoot the old root
     */
    protected void fireTreeStructureChanged(File1 oldRoot) {
        int len = treeModelListeners.size();
        TreeModelEvent e = new TreeModelEvent(this,
                new Object[]{oldRoot});
        for (TreeModelListener tml : treeModelListeners) {
            tml.treeStructureChanged(e);
        }
    }

//////////////// TreeModel interface implementation ///////////////////////

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     * @param l TreeModelListener
     */
    public void addTreeModelListener(TreeModelListener l) {
        treeModelListeners.addElement(l);
    }

    /**
     * Returns the child of parent at index index in the parent's child array.
     *
     * @param parent the object parent
     * @param index the index int
     * @return an object
     */
    public Object getChild(Object parent, int index) {
        File1 p = (File1) parent;
        return p.getChildAt(index);
    }

    /**
     * Returns the number of children of parent.
     *
     * @param parent the object parent
     * @return an index int
     */
    public int getChildCount(Object parent) {
        File1 p = (File1) parent;
        return p.getChildCount();
    }

    /**
     * Returns the index of child in parent.
     *
     * @param parent the object parent
     * @param child the object child
     * @return the index of child int
     */
    public int getIndexOfChild(Object parent, Object child) {
        File1 p = (File1) parent;
        return p.getIndexOfChild((File1) child);
    }

    /**
     * Returns the root of the tree.
     *
     * @return the root object
     */
    public Object getRoot() {
        return rootFile;
    }

    /**
     * Returns true if node is a leaf.
     *
     * @param node the object
     * @return a boolean isLeaf
     */
    public boolean isLeaf(Object node) {
        File1 p = (File1) node;
        return p.getChildCount() == 0;
    }

    /**
     * Removes a listener previously added with addTreeModelListener().
     *
     * @param l the TreeModelListener
     */
    public void removeTreeModelListener(TreeModelListener l) {
        treeModelListeners.removeElement(l);
    }

    /**
     * Messaged when the user has altered the value for the item
     * identified by path to newValue.  Not used by this model.
     *
     * @param path the treePath
     * @param newValue the new value object
     */
    public void valueForPathChanged(TreePath path, Object newValue) {
        System.out.println("*** valueForPathChanged : "
                + path + " --> " + newValue);
    }
}

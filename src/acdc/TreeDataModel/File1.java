package acdc.TreeDataModel;

import java.beans.Transient;
import java.nio.file.attribute.FileTime;
import java.util.*;

/**
 * <b>File1 is the class representing the data model of the tree</b>
 *
 * <p>
 * I choose to implement my own data model and not DefaultMutableTreeNode
 * when I try to build a json cache. It was more easier than with a DefaultMutableTreeNode.
 *
 * <p>
 * I copied the Services offered by DefaultMutableTreeNode in this class
 * to provide the basic actions and
 * the different Enumerations for iterating through the tree (depth first, width first...)
 *
 * @author Baptiste
 * @version 1.0
 */
public class File1 {

    /**
     * An enumeration that is always empty. This is used when an enumeration
     * of a leaf node's children is requested.
     */
    static public final Enumeration<File1> EMPTY_ENUMERATION = Collections.emptyEnumeration();

    /**
     * this node's parent, or null if this node has no parent
     */
    //transient to avoid parent being cached.
    protected transient File1 parent;

    public String filename;
    public long weight;
    public String absolutePath;
    public boolean isDirectory;
    public FileTime lastModifiedTime;

    Vector<File1> children;

    public File1(String filename, long weight, String absolutePath, FileTime lastModifiedTime, boolean isDirectory) {
        this.filename = filename;
        this.weight = weight;
        this.absolutePath = absolutePath;
        this.lastModifiedTime = lastModifiedTime;
        this.isDirectory = isDirectory;

        children = new Vector<>();
    }

    public String getFilename() {
        return filename;
    }

    public long getWeight() {
        return weight;
    }

    public String getAbsolutePath() {
        return absolutePath;
    }

    public FileTime getLastModifiedTime() {
        return lastModifiedTime;
    }

    public int getIndexOfChild(File1 file1) {
        return children.indexOf(file1);
    }


    public void setFilename(String filename) {
        this.filename = filename;
    }

    public void setWeight(long weight) {
        this.weight = weight;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath = absolutePath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public void setLastModifiedTime(FileTime lastModifiedTime) {
        this.lastModifiedTime = lastModifiedTime;
    }


    public String toString() {
        return filename + " (" + String.valueOf(this.weight) + " octets)";
    }

    /**
     * Removes <code>newChild</code> from its present parent (if it has a
     * parent), sets the child's parent to this node, and then adds the child
     * to this node's child array at index <code>childIndex</code>.
     * <code>newChild</code> must not be null and must not be an ancestor of
     * this node.
     *
     * @param newChild   the File1 to insert under this node
     * @param childIndex the index in this node's child array
     *                   where this node is to be inserted
     * @throws ArrayIndexOutOfBoundsException if
     *                                        <code>childIndex</code> is out of bounds
     * @throws IllegalArgumentException       if
     *                                        <code>newChild</code> is null or is an
     *                                        ancestor of this node
     * @throws IllegalStateException          if this node does not allow
     *                                        children
     * @see #isNodeDescendant
     */
    public void insert(File1 newChild, int childIndex) {
        if (newChild == null) {
            throw new IllegalArgumentException("new child is null");
        } else if (isNodeAncestor(newChild)) {
            throw new IllegalArgumentException("new child is an ancestor");
        }

        File1 oldParent = newChild.getParent();

        if (oldParent != null) {
            oldParent.remove(newChild);
        }
        newChild.setParent(this);
        if (children == null) {
            children = new Vector();
        }
        children.insertElementAt(newChild, childIndex);
    }

    /**
     * Removes the child at the specified index from this node's children
     * and sets that node's parent to null. The child node to remove
     * must be a <code>File1</code>.
     *
     * @param childIndex the index in this node's child array
     *                   of the child to remove
     * @throws ArrayIndexOutOfBoundsException if
     *                                        <code>childIndex</code> is out of bounds
     */
    public void remove(int childIndex) {
        File1 child = getChildAt(childIndex);
        children.removeElementAt(childIndex);
        child.setParent(null);
    }

    /**
     * Sets this node's parent to <code>newParent</code> but does not
     * change the parent's child array.  This method is called from
     * <code>insert()</code> and <code>remove()</code> to
     * reassign a child's parent, it should not be messaged from anywhere
     * else.
     *
     * @param newParent this node's new parent
     */
    @Transient
    public void setParent(File1 newParent) {
        parent = newParent;
    }

    /**
     * Returns this node's parent or null if this node has no parent.
     *
     * @return this node's parent File1, or null if this node has no parent
     */
    public File1 getParent() {
        return parent;
    }

    /**
     * Returns the child at the specified index in this node's child array.
     *
     * @param index an index into this node's child array
     * @return the File1 in this node's child array at  the specified index
     * @throws ArrayIndexOutOfBoundsException if <code>index</code>
     *                                        is out of bounds
     */
    public File1 getChildAt(int index) {
        if (children == null) {
            throw new ArrayIndexOutOfBoundsException("node has no children");
        }
        return children.elementAt(index);
    }

    /**
     * Returns the number of children of this node.
     *
     * @return an int giving the number of children of this node
     */
    public int getChildCount() {
        if (children == null) {
            return 0;
        } else {
            return children.size();
        }
    }

    /**
     * Returns the index of the specified child in this node's child array.
     * If the specified node is not a child of this node, returns
     * <code>-1</code>.  This method performs a linear search and is O(n)
     * where n is the number of children.
     *
     * @param aChild the File1 to search for among this node's children
     * @return an int giving the index of the node in this node's child
     * array, or <code>-1</code> if the specified node is a not
     * a child of this node
     * @throws IllegalArgumentException if <code>aChild</code>
     *                                  is null
     */
    public int getIndex(File1 aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(aChild)) {
            return -1;
        }
        return children.indexOf(aChild);        // linear search
    }

    /**
     * Creates and returns a forward-order enumeration of this node's
     * children.  Modifying this node's child array invalidates any child
     * enumerations created before the modification.
     *
     * @return an Enumeration of this node's children
     */
    public Enumeration children() {
        if (children == null) {
            return EMPTY_ENUMERATION;
        } else {
            return children.elements();
        }
    }

    //
    //  Derived methods
    //

    /**
     * Removes the subtree rooted at this node from the tree, giving this
     * node a null parent.  Does nothing if this node is the root of its
     * tree.
     */
    public void removeFromParent() {
        File1 parent = getParent();
        if (parent != null) {
            parent.remove(this);
        }
    }

    /**
     * Removes <code>aChild</code> from this node's child array, giving it a
     * null parent.
     *
     * @param aChild a child of this node to remove
     * @throws IllegalArgumentException if <code>aChild</code>
     *                                  is null or is not a child of this node
     */
    public void remove(File1 aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        if (!isNodeChild(aChild)) {
            throw new IllegalArgumentException("argument is not a child");
        }
        remove(getIndex(aChild));       // linear search
    }

    /**
     * Removes all of this node's children, setting their parents to null.
     * If this node has no children, this method does nothing.
     */
    public void removeAllChildren() {
        for (int i = getChildCount() - 1; i >= 0; i--) {
            remove(i);
        }
    }

    /**
     * Removes <code>newChild</code> from its parent and makes it a child of
     * this node by adding it to the end of this node's child array.
     *
     * @param newChild node to add as a child of this node
     * @throws IllegalArgumentException if <code>newChild</code>
     *                                  is null
     * @throws IllegalStateException    if this node does not allow
     *                                  children
     * @see #insert
     */
    public void add(File1 newChild) {
        if (newChild != null && newChild.getParent() == this)
            insert(newChild, getChildCount() - 1);
        else
            insert(newChild, getChildCount());
    }


    //
    //  Tree Queries
    //

    /**
     * Returns true if <code>anotherNode</code> is an ancestor of this node
     * -- if it is this node, this node's parent, or an ancestor of this
     * node's parent.  (Note that a node is considered an ancestor of itself.)
     * If <code>anotherNode</code> is null, this method returns false.  This
     * operation is at worst O(h) where h is the distance from the root to
     * this node.
     *
     * @param anotherNode node to test as an ancestor of this node
     * @return true if this node is a descendant of <code>anotherNode</code>
     * @see #isNodeDescendant
     * @see #getSharedAncestor
     */
    public boolean isNodeAncestor(File1 anotherNode) {
        if (anotherNode == null) {
            return false;
        }

        File1 ancestor = this;

        do {
            if (ancestor == anotherNode) {
                return true;
            }
        } while ((ancestor = ancestor.getParent()) != null);

        return false;
    }

    /**
     * Returns true if <code>anotherNode</code> is a descendant of this node
     * -- if it is this node, one of this node's children, or a descendant of
     * one of this node's children.  Note that a node is considered a
     * descendant of itself.  If <code>anotherNode</code> is null, returns
     * false.  This operation is at worst O(h) where h is the distance from the
     * root to <code>anotherNode</code>.
     *
     * @param anotherNode node to test as descendant of this node
     * @return true if this node is an ancestor of <code>anotherNode</code>
     * @see #isNodeAncestor
     * @see #getSharedAncestor
     */
    public boolean isNodeDescendant(File1 anotherNode) {
        if (anotherNode == null)
            return false;

        return anotherNode.isNodeAncestor(this);
    }

    /**
     * Returns the nearest common ancestor to this node and <code>aNode</code>.
     * Returns null, if no such ancestor exists -- if this node and
     * <code>aNode</code> are in different trees or if <code>aNode</code> is
     * null.  A node is considered an ancestor of itself.
     *
     * @param aNode node to find common ancestor with
     * @return nearest ancestor common to this node and <code>aNode</code>,
     * or null if none
     * @see #isNodeAncestor
     * @see #isNodeDescendant
     */
    public File1 getSharedAncestor(File1 aNode) {
        if (aNode == this) {
            return this;
        } else if (aNode == null) {
            return null;
        }

        int level1, level2, diff;
        File1 node1, node2;

        level1 = getLevel();
        level2 = aNode.getLevel();

        if (level2 > level1) {
            diff = level2 - level1;
            node1 = aNode;
            node2 = this;
        } else {
            diff = level1 - level2;
            node1 = this;
            node2 = aNode;
        }

        // Go up the tree until the nodes are at the same level
        while (diff > 0) {
            node1 = node1.getParent();
            diff--;
        }

        // Move up the tree until we find a common ancestor.  Since we know
        // that both nodes are at the same level, we won't cross paths
        // unknowingly (if there is a common ancestor, both nodes hit it in
        // the same iteration).

        do {
            if (node1 == node2) {
                return node1;
            }
            node1 = node1.getParent();
            node2 = node2.getParent();
        } while (node1 != null);// only need to check one -- they're at the
        // same level so if one is null, the other is

        if (node1 != null || node2 != null) {
            throw new Error("nodes should be null");
        }

        return null;
    }


    /**
     * Returns true if and only if <code>aNode</code> is in the same tree
     * as this node.  Returns false if <code>aNode</code> is null.
     *
     * @return true if <code>aNode</code> is in the same tree as this node;
     * false if <code>aNode</code> is null
     * @see #getSharedAncestor
     * @see #getRoot
     */
    public boolean isNodeRelated(File1 aNode) {
        return (aNode != null) && (getRoot() == aNode.getRoot());
    }


    /**
     * Returns the depth of the tree rooted at this node -- the longest
     * distance from this node to a leaf.  If this node has no children,
     * returns 0.  This operation is much more expensive than
     * <code>getLevel()</code> because it must effectively traverse the entire
     * tree rooted at this node.
     *
     * @return the depth of the tree whose root is this node
     * @see #getLevel
     */
    public int getDepth() {
        Object last = null;
        Enumeration enum_ = breadthFirstEnumeration();

        while (enum_.hasMoreElements()) {
            last = enum_.nextElement();
        }

        if (last == null) {
            throw new Error("nodes should be null");
        }

        return ((File1) last).getLevel() - getLevel();
    }


    /**
     * Returns the number of levels above this node -- the distance from
     * the root to this node.  If this node is the root, returns 0.
     *
     * @return the number of levels above this node
     * @see #getDepth
     */
    public int getLevel() {
        File1 ancestor;
        int levels = 0;

        ancestor = this;
        while ((ancestor = ancestor.getParent()) != null) {
            levels++;
        }

        return levels;
    }


    /**
     * Returns the path from the root, to get to this node.  The last
     * element in the path is this node.
     *
     * @return an array of File1 objects giving the path, where the
     * first element in the path is the root and the last
     * element is this node.
     */
    public File1[] getPath() {
        return getPathToRoot(this, 0);
    }

    /**
     * Builds the parents of node up to and including the root node,
     * where the original node is the last element in the returned array.
     * The length of the returned array gives the node's depth in the
     * tree.
     *
     * @param aNode the File1 to get the path for
     * @param depth an int giving the number of steps already taken towards
     *              the root (on recursive calls), used to size the returned array
     * @return an array of File1s giving the path from the root to the
     * specified node
     */
    protected File1[] getPathToRoot(File1 aNode, int depth) {
        File1[] retNodes;

        /* Check for null, in case someone passed in a null node, or
           they passed in an element that isn't rooted at root. */
        if (aNode == null) {
            if (depth == 0)
                return null;
            else
                retNodes = new File1[depth];
        } else {
            depth++;
            retNodes = getPathToRoot(aNode.getParent(), depth);
            retNodes[retNodes.length - depth] = aNode;
        }
        return retNodes;
    }

    /**
     * Returns the root of the tree that contains this node.  The root is
     * the ancestor with a null parent.
     *
     * @return the root of the tree that contains this node
     * @see #isNodeAncestor
     */
    public File1 getRoot() {
        File1 ancestor = this;
        File1 previous;

        do {
            previous = ancestor;
            ancestor = ancestor.getParent();
        } while (ancestor != null);

        return previous;
    }


    /**
     * Returns true if this node is the root of the tree.  The root is
     * the only node in the tree with a null parent; every tree has exactly
     * one root.
     *
     * @return true if this node is the root of its tree
     */
    public boolean isRoot() {
        return getParent() == null;
    }


    /**
     * Returns the node that follows this node in a preorder traversal of this
     * node's tree.  Returns null if this node is the last node of the
     * traversal.  This is an inefficient way to traverse the entire tree; use
     * an enumeration, instead.
     *
     * @return the node that follows this node in a preorder traversal, or
     * null if this node is last
     * @see #preorderEnumeration
     */
    public File1 getNextNode() {
        if (getChildCount() == 0) {
            // No children, so look for nextSibling
            File1 nextSibling = getNextSibling();

            if (nextSibling == null) {
                File1 aNode = (File1) getParent();

                do {
                    if (aNode == null) {
                        return null;
                    }

                    nextSibling = aNode.getNextSibling();
                    if (nextSibling != null) {
                        return nextSibling;
                    }

                    aNode = (File1) aNode.getParent();
                } while (true);
            } else {
                return nextSibling;
            }
        } else {
            return (File1) getChildAt(0);
        }
    }


    /**
     * Returns the node that precedes this node in a preorder traversal of
     * this node's tree.  Returns <code>null</code> if this node is the
     * first node of the traversal -- the root of the tree.
     * This is an inefficient way to
     * traverse the entire tree; use an enumeration, instead.
     *
     * @return the node that precedes this node in a preorder traversal, or
     * null if this node is the first
     * @see #preorderEnumeration
     */
    public File1 getPreviousNode() {
        File1 previousSibling;
        File1 myParent = (File1) getParent();

        if (myParent == null) {
            return null;
        }

        previousSibling = getPreviousSibling();

        if (previousSibling != null) {
            if (previousSibling.getChildCount() == 0)
                return previousSibling;
            else
                return previousSibling.getLastLeaf();
        } else {
            return myParent;
        }
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in preorder.  The first node returned by the enumeration's
     * <code>nextElement()</code> method is this node.<P>
     * <p>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @return an enumeration for traversing the tree in preorder
     * @see #postorderEnumeration
     */
    public Enumeration preorderEnumeration() {
        return new File1.PreorderEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in postorder.  The first node returned by the enumeration's
     * <code>nextElement()</code> method is the leftmost leaf.  This is the
     * same as a depth-first traversal.<P>
     * <p>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @return an enumeration for traversing the tree in postorder
     * @see #depthFirstEnumeration
     * @see #preorderEnumeration
     */
    public Enumeration postorderEnumeration() {
        return new File1.PostorderEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in breadth-first order.  The first node returned by the
     * enumeration's <code>nextElement()</code> method is this node.<P>
     * <p>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @return an enumeration for traversing the tree in breadth-first order
     * @see #depthFirstEnumeration
     */
    public Enumeration breadthFirstEnumeration() {
        return new File1.BreadthFirstEnumeration(this);
    }

    /**
     * Creates and returns an enumeration that traverses the subtree rooted at
     * this node in depth-first order.  The first node returned by the
     * enumeration's <code>nextElement()</code> method is the leftmost leaf.
     * This is the same as a postorder traversal.<P>
     * <p>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @return an enumeration for traversing the tree in depth-first order
     * @see #breadthFirstEnumeration
     * @see #postorderEnumeration
     */
    public Enumeration depthFirstEnumeration() {
        return postorderEnumeration();
    }

    /**
     * Creates and returns an enumeration that follows the path from
     * <code>ancestor</code> to this node.  The enumeration's
     * <code>nextElement()</code> method first returns <code>ancestor</code>,
     * then the child of <code>ancestor</code> that is an ancestor of this
     * node, and so on, and finally returns this node.  Creation of the
     * enumeration is O(m) where m is the number of nodes between this node
     * and <code>ancestor</code>, inclusive.  Each <code>nextElement()</code>
     * message is O(1).<P>
     * <p>
     * Modifying the tree by inserting, removing, or moving a node invalidates
     * any enumerations created before the modification.
     *
     * @return an enumeration for following the path from an ancestor of
     * this node to this one
     * @throws IllegalArgumentException if <code>ancestor</code> is
     *                                  not an ancestor of this node
     * @see #isNodeAncestor
     * @see #isNodeDescendant
     */
    public Enumeration pathFromAncestorEnumeration(File1 ancestor) {
        return new PathBetweenNodesEnumeration(ancestor, this);
    }


    //
    //  Child Queries
    //

    /**
     * Returns true if <code>aNode</code> is a child of this node.  If
     * <code>aNode</code> is null, this method returns false.
     *
     * @return true if <code>aNode</code> is a child of this node; false if
     * <code>aNode</code> is null
     */
    public boolean isNodeChild(File1 aNode) {
        boolean retval;

        if (aNode == null) {
            retval = false;
        } else {
            if (getChildCount() == 0) {
                retval = false;
            } else {
                retval = (aNode.getParent() == this);
            }
        }

        return retval;
    }


    /**
     * Returns this node's first child.  If this node has no children,
     * throws NoSuchElementException.
     *
     * @return the first child of this node
     * @throws NoSuchElementException if this node has no children
     */
    public File1 getFirstChild() {
        if (getChildCount() == 0) {
            throw new NoSuchElementException("node has no children");
        }
        return getChildAt(0);
    }


    /**
     * Returns this node's last child.  If this node has no children,
     * throws NoSuchElementException.
     *
     * @return the last child of this node
     * @throws NoSuchElementException if this node has no children
     */
    public File1 getLastChild() {
        if (getChildCount() == 0) {
            throw new NoSuchElementException("node has no children");
        }
        return getChildAt(getChildCount() - 1);
    }


    /**
     * Returns the child in this node's child array that immediately
     * follows <code>aChild</code>, which must be a child of this node.  If
     * <code>aChild</code> is the last child, returns null.  This method
     * performs a linear search of this node's children for
     * <code>aChild</code> and is O(n) where n is the number of children; to
     * traverse the entire array of children, use an enumeration instead.
     *
     * @return the child of this node that immediately follows
     * <code>aChild</code>
     * @throws IllegalArgumentException if <code>aChild</code> is
     *                                  null or is not a child of this node
     * @see #children
     */
    public File1 getChildAfter(File1 aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        int index = getIndex(aChild);           // linear search

        if (index == -1) {
            throw new IllegalArgumentException("node is not a child");
        }

        if (index < getChildCount() - 1) {
            return getChildAt(index + 1);
        } else {
            return null;
        }
    }


    /**
     * Returns the child in this node's child array that immediately
     * precedes <code>aChild</code>, which must be a child of this node.  If
     * <code>aChild</code> is the first child, returns null.  This method
     * performs a linear search of this node's children for <code>aChild</code>
     * and is O(n) where n is the number of children.
     *
     * @return the child of this node that immediately precedes
     * <code>aChild</code>
     * @throws IllegalArgumentException if <code>aChild</code> is null
     *                                  or is not a child of this node
     */
    public File1 getChildBefore(File1 aChild) {
        if (aChild == null) {
            throw new IllegalArgumentException("argument is null");
        }

        int index = getIndex(aChild);           // linear search

        if (index == -1) {
            throw new IllegalArgumentException("argument is not a child");
        }

        if (index > 0) {
            return getChildAt(index - 1);
        } else {
            return null;
        }
    }


    //
    //  Sibling Queries
    //


    /**
     * Returns true if <code>anotherNode</code> is a sibling of (has the
     * same parent as) this node.  A node is its own sibling.  If
     * <code>anotherNode</code> is null, returns false.
     *
     * @param anotherNode node to test as sibling of this node
     * @return true if <code>anotherNode</code> is a sibling of this node
     */
    public boolean isNodeSibling(File1 anotherNode) {
        boolean retval;

        if (anotherNode == null) {
            retval = false;
        } else if (anotherNode == this) {
            retval = true;
        } else {
            File1 myParent = getParent();
            retval = (myParent != null && myParent == anotherNode.getParent());

            if (retval && !((File1) getParent())
                    .isNodeChild(anotherNode)) {
                throw new Error("sibling has different parent");
            }
        }

        return retval;
    }


    /**
     * Returns the number of siblings of this node.  A node is its own sibling
     * (if it has no parent or no siblings, this method returns
     * <code>1</code>).
     *
     * @return the number of siblings of this node
     */
    public int getSiblingCount() {
        File1 myParent = getParent();

        if (myParent == null) {
            return 1;
        } else {
            return myParent.getChildCount();
        }
    }


    /**
     * Returns the next sibling of this node in the parent's children array.
     * Returns null if this node has no parent or is the parent's last child.
     * This method performs a linear search that is O(n) where n is the number
     * of children; to traverse the entire array, use the parent's child
     * enumeration instead.
     *
     * @return the sibling of this node that immediately follows this node
     * @see #children
     */
    public File1 getNextSibling() {
        File1 retval;

        File1 myParent = (File1) getParent();

        if (myParent == null) {
            retval = null;
        } else {
            retval = (File1) myParent.getChildAfter(this);      // linear search
        }

        if (retval != null && !isNodeSibling(retval)) {
            throw new Error("child of parent is not a sibling");
        }

        return retval;
    }


    /**
     * Returns the previous sibling of this node in the parent's children
     * array.  Returns null if this node has no parent or is the parent's
     * first child.  This method performs a linear search that is O(n) where n
     * is the number of children.
     *
     * @return the sibling of this node that immediately precedes this node
     */
    public File1 getPreviousSibling() {
        File1 retval;

        File1 myParent = (File1) getParent();

        if (myParent == null) {
            retval = null;
        } else {
            retval = (File1) myParent.getChildBefore(this);     // linear search
        }

        if (retval != null && !isNodeSibling(retval)) {
            throw new Error("child of parent is not a sibling");
        }

        return retval;
    }


    //
    //  Leaf Queries
    //

    /**
     * Returns true if this node has no children.  To distinguish between
     * nodes that have no children and nodes that <i>cannot</i> have
     * children (e.g. to distinguish files from empty directories), use this
     * method in conjunction with <code>getAllowsChildren</code>
     *
     * @return true if this node has no children
     */
    public boolean isLeaf() {
        return (getChildCount() == 0);
    }


    /**
     * Finds and returns the first leaf that is a descendant of this node --
     * either this node or its first child's first leaf.
     * Returns this node if it is a leaf.
     *
     * @return the first leaf in the subtree rooted at this node
     * @see #isLeaf
     * @see #isNodeDescendant
     */
    public File1 getFirstLeaf() {
        File1 node = this;

        while (!node.isLeaf()) {
            node = (File1) node.getFirstChild();
        }

        return node;
    }


    /**
     * Finds and returns the last leaf that is a descendant of this node --
     * either this node or its last child's last leaf.
     * Returns this node if it is a leaf.
     *
     * @return the last leaf in the subtree rooted at this node
     * @see #isLeaf
     * @see #isNodeDescendant
     */
    public File1 getLastLeaf() {
        File1 node = this;

        while (!node.isLeaf()) {
            node = (File1) node.getLastChild();
        }

        return node;
    }


    /**
     * Returns the leaf after this node or null if this node is the
     * last leaf in the tree.
     * <p>
     * In this implementation of the <code>MutableNode</code> interface,
     * this operation is very inefficient. In order to determine the
     * next node, this method first performs a linear search in the
     * parent's child-list in order to find the current node.
     * <p>
     * That implementation makes the operation suitable for short
     * traversals from a known position. But to traverse all of the
     * leaves in the tree, you should use <code>depthFirstEnumeration</code>
     * to enumerate the nodes in the tree and use <code>isLeaf</code>
     * on each node to determine which are leaves.
     *
     * @return returns the next leaf past this node
     * @see #depthFirstEnumeration
     * @see #isLeaf
     */
    public File1 getNextLeaf() {
        File1 nextSibling;
        File1 myParent = (File1) getParent();

        if (myParent == null)
            return null;

        nextSibling = getNextSibling(); // linear search

        if (nextSibling != null)
            return nextSibling.getFirstLeaf();

        return myParent.getNextLeaf();  // tail recursion
    }


    /**
     * Returns the leaf before this node or null if this node is the
     * first leaf in the tree.
     * <p>
     * In this implementation of the <code>MutableNode</code> interface,
     * this operation is very inefficient. In order to determine the
     * previous node, this method first performs a linear search in the
     * parent's child-list in order to find the current node.
     * <p>
     * That implementation makes the operation suitable for short
     * traversals from a known position. But to traverse all of the
     * leaves in the tree, you should use <code>depthFirstEnumeration</code>
     * to enumerate the nodes in the tree and use <code>isLeaf</code>
     * on each node to determine which are leaves.
     *
     * @return returns the leaf before this node
     * @see #depthFirstEnumeration
     * @see #isLeaf
     */
    public File1 getPreviousLeaf() {
        File1 previousSibling;
        File1 myParent = (File1) getParent();

        if (myParent == null)
            return null;

        previousSibling = getPreviousSibling(); // linear search

        if (previousSibling != null)
            return previousSibling.getLastLeaf();

        return myParent.getPreviousLeaf();              // tail recursion
    }


    /**
     * Returns the total number of leaves that are descendants of this node.
     * If this node is a leaf, returns <code>1</code>.  This method is O(n)
     * where n is the number of descendants of this node.
     *
     * @return the number of leaves beneath this node
     * @see #isNodeAncestor
     */
    public int getLeafCount() {
        int count = 0;

        File1 node;
        Enumeration enum_ = breadthFirstEnumeration(); // order matters not

        while (enum_.hasMoreElements()) {
            node = (File1) enum_.nextElement();
            if (node.isLeaf()) {
                count++;
            }
        }

        if (count < 1) {
            throw new Error("tree has zero leaves");
        }

        return count;
    }


    //
    //  Overrides
    //

    /**
     * Overridden to make clone public.  Returns a shallow copy of this node;
     * the new node has no parent or children and has a reference to the same
     * user object, if any.
     *
     * @return a copy of this node
     */
    public Object clone() {
        File1 newNode;

        try {
            newNode = (File1) super.clone();

            // shallow copy -- the new node has no parent or children
            newNode.children = null;
            newNode.parent = null;

        } catch (CloneNotSupportedException e) {
            // Won't happen because we implement Cloneable
            throw new Error(e.toString());
        }

        return newNode;
    }

    private final class PreorderEnumeration implements Enumeration<File1> {
        private final Stack<Enumeration> stack = new Stack<Enumeration>();

        public PreorderEnumeration(File1 rootNode) {
            super();
            Vector<File1> v = new Vector<File1>(1);
            v.addElement(rootNode);     // PENDING: don't really need a vector
            stack.push(v.elements());
        }

        public boolean hasMoreElements() {
            return (!stack.empty() && stack.peek().hasMoreElements());
        }

        public File1 nextElement() {
            Enumeration enumer = stack.peek();
            File1 node = (File1) enumer.nextElement();
            Enumeration children = node.children();

            if (!enumer.hasMoreElements()) {
                stack.pop();
            }
            if (children.hasMoreElements()) {
                stack.push(children);
            }
            return node;
        }

    }  // End of class PreorderEnumeration


    final class PostorderEnumeration implements Enumeration<File1> {
        protected File1 root;
        protected Enumeration<File1> children;
        protected Enumeration<File1> subtree;

        public PostorderEnumeration(File1 rootNode) {
            super();
            root = rootNode;
            children = root.children();
            subtree = EMPTY_ENUMERATION;
        }

        public boolean hasMoreElements() {
            return root != null;
        }

        public File1 nextElement() {
            File1 retval;

            if (subtree.hasMoreElements()) {
                retval = subtree.nextElement();
            } else if (children.hasMoreElements()) {
                subtree = new PostorderEnumeration(children.nextElement());
                retval = subtree.nextElement();
            } else {
                retval = root;
                root = null;
            }

            return retval;
        }

    }  // End of class PostorderEnumeration


    final class BreadthFirstEnumeration implements Enumeration<File1> {
        protected BreadthFirstEnumeration.Queue queue;

        public BreadthFirstEnumeration(File1 rootNode) {
            super();
            Vector<File1> v = new Vector<File1>(1);
            v.addElement(rootNode);     // PENDING: don't really need a vector
            queue = new BreadthFirstEnumeration.Queue();
            queue.enqueue(v.elements());
        }

        public boolean hasMoreElements() {
            return (!queue.isEmpty() &&
                    ((Enumeration) queue.firstObject()).hasMoreElements());
        }

        public File1 nextElement() {
            Enumeration enumer = (Enumeration) queue.firstObject();
            File1 node = (File1) enumer.nextElement();
            Enumeration children = node.children();

            if (!enumer.hasMoreElements()) {
                queue.dequeue();
            }
            if (children.hasMoreElements()) {
                queue.enqueue(children);
            }
            return node;
        }


        // A simple queue with a linked list data structure.
        final class Queue {
            BreadthFirstEnumeration.Queue.QNode head; // null if empty
            BreadthFirstEnumeration.Queue.QNode tail;

            final class QNode {
                public Object object;
                public BreadthFirstEnumeration.Queue.QNode next;   // null if end

                public QNode(Object object, BreadthFirstEnumeration.Queue.QNode next) {
                    this.object = object;
                    this.next = next;
                }
            }

            public void enqueue(Object anObject) {
                if (head == null) {
                    head = tail = new BreadthFirstEnumeration.Queue.QNode(anObject, null);
                } else {
                    tail.next = new BreadthFirstEnumeration.Queue.QNode(anObject, null);
                    tail = tail.next;
                }
            }

            public Object dequeue() {
                if (head == null) {
                    throw new NoSuchElementException("No more elements");
                }

                Object retval = head.object;
                BreadthFirstEnumeration.Queue.QNode oldHead = head;
                head = head.next;
                if (head == null) {
                    tail = null;
                } else {
                    oldHead.next = null;
                }
                return retval;
            }

            public Object firstObject() {
                if (head == null) {
                    throw new NoSuchElementException("No more elements");
                }

                return head.object;
            }

            public boolean isEmpty() {
                return head == null;
            }

        } // End of class Queue

    }  // End of class BreadthFirstEnumeration


    final class PathBetweenNodesEnumeration implements Enumeration<File1> {
        protected Stack<File1> stack;

        public PathBetweenNodesEnumeration(File1 ancestor,
                                           File1 descendant) {
            super();

            if (ancestor == null || descendant == null) {
                throw new IllegalArgumentException("argument is null");
            }

            File1 current;

            stack = new Stack<File1>();
            stack.push(descendant);

            current = descendant;
            while (current != ancestor) {
                current = current.getParent();
                if (current == null && descendant != ancestor) {
                    throw new IllegalArgumentException("node " + ancestor +
                            " is not an ancestor of " + descendant);
                }
                stack.push(current);
            }
        }

        public boolean hasMoreElements() {
            return stack.size() > 0;
        }

        public File1 nextElement() {
            try {
                return stack.pop();
            } catch (EmptyStackException e) {
                throw new NoSuchElementException("No more elements");
            }
        }

    } // End of class PathBetweenNodesEnumeration
}

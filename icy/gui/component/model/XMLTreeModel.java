/**
 * 
 */
package icy.gui.component.model;

import icy.util.StringUtil;

import java.util.ArrayList;

import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Stephane
 */
public class XMLTreeModel implements TreeModel
{
    public static class XMLAdapterNode
    {
        public Node node;

        /**
         * Creates a new instance of the XMLAdapterNode class
         */
        public XMLAdapterNode(Node node)
        {
            super();

            this.node = node;
        }

        /**
         * Return all children
         */
        public ArrayList<Node> getChildren()
        {
            final ArrayList<Node> result = new ArrayList<Node>();

            if (node.hasAttributes())
            {
                final NamedNodeMap attributes = node.getAttributes();
                final int count = attributes.getLength();

                for (int i = 0; i < count; i++)
                    result.add(attributes.item(i));
            }

            final NodeList nodes = node.getChildNodes();
            final int count = nodes.getLength();

            for (int i = 0; i < count; i++)
            {
                final Node node = nodes.item(i);

                if (node instanceof Element)
                    result.add(node);
            }

            return result;
        }

        /**
         * Return index of child in this node.
         * 
         * @param child
         *        The child to look for
         * @return index of child, -1 if not present (error)
         */
        public int index(XMLAdapterNode child)
        {
            int result = 0;

            for (Node node : getChildren())
            {
                if (child.node == node)
                    return result;

                result++;
            }

            return -1; // Should never get here.
        }

        /**
         * Returns an adapter node given a valid index found through
         * the method: public int index(XMLAdapterNode child)
         * 
         * @param index
         *        find this by calling index(XMLAdapterNode)
         * @return the desired child
         */
        public XMLAdapterNode child(int index)
        {
            return new XMLAdapterNode(getChildren().get(index));
        }

        /**
         * Return the number of element children for this element/node
         * 
         * @return int number of element children
         */
        public int childCount()
        {
            return getChildren().size();
        }

        /**
         * Return the value of this node from its sub text nodes
         */
        protected String getValue()
        {
            final NodeList nodes = node.getChildNodes();
            final int count = nodes.getLength();
            String result = "";

            for (int i = 0; i < count; i++)
            {
                final Node node = nodes.item(i);

                // text node
                if (!(node instanceof Element))
                {
                    final String value = node.getNodeValue();

                    if ((value != null) && !StringUtil.equals(value, "null"))
                        result += value + " ";
                }
            }

            return result.trim();
        }

        @Override
        public String toString()
        {
            final String nodeName = node.getNodeName();
            final String nodeValue = node.getNodeValue();

            if (!StringUtil.isEmpty(nodeValue) && !StringUtil.equals(nodeValue, "null"))
                return nodeName + " = " + nodeValue;

            return nodeName;
        }
    }

    protected Document document;

    /**
     * listeners
     */
    protected EventListenerList listeners = new EventListenerList();

    public XMLTreeModel(Document doc)
    {
        super();

        document = doc;
    }

    @Override
    public Object getRoot()
    {
        if (document == null)
            return null;

        return new XMLAdapterNode(document.getDocumentElement());
    }

    @Override
    public Object getChild(Object parent, int index)
    {
        return ((XMLAdapterNode) parent).child(index);
    }

    @Override
    public int getIndexOfChild(Object parent, Object child)
    {
        return ((XMLAdapterNode) parent).index((XMLAdapterNode) child);
    }

    @Override
    public int getChildCount(Object parent)
    {
        return ((XMLAdapterNode) parent).childCount();
    }

    @Override
    public boolean isLeaf(Object node)
    {
        return ((XMLAdapterNode) node).childCount() == 0;
    }

    @Override
    public void valueForPathChanged(TreePath path, Object newValue)
    {
        // ignore here
    }

    /*
     * Use these methods to add and remove event listeners.
     * (Needed to satisfy TreeModel interface, but not used.)
     */

    /**
     * Adds a listener for the TreeModelEvent posted after the tree changes.
     * 
     * @see #removeTreeModelListener
     * @param l
     *        the listener to add
     */
    @Override
    public void addTreeModelListener(TreeModelListener l)
    {
        listeners.add(TreeModelListener.class, l);
    }

    /**
     * Removes a listener previously added with <B>addTreeModelListener()</B>.
     * 
     * @see #addTreeModelListener
     * @param l
     *        the listener to remove
     */
    @Override
    public void removeTreeModelListener(TreeModelListener l)
    {
        listeners.remove(TreeModelListener.class, l);
    }

    public void fireTreeNodesChanged(TreeModelEvent e)
    {
        for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesChanged(e);
    }

    public void fireTreeNodesInserted(TreeModelEvent e)
    {
        for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesInserted(e);
    }

    public void fireTreeNodesRemoved(TreeModelEvent e)
    {
        for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeNodesRemoved(e);
    }

    public void fireTreeStructureChanged(TreeModelEvent e)
    {
        for (TreeModelListener listener : listeners.getListeners(TreeModelListener.class))
            listener.treeStructureChanged(e);
    }
}

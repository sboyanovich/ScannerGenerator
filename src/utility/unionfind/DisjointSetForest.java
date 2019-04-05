package utility.unionfind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DisjointSetForest {

    private Map<Integer, Integer> indexMap;
    private List<SetTreeNode> elements;

    /*
     * QUICK RUNDOWN:
     *   All elements present are contained in list elements (as nodes).
     *   Map indexMap knows at which index in the list can an element be found (if at all present).
     * */

    public DisjointSetForest() {
        this.indexMap = new HashMap<>();
        this.elements = new ArrayList<>();
    }

    /**
     * It is recommended to call this, before attempting operations.
     */
    public boolean contains(int n) {
        return this.indexMap.containsKey(n);
    }

    /**
     * Adds new, one element set to the forest, containing n
     */
    public boolean makeSet(int n) {
        if (!this.contains(n)) {
            int index = this.elements.size();
            this.elements.add(new SetTreeNode(n, index, 0));
            this.indexMap.put(n, index);
            return true;
        }
        return false;
    }

    /**
     * Finds representative for equivalence class of n.
     *
     * @param n natural number
     */
    public int find(int n) {
        if (!contains(n)) {
            throw new IllegalArgumentException("Element " + n + " is not present in the forest!");
        }
        int index = this.indexMap.get(n);
        return this.elements.get(findByIndex(index)).getX();
    }

    /**
     * Merges equivalence classes of m and n (it is assumed both are contained)
     */
    public void union(int m, int n) {
        if (!contains(n)) {
            throw new IllegalArgumentException("Element " + n + " is not present in the forest!");
        }
        if (!contains(m)) {
            throw new IllegalArgumentException("Element " + m + " is not present in the forest!");
        }
        int rootm = findByIndex(m);
        int rootn = findByIndex(n);
        int mDepth = getNode(rootm).getDepth();
        int nDepth = getNode(rootn).getDepth();
        if (mDepth < nDepth) {
            setNodeParent(rootm, rootn);
        } else {
            setNodeParent(rootn, rootm);
            if ((mDepth == nDepth) && (rootm != rootn)) {
                setNodeDepth(rootm, mDepth + 1);
            }
        }
    }

    public boolean areEquivalent(int m, int n) {
        if (!contains(n)) {
            throw new IllegalArgumentException("Element " + n + " is not present in the forest!");
        }
        if (!contains(m)) {
            throw new IllegalArgumentException("Element " + m + " is not present in the forest!");
        }
        return findByIndex(m) == findByIndex(n);
    }

    public int numberOfClasses() {
        int n = 0;
        for (int i = 0; i < elements.size(); i++) {
            if (getNode(i).getParent() == i) {
                n++;
            }
        }
        return n;
    }

    public List<Integer> getRepresentatives() {
        List<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < elements.size(); i++) {
            if (getNode(i).getParent() == i) {
                result.add(getNode(i).getX());
            }
        }
        return result;
    }

    private SetTreeNode getNode(int nodeIndex) {
        return this.elements.get(nodeIndex);
    }

    private void setNode(int nodeIndex, int nodeValue, int nodeParent, int nodeDepth) {
        this.elements.set(nodeIndex, new SetTreeNode(nodeValue, nodeParent, nodeDepth));
    }

    private void setNodeValue(int nodeIndex, int nodeValue) {
        SetTreeNode node = getNode(nodeIndex);
        setNode(nodeIndex, nodeValue, node.getParent(), node.getDepth());
    }

    private void setNodeParent(int nodeIndex, int nodeParent) {
        SetTreeNode node = getNode(nodeIndex);
        setNode(nodeIndex, node.getX(), nodeParent, node.getDepth());
    }

    private void setNodeDepth(int nodeIndex, int nodeDepth) {
        SetTreeNode node = getNode(nodeIndex);
        setNode(nodeIndex, node.getX(), node.getParent(), nodeDepth);
    }

    // returns index of represent
    private int findByIndex(int n) {
        int root;
        SetTreeNode currNode = this.elements.get(n);
        if (currNode.getParent() == n) {
            root = n;
        } else {
            int newParent = findByIndex(currNode.getParent());
            setNodeParent(n, newParent);
            root = newParent;
        }
        return root;
    }

    static class SetTreeNode {
        private int x;
        private int parent;
        private int depth;

        SetTreeNode(int x, int parent, int depth) {
            this.x = x;
            this.parent = parent;
            this.depth = depth;
        }

        int getX() {
            return x;
        }

        int getParent() {
            return parent;
        }

        int getDepth() {
            return depth;
        }
    }
}

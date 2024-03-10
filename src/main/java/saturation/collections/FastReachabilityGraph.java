package saturation.collections;

import java.util.*;

public class FastReachabilityGraph<T> {

    private final Map<T, InternalNode> internalNodeByNode = new HashMap<>();
    private final Map<Integer, Set<InternalNode>> internalNodesBySccId = new HashMap<>();
    private int totScc = 0;

    private class InternalNode {
        T data;
        int sccId;

        InternalNode(T data, int sccId) {
            this.data = data;
            this.sccId = sccId;
        }

        @Override
        public boolean equals(Object other) {
            if (!(other instanceof FastReachabilityGraph<?>.InternalNode otherInternalNode)) return false;
            return data.equals(otherInternalNode.data);
        }

        @Override
        public int hashCode() {
            return data.hashCode();
        }
    }

    public boolean connectStronglyConnectedComponents(T node1, T node2) {
        if (!containsNode(node1) || !containsNode(node2)) return false;
        InternalNode internalNode1 = internalNodeByNode.get(node1);
        InternalNode internalNode2 = internalNodeByNode.get(node2);

        int internalNode1SccId = internalNode1.sccId;
        int internalNode2SccId = internalNode2.sccId;
        if (internalNode1SccId == internalNode2SccId) return false;

        Set<InternalNode> scc1Nodes = internalNodesBySccId.get(internalNode1SccId);
        Set<InternalNode> scc2Nodes = internalNodesBySccId.get(internalNode2SccId);

        boolean isScc1SmallerThanScc2 = scc1Nodes.size() < scc2Nodes.size();
        Set<InternalNode> sccMinorCardinalitySet = (isScc1SmallerThanScc2) ? scc1Nodes : scc2Nodes;

        int boostedSccId = (isScc1SmallerThanScc2) ? internalNode2SccId : internalNode1SccId;
        Set<InternalNode> boostedScc = internalNodesBySccId.get(boostedSccId);
        for (InternalNode internalNode: sccMinorCardinalitySet) {
            internalNode.sccId = boostedSccId;
            boostedScc.add(internalNode);
        }

        int smallerSccId = (isScc1SmallerThanScc2) ? internalNode1SccId : internalNode2SccId;
        internalNodesBySccId.remove(smallerSccId);

        return true;
    }

    public boolean addNode(T node) {
        if (containsNode(node)) return false;
        InternalNode internalNode = new InternalNode(node, totScc++);
        internalNodeByNode.put(node, internalNode);
        internalNodesBySccId
            .computeIfAbsent(internalNode.sccId, __ -> new HashSet<>())
            .add(internalNode);
        return true;
    }

    public boolean reachability(T origin, T destination) {
        if (!containsNode(origin) || !containsNode(destination)) return false;
        InternalNode internalNodeOrigin = internalNodeByNode.get(origin);
        InternalNode internalNodeDestination = internalNodeByNode.get(destination);
        return internalNodeOrigin.sccId == internalNodeDestination.sccId;
    }

    public boolean containsNode(T node) {
        return internalNodeByNode.containsKey(node);
    }
}

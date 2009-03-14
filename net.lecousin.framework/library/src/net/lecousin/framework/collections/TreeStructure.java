package net.lecousin.framework.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreeStructure<T> {

	private Set<TreeNode<T>> startingNodes = new HashSet<TreeNode<T>>();
	
	public Collection<TreeNode<T>> getStartingNodes() { return startingNodes; }
	public void addNode(TreeNode<T> node) { startingNodes.add(node); }
	
	public TreeNode<T> getNode(T element) {
		Set<TreeNode<T>> visited = new HashSet<TreeNode<T>>();
		for (Iterator<TreeNode<T>> it = startingNodes.iterator(); it.hasNext(); ) {
			TreeNode<T> node = it.next().search(element, visited);
			if (node != null) return node;
		}
		return null;
	}
	
	public List<TreeNode<T>> getAllNodes() {
		LinkedList<TreeNode<T>> result = new LinkedList<TreeNode<T>>();
		Set<TreeNode<T>> visited = new HashSet<TreeNode<T>>();
		for (Iterator<TreeNode<T>> it = startingNodes.iterator(); it.hasNext(); ) {
			TreeNode<T> node = it.next();
			if (visited.contains(node)) continue;
			result.add(node);
			result.addAll(node.getAllDescendents(visited));
		}
		return result;
	}
}

package net.lecousin.framework.collections;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TreeNode<T> {

	public TreeNode(T element) { 
		this.element = element; 
		this.children = new HashSet<TreeNode<T>>(); 
	}
	
	private T element;
	private Set<TreeNode<T>> children;
	
	public T getElement() { return element; }
	public Set<TreeNode<T>> getChildren() { return children; }
	
	public void addChild(TreeNode<T> child) { children.add(child); }
	public void addChildren(Collection<TreeNode<T>> children) { this.children.addAll(children); }
	
	public int hashCode() { return element.hashCode(); }
	
	TreeNode<T> search(T element, Set<TreeNode<T>> visited) {
		if (this.element.equals(element)) return this;
		visited.add(this);
		for (Iterator<TreeNode<T>> it = children.iterator(); it.hasNext(); ) {
			TreeNode<T> child = it.next();
			if (visited.contains(child)) continue;
			TreeNode<T> result = child.search(element, visited);
			if (result != null) return result;
		}
		return null;
	}
	
	public boolean hasDescendent(T element) {
		Set<TreeNode<T>> visited = new HashSet<TreeNode<T>>();
		visited.add(this);
		for (Iterator<TreeNode<T>> it = children.iterator(); it.hasNext(); ) {
			TreeNode<T> child = it.next();
			if (visited.contains(child)) continue;
			TreeNode<T> found = child.search(element, visited);
			if (found != null) return true;
		}
		return false;
	}
	
	public List<TreeNode<T>> getAllDescendents() {
		return getAllDescendents(new HashSet<TreeNode<T>>());
	}
	
	List<TreeNode<T>> getAllDescendents(Set<TreeNode<T>> visited) {
		LinkedList<TreeNode<T>> result = new LinkedList<TreeNode<T>>();
		visited.add(this);
		for (Iterator<TreeNode<T>> it = children.iterator(); it.hasNext(); ) {
			TreeNode<T> child = it.next();
			if (visited.contains(child)) continue;
			result.add(child);
			result.addAll(child.getAllDescendents(visited));
		}
		return result;
	}
}

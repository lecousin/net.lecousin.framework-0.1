package net.lecousin.framework.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedList;

public class SortedListTree<T> implements SortedList<T> {

	public SortedListTree(Comparator<T> comparator) {
		this.comparator = comparator;
	}
	
	private Comparator<T> comparator;
	private Node<T> head = null;
	@SuppressWarnings("unchecked")
	private Node<T>[] nodeBank = new Node[20];
	private int indexBank = -1;
	
	private Node<T> createNode(T e, Node<T> l, Node<T> r) {
		if (indexBank == -1) return new Node<T>(e,l,r);
		Node<T> n = nodeBank[indexBank--];
		n.element = e;
		n.left = l;
		n.right = r;
		return n;
	}
	private void free(Node<T> n) {
		if (indexBank == 19) return;
		nodeBank[++indexBank] = n;
	}
	
	private static class Node<T> {
		Node(T element, Node<T> left, Node<T> right)
		{ this.element = element; this.left = left; this.right = right; }
		T element;
		Node<T> left;
		Node<T> right;
	}
	
	public boolean isEmpty() { return head == null; }
	public int size() { return size(head); }
	private int size(Node<T> node) {
		if (node == null) return 0;
		return 1 + size(node.left) + size(node.right);
	}
	
	public void add(T element) {
		head = add(element, head);
	}
	private Node<T> add(T element, Node<T> node) {
		if (node == null)
			return createNode(element, null, null);
		int cmp = comparator.compare(element, node.element);
		if (cmp == 0) {
			Node<T> n = createNode(element, node, null);
			return n;
		}
		if (cmp < 0)
			node.left = add(element, node.left);
		else
			node.right = add(element, node.right);
		return node;
	}
	public void addAll(Iterable<T> col) {
		for (T e : col)
			add(e);
	}
	
	public T first() { return head != null ? head.element : null; }
	public T last() { return head != null ? right(head).element : null; }
	private Node<T> right(Node<T> node) {
		if (node.right == null)
			return node;
		return right(node.right);
	}
	private Node<T> left(Node<T> node) {
		if (node.left == null)
			return node;
		return left(node.left);
	}
	
	public T get(int index) {
		if (head == null) return null;
		int i = 0;
		Iterator it = new Iterator();
		while (i < index && it.hasNext()) {
			it.next();
			i++;
		}
		if (!it.hasNext()) return null;
		return it.next();
	}
	
	/** return true if the comparator return 0 for one of the present element */
	public boolean containsEquivalent(T element) {
		return containsEquivalent(head, element);
	}
	private boolean containsEquivalent(Node<T> node, T element) {
		if (node == null) return false;
		int cmp = comparator.compare(node.element, element);
		if (cmp == 0) return true;
		if (cmp < 0)
			return containsEquivalent(node.left, element);
		return containsEquivalent(node.right, element);
	}
	
	public Iterator iterator() { return new Iterator(); }
	
	private class Iterator implements java.util.Iterator<T> {
		public Iterator() {
			node = head;
			if (node != null)
				while (node.left != null) {
					parent.add(node);
					node = node.left;
				}
		}
		private Node<T> node;
		private LinkedList<Node<T>> parent = new LinkedList<Node<T>>();
		private Node<T> lastNode = null;
		
		public boolean hasNext() {
			return node != null;
		}
		public T next() {
			if (node == null) return null;
			lastNode = node;
			T next = node.element;
			if (node.right != null) {
				node = node.right;
				while (node.left != null) {
					parent.add(node);
					node = node.left;
				}
			} else {
				if (parent.isEmpty())
					node = null;
				else
					node = parent.removeLast();
			}
			return next;
		}
		public void remove() {
			if (lastNode == null) return;
			SortedListTree.this.remove(lastNode.element);
		}
	}
	
	public void remove(T element) {
		searchRemove(element, null, head);
	}
	private void searchRemove(T element, Node<T> parent, Node<T> pos) {
		int cmp = comparator.compare(element, pos.element);
		if (cmp == 0) {
			remove(pos, parent);
			return;
		}
		if (cmp < 0)
			searchRemove(element, pos, pos.left);
		else
			searchRemove(element, pos, pos.right);
	}
	
	private void remove(Node<T> node, Node<T> parent) {
		free(node);
		if (parent == null) {
			if (node.left == null)
				head = node.right;
			else if (node.right == null)
				head = node.left;
			else {
				head = node.left;
				if (head.right == null)
					head.right = node.right;
				else {
					putRight(head.right, node.right);
				}
			}
			return;
		}
		if (parent.left == node) {
			if (node.right != null) {
				parent.left = node.right;
				if (node.left != null)
					putLeft(node.right, node.left);
			} else {
				parent.left = node.left;
			}
		} else {
			if (node.left != null) {
				parent.right = node.left;
				if (node.right != null)
					putRight(node.left, node.right);
			} else {
				parent.right = node.right;
			}
		}
	}
	
	private void putRight(Node<T> target, Node<T> source) {
		Node<T> n = target;
		while (n.right != null) n = n.right;
		n.right = source;
	}
	private void putLeft(Node<T> target, Node<T> source) {
		Node<T> n = target;
		while (n.left != null) n = n.left;
		n.left = source;
	}
	
	public <TCursor> Cursor<TCursor> getCursor(TCursor value) {
		return new Cursor<TCursor>(value);
	}
	
	public class Cursor<TC> {
		@SuppressWarnings("unchecked")
		private Cursor(TC value) {
			node = head;
			if (node != null) {
				int cmp = comparator.compare((T)value, node.element);
				if (cmp < 0) {
					do {
						T prev = previous();
						if (prev == null) break;
						if (comparator.compare((T)value, prev) > 0)
							break;
						goPrevious();
					} while (true);
				} else if (cmp > 0) {
					do {
						T next = next();
						if (next == null) break;
						if (comparator.compare((T)value, next) < 0)
							break;
						goNext();
					} while (true);
				}
			}
		}
		private Node<T> node;
		private ArrayList<Node<T>> parents = new ArrayList<Node<T>>();
		
		public boolean hasPrevious() {
			if (node == null) return false;
			if (node.left != null) return true;
			if (parents.isEmpty()) return false;
			return hasPrevious(node, parents.size()-1);
		}
		private boolean hasPrevious(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.left != null && p.left != child) return true;
			if (pi == 0) return false;
			return hasPrevious(p, pi-1);
		}
		public boolean goPrevious() {
			if (node.left != null) {
				while (node.left != null) {
					parents.add(node);
					node = node.left;
				}
				return true;
			}
			return goPrevious(node, parents.size()-1);
		}
		private boolean goPrevious(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.left != null && p.left != child) {
				while (parents.size() > pi+2)
					parents.remove(pi+1);
				node = p.left;
				while (node.left != null) {
					parents.add(node);
					node = node.left;
				}
				return true;
			}
			if (pi == 0) return false;
			return goPrevious(p, pi-1);
		}
		public T previous() {
			if (node == null) return null;
			if (node.left != null) {
				Node<T> n = node.left;
				while (n.left != null) n = n.left;
				return n.element;
			}
			if (parents.isEmpty()) return null;
			return previous(node, parents.size()-1);
		}
		private T previous(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.left != null && p.left != child) {
				Node<T> n = p.left;
				while (n.left != null) n = n.left;
				return n.element;
			}
			if (pi == 0) return null;
			return previous(p, pi-1);
		}


		public boolean hasNext() {
			if (node == null) return false;
			if (node.right != null) return true;
			if (parents.isEmpty()) return false;
			return hasNext(node, parents.size()-1);
		}
		private boolean hasNext(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.right != null && p.right != child) return true;
			if (pi == 0) return false;
			return hasNext(p, pi-1);
		}
		public boolean goNext() {
			if (node.right != null) {
				while (node.right != null) {
					parents.add(node);
					node = node.right;
				}
				return true;
			}
			return goNext(node, parents.size()-1);
		}
		private boolean goNext(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.right != null && p.right != child) {
				while (parents.size() > pi+2)
					parents.remove(pi+1);
				node = p.right;
				while (node.right != null) {
					parents.add(node);
					node = node.right;
				}
				return true;
			}
			if (pi == 0) return false;
			return goNext(p, pi-1);
		}
		public T next() {
			if (node == null) return null;
			if (node.right != null) {
				Node<T> n = node.right;
				while (n.right != null) n = n.right;
				return n.element;
			}
			if (parents.isEmpty()) return null;
			return next(node, parents.size()-1);
		}
		private T next(Node<T> child, int pi) {
			Node<T> p = parents.get(pi);
			if (p.right != null && p.right != child) {
				Node<T> n = p.right;
				while (n.right != null) n = n.right;
				return n.element;
			}
			if (pi == 0) return null;
			return next(p, pi-1);
		}
		
		public T get() { return node != null ? node.element : null; }
	}
}

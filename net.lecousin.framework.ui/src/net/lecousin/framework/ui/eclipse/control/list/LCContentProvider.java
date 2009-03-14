package net.lecousin.framework.ui.eclipse.control.list;

public interface LCContentProvider<T> {

	public Iterable<T> getElements();
	
	public static class StaticList<T> implements LCContentProvider<T> {
		public StaticList(Iterable<T> elements) { this.elements = elements; }
		private Iterable<T> elements;
		public Iterable<T> getElements() { return elements; }
	}
}

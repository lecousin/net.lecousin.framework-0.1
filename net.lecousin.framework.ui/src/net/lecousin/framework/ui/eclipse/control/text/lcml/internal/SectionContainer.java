package net.lecousin.framework.ui.eclipse.control.text.lcml.internal;

import java.util.LinkedList;
import java.util.List;

public abstract class SectionContainer extends Section {

	protected List<Section> sections = new LinkedList<Section>();
	
	public void add(Section section) {
		sections.add(section);
	}
	
	protected void freeSections() {
		for (Section s : sections)
			s.free();
		sections.clear();
		sections = null;
	}
	
	public Link findLink(String href) {
		return findSection(Link.class, new FinderData<Link,String>(href) {
			public boolean check(Link link) {
				return link.getHRef().equals(data());
			}
		});
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Section> T findSection(Class<T> clazz, Finder<T> finder) {
		for (Section s : sections) {
			if (clazz.isAssignableFrom(s.getClass()) && finder.check((T)s))
				return (T)s;
			if (s instanceof SectionContainer) {
				T result = ((SectionContainer)s).findSection(clazz, finder);
				if (result != null) return result;
			}
		}
		return null;
	}
	@SuppressWarnings("unchecked")
	public <T extends Section> List<T> findSections(Class<T> clazz, Finder<T> finder) {
		List<T> list = new LinkedList<T>();
		for (Section s : sections) {
			if (clazz.isAssignableFrom(s.getClass()) && finder.check((T)s))
				list.add((T)s);
			if (s instanceof SectionContainer)
				list.addAll(((SectionContainer)s).findSections(clazz, finder));
		}
		return list;
	}
	
	public static interface Finder<T extends Section> {
		boolean check(T section);
	}
	public static abstract class FinderData<T extends Section, TData> implements Finder<T> {
		public FinderData(TData data) { this.data = data; }
		private TData data;
		protected TData data() { return data; }
	}
	public static class GetAllFinder<T extends Section> implements Finder<T> {
		public boolean check(T section) { return true; }
	}
}

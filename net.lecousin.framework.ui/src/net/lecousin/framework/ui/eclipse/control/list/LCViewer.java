package net.lecousin.framework.ui.eclipse.control.list;

import java.util.List;

import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;

import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.widgets.Control;

public interface LCViewer<TData, TControl extends Control> {

	public TControl getControl();

	public void addContentChangedEvent(Event<?> event);
	public void addAddElementEvent(Event<TData> event);
	public void addRemoveElementEvent(Event<TData> event);
	public void addElementChangedEvent(Event<TData> event);
	
	public void addSelectionChangedListener(Listener<List<TData>> listener);
	public void addDoubleClickListener(Listener<TData> listener);
	public void addRightClickListener(Listener<TData> listener);
	public void addKeyListener(KeyListener listener);
	
	public List<TData> getSelection();

	public static interface DragListener<TData> {
		void dragFinished(DragSourceEvent event, List<TData> data); 
		void dragSetData(DragSourceEvent event, List<TData> data); 
		void dragStart(DragSourceEvent event, List<TData> data); 
	}
	public void addDragSupport(int style, Transfer[] transfers, DragListener<TData> listener);
}

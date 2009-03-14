package net.lecousin.framework.ui.eclipse;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Iterator;

import net.lecousin.framework.Pair;
import net.lecousin.framework.event.Event;
import net.lecousin.framework.event.Event.Listener;
import net.lecousin.framework.event.Event.ListenerData;
import net.lecousin.framework.thread.RunnableWithData;
import net.lecousin.framework.ui.eclipse.event.SelectionListenerWithData;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class UISync {

	public static interface Setter<T> {
		public void set(T value);
	}
	public static interface Getter<T> {
		public T get();
	}
	public static class SetterMethod<T> implements Setter<T> {
		public SetterMethod(Object instance, String methodName, Class<T> clazz) {
			this.instance = instance;
			try {
				method = instance.getClass().getMethod(methodName, new Class[]{ clazz });
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		private Object instance;
		private Method method;
		public void set(T value) {
			if (method == null) return;
			try { method.invoke(instance, new Object[] { value }); }
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	public static class SetterAttribute<T> implements Setter<T> {
		public SetterAttribute(Object instance, String attrName) {
			this.instance = instance;
			try {
				field = instance.getClass().getField(attrName);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		private Object instance;
		private Field field;
		public void set(T value) {
			if (field == null) return;
			try { field.set(instance, value); }
			catch (Throwable t) {
				t.printStackTrace();
			}
		}
	}
	public static class GetterMethod<T> implements Getter<T> {
		public GetterMethod(Object instance, String methodName) {
			this.instance = instance;
			try {
				method = instance.getClass().getMethod(methodName, new Class[]{});
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}
		private Object instance;
		private Method method;
		@SuppressWarnings("unchecked")
		public T get() {
			if (method == null) return null;
			try { return (T)method.invoke(instance, new Object[] {}); }
			catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}
	public static class GetterAttribute<T> implements Getter<T> {
		public GetterAttribute(Object instance, String attrName) {
			this.instance = instance;
			try {
				field = instance.getClass().getField(attrName);
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		protected Object instance;
		protected Field field;
		@SuppressWarnings("unchecked")
		public T get() {
			if (field == null) return null;
			try { return (T)field.get(instance); }
			catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}
	public static class GetterAttributeCondition<T> extends GetterAttribute<Boolean> {
		public GetterAttributeCondition(Object instance, String attrName, Condition<T> cd) { super(instance, attrName); this.cd = cd; }
		public static interface Condition<T> { public boolean check(T data); }
		private Condition<T> cd;
		@SuppressWarnings("unchecked")
		@Override
		public Boolean get() {
			if (field == null) return null;
			try { T data = (T)field.get(instance); return cd.check(data); }
			catch (Throwable t) {
				t.printStackTrace();
				return null;
			}
		}
	}
	
	public static void syncTextChanged(Text text, Setter<String> setter) {
		class SyncTextChangedListener implements ModifyListener {
			SyncTextChangedListener(Setter<String> setter) { this.setter = setter; }
			private Setter<String> setter;
			public void modifyText(ModifyEvent e) {
				setter.set(((Text)e.widget).getText());
			}
		}
		SyncTextChangedListener listener = new SyncTextChangedListener(setter);
		text.addModifyListener(listener);
	}
	
	private static class SyncEnabledListener implements Event.Listener<Boolean>, DisposeListener {
		SyncEnabledListener(Control control, Event<Boolean> event, boolean isEnable)
		{ this.control = control; this.event = event; this.isEnable = isEnable; }
		private Control control;
		private Event<Boolean> event;
		private boolean isEnable;
		public void fire(Boolean event) {
			control.getDisplay().syncExec(new RunnableWithData<Boolean>(event) {
				public void run() {
					control.setEnabled(isEnable ? data().booleanValue() : !data().booleanValue());
				}
			});
		}
		public void widgetDisposed(DisposeEvent e) {
			event.removeListener(this);
		}
	}
	public static void syncDisabled(Control control, Event<Boolean> event) {
		SyncEnabledListener listener = new SyncEnabledListener(control, event, false);
		event.addListener(listener);
		control.addDisposeListener(listener);
	}
	public static void syncEnabled(Control control, Event<Boolean> event) {
		SyncEnabledListener listener = new SyncEnabledListener(control, event, true);
		event.addListener(listener);
		control.addDisposeListener(listener);
	}
	public static void syncDisabled(Control control, Button button) {
		button.addSelectionListener(new SelectionListenerWithData<Control>(control) {
			public void widgetSelected(SelectionEvent e) {
				data().setEnabled(((Button)e.widget).getSelection());
			}
		});
	}
	
	public static void syncChecked(Button button, Setter<Boolean> setter) {
		class CheckedListener implements SelectionListener {
			CheckedListener(Setter<Boolean> setter) { this.setter = setter; }
			private Setter<Boolean> setter;
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setter.set(((Button)e.widget).getSelection());
			}
		}
		button.addSelectionListener(new CheckedListener(setter));
	}

	public static void syncSlider(Slider slider, Setter<Integer> setter) {
		class SliderListener implements SelectionListener {
			SliderListener(Setter<Integer> setter) { this.setter = setter; }
			private Setter<Integer> setter;
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setter.set(((Slider)e.widget).getSelection());
			}
		}
		slider.addSelectionListener(new SliderListener(setter));
	}

	public static void syncSpinner(Spinner spinner, Setter<Integer> setter) {
		class SliderListener implements SelectionListener {
			SliderListener(Setter<Integer> setter) { this.setter = setter; }
			private Setter<Integer> setter;
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				setter.set(((Spinner)e.widget).getSelection());
			}
		}
		spinner.addSelectionListener(new SliderListener(setter));
	}
	
	public static <EventType> void syncContent(Text text, Event<EventType> event, Getter<String> getter) {
		Listener<EventType> listener = new ListenerData<EventType,Pair<Text,Getter<String>>>(new Pair<Text,Getter<String>>(text, getter)){
			public void fire(EventType event) {
				String newContent = data().getValue2().get();
				if (data().getValue1().getText().equals(newContent)) return;
				data().getValue1().setText(newContent);
			}
		};
		event.addListener(listener);
		listener.fire(null);
	}
	public static <EventType> void syncContent(Button button, Event<EventType> event, Getter<Boolean> getter) {
		Listener<EventType> listener = new ListenerData<EventType,Pair<Button,Getter<Boolean>>>(new Pair<Button,Getter<Boolean>>(button, getter)){
			public void fire(EventType event) {
				boolean newContent = data().getValue2().get();
				if (data().getValue1().getSelection() == newContent) return;
				data().getValue1().setSelection(newContent);
			}
		};
		event.addListener(listener);
		listener.fire(null);
	}
	public static <EventType> void syncContent(Spinner spinner, Event<EventType> event, Getter<Integer> getter) {
		Listener<EventType> listener = new ListenerData<EventType,Pair<Spinner,Getter<Integer>>>(new Pair<Spinner,Getter<Integer>>(spinner, getter)){
			public void fire(EventType event) {
				int newContent = data().getValue2().get();
				if (data().getValue1().getSelection() == newContent) return;
				data().getValue1().setSelection(newContent);
			}
		};
		event.addListener(listener);
		listener.fire(null);
	}
	public static <EventType> void syncContent(List list, Event<EventType> event, Getter<Iterable<String>> getter) {
		Listener<EventType> listener = new ListenerData<EventType,Pair<List,Getter<Iterable<String>>>>(new Pair<List,Getter<Iterable<String>>>(list, getter)){
			public void fire(EventType event) {
				Iterable<String> newContent = data().getValue2().get();
				for (Iterator<String> it = newContent.iterator(); it.hasNext(); ) {
					String s = it.next();
					String[] strs = data().getValue1().getItems();
					boolean found = false;
					for (int i = 0; i < strs.length && !found; ++i)
						if (strs[i].equals(s))
							found = true;
					if (!found)
						data().getValue1().add(s);
				}
				String[] strs = data().getValue1().getItems();
				for (int i = 0; i < strs.length; ++i) {
					boolean found = false;
					for (Iterator<String> it = newContent.iterator(); it.hasNext() && !found; ) {
						String s = it.next();
						if (s.equals(strs[i]))
							found = true;
					}					
					if (!found)
						data().getValue1().remove(strs[i]);
				}
			}
		};
		event.addListener(listener);
		listener.fire(null);
	}
}

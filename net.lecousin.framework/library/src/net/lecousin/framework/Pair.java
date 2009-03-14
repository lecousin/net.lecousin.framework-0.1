package net.lecousin.framework;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

public class Pair<T1,T2> implements Externalizable {

	public static class PairSame<T> extends Pair<T,T> {
		public PairSame(T v1, T v2) { super(v1,v2); }
		/** @deprecated for serialization purpose only */
		public PairSame() { super(); }
	}
	public static class String extends PairSame<java.lang.String> {
		public String(java.lang.String v1, java.lang.String v2) { super(v1,v2); }
		/** @deprecated for serialization purpose only */
		public String() { super(); }
	}
	
    public Pair(T1 value1, T2 value2) {
        super();
        this.value1 = value1;
        this.value2 = value2;
    }
    /** @deprecated please initialize this object with values. This no-argument constructor is for serialization purpose only. */ 
    public Pair() { /* for serialization */ }
    
    private T1 value1;
    private T2 value2;

    public T1 getValue1() { return value1; }
    public T2 getValue2() { return value2; }
    
    public void setValue1(T1 value) { value1 = value; }
    public void setValue2(T2 value) { value2 = value; }
    
    @Override
    public boolean equals(Object obj) {
    	if (obj == null || !(obj instanceof Pair)) return false;
    	if (!CommonUtil.equalsOrNull(value1, ((Pair<?,?>)obj).value1))
    		return false;
    	if (!CommonUtil.equalsOrNull(value2, ((Pair<?,?>)obj).value2))
    		return false;
    	return true;
    }
    
    @Override
    public int hashCode() {
    	return (value1 == null ? 0 : value1.hashCode()) + (value2 == null ? 0 : value2.hashCode());
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
    	out.writeObject(value1);
    	out.writeObject(value2);
    }
    @SuppressWarnings("unchecked")
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    	value1 = (T1)in.readObject();
    	value2 = (T2)in.readObject();
    }
    
    @Override
    public java.lang.String toString() {
    	return new StringBuilder("{").append(value1 == null ? "null" : value1.toString()).append(",").append(value2 == null ? "null" : value2.toString()).append("}").toString();
    }
}

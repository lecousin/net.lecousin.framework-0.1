package net.lecousin.framework.io;

import java.io.ByteArrayOutputStream;

public class MyByteArrayOutputStream extends ByteArrayOutputStream {

	public MyByteArrayOutputStream() {
	}

	public MyByteArrayOutputStream(int size) {
		super(size);
	}

	public byte[] getBuffer() { return buf; }
	public int getBufferSize() { return count; }
}

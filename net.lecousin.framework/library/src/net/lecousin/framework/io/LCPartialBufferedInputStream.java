package net.lecousin.framework.io;

import java.io.IOException;
import java.io.InputStream;

public class LCPartialBufferedInputStream extends InputStream {

	public interface StreamProvider {
		public InputStream open();
	}
	
	public LCPartialBufferedInputStream(StreamProvider provider) {
		this.provider = provider;
	}
	
	private StreamProvider provider;
	private long pos = 0;
	private LCFullBufferedInputStream firstStream = null;
	private LCBufferedInputStream nextStream = null;
	private long nextPos = 0;
	
	private static final int FIRST_SIZE = 4096;
	private static final int NEXT_SIZE = 32768;
	private static final int FIRST_STREAM_SIZE = FIRST_SIZE + NEXT_SIZE*10;
	
	public void move(long position) {
		pos = position;
	}
	
	@Override
	public int read() throws IOException {
		if (pos < FIRST_STREAM_SIZE) {
			if (firstStream == null) {
				InputStream stream = provider.open();
				if (stream == null)
					return -1;
				firstStream = new LCFullBufferedInputStream(stream, FIRST_SIZE, NEXT_SIZE);
			}
			firstStream.move(pos);
			int result = firstStream.read();
			if (result != -1) {
				pos++;
				return result;
			}
			return -1;
		}
		if (nextPos > pos) {
			nextStream = null;
		}
		if (nextStream == null) {
			InputStream stream = provider.open();
			if (stream == null)
				return -1;
			stream.skip(FIRST_STREAM_SIZE);
			nextStream = new LCBufferedInputStream(stream);
			nextPos = FIRST_STREAM_SIZE;
		}
		if (nextPos < pos) {
			nextStream.skip(pos-nextPos);
			nextPos = pos;
		}
		int result = nextStream.read();
		if (result != -1) {
			nextPos++;
			pos++;
			return result;
		}
		return -1;
	}
	
	@Override
	public int read(byte[] buf) throws IOException {
		return read(buf, 0, buf.length);
	}
	
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int read = 0;
		if (pos < FIRST_STREAM_SIZE) {
			if (firstStream == null) {
				InputStream stream = provider.open();
				if (stream == null) return -1;
				firstStream = new LCFullBufferedInputStream(stream, FIRST_SIZE, NEXT_SIZE);
			}
			firstStream.move(pos);
			int l = (int)(len > FIRST_STREAM_SIZE - pos ? FIRST_STREAM_SIZE - pos : len);
			int result = firstStream.read(b, off, l);
			if (result > 0)
				pos += result;
			if (result != l)
				return result;
			read += l;
			len -= l;
		}
		if (len <= 0) return read;
		if (nextPos > pos) {
			nextStream = null;
		}
		if (nextStream == null) {
			InputStream stream = provider.open();
			if (stream == null)
				return read == 0 ? -1 : read; 
			stream.skip(FIRST_STREAM_SIZE);
			nextStream = new LCBufferedInputStream(stream);
			nextPos = FIRST_STREAM_SIZE;
		}
		if (nextPos < pos) {
			nextStream.skip(pos-nextPos);
			nextPos = pos;
		}
		int result = nextStream.read(b, off+read, len);
		if (result != -1) {
			nextPos += result;
			pos += result;
			return read+result;
		} else if (read == 0)
			return -1;
		return read;
	}
	
	@Override
	public void close() throws IOException {
		if (firstStream != null) {
			firstStream.close();
			firstStream = null;
		}
		if (nextStream != null) {
			nextStream.close();
			nextStream = null;
		}
		pos = 0;
	}
}

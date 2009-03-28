package net.lecousin.framework.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class LCFullBufferedInputStream extends LCMovableInputStream {

	public static final int DEFAULT_FIRST_BUFFER_SIZE = 2048;
	public static final int DEFAULT_NEXT_BUFFER_SIZE = 32768;
	
	public LCFullBufferedInputStream(InputStream stream, long size) { this(stream, DEFAULT_FIRST_BUFFER_SIZE, DEFAULT_NEXT_BUFFER_SIZE, size); }
	public LCFullBufferedInputStream(InputStream stream, int firstBufferSize, int nextBufferSize, long size) {
		super(size);
		this.stream = stream;
		this.firstSize = firstBufferSize;
		this.nextSize = nextBufferSize;
	}
	
	private InputStream stream;
	private int firstSize, nextSize;
	private ArrayList<byte[]> parts = new ArrayList<byte[]>();
	private long pos = 0;
	private long buffered = 0;
	private boolean endReached = false;
	private int lastSize = 0;
	
	@Override
	public int available() throws IOException {
		return (int)(buffered - pos + stream.available());
	}
	
	public void moveToStart() {
		pos = 0;
	}
	
	@Override
	public void move(long position) throws IOException {
		if (position <= buffered) {
			pos = position;
			return;
		}
		skip(position - pos);
	}
	
	@Override
	public long skip(long size) throws IOException {
		if (buffered - pos >= size) {
			pos += size;
			return size;
		}
		long prevPos = pos;
		size -= buffered-pos;
		pos = buffered;
		bufferize(size);
		if (buffered < prevPos+size)
			pos = buffered;
		else
			pos += size;
		return pos-prevPos;
	}
	
	@Override
	public long getPosition() { return pos; }
	
	private void bufferize(long size) throws IOException {
		if (endReached) return;
		if (buffered == 0) {
			byte[] buf = new byte[firstSize];
			int read = 0;
			do {
				int nb = stream.read(buf, read, firstSize-read);
				if (nb < 0) {
					if (read > 0)
						parts.add(buf);
					endReached = true;
					lastSize = read;
					buffered = read;
					return;
				}
				read += nb;
			} while (read < firstSize);
			lastSize = firstSize;
			buffered = firstSize;
			parts.add(buf);
			size -= firstSize;
		}
		if (size <= 0) return;
		do {
			byte[] buf = new byte[nextSize];
			int read = 0;
			do {
				int nb = stream.read(buf, read, nextSize-read);
				if (nb < 0) {
					if (read > 0)
						parts.add(buf);
					endReached = true;
					lastSize = read;
					buffered += read;
					return;
				}
				read += nb;
			} while (read < nextSize);
			lastSize = nextSize;
			buffered += nextSize;
			parts.add(buf);
			size -= nextSize;
		} while (size > 0);
	}
	
	public int read(byte[] buffer) throws IOException {
		return read(buffer, 0, buffer.length);
	}
	
	public int read(byte[] buffer, int offset, int length) throws IOException {
		int totalRead = 0;
		while (pos < buffered) {
			int nb = readCurrentPart(buffer, offset, length);
			totalRead += nb;
			if (nb == length)
				return totalRead;
			offset += nb;
			length -= nb;
		}
		if (endReached)
			return totalRead > 0 ? totalRead : -1;
		bufferize(length);
		int nb = read(buffer, offset, length);
		if (nb <= 0)
			return totalRead > 0 ? totalRead : -1;
		totalRead += nb;
		return totalRead;
	}
	
	private int readCurrentPart(byte[] buffer, int offset, int length) {
		if (pos < firstSize) {
			if (buffered < firstSize) {
				int l = length;
				if (l > buffered - pos)
					l = (int)(buffered-pos);
				System.arraycopy(parts.get(0), (int)pos, buffer, offset, l);
				pos += l;
				return l;
			}
			int l = length;
			if (l > firstSize - pos)
				l = (int)(firstSize-pos);
			System.arraycopy(parts.get(0), (int)pos, buffer, offset, l);
			pos += l;
			return l;
		}
		int partIndex = (int)(((pos-firstSize)/nextSize))+1;
		byte[] part = parts.get(partIndex);
		int partSize = partIndex == parts.size()-1 ? lastSize : nextSize;
		long partStart = firstSize+(partIndex-1)*nextSize;
		int startIndex = (int)(pos-partStart);
		int l = length;
		if (l > partSize-startIndex)
			l = partSize-startIndex;
		System.arraycopy(part, startIndex, buffer, offset, l);
		pos += l;
		return l;
	}
	
	private byte[] oneBuf = new byte[1];
	@Override
	public int read() throws IOException {
		int nb = read(oneBuf);
		if (nb <= 0) return -1;
		return oneBuf[0] & 0xFF;
	}
	
	@Override
	public void close() throws IOException {
		stream.close();
	}
}

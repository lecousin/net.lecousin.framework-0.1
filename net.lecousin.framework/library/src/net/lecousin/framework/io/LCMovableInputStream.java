package net.lecousin.framework.io;

import java.io.IOException;
import java.io.InputStream;

public abstract class LCMovableInputStream extends InputStream {

	public LCMovableInputStream(long size) {
		this.size = size;
	}
	
	private long size;
	private long lastMark = 0;
	
	public abstract void move(long pos) throws IOException;
	public abstract long getPosition();
	
	@Override
	public boolean markSupported() {
		return true;
	}
	
	@Override
	public synchronized void mark(int readLimit) {
		lastMark = getPosition();
	}

	@Override
	public synchronized void reset() throws IOException {
		move(lastMark);
	}
	
	public final long getSize() {
		if (size >= 0) return size;
		long pos = getPosition();
		long skipped;
		try { skipped = skip(Long.MAX_VALUE); }
		catch (IOException e) { skipped = 0; }
		try { move(pos); }
		catch (IOException e) { /* too bad */ }
		return pos + skipped;
	}
	
	public final int readLastBytes(byte[] buffer) throws IOException {
		return readLastBytes(buffer, 0, buffer.length);
	}
	public final int readLastBytes(byte[] buffer, int off, int len) throws IOException {
		if (size >= 0) {
			long p = size-len;
			if (p < 0) return 0;
			move(p);
			return IOUtil.readAllBuffer(this, buffer, off, len);
		}
		int blen = len*2 > 65536 ? len*2 : 65536;
		byte[] buf = new byte[blen];
		int i = 0;
		int nb = 0;
		while (true) {
			if (i > 0)
				System.arraycopy(buf, nb - i, buf, 0, i);
			nb = IOUtil.readAllBuffer(this, buf, i, blen - i);
			if (nb <= 0) { nb = i; break; }
			nb += i;
			i = len;
		};
		// TODO si deja en partie depasse...
		if (nb < len) len = nb;
		System.arraycopy(buf, nb-len, buffer, off, len);
		return len;
	}
}

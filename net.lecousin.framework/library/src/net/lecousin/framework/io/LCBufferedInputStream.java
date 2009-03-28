package net.lecousin.framework.io;

import java.io.IOException;
import java.io.InputStream;

public class LCBufferedInputStream extends InputStream {

	public LCBufferedInputStream(InputStream stream) {
		this(null, 0, 0, stream);
	}
	public LCBufferedInputStream(byte[] headingBuffer, int headingBufferPos, int headingBufferLen, InputStream stream) {
		this.headingBuffer = headingBuffer;
		this.headingBufferPos = headingBufferPos;
		this.headingBufferLen = headingBufferLen;
		this.in = stream;
	}
	
	private static final int BUFFER_SIZE = 16384;
	private static final int SKIP_BUFFERIZE_LIMIT = 1024;
	private byte[] headingBuffer = null;
	private int headingBufferPos = 0;
	private int headingBufferLen = 0;
	private byte[] buffer = new byte[BUFFER_SIZE];
	private int bufferPos = 0;
	private int bufferLen = 0;
	private InputStream in;
	
	private void bufferize() throws IOException {
		bufferLen = in.read(buffer);
		if (bufferLen == -1) bufferLen = 0;
		bufferPos = 0;
	}
	
	@Override
	public int read() throws IOException {
		if (headingBufferPos < headingBufferLen)
			return headingBuffer[headingBufferPos++];
		if (bufferPos >= bufferLen)
			bufferize();
		if (bufferPos >= bufferLen) return -1;
		return buffer[bufferPos++] & 0xFF;
	}
	@Override
	public int available() throws IOException {
		return in.available() + headingBufferLen - headingBufferPos + bufferLen - bufferPos;
	}
	
	@Override
	public int read(byte[] b) throws IOException {
		int nb = 0;
		if (headingBufferPos < headingBufferLen) {
			nb = headingBufferLen - headingBufferPos;
			if (nb > b.length)
				nb = b.length;
			System.arraycopy(headingBuffer, headingBufferPos, b, 0, nb);
			headingBufferPos += nb;
		}
		if (nb == b.length)
			return nb;
		if (bufferPos >= bufferLen && (b.length-nb) < SKIP_BUFFERIZE_LIMIT)
			bufferize();
		if (bufferPos < bufferLen) {
			int nb2 = bufferLen - bufferPos;
			if (nb2 > b.length - nb)
				nb2 = b.length - nb;
			System.arraycopy(buffer, bufferPos, b, nb, nb2);
			bufferPos += nb2;
			nb += nb2;
		}
		if (nb == b.length)
			return nb;
		int nb2 = in.read(b, nb, b.length-nb);
		if (nb2 == -1)
			return nb > 0 ? nb : -1;
		return nb + nb2;
	}
	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		int nb = 0;
		if (headingBufferPos < headingBufferLen) {
			nb = headingBufferLen - headingBufferPos;
			if (nb > len)
				nb = len;
			System.arraycopy(headingBuffer, headingBufferPos, b, off, nb);
			headingBufferPos += nb;
		}
		if (nb == len)
			return nb;
		if (bufferPos >= bufferLen && (len-nb) < SKIP_BUFFERIZE_LIMIT)
			bufferize();
		if (bufferPos < bufferLen) {
			int nb2 = bufferLen - bufferPos;
			if (nb2 > len - nb)
				nb2 = len - nb;
			System.arraycopy(buffer, bufferPos, b, off+nb, nb2);
			bufferPos += nb2;
			nb += nb2;
		}
		if (nb == len)
			return nb;
		int nb2 = super.read(b, off+nb, len-nb);
		if (nb2 == -1)
			return nb > 0 ? nb : -1;
		return nb + nb2;
	}
	@Override
	public long skip(long n) throws IOException {
		int nb = 0;
		if (headingBufferPos < headingBufferLen) {
			nb = headingBufferLen - headingBufferPos;
			if (nb > n)
				nb = (int)n;
			headingBufferPos += nb;
		}
		if (bufferPos < bufferLen) {
			int nb2 = bufferLen - bufferPos;
			if (nb + nb2 > n)
				nb2 = (int)(n - nb);
			bufferPos += nb2;
			nb += nb2;
		}
		
		if (nb == n)
			return n;
		return in.skip(n-nb)+nb;
	}
}

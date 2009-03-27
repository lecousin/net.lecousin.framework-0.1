package net.lecousin.framework.files;

import java.net.URI;

import net.lecousin.framework.io.LCFullBufferedInputStream;
import net.lecousin.framework.io.LCPartialBufferedInputStream;

public interface FileTypeDetector {

	/** may specify specific URI scheme, i.e. cdda or anything.. should not be so much used..<br>
	 * may return null, all scheme will be called with toLowerCase so no need to do it. */
	public String[] getSpecificURISchemeSupported();
	/** true if this detector only supports the URI schemes returned by {@link #getSpecificURISchemeSupported()}, meaning it doesn't really handle files as we cannot assume we know all the files protocol (file, http, ftp...) */
	public boolean isSupportingOnlyGivenURIScheme();
	
	/** returns supported extensions: used to sort detectors, so detectors with the good extension will be tested first.<br>
	 * may return null, all extensions will be called with toLowerCase so no need to do it. */
	public String[] getSupportedExtensions();
	/** returns true if the method {@link #detect(LCFullBufferedInputStream)} must not be called if the scheme is not supported nor the extension */
	public boolean relyOnlyOnExtension();
	
	/** returns all supported types. must not return null, and should not return an empty array... */
	public FileType[] getSupportedFileTypes();
	
	/** Try to detect the type of file and return a filled TypedFile.
	 * 
	 * @param scheme the lower case scheme, or null if not available
	 * @param extension the lower case extension, or null if not available
	 * @param uri the URI
	 * @param stream the input stream for detection and filling the TypedFile
	 * @return the detected type of file or null if not detected
	 */
	public TypedFile detect(String scheme, String extension, URI uri, LCPartialBufferedInputStream stream);
}

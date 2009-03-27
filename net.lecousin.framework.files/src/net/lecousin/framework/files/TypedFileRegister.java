package net.lecousin.framework.files;


public interface TypedFileRegister {

	public void registerTypes(FileTypeRegistry registry);
	public FileTypeDetector[] getDetectors();
	
}

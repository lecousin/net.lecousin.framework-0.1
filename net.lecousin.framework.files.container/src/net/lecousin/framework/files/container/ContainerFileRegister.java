package net.lecousin.framework.files.container;

import net.lecousin.framework.files.FileTypeDetector;
import net.lecousin.framework.files.FileTypeRegistry;
import net.lecousin.framework.files.TypedFileRegister;
import net.lecousin.framework.files.container.asf.ASFFile;

public class ContainerFileRegister implements TypedFileRegister {

	public ContainerFileRegister() {
	}

	public FileTypeDetector[] getDetectors() {
		return new FileTypeDetector[] {
			new ContainerFileDetector(),
		};
	}

	public void registerTypes(FileTypeRegistry registry) {
		ASFFile.registerTypes(registry);
	}

}

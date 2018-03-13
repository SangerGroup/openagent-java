package openagent.test;

import java.io.File;
import java.nio.file.Paths;

public class PathBuilder {
	
	private String path = "";
	
	public PathBuilder currentDir() {
		path += Paths.get(".").toAbsolutePath().normalize().toString();
		return this;
	}
	
	public PathBuilder sep() {
		path += File.separator;
		return this;
	}
	
	public PathBuilder folderOrFile(String folderName) {
		path += folderName;
		return this;
	}

	public String build() {
		return path;
	}
	
	public File buildFile() {
		return new File(path);
	}
	
}

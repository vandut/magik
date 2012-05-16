package net.vandut.magik;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class Utils {
	
	public static File getAppBaseDirectory() throws IOException {
		File userDir = FileUtils.getUserDirectory();
		File baseDir = new File(userDir, "Magik");
		createDirIfNotExists(baseDir);
		return baseDir;
	}

	public static void createDirIfNotExists(File dir) throws IOException {
		if (!dir.exists()) {
			if (!dir.mkdirs()) {
				throw new IOException("Failed to create directory: "
						+ dir.getAbsolutePath());
			}
		}
	}

}

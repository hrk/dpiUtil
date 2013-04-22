package ldpiUtil;

import java.io.File;

public class FileInfo {

	private File file;
	private int width;
	private int height;

	public FileInfo(File file, int width, int height) {
		setFile(file);
		setWidth(width);
		setHeight(height);
	}

	/* get/set */
	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}

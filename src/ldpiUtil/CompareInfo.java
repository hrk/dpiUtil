package ldpiUtil;

public class CompareInfo {

	private FileInfo ldpi;
	private FileInfo hdpi;
	private FileInfo override;

	public CompareInfo(FileInfo ldpiFileInfo, FileInfo hdpiFileInfo, FileInfo override) {
		setLdpi(ldpiFileInfo);
		setHdpi(hdpiFileInfo);
		setOverride(override);
	}

	/* get/set */
	public FileInfo getLdpi() {
		return ldpi;
	}

	public void setLdpi(FileInfo ldpi) {
		this.ldpi = ldpi;
	}

	public FileInfo getHdpi() {
		return hdpi;
	}

	public void setHdpi(FileInfo hdpi) {
		this.hdpi = hdpi;
	}

	public FileInfo getOverride() {
		return override;
	}

	public void setOverride(FileInfo override) {
		this.override = override;
	}
}

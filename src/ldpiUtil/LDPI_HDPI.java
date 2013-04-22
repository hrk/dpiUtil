package ldpiUtil;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;

import com.floreysoft.jmte.Engine;

public class LDPI_HDPI {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		ArgumentParser parser = ArgumentParsers.newArgumentParser("dpiUtil", false, "-", "@");
		parser
				.description("Compares a high-dpi folder with a low-dpi folder (and an optional override folder), returning a HTML representation of the differences.");

		parser.addArgument("-r", "--root").required(true)
				.help("base 'res' directory containing (at least) the high dpi drawable directory.");
		parser.addArgument("-o", "--overlay").setDefault("c:d:/this/must/not/exist")
				.help("complete path to an overlay directory, low-density folder.");
		parser.addArgument("-h", "--high").choices("h", "xh", "xxh").setDefault("h")
				.help("high density prefix, defaults to 'h' for hdpi.");
		parser.addArgument("-l", "--low").choices("l", "m", "h").setDefault("l")
				.help("low density prefix, defaults to 'l' for ldpi.");
		parser.addArgument("-v", "--variant").help("variant (ie: land, lang-code, ...)");
		parser.addArgument("--out").help(
				"file name (with path) where the result will be written. If omitted, stdout is used.");

		if (args.length == 0) {
			parser.printHelp();
			System.exit(-1);
		}
		Namespace ns = null;
		try {
			ns = parser.parseArgs(args);
		} catch (ArgumentParserException apex) {
			System.out.println(apex.getMessage());
			parser.printHelp();
			System.exit(-1);
		}

		String _root = ns.getString("root");
		String _overlay = ns.getString("overlay");
		String _h = ns.getString("high");
		String _l = ns.getString("low");
		String _variant = ns.getString("variant");
		String _out = ns.getString("out");

		File res = new File(_root);
		if (!res.getName().equals("res")) {
			res = new File(res, "res");
		}

		/* Build correct resource path and check its existance. */
		String ldpiPath = "drawable-" + _l + "dpi";
		String hdpiPath = "drawable-" + _h + "dpi";
		if (_variant != null) {
			ldpiPath += "-" + _variant;
			hdpiPath += "-" + _variant;
		}
		File ldpiDir = new File(res, ldpiPath);
		File hdpiDir = new File(res, hdpiPath);
		File overlayDir = new File(_overlay);

		if (!hdpiDir.exists()) {
			System.err.println("high density directory doesn't exist, check your --root, --high and --variant arguments");
			System.exit(-1);
		}

		/*
		 * Set headless=true so that AWT doesn't spawn useless threads and windows
		 * when we use ImageIO.
		 */
		System.setProperty("java.awt.headless", "true");

		FilenameFilter pngFilter = new FilenameFilter() {
			@Override
			public boolean accept(File path, String name) {
				return name.endsWith("png") && !name.endsWith(".9.png");
			}
		};

		final int hdpiFactor = 1;
		final int ldpiFactor = hdpiFactor * 2;

		File[] hdpiFiles = hdpiDir.listFiles(pngFilter);
		File[] ldpiFiles = ldpiDir.exists() ? ldpiDir.listFiles(pngFilter) : new File[0];
		File[] overrideFiles = overlayDir.exists() ? overlayDir.listFiles(pngFilter) : new File[0];

		Set<String> uniqueNames = new HashSet<String>();
		HashMap<String, FileInfo> ldpiMap = new HashMap<String, FileInfo>();
		HashMap<String, FileInfo> hdpiMap = new HashMap<String, FileInfo>();
		HashMap<String, FileInfo> overrideMap = new HashMap<String, FileInfo>();
		for (File hdpi : hdpiFiles) {
			uniqueNames.add(hdpi.getName());
			BufferedImage bi = ImageIO.read(hdpi);
			FileInfo fi = new FileInfo(hdpi, bi.getWidth() * hdpiFactor, bi.getHeight() * hdpiFactor);
			bi.flush();
			hdpiMap.put(hdpi.getName(), fi);
		}

		for (File ldpi : ldpiFiles) {
			uniqueNames.add(ldpi.getName());
			BufferedImage bi = ImageIO.read(ldpi);
			FileInfo fi = new FileInfo(ldpi, bi.getWidth() * ldpiFactor, bi.getHeight() * ldpiFactor);
			bi.flush();
			ldpiMap.put(ldpi.getName(), fi);
		}

		for (File ldpi : overrideFiles) {
			BufferedImage bi = ImageIO.read(ldpi);
			FileInfo fi = new FileInfo(ldpi, bi.getWidth() * ldpiFactor, bi.getHeight() * ldpiFactor);
			bi.flush();
			overrideMap.put(ldpi.getName(), fi);
		}

		List<String> s = new ArrayList<String>(uniqueNames);
		Collections.sort(s);

		/* Candidates for deletion */
		List<CompareInfo> onlyInLDPI = new ArrayList<CompareInfo>();
		/* Must be generated */
		List<CompareInfo> notInLDPI = new ArrayList<CompareInfo>();
		/* Comparable */
		List<CompareInfo> common = new ArrayList<CompareInfo>();

		for (String fileName : s) {
			FileInfo ldpiFile = ldpiMap.get(fileName);
			FileInfo hdpiFile = hdpiMap.get(fileName);
			FileInfo overrideFile = overrideMap.get(fileName);
			CompareInfo ci = new CompareInfo(ldpiFile, hdpiFile, overrideFile);
			if (hdpiFile == null) {
				onlyInLDPI.add(ci);
			} else if (ldpiFile == null) {
				notInLDPI.add(ci);
			} else {
				common.add(ci);
			}
		}

		/* Read template */
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(new File("ldpi.html"))));
		String line = null;
		String html = "";
		while ((line = br.readLine()) != null) {
			html += line + "\r\n";
		}
		br.close();

		/* Process template w/ JMTE */
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("onlyInLDPI", onlyInLDPI);
		model.put("common", common);
		model.put("notInLDPI", notInLDPI);
		Engine engine = new Engine();
		html = engine.transform(html, model);

		/* Output where requested */
		if (_out != null) {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(_out)));
			bw.write(html);
			bw.close();
		} else {
			System.out.println(html);
		}
	}

}

package tameD;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.servlet.http.HttpServlet;

public class Keijiban{
	public String mTextData;

	public boolean open(HttpServlet servlet, String fileName) {
		File file = null;
		StringBuilder sb = null;
		try {
			String path = servlet.getServletContext().getRealPath(
					"/WEB-INF/" + fileName);
			file = new File(path);
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);

			sb = new StringBuilder();
			String str;
			while ((str = br.readLine()) != null) {
				// System.out.println(str);
				sb.append(str);
			}
			mTextData = sb.toString();
		} catch (Exception e) {
			return false;
		} finally {
			if (file != null)
				file.delete();
		}

		return true;
	}

	void replace(String src, String dest) {
		if (mTextData != null) {
			String s = ToLiteral(src);
			mTextData = mTextData.replaceAll(s, dest);
		}
	}

	String getText() {
		return mTextData;
	}

	static public String ToLiteral(String src) {
		if (src.indexOf('\\') == -1 && src.indexOf('$') == -1) {
			return src;
		}
		final char[] cs = { '\\', '$', '(', ')' };
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < src.length(); i++) {
			char c = src.charAt(i);
			int j;
			for (j = 0; j < cs.length; j++) {
				if (c == cs[j]) {
					buffer.append('\\');
					buffer.append(c);
					break;
				}
			}
			if (j == cs.length)
				buffer.append(c);
		}
		return buffer.toString();
	}

}

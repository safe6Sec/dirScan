package cn.safe6.dirScan.utils;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HtmlUtil {
	
	public static String getTitle(String html) {
		
		Document doc = Jsoup.parse(html);
		// 鑾峰彇html鐨勬爣棰�
		String title = doc.select("title").text();
		
		if(!title.equals("")) {
			return title;
		}
		return "鏈幏鍙栧埌鏍囬";
	}

}

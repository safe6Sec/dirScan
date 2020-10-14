package cn.safe6.dirScan.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
 
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
 
/**
 * Jsoup瑙ｆ瀽html鏍囩鏃剁被浼间簬JQuery鐨勪竴浜涚鍙�
 * 
 * @author safe6
 *
 */
public class HtmlParser {
	protected List<List<String>> data = new LinkedList<List<String>>();
 
	/**
	 * 鑾峰彇value鍊�
	 * 
	 * @param e
	 * @return
	 */
	public static String getValue(Element e) {
		return e.attr("value");
	}
 
	/**
	 * 鑾峰彇
	 * <tr>
	 * 鍜�
	 * </tr>
	 * 涔嬮棿鐨勬枃鏈�
	 * 
	 * @param e
	 * @return
	 */
	public static String getText(Element e) {
		return e.text();
	}
 
	/**
	 * 璇嗗埆灞炴�d鐨勬爣绛�,涓�鑸竴涓猦tml椤甸潰id鍞竴
	 * 
	 * @param body
	 * @param id
	 * @return
	 */
	public static Element getID(String body, String id) {
		Document doc = Jsoup.parse(body);
		// 鎵�鏈�#id鐨勬爣绛�
		Elements elements = doc.select("#" + id);
		// 杩斿洖绗竴涓�
		return elements.first();
	}
 
	/**
	 * 璇嗗埆灞炴�lass鐨勬爣绛�
	 * 
	 * @param body
	 * @param class
	 * @return
	 */
	public static Elements getClassTag(String body, String classTag) {
		Document doc = Jsoup.parse(body);
		// 鎵�鏈�#id鐨勬爣绛�
		return doc.select("." + classTag);
	}
 
	/**
	 * 鑾峰彇tr鏍囩鍏冪礌缁�
	 * 
	 * @param e
	 * @return
	 */
	public static Elements getTR(Element e) {
		return e.getElementsByTag("tr");
	}
 
	/**
	 * 鑾峰彇td鏍囩鍏冪礌缁�
	 * 
	 * @param e
	 * @return
	 */
	public static Elements getTD(Element e) {
		return e.getElementsByTag("td");
	}
	/**
	 * 鑾峰彇琛ㄥ厓缁�
	 * @param table
	 * @return
	 */
	public static List<List<String>> getTables(Element table){
		List<List<String>> data = new ArrayList<>();
		
		for (Element etr : table.select("tr")) {
			List<String> list = new ArrayList<>();
			for (Element etd : etr.select("td")) {
				String temp = etd.text();
				//澧炲姞涓�琛屼腑鐨勪竴鍒�
				list.add(temp);
			}
			//澧炲姞涓�琛�
			data.add(list);
		}
		return data;
	}
	/**
	 * 璇籬tml鏂囦欢
	 * @param fileName
	 * @return
	 */
	public static String readHtml(String fileName){
		FileInputStream fis = null;
		StringBuffer sb = new StringBuffer();
		try {
			fis = new FileInputStream(fileName);
			byte[] bytes = new byte[1024];
			while (-1 != fis.read(bytes)) {
				sb.append(new String(bytes));
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fis.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		// String url = "http://www.baidu.com";
		// String body = HtmlBody.getBody(url);
		// System.out.println(body);
 
		Document doc = Jsoup.parse(readHtml("./index.html"));
		// 鑾峰彇html鐨勬爣棰�
		String title = doc.select("title").text();
		System.out.println(title);
		// 鑾峰彇鎸夐挳鐨勬枃鏈�
		String btnText = doc.select("div div div div div form").select("#su").attr("value");
		System.out.println(btnText);
		// 鑾峰彇瀵艰埅鏍忔枃鏈�
		Elements elements = doc.select(".head_wrapper").select("#u1").select("a");
		for (Element e : elements) {
			System.out.println(e.text());
		}
		Document doc2 = Jsoup.parse(readHtml("./table.html"));
		Element table = doc2.select("table").first();
		List<List<String>> list = getTables(table);
		for (List<String> list2 : list) {
			for (String string : list2) {
				System.out.print(string+",");
			}
			System.out.println();
		}
	}
 
}

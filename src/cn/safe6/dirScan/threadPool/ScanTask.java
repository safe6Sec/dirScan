package cn.safe6.dirScan.threadPool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import cn.safe6.dirScan.MainFrame;
import cn.safe6.dirScan.utils.HttpClientUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class ScanTask implements Runnable {

    private String url;
    private List<String> dictList;
    private int timeOut;
    private String method;
    private String stateCode;

    public ScanTask(String url, List<String> dictList, int timeOut, String method, String stateCode) {
        super();
        this.url = url;
        this.dictList = dictList;
        this.timeOut = timeOut;
        this.method = method;
        this.stateCode = stateCode;
    }


    @Override
    public void run() {

        HttpClientUtil.timeOut = timeOut;
        try {
            if ("head".equals(method)) {
                headUrl();
            } else {
                getUrl();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void getUrl() throws IOException {
        for (String dict : dictList) {
            String reqUrl = url.trim().concat(java.net.URLEncoder.encode(dict.trim(), "ISO-8859-1"));
            MainFrame.scanLog.setText(dict);
            try {
                this.get(reqUrl);
            } catch (IOException e) {
                e.printStackTrace();
                this.get(reqUrl);
            }
        }
        System.out.println(Thread.currentThread().getName() + "运行结束。。");
        MainFrame.threadFlag -= 1;

    }

    private void headUrl() throws IOException {
        for (String dict : dictList) {
            String reqUrl = url.trim().concat(java.net.URLEncoder.encode(dict.trim(), "ISO-8859-1"));
            MainFrame.scanLog.setText(dict);
            try {
                this.head(reqUrl);
            } catch (IOException e) {
                e.printStackTrace();
                this.head(reqUrl);
            }
        }
        System.out.println(Thread.currentThread().getName() + "运行结束。。");
        MainFrame.threadFlag -= 1;
    }


    /**
     * 执行head请求
     *
     * @param reqUrl
     */
    private void head(String reqUrl) throws IOException {
        CloseableHttpResponse response = HttpClientUtil.head(reqUrl);
        System.out.println(reqUrl);
        MainFrame.scanNumber += 1;
        int code = response.getStatusLine().getStatusCode();
        //head 拿不到title
        if (!stateCode.contains(String.valueOf(code))) {
            if (code == 200) {
                this.get(reqUrl);
            } else {
                MainFrame.defaultTableModel.addRow(new Object[]{"", reqUrl, "", code});
                MainFrame.table.updateUI();
            }
        }
        response.close();
        try {
            if (MainFrame.delayTime != 0) {
                Thread.sleep(MainFrame.delayTime * 1000);
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 执行get请求
     *
     * @param reqUrl
     */
    private void get(String reqUrl) throws IOException {
        System.out.println(reqUrl);
        //扫描数+1
        MainFrame.scanNumber += 1;
        CloseableHttpResponse response = HttpClientUtil.get(reqUrl);
        int code = response.getStatusLine().getStatusCode();
        if (!stateCode.contains(String.valueOf(code))) {
            HttpEntity entity = response.getEntity();
            String html = EntityUtils.toString(entity, "utf-8");
            Document doc = Jsoup.parse(html);
            //排除假性404
            if (code == 200) {
                if (doc.title().contains("404") || html.contains("404")) {
                    response.close();
                    return;
                }
            }
            MainFrame.defaultTableModel.addRow(new Object[]{doc.title(), reqUrl, html.length(), code});
            MainFrame.table.updateUI();
            response.close();
        }
        try {
            if (MainFrame.delayTime != 0) {
                Thread.sleep(MainFrame.delayTime * 1000);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}

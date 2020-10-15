package cn.safe6.dirScan.threadPool;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;

import cn.hutool.http.HttpUtil;
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
        //System.out.println(Thread.currentThread().getName() + "开始扫描。。");
        //System.out.println("字典数:"+dictList.size());
        HttpClientUtil.timeOut = timeOut;
        if ("head".equals(method)) {
            headUrl();
        } else {
            getUrl();
        }

    }

    private synchronized void getUrl()  {
        for (String dict : dictList) {
            String reqUrl="";
            //扫描数+1
            MainFrame.scanNumber += 1;
            try {
                reqUrl = url.trim().concat(java.net.URLEncoder.encode(dict.trim(), "ISO-8859-1"));
                System.out.println(reqUrl);
                MainFrame.scanLog.setText(dict);
                this.get(reqUrl);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    this.get(reqUrl);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        MainFrame.threadFlag -= 1;

    }

    private synchronized void headUrl() {
        for (String dict : dictList) {
            String reqUrl ="";
            MainFrame.scanNumber += 1;
            try {
                reqUrl = url.trim().concat(java.net.URLEncoder.encode(dict.trim(), "ISO-8859-1"));
                System.out.println(reqUrl);
                MainFrame.scanLog.setText(dict);
                this.head(reqUrl);
            } catch (IOException e) {
                e.printStackTrace();
                try {
                    this.get(reqUrl);
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
        MainFrame.threadFlag -= 1;
    }


    /**
     * 执行head请求
     *
     * @param reqUrl
     */
    private void head(String reqUrl) throws IOException {
        CloseableHttpResponse response = HttpClientUtil.head(reqUrl);
        if (response!=null){
            int code = response.getStatusLine().getStatusCode();
            //head 拿不到title
            if (!stateCode.contains(String.valueOf(code))) {
                if (code == 200) {
                    this.get(reqUrl);
                } else {
                    MainFrame.defaultTableModel.addRow(new Object[]{"", java.net.URLDecoder.decode(reqUrl,"ISO-8859-1"), "", code});
                    MainFrame.table.updateUI();
                }
            }
            response.close();
            try {
                if (MainFrame.delayTime != 0) {
                    Thread.sleep(MainFrame.delayTime * 1000);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行get请求
     *
     * @param reqUrl
     */
    private void get(String reqUrl) throws IOException {
        CloseableHttpResponse response = HttpClientUtil.get(reqUrl);
        if (response!=null){
            int code = response.getStatusLine().getStatusCode();
            if (!stateCode.contains(String.valueOf(code))) {
                HttpEntity entity = response.getEntity();
                String title ="";
                //排除假性404
                if (entity.getContentLength()<1800){
                    String html = EntityUtils.toString(entity, "utf-8");
                    Document doc = Jsoup.parse(html);
                    title = doc.title();
                }
                if (title.contains("404")) {
                    response.close();
                    return;
                }
                MainFrame.defaultTableModel.addRow(new Object[]{title, java.net.URLDecoder.decode(reqUrl,"ISO-8859-1"), entity.getContentLength(), code});
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

}

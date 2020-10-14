package cn.safe6.dirScan.threadPool;

import cn.safe6.dirScan.MainFrame;
import cn.safe6.dirScan.utils.HttpClientUtil;

public class StatueListening implements Runnable {



    @Override
    public void run() {
        while (true) {
            MainFrame.scanProgress.setText(MainFrame.scanNumber+"/"+MainFrame.dictSize);
            MainFrame.scanState.setText("当前线程数"+MainFrame.threadFlag);
            if (MainFrame.threadFlag==1) {
                MainFrame.scanState.setText("扫描结束"+"找到"+MainFrame.table.getRowCount()+"条");
                System.out.println("扫描结束！");
                MainFrame.startScan.setSelected(false);
                MainFrame.startScan.setText("开始");
                break;
            }
            //HttpClientUtil.getSize();
        }
    }
}

package cn.safe6.dirScan;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.system.SystemUtil;
import cn.safe6.dirScan.threadPool.ScanTask;
import cn.safe6.dirScan.threadPool.StatueListening;
import com.sun.jna.platform.FileUtils;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainFrame extends JFrame {

	private JPanel contentPane;
	public static JTable table;
	private JTextField url;
	private JTextField stateCode;
	private JTextField dictFile;
	public static JToggleButton startScan;
	private JComboBox threadNumber;
	private JComboBox method;
	private String[] suffix = {".7z",".tar",".zip",".rar",".txt",".doc",".docx",".jar",".war",".tmp",".html",".exe",".pdf",".db",".mdf",".xls",".xlsx",".gz",".tar.gz",".ini",".bak",".apk",".z",".bz2",".xz",".tar.bz2",".tar.xz"};
	private JComboBox localDict;
	private JComboBox timeOut;
	public static JLabel scanState;
	public static JLabel scanProgress;
	private String dictFilePath;
	public static JLabel scanLog;
	private List<String> dictList;
	public static ExecutorService pool;
	public static int threadFlag;
	public static int dictSize;
	public static int scanNumber;
	public static boolean isScan = false;
	public static int delayTime;
	public static JComboBox delay;
	public static DefaultTableModel defaultTableModel= new DefaultTableModel(new Object[][]{},new String[] { "title", "url", "length", "code" }) {
		@Override
		public boolean isCellEditable(int row, int column)
		{
			return false;
		}
	};

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setResizable(false);
		setTitle("safe6 目录扫描工具v1.2   www.safe6.cn 20201014");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 771) / 2, (screenSize.height - 547) / 2, 771, 547);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		JPanel panel = new JPanel();
		panel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panel.setBounds(10, 10, 745, 97);
		contentPane.add(panel);
		panel.setLayout(null);

		JLabel lblUrl = new JLabel("url：");
		lblUrl.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblUrl.setBounds(24, 26, 30, 15);
		panel.add(lblUrl);

		url = new JTextField();
		url.setBounds(80, 23, 145, 21);
		panel.add(url);
		url.setColumns(10);

		JLabel label = new JLabel("过滤：");
		label.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label.setBounds(24, 62, 46, 15);
		panel.add(label);

		stateCode = new JTextField();
		stateCode.setText("404,400");
		stateCode.setColumns(10);
		stateCode.setBounds(80, 59, 145, 21);
		panel.add(stateCode);

		JLabel label_1 = new JLabel("字典：");
		label_1.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_1.setBounds(250, 26, 40, 15);
		panel.add(label_1);

		dictFile = new JTextField();
		dictFile.setEditable(false);
		dictFile.setColumns(10);
		dictFile.setBounds(290, 23, 100, 21);
		panel.add(dictFile);

		JButton importDictFile = new JButton("导入");
		importDictFile.addActionListener(arg0 -> {
			FileDialog fileDialog = new FileDialog(new TextDialog(), "打开", FileDialog.LOAD);
			fileDialog.setVisible(true);
			String file = fileDialog.getFile();
			if (file != null) {
				dictFilePath = fileDialog.getDirectory().concat(file);
				//清空
				dictList = new ArrayList<>();
				try {
					BufferedReader fb = new BufferedReader(new FileReader(new File(dictFilePath)));
					while (fb.ready()) {
						String tmp =fb.readLine().trim();
						if (!tmp.startsWith("/")){
							dictList.add(tmp);
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				dictSize = dictList.size();
				dictFile.setText(" "+file + " [" + dictSize + "]");
				scanProgress.setText("0/"+dictSize);
			}

		});
		importDictFile.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		importDictFile.setBounds(590, 22, 70, 23);
		panel.add(importDictFile);

		//String[] s = new String[] { "10", "20", "30", "40", "50", "60", "100", "200", "500" };

		JLabel label_2 = new JLabel("线程：");
		label_2.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_2.setBounds(250, 62, 40, 15);
		panel.add(label_2);

		threadNumber = new JComboBox();
		threadNumber.setMaximumRowCount(10);
		threadNumber.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		threadNumber.setModel(new DefaultComboBoxModel(
				new String[] { "8", "16", "32", "40", "48", "56", "64", "72", "80", "100" }));
		threadNumber.setSelectedIndex(2);
		threadNumber.setBounds(290, 59, 56, 21);
		panel.add(threadNumber);

		JLabel label_3 = new JLabel("超时：");
		label_3.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_3.setBounds(360, 62, 40, 15);
		panel.add(label_3);

		timeOut = new JComboBox();
		timeOut.setModel(new DefaultComboBoxModel(new String[] { "3", "5", "10", "15", "30" }));
		timeOut.setSelectedIndex(2);
		timeOut.setMaximumRowCount(10);
		timeOut.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		timeOut.setBounds(406, 59, 46, 21);
		panel.add(timeOut);

		startScan = new JToggleButton("开始");

		// 点击开始扫描
		startScan.addActionListener(e -> {

				if ("".equals(url.getText())) {
					new TextDialog("url不能为空").setVisible(true);
					startScan.setSelected(false);
					return;
				}

				if (!url.getText().contains("http")) {
					new TextDialog("请输入正确的url").setVisible(true);
					startScan.setSelected(false);
					return;
				}

				if ("".equals(dictFile.getText())){
					if (dictList==null||dictList.size()==0){
						this.initDictList();
					}else if (localDict.getSelectedItem()==null){
						new TextDialog("请导入字典").setVisible(true);
						startScan.setSelected(false);
						return;
					}
				}

				if (!isScan){
					//生成当前域名备份字典
					Pattern p = Pattern.compile("(?<=//|)((\\w)+\\.)+\\w+");
					Matcher m = p.matcher(url.getText());
					if(m.find()){
						String group = m.group();
						String[] split = group.split("\\.");
						String bak1 = group.replace(".","_");
						String bak2 = "www";
						String bak3;
						String bak4;
						if (split.length>2){
							bak2 = bak2+"_"+split[1]+"_"+split[2];
						}
						if (split.length>2){
							bak3 = split[1];
							bak4 = split[1]+"_"+split[2];
						}else {
							bak3 = split[0];
							bak4 = split[0]+"_"+split[1];
						}

						for (String suff :suffix){
							if (bak1!=null){
								dictList.add(bak1+suff);
							}
							if (bak2!=null){
								dictList.add(bak2+suff);
							}
							if (bak3!=null){
								dictList.add(bak3+suff);
							}
							if (bak4!=null){
								dictList.add(bak4+suff);
							}
						}
					}
					dictSize = dictList.size();
					isScan = true;
				}

			if (startScan.isSelected()) {
				startScan.setText("停止");
				//重置已经扫描数
				scanNumber=0;
				delayTime= Integer.parseInt(delay.getSelectedItem().toString());

				int threadNum = Integer.parseInt(threadNumber.getSelectedItem().toString());
				threadFlag = threadNum;
				int timeOutv = Integer.parseInt(timeOut.getSelectedItem().toString());
				String methodv = method.getSelectedItem().toString();
				String stateCodev = stateCode.getText();
				// maximumPoolSize设置为2 ，拒绝策略为AbortPolic策略，直接抛出异常
				pool = new ThreadPoolExecutor(100, 600, timeOutv*1000, TimeUnit.MILLISECONDS,
						new LinkedBlockingQueue<Runnable>(), Executors.defaultThreadFactory(),
						new ThreadPoolExecutor.AbortPolicy());

				int block = (int) Math.ceil(dictSize / threadNum);
				int index = 0;
				for (int i = 0; i < threadNum; i++) {
					// 加入线程池
					int index1 = index + block;
					if(i==(threadNum-1)){
						//启用一个监听线程
						if(threadFlag>1){
							pool.execute(new StatueListening());
						}
						pool.execute(new ScanTask(url.getText().trim(), dictList.subList(index, dictSize), timeOutv,
								methodv, stateCodev));
						break;
					}
					pool.execute(new ScanTask(url.getText().trim(), dictList.subList(index, index1), timeOutv,
							methodv, stateCodev));
					index = index1;
				}
				scanState.setText("当前线程数"+threadFlag);
			} else {
				startScan.setText("开始");
				pool.shutdown();
				try {
					if (!pool.awaitTermination(10, TimeUnit.MILLISECONDS)) {
						pool.shutdownNow();
					}
				} catch (InterruptedException ex) {
					pool.shutdownNow();
				}
			}

		});

		startScan.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		startScan.setBounds(663, 22, 75, 23);
		panel.add(startScan);

		JLabel label_55 = new JLabel("内置字典：");
		label_55.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_55.setBounds(400, 26, 100, 15);
		panel.add(label_55);

		localDict = new JComboBox();


		File dir = new File("./dict");
		String[] files ={};
		if (FileUtil.isDirectory(dir)){
			List<String> list = FileUtil.listFileNames(dir.getAbsolutePath());
			files = list.toArray(files);
		}
		localDict.setModel(new DefaultComboBoxModel(files));
		localDict.setMaximumRowCount(10);
		localDict.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		localDict.setBounds(465, 22, 120, 22);
		localDict.addActionListener(e1 -> {
			this.initDictList();
		});
		panel.add(localDict);

		JLabel label_4 = new JLabel("扫描方式：");
		label_4.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_4.setBounds(465, 62, 67, 15);
		panel.add(label_4);

		method = new JComboBox();
		method.setModel(new DefaultComboBoxModel(new String[] {"head","get"}));
		method.setMaximumRowCount(10);
		method.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		method.setBounds(530, 59, 67, 21);
		panel.add(method);
		
		JLabel label_7 = new JLabel("延时：");
		label_7.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_7.setBounds(610, 62, 67, 15);
		panel.add(label_7);
		
		delay = new JComboBox();
		delay.setModel(new DefaultComboBoxModel(new String[] {"0", "1", "2", "3", "5", "10"}));
		delay.setMaximumRowCount(10);
		delay.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		delay.setBounds(650, 59, 46, 21);
		panel.add(delay);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(10, 142, 745, 366);
		contentPane.add(scrollPane);

		table = new JTable();
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int clicked = e.getClickCount();
				if (clicked != 2) {
					return;
				}else {
					String surl = table.getValueAt(table.getSelectedRow(), 1).toString();
					System.out.println(surl);
					try {
						browse2(surl);
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				}

			}
		});
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scrollPane.setViewportView(table);
		table.setModel(defaultTableModel);
		table.setRowHeight(22);
		table.getColumnModel().getColumn(0).setPreferredWidth(50);
		table.getColumnModel().getColumn(0).setMinWidth(6);
		table.getColumnModel().getColumn(1).setPreferredWidth(376);
		table.getColumnModel().getColumn(1).setMinWidth(26);
		table.getColumnModel().getColumn(3).setPreferredWidth(60);
		table.setFont(new Font("微软雅黑", Font.PLAIN, 12));

		JLabel lblNewLabel = new JLabel("扫描信息：");
		lblNewLabel.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		lblNewLabel.setBounds(20, 117, 100, 15);
		contentPane.add(lblNewLabel);

		scanLog = new JLabel("未运行");
		scanLog.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scanLog.setBounds(80, 117, 200, 15);
		contentPane.add(scanLog);
		
		JLabel label_5 = new JLabel("扫描状态：");
		label_5.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_5.setBounds(550, 117, 70, 15);
		contentPane.add(label_5);
		
		scanState = new JLabel("未运行");
		scanState.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scanState.setBounds(617, 117, 138, 15);
		contentPane.add(scanState);
		
		JLabel label_6 = new JLabel("扫描进度：");
		label_6.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		label_6.setBounds(306, 117, 70, 15);
		contentPane.add(label_6);
		
		scanProgress = new JLabel("0/0");
		scanProgress.setFont(new Font("微软雅黑", Font.PLAIN, 12));
		scanProgress.setBounds(373, 117, 124, 15);
		contentPane.add(scanProgress);
	}


	/**
	 * @title 使用默认浏览器打开
	 */
	private static void browse2(String url) throws Exception {
		Desktop desktop = Desktop.getDesktop();
		if (Desktop.isDesktopSupported() && desktop.isSupported(Desktop.Action.BROWSE)) {
			URI uri = new URI(url);
			desktop.browse(uri);
		}
	}


	/**
	 * 初始化内置字典
	 */
	private void initDictList(){
		if (localDict.getSelectedItem()!=null){
			//清空
			dictList = new ArrayList<>();
			BufferedReader fb = null;
			try {
				fb = new BufferedReader(new FileReader(new File("dict/"+localDict.getSelectedItem().toString())));
				while (fb.ready()) {
					String tmp =fb.readLine().trim();
					if (!tmp.startsWith("//")){
						dictList.add(tmp);
					}
				}
			} catch (FileNotFoundException fileNotFoundException) {
				fileNotFoundException.printStackTrace();
			} catch (IOException ioException) {
				ioException.printStackTrace();
			}
			dictSize = dictList.size();
			scanProgress.setText("0/"+dictSize);
		}
	}
}

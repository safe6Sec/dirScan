package cn.safe6.dirScan;

import java.awt.*;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.JLabel;

public class TextDialog extends JDialog {
	public TextDialog() {
	}

	/**
	 * Create the dialog.
	 */
	public TextDialog(String content) {
		setTitle("提示");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setBounds((screenSize.width - 230) / 2, (screenSize.height - 160) / 2, 230, 160);
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		
		JPanel panel = new JPanel();
		getContentPane().add(panel, BorderLayout.CENTER);
		panel.setLayout(null);
		
		JLabel text = new JLabel(content);
		text.setBounds(76, 49, 100, 15);
		panel.add(text);
	}
}

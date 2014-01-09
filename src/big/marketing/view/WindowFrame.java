package big.marketing.view;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import org.apache.log4j.Logger;

import big.marketing.Application;
import big.marketing.Settings;
import big.marketing.controller.DataController;
import big.marketing.controller.MongoController;
import big.marketing.data.DataType;
import big.marketing.test.DatabasePerformance;

public class WindowFrame extends JFrame {
	private static final long serialVersionUID = -8346810238547214403L;
	private final DataController controller;
	private static int FRAME_WIDTH = 1200;
	private static int FRAME_HEIGHT = 600;
	private static final String FRAME_TITLE = "eyeNet - Network Monitor";
	static Logger logger = Logger.getLogger(WindowFrame.class);

	public WindowFrame(DataController controller, JComponent graphPanel, JComponent pCoordinatesPanel, JComponent controlsPanel) {
		loadSettings();
		this.controller = controller;
		this.setTitle(FRAME_TITLE);
		this.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Application.quit();
				super.windowClosing(e);
			}
		});
		GridBagLayout gbl = new GridBagLayout();
		this.setLayout(gbl);
		JSplitPane splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT, graphPanel, pCoordinatesPanel);
		addComponent(getContentPane(), gbl, splitter, 0, 0, 1, 2, 1, 1);
		addComponent(getContentPane(), gbl, controlsPanel, 0, 2, 1, 1, 0, 0);

		setJMenuBar(createMenuBar());
		this.pack();
		splitter.setDividerLocation(0.5);
		this.pack();
		this.setVisible(true);
	}

	private void loadSettings() {
		FRAME_HEIGHT = Settings.getInt("view.frame.height");
		FRAME_WIDTH = Settings.getInt("view.frame.width");
	}

	private JMenuBar createMenuBar() {
		JMenuBar menubar = new JMenuBar();
		menubar.add(getFileMenu());
		menubar.add(getDBMenu());
		return menubar;

	}

	private JMenu getFileMenu() {
		JMenu fileMenu = new JMenu("File");
		JMenuItem quitItem = new JMenuItem("Quit");
		quitItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				Application.quit();
			}
		});
		fileMenu.add(quitItem);
		return fileMenu;
	}

	private JMenu getDBMenu() {
		JMenu dbMenu = new JMenu("Database");
		JMenuItem dropItem = new JMenuItem("Drop all data");
		dropItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				for (DataType t : DataType.values()) {
					MongoController.getInstance().clearCollection(t);
				}
			}
		});
		dbMenu.add(dropItem);

		JMenuItem readItem = new JMenuItem("Read data");
		readItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataController.getInstance().readData();
			}
		});
		dbMenu.add(readItem);

		JMenuItem processItem = new JMenuItem("Process data");
		processItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataController.getInstance().processData();
			}
		});
		dbMenu.add(processItem);

		JMenuItem perfItem = new JMenuItem("Performance test");
		perfItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(new Runnable() {

					@Override
					public void run() {
						DatabasePerformance.main(null);

					}
				}, "PerformanceTester").start();
			}
		});
		dbMenu.add(perfItem);

		JMenuItem qWindowItem = new JMenuItem("QueryWindow test");
		qWindowItem.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				System.out.println("TRst");
				controller.moveQueryWindow(1364830798);
			}
		});
		dbMenu.add(qWindowItem);

		return dbMenu;
	}

	static void addComponent(Container cont, GridBagLayout gbl, Component c, int x, int y, int width, int height, double weightx,
	      double weighty) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = width;
		gbc.gridheight = height;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		int border = 2;
		gbc.insets = new Insets(border / 2, border, border / 2, border);
		gbl.setConstraints(c, gbc);
		cont.add(c);
	}
}

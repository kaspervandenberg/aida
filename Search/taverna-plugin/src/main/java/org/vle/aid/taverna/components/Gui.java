package org.vle.aid.taverna.components;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

import org.apache.axis.wsdl.toJava.JavaGeneratorFactory;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXDialog;
import org.jdesktop.swingx.JXFrame;
import org.jdesktop.swingx.JXGlassBox;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.painter.BusyPainter;

public class Gui {
    public static JXBusyLabel getBusyLabel() {
	JXBusyLabel busyLabel = new JXBusyLabel(new Dimension(75, 75));
	BusyPainter painter = busyLabel.getBusyPainter();
	painter.setFrame(5);
	painter.setPoints(10);
	painter.setTrailLength(10);
	painter.setHighlightColor(new Color(44, 61, 146).darker());
	painter.setBaseColor(new Color(168, 204, 241).brighter());

	busyLabel.setBusyPainter(painter);
	return busyLabel;
    }

    /* Eye candy, some busy label from swinglab */
    public static JXPanel getBusyWheel() {

	JXBusyLabel busyLabel = getBusyLabel();
	busyLabel.setBusy(true);

	/* Put it on panel to make it centered */
	JXPanel busyPanel = new JXPanel();
	busyPanel.add(busyLabel);

	return busyPanel;
    }

    public static void showInFrame(Component comp) {
	final Component cmp = comp;
	SwingUtilities.invokeLater(new Runnable() {

	    public void run() {
		JXFrame frame = new JXFrame("Test Frame");

		JMenuBar mb = new JMenuBar();
		mb.add(Gui.createLookAndFeelMenu(frame));
		frame.setJMenuBar(mb);

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(cmp);
		frame.setSize(200,300);
		frame.setVisible(true);
		
		frame.setIconImage(getIcon("icons/vle.png").getImage());
	    }

	});

    }

    public static JMenu createLookAndFeelMenu(final Component toUpdate) {

	final JMenu lnf = new JMenu("L&F");
	for (final LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
	    final JMenuItem mi = new JMenuItem(info.getName());
	    lnf.add(mi);

	    mi.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    try {
			UIManager.setLookAndFeel(info.getClassName());
			SwingUtilities.updateComponentTreeUI(toUpdate);
		    } catch (Exception ex) {
			mi.setEnabled(false);
			ex.printStackTrace();
		    }
		}
	    });
	}
	return lnf;
    }

    public static ImageIcon getIcon(String iconName) {
	URL iconURL = Gui.class.getResource("/" + iconName);
	if (iconURL == null) {
	    return null;
	} else {
	    return new ImageIcon(iconURL);
	}
    }

    public static void showErrorWarnings(String message, Exception e) {
	System.err.println(" AIDA Error : "+message);
	e.printStackTrace();
    }
    public static void showErrorWarnings1(String message, Exception e) {
	StringBuffer details = new StringBuffer(e.getMessage());
	for (StackTraceElement st : e.getStackTrace())
	    details.append("\n\n" + st);

	String detail = details.toString();

	JXTaskPaneContainer container = new JXTaskPaneContainer();

	JXTaskPane errorTaskPane = new JXTaskPane();
	errorTaskPane.setTitle("Error Message");
	errorTaskPane.setIcon(getIcon("icons/error.png"));

	JTextArea msgArea = new JTextArea(message);
	errorTaskPane.add(msgArea);

	JXTaskPane detailTaskPane = new JXTaskPane();
	JTextArea detailArea = new JTextArea(detail);
	JScrollPane detailPane = new JScrollPane(detailArea);
	detailPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
	detailPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);

	detailTaskPane.add(detailPane);
	detailTaskPane.setExpanded(false);
	detailTaskPane.setTitle("Error Details");

	container.add(errorTaskPane);
	container.add(detailTaskPane);

	JXDialog dialog = new JXDialog(new JScrollPane(container));
	dialog.setSize(500, 250);
	dialog.setTitle("AIDA Taverna Plugin Error");
	//dialog.setIconImage(getIcon("icons/vle.png").getImage());
	//dialog.setModal(true);
	dialog.setVisible(true);

	detailTaskPane.setScrollOnExpand(true);

    }
}

package org.vle.aid.taverna.search;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicArrowButton;

import org.jdesktop.swingx.JXPanel;
import org.vle.aid.taverna.panel.AIDSearchPanel;

/**
 * Class that will display a collection of button to move around page
 * So vague description, lets start with basic display
 * Test
 * @author wibisono
 * @date Apr 29, 2009 8:28:37 AM
 */
@SuppressWarnings("serial")
public class AIDSearchResultNavigator extends JXPanel implements ActionListener, KeyListener {

	BasicArrowButton btnLeft  = new BasicArrowButton(BasicArrowButton.WEST);
	BasicArrowButton btnRight = new BasicArrowButton(BasicArrowButton.EAST);
	
	BasicArrowButton btnStart = new BasicArrowButton(BasicArrowButton.WEST);
	BasicArrowButton btnEnd	  = new BasicArrowButton(BasicArrowButton.EAST);
	
	BasicArrowButton btnRefresh = new BasicArrowButton(BasicArrowButton.NORTH);
	JTextField txtCurrent = new JTextField(3);
	JLabel txtMaxPage = new JLabel("1");
	
	JProgressBar progressBar = new JProgressBar();
	
	private final PropertyChangeSupport propertyChangeSupport = new PropertyChangeSupport(this);
	
	int increase, maxPage, currentPage, totalHits;
	
	JEditorPane queryString = new JEditorPane();
	
	
	public AIDSearchResultNavigator(int current, int increase, int maxPage) {
		this.increase = increase;
		this.currentPage  = current;
		this.maxPage  = maxPage;
		
		setupButtons();
		setupToolTips();
		setupListeners();
		
	}
	
	public void update(int lastStart, int hits, String qString) {
		currentPage = lastStart/10;
		if(currentPage == 0) currentPage = 1;
		
		maxPage = hits/10;
		if(maxPage == 0) maxPage = 1;
		
		increase = 10;

		queryString.setContentType("text/html");
		queryString.setText("<html> <center><b>Query string : <i>"+qString+"</i></b> </center></html>");
		txtMaxPage.setText(""+maxPage);
		txtCurrent.setText(""+currentPage);
		totalHits = hits;
		
		progressBar.setValue(lastStart+Math.min(10,hits));
		progressBar.setMaximum(totalHits);
	}
	public void addPropertyChangeListener(PropertyChangeListener l) {
			propertyChangeSupport.addPropertyChangeListener(l);
	}


	private void setupButtons() {
		txtCurrent.setText(""+currentPage);
		txtMaxPage.setText(""+maxPage);
		setLayout(new BorderLayout());
		JXPanel panel = new JXPanel();
		panel.setLayout(new FlowLayout());
		panel.add(btnStart); 
		panel.add(btnLeft);
		panel.add(new JLabel("Page "));
		panel.add(txtCurrent);
		panel.add(new JLabel(" of "));
		panel.add(txtMaxPage);
		panel.add(btnRight);
		panel.add(btnEnd);
		panel.add(btnRefresh);
		panel.add(progressBar);
		add(panel,BorderLayout.NORTH);
		add(queryString, BorderLayout.CENTER);
	}
	private void setupToolTips(){
		btnRefresh.setToolTipText("Refresh Query");
		btnLeft.setToolTipText("Previous Page");
		btnRight.setToolTipText("Next Page");
		btnStart.setToolTipText("First Page");
		btnEnd.setToolTipText("Last Page");
	}
	/**
	 * Setup action commands and listener for each buttons.
	 *
	 */
	private void setupListeners(){
		btnLeft.addActionListener(this);  btnLeft.setActionCommand("Left");
		btnRight.addActionListener(this); btnRight.setActionCommand("Right");
		btnStart.addActionListener(this); btnStart.setActionCommand("Start");
		btnEnd.addActionListener(this);	  btnEnd.setActionCommand("End");
		txtCurrent.addKeyListener(this);
		btnRefresh.addActionListener(this); btnRefresh.setActionCommand("Refresh");
	}
	
	public int getCurrent() {
		return currentPage;
	}
	
	/**
	 *  Setters which will trigger property change, currently {@link AIDSearchPanel} is listening to this changes
	 */
	public void setCurrent(int newCurrentPage) {
		propertyChangeSupport.firePropertyChange("NAVIGATE",this.currentPage, newCurrentPage);
		this.currentPage = newCurrentPage;
		txtCurrent.setText(""+newCurrentPage);
	}

	public int getIncrease() {
		return increase;
	}

	public void setIncrease(int increase) {
		propertyChangeSupport.firePropertyChange("INCREASE",this.increase, increase);
		this.increase = increase;
	}

	public int getTotal() {
		return maxPage;
	}

	public void setTotal(int total) {
		this.maxPage = total;
	}


	public void actionPerformed(ActionEvent evt) {
		if(evt.getActionCommand().equals("Left")){
				if(currentPage > 0)
						setCurrent(currentPage-1);
		}
		if(evt.getActionCommand().equals("Right")){
			if(currentPage < maxPage)
					setCurrent(currentPage+1);
		}
		if(evt.getActionCommand().equals("Start")){
			setCurrent(1);
		}
		if(evt.getActionCommand().equals("End")){
			setCurrent((maxPage/increase)*increase);
		}
		if(evt.getActionCommand().equals("Refresh")){
			propertyChangeSupport.firePropertyChange("REFRESH",currentPage-1, currentPage);
		}
	}


	public void keyPressed(KeyEvent evt) {
		
	}

	public void keyReleased(KeyEvent evt) {
		
	}
	
	public void keyTyped(KeyEvent evt) {
		// Trapping enter on navigator page text
		// Checking if its integer and allowable value otherwise keep previous value.
		if(evt.getKeyChar() == 10){
			try {
				Integer newPage = new Integer(txtCurrent.getText());
				if(newPage >= 1 && newPage < maxPage)
					setCurrent(newPage);
			}catch(Exception e){
				setCurrent(currentPage);
			}
		}
	}

	
}

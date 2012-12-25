package it.unipd.testbase.gui;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.SwingWorker;

/**
 *
 * @author Fabio De Gaspari
 */
public class CarWidget extends javax.swing.JPanel {
	public static final int STATE_FORWARDED = 1;
	public static final int STATE_DEAD = 2;
	public static final int STATE_ACTIVE = 3;
	public static final int STATE_WAITING = 4;
	//public static final int STATE_FORWARDED = 1;

	public static final int CW_UPDATE_DELTA = 1;


	private static final long serialVersionUID = 1L;
	
	private boolean keepUpdating = true;


	public CarWidget() {
		initComponents();
	}


	@SuppressWarnings("unchecked")
	private void initComponents() {

		backgroud = new javax.swing.JTextField();
		contWindow = new javax.swing.JProgressBar();
		state = new javax.swing.JLabel();

		backgroud.setEditable(false);

		contWindow.setFocusable(false);
		setLayout(new GridLayout(3 ,1));
		add(contWindow);
		add(backgroud);
		add(state);
	}

	private javax.swing.JLabel state;
	private javax.swing.JProgressBar contWindow;
	private javax.swing.JTextField backgroud;

	public void setState(int s) {
		switch (s) {
		case STATE_FORWARDED:
			backgroud.setBackground(Color.orange);
			state.setText("Has Forwarded");
			break;
		case STATE_ACTIVE:
			backgroud.setBackground(Color.green);
			state.setText("Active");
			break;
		case STATE_DEAD:
			backgroud.setBackground(Color.darkGray);
			keepUpdating = false;
			state.setText("Dead, another car forwarded");
			break;
		case STATE_WAITING:
			backgroud.setBackground(Color.blue);
			keepUpdating = false;
			state.setText("Waiting");
			break;
		default:
			break;
		}
	}

	public void setContentionWindow(final int start, int max) {
		contWindow.setMaximum(max);
		contWindow.setValue(start);
		new SwingWorker<Void, Void>() {

			@Override
			protected Void doInBackground() throws Exception {
				contWindow.setValue(start);
				int value = start;
				while(value > 0 && keepUpdating) {
					Thread.sleep(CW_UPDATE_DELTA);
					value -= CW_UPDATE_DELTA;
					contWindow.setValue(value);
				}
				return null;
			}
		}.execute();

	}
}

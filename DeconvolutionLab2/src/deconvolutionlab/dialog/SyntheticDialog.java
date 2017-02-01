/*
 * DeconvolutionLab2
 * 
 * Conditions of use: You are free to use this software for research or
 * educational purposes. In addition, we expect you to include adequate
 * citations and acknowledgments whenever you present or publish results that
 * are based on it.
 * 
 * Reference: DeconvolutionLab2: An Open-Source Software for Deconvolution
 * Microscopy D. Sage, L. Donati, F. Soulez, D. Fortun, G. Schmit, A. Seitz,
 * R. Guiet, C. Vonesch, M Unser, Methods of Elsevier, 2017.
 */

/*
 * Copyright 2010-2017 Biomedical Imaging Group at the EPFL.
 * 
 * This file is part of DeconvolutionLab2 (DL2).
 * 
 * DL2 is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * DL2 is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * DL2. If not, see <http://www.gnu.org/licenses/>.
 */

package deconvolutionlab.dialog;

import ij.gui.GUI;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;

import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import lab.component.GridPanel;
import lab.component.SpinnerRangeDouble;
import lab.component.SpinnerRangeInteger;
import lab.tools.NumFormat;
import signal.RealSignal;
import signal.factory.SignalFactory;

public class SyntheticDialog extends JDialog implements ActionListener, WindowListener {

	private SpinnerRangeDouble	spnSignal		= new SpinnerRangeDouble(100, -999999, 999999, 1);
	private SpinnerRangeDouble	spnBackground	= new SpinnerRangeDouble(0, -999999, 999999, 1);

	private SpinnerRangeInteger	spnWidth		= new SpinnerRangeInteger(128, 1, 9999, 1);
	private SpinnerRangeInteger	spnHeight		= new SpinnerRangeInteger(128, 1, 9999, 1);
	private SpinnerRangeInteger	spnSlices		= new SpinnerRangeInteger(32, 1, 9999, 1);

	private SpinnerRangeDouble	spnCenterX		= new SpinnerRangeDouble(0.5, -10, 10, 0.05);
	private SpinnerRangeDouble	spnCenterY		= new SpinnerRangeDouble(0.5, -10, 10, 0.05);
	private SpinnerRangeDouble	spnCenterZ		= new SpinnerRangeDouble(0.5, -10, 10, 0.05);

	private SpinnerRangeDouble	spnParameter1	= new SpinnerRangeDouble(10, -9999, 9999, 1);
	private SpinnerRangeDouble	spnParameter2	= new SpinnerRangeDouble(10, -9999, 9999, 1);
	private SpinnerRangeDouble	spnParameter3	= new SpinnerRangeDouble(10, -9999, 9999, 1);

	private JLabel				lbl1			= new JLabel("Parameters 1 of the shape");
	private JLabel				lbl2			= new JLabel("Parameters 2 of the shape");
	private JLabel				lbl3			= new JLabel("Parameters 3 of the shape");

	private JComboBox<String>	cmbShapes;
	private JButton				bnShow			= new JButton("Show");
	private JButton				bnOK			= new JButton("OK");
	private JButton				bnCancel		= new JButton("Cancel");
	private boolean				cancel			= false;

	private String				shape;
	private String				command;

	public SyntheticDialog(ArrayList<SignalFactory> list) {
		super(new JFrame(), "Synthetic");
		String[] cmb = new String[list.size()];
		for (int i = 0; i < list.size(); i++) {
			cmb[i] = list.get(i).getName();
		}
		cmbShapes = new JComboBox<String>(cmb);
	
		GridPanel pnIntensity = new GridPanel("Intensity", 3);
		pnIntensity.place(1, 0, "Signal Intensity");
		pnIntensity.place(1, 1, spnSignal);
		pnIntensity.place(2, 0, "Background Intensity");
		pnIntensity.place(2, 1, spnBackground);
		
		GridPanel pnSize = new GridPanel("Dimension", 3);
		pnSize.place(1, 0, "Width [pixels] (nx)");
		pnSize.place(1, 1, spnWidth);
		pnSize.place(2, 0, "Height [pixels] (ny)");
		pnSize.place(2, 1, spnHeight);
		pnSize.place(3, 0, "Number of Slices (nz)");
		pnSize.place(3, 1, spnSlices);

		GridPanel pnCenter = new GridPanel("Center", 3);
		pnCenter.place(4, 0, "Center [% of nx] (cx)");
		pnCenter.place(4, 1, spnCenterX);
		pnCenter.place(5, 0, "Center [% of ny] (cy)");
		pnCenter.place(5, 1, spnCenterY);
		pnCenter.place(7, 0, "Center [% of nz] (cz)");
		pnCenter.place(7, 1, spnCenterZ);

		GridPanel pnParams = new GridPanel("Specific Parameters", 3);
		pnParams.place(8, 0, lbl1);
		pnParams.place(8, 1, spnParameter1);
		pnParams.place(9, 0, lbl2);
		pnParams.place(9, 1, spnParameter2);
		pnParams.place(10, 0, lbl3);
		pnParams.place(10, 1, spnParameter3);

		GridPanel pn = new GridPanel(false);	
		pn.place(0, 0, 3, 1, cmbShapes);
		pn.place(1, 0, 3, 1, pnIntensity);
		pn.place(2, 0, 3, 1, pnParams);
		pn.place(3, 0, 3, 1, pnSize);
		pn.place(4, 0, 3, 1, pnCenter);
		pn.place(11, 0, bnCancel);
		pn.place(11, 1, bnShow);
		pn.place(11, 2, bnOK);
		
		pn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		bnShow.addActionListener(this);
		bnOK.addActionListener(this);
		bnCancel.addActionListener(this);
		cmbShapes.addActionListener(this);
		add(pn);
		pack();
		updateInterface();
		addWindowListener(this);
		GUI.center(this);
		setModal(true);
	}

	private void updateInterface() {
		SignalFactory factory = SignalFactory.get((String)cmbShapes.getSelectedItem());
		String labels[] = factory.getParametersName();
		lbl1.setVisible(false);
		lbl2.setVisible(false);
		lbl3.setVisible(false);
		if (labels.length >= 1) {
			lbl1.setVisible(true);
			lbl1.setText(labels[0]);
		}
		if (labels.length >= 2) {
			lbl2.setVisible(true);
			lbl2.setText(labels[1]);
		}
		if (labels.length >= 3) {
			lbl3.setVisible(true);
			lbl3.setText(labels[2]);
		}
		double params[] = factory.getParameters();
		spnParameter1.setVisible(false);
		spnParameter2.setVisible(false);
		spnParameter3.setVisible(false);
		if (params.length >= 1) {
			spnParameter1.setVisible(true);
			spnParameter1.set(params[0]);
		}
		if (params.length >= 2) {
			spnParameter2.setVisible(true);
			spnParameter2.set(params[1]);
		}
		if (params.length >= 3) {
			spnParameter3.setVisible(true);
			spnParameter3.set(params[2]);
		}
		pack();
		
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnShow) {
			SignalFactory factory = SignalFactory.get((String)cmbShapes.getSelectedItem());
			double params[] = factory.getParameters();
			factory.setParameters(params);
			factory.center(spnCenterX.get(), spnCenterX.get(), spnCenterX.get());
			RealSignal signal = factory.generate(spnWidth.get(), spnWidth.get(), spnWidth.get());
			Lab.show(Monitors.createDefaultMonitor(), signal, factory.getName());
		}
		if (e.getSource() == cmbShapes) {
			updateInterface();
		}
		if (e.getSource() == bnCancel) {
			dispose();
			cancel = true;
			shape = "";
			command = "";
			return;
		}
		if (e.getSource() == bnOK) {
			int nx = spnWidth.get();
			int ny = spnHeight.get();
			int nz = spnSlices.get();
			shape = (String)cmbShapes.getSelectedItem();
			command = shape + " " + spnSignal.get() + " " + spnBackground.get() + " ";
			SignalFactory factory = SignalFactory.get(shape);
			int n = factory.getParameters().length;
			if (n >= 1) {
				command += "" + spnParameter1.get();
				if (n >= 2)
					command += " " + spnParameter2.get();
				if (n >= 3)
					command += " " + spnParameter3.get();
				command += " ";
			}
			command += " size " + nx + " " + ny + " " + nz + " ";
			if (spnCenterX.get() != 0.5 || spnCenterY.get() != 0.5 || spnCenterZ.get() != 0.5) {
				double cx = Math.round(spnCenterX.get() * 1000000) / 1000000.0;
				double cy = Math.round(spnCenterY.get() * 1000000) / 1000000.0;
				double cz = Math.round(spnCenterZ.get() * 1000000) / 1000000.0;
				command += " center " + cx + " " + cy + " " + cz + " ";				
			}
			dispose();
			cancel = false;
		}
	}

	public void setParameters(String name, String parameters) {
		double params[] = NumFormat.parseNumbers(parameters);
		cmbShapes.setSelectedItem(name);
		SignalFactory factory = SignalFactory.getFactoryByName(name);
		if (factory == null)
			return;
		int np = factory.getParameters().length;
		if (params.length > 0)
			spnSignal.set(params[0]);
		if (params.length > 1)
			spnBackground.set(params[1]);
		if (params.length > 2+np)
			spnWidth.set((int)params[2+np]);
		if (params.length > 3+np)
			spnHeight.set((int)params[3+np]);
		if (params.length > 4+np)
			spnSlices.set((int)params[4+np]);
		if (params.length > 5+np)
			spnCenterX.set(params[5+np]);
		if (params.length > 6+np)
			spnCenterY.set(params[6+np]);
		if (params.length > 7+np)
			spnCenterZ.set(params[7+np]);	
		
		if (np >= 1 && params.length > 2+np)
			spnParameter1.set(params[2]);
		if (np >= 2 && params.length > 3+np)
			spnParameter2.set(params[3]);
		if (np >= 3 && params.length > 4+np)
			spnParameter3.set(params[4]);

	}
	
	public String getShapeName() {
		return shape;
	}

	public String getCommand() {
		return command;
	}

	public boolean wasCancel() {
		return cancel;
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}

	@Override
	public void windowClosing(WindowEvent e) {
		dispose();
		cancel = true;
		shape = "";
		command = "";
		return;
	}

	@Override
	public void windowClosed(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}
}

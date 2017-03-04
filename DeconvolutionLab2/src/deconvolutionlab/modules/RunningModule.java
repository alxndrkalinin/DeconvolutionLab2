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

package deconvolutionlab.modules;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.JPanel;
import javax.swing.JTextField;

import deconvolution.Command;
import deconvolutionlab.Config;
import deconvolutionlab.Constants;
import lab.component.CustomizedColumn;
import lab.component.CustomizedTable;
import lab.tools.Files;

public class RunningModule extends AbstractModule implements MouseListener {

	private CustomizedTable	table;

	private String[]		valuesMonitor;
	private String[]		valuesVerbose;
	private String[]		valuesSystem;
	private String[]		valuesMultithreading;
	private String[]		valuesDisplay;
	private String[]		valuesStats;
	private String[]		valuesPath;

	private JTextField		txtMonitor;
	private JTextField		txtVerbose;
	private JTextField		txtSystem;
	private JTextField		txtMultithreading;
	private JTextField		txtDisplay;
	private JTextField		txtStats;
	private JTextField		txtPath;
	private JTextField		txtDirectory;

	public RunningModule(boolean expanded) {
		super("Running", "", "Default", "Browse", expanded);
	}

	@Override
	public String getCommand() {
		String cmd = "";
		String p = txtPath.getText();
		if (!p.equalsIgnoreCase(valuesPath[0]))
			cmd += " -path " + txtDirectory.getText().toLowerCase();
		if (!txtMonitor.getText().equalsIgnoreCase(valuesMonitor[0]))
			cmd += " -monitor " + txtMonitor.getText().toLowerCase();
		if (!txtVerbose.getText().equalsIgnoreCase(valuesVerbose[0]))
			cmd += " -verbose " + txtVerbose.getText().toLowerCase();
		if (!txtSystem.getText().equalsIgnoreCase(valuesSystem[0]))
			cmd += " -system " + txtSystem.getText().toLowerCase();
		if (!txtDisplay.getText().equalsIgnoreCase(valuesDisplay[0]))
			cmd += " -display " + txtDisplay.getText().toLowerCase();
		if (!txtStats.getText().equalsIgnoreCase(valuesStats[0]))
			cmd += " -stats " + txtStats.getText().toLowerCase();
		if (!txtMultithreading.getText().equalsIgnoreCase(valuesMultithreading[0]))
			cmd += " -multithreading " + txtMultithreading.getText().toLowerCase();

		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		valuesPath = new String[] { "current", "specify" };
		valuesMonitor = new String[] { "console table", "console", "table", "no" };
		valuesStats = new String[] { "no", "show", "save", "show save" };
		valuesVerbose = new String[] { "log", "quiet", "mute", "prolix" };
		valuesSystem = new String[] { "yes", "no" };
		valuesMultithreading = new String[] { "yes", "no" };
		valuesDisplay = new String[] { "yes", "no" };

		txtDirectory = new JTextField(System.getProperty("user.dir"));
		txtPath = new JTextField(valuesPath[0]);
		txtMonitor = new JTextField(valuesMonitor[0]);
		txtStats = new JTextField(valuesStats[0]);
		txtVerbose = new JTextField(valuesVerbose[0]);
		txtSystem = new JTextField(valuesSystem[0]);
		txtMultithreading = new JTextField(valuesMultithreading[0]);
		txtDisplay = new JTextField(valuesDisplay[0]);

		ArrayList<CustomizedColumn> columns = new ArrayList<CustomizedColumn>();
		columns.add(new CustomizedColumn("Settings", String.class, 150, false));
		columns.add(new CustomizedColumn("State", String.class, 100, false));
		columns.add(new CustomizedColumn("Information", String.class, Constants.widthGUI - 250, true));
		columns.add(new CustomizedColumn("", String.class, 100, "Change", "Change this setting"));
		table = new CustomizedTable(columns, false);

		table.getColumnModel().getColumn(3).setMaxWidth(140);
		table.getColumnModel().getColumn(3).setMaxWidth(140);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(table.getPane(100, 100), BorderLayout.CENTER);

		Config.register(getName(), "Path", txtPath, valuesPath[0]);
		Config.register(getName(), "Monitor", txtMonitor, valuesMonitor[0]);
		Config.register(getName(), "Stats", txtStats, valuesStats[0]);
		Config.register(getName(), "Verbose", txtVerbose, valuesVerbose[0]);
		Config.register(getName(), "System", txtSystem, valuesSystem[0]);
		Config.register(getName(), "Multithreading", txtMultithreading, valuesMultithreading[0]);
		Config.register(getName(), "Display", txtDisplay, valuesDisplay[0]);
		Config.register(getName(), "Directory", txtDirectory, System.getProperty("user.dir"));

		getAction1Button().addActionListener(this);
		getAction2Button().addActionListener(this);

		table.addMouseListener(this);

		return panel;
	}

	public void init() {
		table.append(new String[] { "Path", txtPath.getText(), txtDirectory.getText(), "Change" });
		table.append(new String[] { "Monitor", txtMonitor.getText(), "Monitor in table and in console", "Change" });
		table.append(new String[] { "Stats", txtStats.getText(), "Compute stats (slow down)", "Change" });
		table.append(new String[] { "Verbose", txtVerbose.getText(), "Level of messages in monitor", "Change" });
		table.append(new String[] { "System", txtSystem.getText(), "Open the system window", "Change" });
		table.append(new String[] { "Multithreading", txtMultithreading.getText(), "Activation of the multithreading", "Change" });
		table.append(new String[] { "Display", txtDisplay.getText(), "Display result at the end", "Change" });
		update();
	}

	public void update() {
		setCommand(getCommand());
		setSynopsis(txtDirectory.getText());
		Command.command();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == getAction1Button()) {
			for (int row = 0; row < table.getRowCount(); row++) {
				if (table.getCell(row, 0).equalsIgnoreCase("path")) {
					setDefault(row, valuesPath, txtPath);
					txtDirectory.setText(System.getProperty("user.dir"));
					table.setCell(0, 2, System.getProperty("user.dir"));
				}
				if (table.getCell(row, 0).equalsIgnoreCase("monitor"))
					setDefault(row, valuesMonitor, txtMonitor);
				if (table.getCell(row, 0).equalsIgnoreCase("stats"))
					setDefault(row, valuesStats, txtStats);
				if (table.getCell(row, 0).equalsIgnoreCase("verbose"))
					setDefault(row, valuesVerbose, txtVerbose);
				if (table.getCell(row, 0).equalsIgnoreCase("system"))
					setDefault(row, valuesSystem, txtSystem);
				if (table.getCell(row, 0).equalsIgnoreCase("multithreading"))
					setDefault(row, valuesMultithreading, txtMultithreading);
				if (table.getCell(row, 0).equalsIgnoreCase("display"))
					setDefault(row, valuesDisplay, txtDisplay);
			}
		}
		if (e.getSource() == getAction2Button()) {
			File f = Files.browseDirectory(txtPath.getText());
			if (f != null) {
				txtDirectory.setText(f.getAbsolutePath());
				txtPath.setText(valuesPath[1]);
				table.setCell(0, 1, txtPath.getText());
				table.setCell(0, 2, txtDirectory.getText());
			}
		}
		update();
	}

	@Override
	public void close() {
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		int row = table.getSelectedRow();
		if (table.getSelectedColumn() == 3) {
			if (table.getCell(row, 0).equalsIgnoreCase("path")) {
				toggle(row, valuesPath, txtPath);
				txtDirectory.setText(System.getProperty("user.dir"));
				table.setCell(0, 2, System.getProperty("user.dir"));
			}
			if (table.getCell(row, 0).equalsIgnoreCase("monitor"))
				toggle(row, valuesMonitor, txtMonitor);
			if (table.getCell(row, 0).equalsIgnoreCase("stats"))
				toggle(row, valuesStats, txtStats);
			if (table.getCell(row, 0).equalsIgnoreCase("verbose"))
				toggle(row, valuesVerbose, txtVerbose);
			if (table.getCell(row, 0).equalsIgnoreCase("system"))
				toggle(row, valuesSystem, txtSystem);
			if (table.getCell(row, 0).equalsIgnoreCase("multithreading"))
				toggle(row, valuesMultithreading, txtMultithreading);
			if (table.getCell(row, 0).equalsIgnoreCase("display"))
				toggle(row, valuesDisplay, txtDisplay);
		}
		update();
		Command.command();
	}

	@Override
	public void mousePressed(MouseEvent e) {
	}

	@Override
	public void mouseReleased(MouseEvent e) {
	}

	@Override
	public void mouseEntered(MouseEvent e) {
	}

	@Override
	public void mouseExited(MouseEvent e) {
	}

	private void toggle(int row, String values[], JTextField txt) {
		for (int i = 0; i < values.length; i++) {
			if (table.getCell(row, 1).equalsIgnoreCase(values[i])) {
				int k = i == values.length - 1 ? 0 : i + 1;
				table.setCell(row, 1, values[k]);
				txt.setText(values[k]);
				return;
			}
		}
		setDefault(row, values, txt);
	}

	private void setDefault(int row, String values[], JTextField txt) {
		table.setCell(row, 1, values[0]);
		txt.setText(values[0]);
	}

}

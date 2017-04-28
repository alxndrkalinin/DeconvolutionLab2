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

package deconvolutionlab.module;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import bilib.component.GridPanel;
import bilib.tools.Files;
import deconvolution.Command;
import deconvolutionlab.Config;

public class DirectoryModule extends AbstractModule implements ActionListener, KeyListener {

	private JComboBox<String>	cmbPath;
	private JTextField			txtPath;
	private JButton				bnBrowse;
	
	private JCheckBox			chkSystem;
	private JCheckBox			chkDisplayFinal;

	public DirectoryModule() {
		super("Path", "", "Default", "");
	}

	@Override
	public String getCommand() {
		String cmd = "";
		if (cmbPath.getSelectedIndex() != 0)
			cmd += " -path " + txtPath.getText();
		if (!chkSystem.isSelected())
			cmd += " -system no";
		if (!chkDisplayFinal.isSelected())
			cmd += " -display no";
		return cmd;
	}

	@Override
	public JPanel buildExpandedPanel() {

		cmbPath = new JComboBox<String>(new String[] { "Current", "Specify"});
		txtPath = new JTextField("", 35);
		bnBrowse = new JButton("Browse");
		chkSystem = new JCheckBox("Show the system panel");
		chkDisplayFinal = new JCheckBox("Display the final output");
		
		
		GridPanel pn1 = new GridPanel(true, 3);
		pn1.place(0, 0, 3, 1, "Working directory");
		pn1.place(1, 0, cmbPath);
		pn1.place(1, 1, bnBrowse);
		pn1.place(2, 0, 3, 1, txtPath);

		pn1.place(5, 0, 3, 1, "  ");
		pn1.place(6, 0, 3, 1, chkSystem);
		pn1.place(7, 0, 3, 1, chkDisplayFinal);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.add(pn1);
		String dir = System.getProperty("user.dir");
		Config.register(getName(), "current", cmbPath, cmbPath.getItemAt(0));
		Config.register(getName(), "path", txtPath, dir);
		Config.register(getName(), "system", chkSystem, true);
		Config.register(getName(), "display", chkDisplayFinal, true);

	
		// Add drop area
		pn1.setDropTarget(new LocalDropTarget());
		txtPath.setDropTarget(new LocalDropTarget());
		getCollapsedPanel().setDropTarget(new LocalDropTarget());
		bnTitle.setDropTarget(new LocalDropTarget());
		bnSynopsis.setDropTarget(new LocalDropTarget());
		bnExpand.setDropTarget(new LocalDropTarget());

		chkSystem.addActionListener(this);
		chkDisplayFinal.addActionListener(this);
		cmbPath.addActionListener(this);
		txtPath.addKeyListener(this);
		bnBrowse.addActionListener(this);
		getAction1Button().addActionListener(this);
		return panel;
	}

	private void update() {
		setCommand(getCommand());
		if (cmbPath.getSelectedIndex() == 0) {
			txtPath.setText(System.getProperty("user.dir"));
			txtPath.setEnabled(false);
			bnBrowse.setEnabled(false);
		}
		else {
			txtPath.setEnabled(true);
			bnBrowse.setEnabled(true);
		}
		setSynopsis(new File(txtPath.getText()).getName());
		Command.command();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		super.actionPerformed(e);
		if (e.getSource() == bnBrowse) {
			File f = Files.browseDirectory(txtPath.getText());
			if (f != null) {
				txtPath.setText(f.getAbsolutePath());
			}
		}
		else if (e.getSource() == cmbPath) {
			if (cmbPath.getSelectedIndex() == 0) {
				File f = new File(System.getProperty("user.dir"));
				txtPath.setText(f.getAbsolutePath());
			}
		}
		else if (e.getSource() == getAction1Button()) {
			txtPath.setText(System.getProperty("user.dir"));
			txtPath.setEnabled(false);
			bnBrowse.setEnabled(false);
			chkDisplayFinal.setSelected(true);
			chkSystem.setSelected(true);
		}
		update();
	}

	@Override
	public void close() {
		chkDisplayFinal.removeActionListener(this);
		chkSystem.removeActionListener(this);
		cmbPath.removeActionListener(this);
		txtPath.removeKeyListener(this);
		bnBrowse.removeActionListener(this);
	}
	

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
		update();
	}

	public class LocalDropTarget extends DropTarget {
		
		@Override
		public void drop(DropTargetDropEvent e) {
			e.acceptDrop(DnDConstants.ACTION_COPY);
			e.getTransferable().getTransferDataFlavors();
			Transferable transferable = e.getTransferable();
			DataFlavor[] flavors = transferable.getTransferDataFlavors();
			for (DataFlavor flavor : flavors) {
				if (flavor.isFlavorJavaFileListType()) {
					try {
						List<File> files = (List<File>) transferable.getTransferData(flavor);
						for (File file : files) {
							cmbPath.setSelectedIndex(1);
							bnBrowse.setEnabled(true);
							txtPath.setEnabled(true);
							if (file.isDirectory())
								txtPath.setText(file.getAbsolutePath());
							else
								txtPath.setText(file.getParent());
							update();
						}
					}
					catch (UnsupportedFlavorException ex) {
						ex.printStackTrace();
					}
					catch (IOException ex) {
						ex.printStackTrace();
					}
				}
			}
			e.dropComplete(true);
			super.drop(e);
		}
	}
}

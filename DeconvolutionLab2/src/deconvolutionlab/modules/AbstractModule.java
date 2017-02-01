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
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

public abstract class AbstractModule extends JPanel implements ActionListener {
	private JButton				bnTitle;
	private JButton				bnSynopsis;
	private JButton				bnAction1 = new JButton();
	private JButton				bnAction2 = new JButton();
	private JLabel				lblCommand;

	private JButton				bnExpand;
	private CardLayout			card		= new CardLayout();
	private JPanel				space		= new JPanel(card);
	private boolean				expanded	= false;
	private GroupedModulePanel	mpns;
	private JPanel				pnExpanded;
	private JPanel				pnCollapsed;
	private String				name;
	private String				key;

	public AbstractModule(String name, String key, String action1, String action2, boolean expanded) {
		this.name = name;
		this.key = key;
		pnCollapsed = buildCollapsedPanel();
		pnExpanded = buildExpandedPanel();
		setLayout(new BorderLayout());
		this.expanded = expanded;
		bnTitle = new JButton("<html><b>" + name + "</b></html>");
		bnTitle.setHorizontalAlignment(SwingConstants.LEFT);
		bnTitle.setPreferredSize(new Dimension(180, 20));
		bnTitle.setMaximumSize(new Dimension(250, 20));
		bnTitle.setMinimumSize(new Dimension(120, 20));
		bnTitle.addActionListener(this);

		bnSynopsis = new JButton("");
		bnSynopsis.setHorizontalAlignment(SwingConstants.LEFT);
		bnSynopsis.setPreferredSize(new Dimension(200, 20));
		bnSynopsis.addActionListener(this);

		bnExpand = new JButton("\u25BA");
		bnExpand.setPreferredSize(new Dimension(40, 20));
		bnExpand.setMaximumSize(new Dimension(40, 20));
		bnExpand.setMinimumSize(new Dimension(40, 20));
		bnExpand.addActionListener(this);

		JPanel tool0 = new JPanel(new BorderLayout());
		JPanel tool1 = new JPanel(new BorderLayout());
		tool1.add(bnExpand, BorderLayout.WEST);
		tool1.add(bnTitle, BorderLayout.EAST);
		tool0.add(tool1, BorderLayout.WEST);
		tool0.add(bnSynopsis, BorderLayout.CENTER);

		JPanel toola = null;
		if (!action1.equals("")) {
			bnAction1.setText(action1);
			bnAction1.setPreferredSize(new Dimension(50, 20));
			if (toola == null)
				toola = new JPanel(new BorderLayout());
			toola.add(bnAction1, BorderLayout.WEST);
		}
		if (!action2.equals("")) {
			bnAction2.setText(action2);
			bnAction2.setPreferredSize(new Dimension(50, 20));
			if (toola == null)
				toola = new JPanel(new BorderLayout());
			toola.add(bnAction2, BorderLayout.EAST);
		}

		if (toola != null) 
			tool0.add(toola, BorderLayout.EAST);
		
		space.add(pnExpanded, "expand");
		space.add(pnCollapsed, "collapse");
		add(tool0, BorderLayout.NORTH);
		add(space, BorderLayout.CENTER);
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		if (expanded)
			expand();
		else
			collapse();
		setPreferredSize(pnCollapsed.getPreferredSize());
	}

	public JPanel buildCollapsedPanel() {
		JPanel panel = new JPanel(new BorderLayout());
		lblCommand = new JLabel("");
		lblCommand.setBorder(BorderFactory.createEtchedBorder());
		lblCommand.setHorizontalAlignment(SwingConstants.LEFT);
		lblCommand.setPreferredSize(new Dimension(500, 30));
		panel.add(lblCommand, BorderLayout.NORTH);
		return panel;
	}

	public abstract JPanel buildExpandedPanel();
	public abstract String getCommand();
	public abstract void close();
	
	public JButton getAction1Button() {
		return bnAction1;
	}

	public JButton getAction2Button() {
		return bnAction2;
	}

	@Override
	public String getName() {
		return name;
	}
	
	public String getKey() {
		return key;
	}

	public String getTitle() {
		return bnTitle.getText();
	}

	public void setCommand(String command) {
		lblCommand.setText("<html><p style=\"font-family: monospace\"><small>" + command + "</small></p></html>");
	}
	
	public void setSynopsis(String synopsis) {
		bnSynopsis.setText(synopsis);
	}

	public JPanel getCollapsedPanel() {
		return pnCollapsed;
	}

	public JPanel getExpandedPanel() {
		return pnExpanded;
	}

	public void setMultipleModulePanel(GroupedModulePanel mpns) {
		this.mpns = mpns;
	}

	@Override
	public Dimension getPreferredSize() {
		if (expanded)
			return pnExpanded.getPreferredSize();
		else
			return pnCollapsed.getPreferredSize();
	}

	public boolean isExpanded() {
		return expanded;
	}

	public void collapse() {
		expanded = false;
		card.show(space, "collapse");
		bnExpand.setText("\u25BA");
	}

	public void expand() {
		expanded = true;
		card.show(space, "expand");
		bnExpand.setText("\u25BC");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == bnTitle || e.getSource() == bnExpand || e.getSource() == bnSynopsis) {
			if (expanded) {
				collapse();
			}
			else {
				for (AbstractModule module : mpns.getModules())
					module.collapse();
				expand();
			}
			mpns.organize();
		}
	}
}

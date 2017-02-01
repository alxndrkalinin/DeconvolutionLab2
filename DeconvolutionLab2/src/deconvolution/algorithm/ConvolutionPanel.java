package deconvolution.algorithm;

import javax.swing.JPanel;

import lab.component.GridPanel;

public class ConvolutionPanel extends AbstractAlgorithmPanel {
	
	private Convolution algo = new Convolution();
	
	@Override
	public JPanel getPanelParameters() {
		GridPanel pn = new GridPanel(false);
		pn.place(1, 0, "<html><span \"nowrap\">No parameters</span></html>");
		return pn;
	}
	
	@Override
	public String getCommand() {
		return "";
	}
	
	@Override
	public String getName() {
		return algo.getName();
	}

	@Override
	public String getShortname() {
		return algo.getShortname();
	}

	@Override
	public String getDocumentation() {
		String s = ""; 
		s += "<h1>" + getName() + "</h1>";
		s += "<p>This algorithm is only used for simulation. It convolves the input image with the PSF.</p>";
		return s;
	}

}

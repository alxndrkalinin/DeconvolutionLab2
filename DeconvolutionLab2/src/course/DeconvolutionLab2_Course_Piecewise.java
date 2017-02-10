package course;

import java.io.File;
import java.util.Random;

import javax.swing.filechooser.FileSystemView;

import deconvolution.Deconvolution;
import deconvolutionlab.Lab;
import deconvolutionlab.monitor.Monitors;
import ij.plugin.PlugIn;
import signal.RealSignal;
import signal.factory.Constant;
import signal.factory.Cube;

public class DeconvolutionLab2_Course_Piecewise implements PlugIn {

	private String	desktop	= FileSystemView.getFileSystemView().getHomeDirectory().getAbsolutePath() + File.separator + "Desktop";
	private String	root	= desktop + File.separator + "DeconvolutionLab2-Course" + File.separator;
	private String	res		= root + "Results" + File.separator + "piecewise" + File.separator;
	private String	data	= root + "Data" + File.separator + "piecewise" + File.separator;

	public DeconvolutionLab2_Course_Piecewise() {

		Monitors monitors = Monitors.createDefaultMonitor();
		new File(res).mkdir();
		System.setProperty("user.dir", res);

		new File(res + "RIF").mkdir();
		new File(res + "LW").mkdir();
		new File(res + "LW+").mkdir();
		new File(res + "RL").mkdir();
		new File(res + "RLTV").mkdir();
		new File(res + "ISTA").mkdir();
		new File(res + "FISTA").mkdir();

		int nx = 128;
		int ny = 128;
		int nz = 128;
		int spacing = 16;

		Random rand = new Random(1234);
		RealSignal x = new Constant().intensity(0, 10).generate(nx, ny, nz);
		for(int i = 0; i< 12; i++) {
			double xc = (rand.nextDouble()*0.6 + 0.2);
			double yc = (rand.nextDouble()*0.6 + 0.2);
			double zc = (rand.nextDouble()*0.6 + 0.2);
			double size = 15 + (rand.nextDouble()*30);
			double ampl = (rand.nextDouble()+0.5)*10;
			x.plus(new Cube(size, 0.1).intensity(0, ampl).center(xc, yc, zc).generate(nx, ny, nz));
		}
		Lab.show(monitors, x, "reference");
		Lab.save(monitors, x, res + "ref.tif");

		String algo = " ";
		String ground = " -image file " + res + "ref.tif ";
		//String psf = " -psf file ../../Data/resolution/psfgl.tif";
		 String psf = " -psf synthetic gaussian 100.0 0.0 1.2 1.2 3.6 size ";
		// nx + " " + ny + " " + nz;
		String signal = " -image file signal.tif -reference " + res + "ref.tif -disable monitor";

		String paramout = " intact float  (" + spacing + "," + spacing + "," + spacing + ")";

		algo = " -algorithm CONV  -showstats @3 PR -out stack PR -out ortho PRo ";
		new Deconvolution(ground + "-reference reference.tif -psf synthetic impulse 100 0 size 128 128 128 " + algo).deconvolve(false);
		
		algo = " -algorithm SIM 0 1 1  -showstats @3 SIM -out stack signal -out ortho SIGNALo ";
		new Deconvolution(ground + psf + algo).deconvolve(false);
		  
		algo = " -algorithm NIF -out ortho NIF " + paramout; 
		new Deconvolution(signal + psf + algo).deconvolve(false);
/*
		algo = " -algorithm LW+ 15 1.5 -showstats @1 LW+  -out ortho @1 LW+/LW+" + paramout; 
		new Deconvolution(signal + psf + algo).deconvolve(false);

		algo = " -algorithm FISTA 15 1 1 -showstats @1 FISTA  -out ortho @1 FISTA/FISTA" + paramout; 
		new Deconvolution(signal + psf + algo).deconvolve(false);

		algo = " -algorithm ISTA 15 1 1 -showstats @1 ISTA  -out ortho @1 ISTA/ISTA" + paramout; 
		new Deconvolution(signal + psf + algo).deconvolve(false);
*/
		algo = " -algorithm RLTV 15 0.01 -showstats @1 RLTV  -out ortho @1 RLTV/RLTV" + paramout; 
		new Deconvolution(signal + psf + algo).deconvolve(false);
		
		 /* 
		for(int i=0; i<=24; i++) { 
			double p = Math.pow(10, i-18); 
			algo =" -algorithm RIF " + p + " -out ortho @5 RIF/RIF" + i + paramout; 
			new Deconvolution(signal + psf + algo).deconvolve(false); 
		}
		*/


	}

	public static void main(String arg[]) {
		new DeconvolutionLab2_Course_Piecewise();
	}

	@Override
	public void run(String arg) {
		new DeconvolutionLab2_Course_Piecewise();
	}

}
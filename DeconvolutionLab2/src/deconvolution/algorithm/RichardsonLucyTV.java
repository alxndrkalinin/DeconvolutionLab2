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

package deconvolution.algorithm;

import java.util.concurrent.Callable;

import signal.ComplexSignal;
import signal.Operations;
import signal.RealSignal;

public class RichardsonLucyTV extends AbstractAlgorithm implements Callable<RealSignal> {

	private double lambda = 0.1;

	public RichardsonLucyTV(int iter, double lambda) {
		super();
		controller.setIterationMax(iter);
		this.lambda = lambda;
	}

	// x(k+1) = x(k) *. Hconj * ( y /. H x(k))
	@Override
	public RealSignal call() {
		ComplexSignal H = fft.transform(h);
		ComplexSignal U = new ComplexSignal(y.nx, y.ny, y.nz);
		RealSignal x = y.duplicate();
		RealSignal gx = y.duplicate();
		RealSignal gy = y.duplicate();
		RealSignal gz = y.duplicate();
		RealSignal ggx = y.duplicate();
		RealSignal ggy = y.duplicate();
		RealSignal ggz = y.duplicate();

		RealSignal u  = gx;	// resued memory
		RealSignal p  = gy;	// resued memory
		RealSignal tv = gz; // resued memory

		while(!controller.ends(x)) {
			gradientX(x, gx);
			gradientY(x, gy);
			gradientZ(x, gz);
			normalize(gx, gy, gz);	
			gradientX(gx, ggx);
			gradientY(gy, ggy);
			gradientZ(gz, ggz);
			compute((float)lambda, ggx, ggy, ggz, tv);
			
			fft.transform(x, U);
			U.times(H);
			fft.inverse(U, u);
			Operations.divide(y, u, p);
			fft.transform(p, U);
			U.timesConjugate(H);
			fft.inverse(U, u);
			x.times(u); 
			x.times(tv);
		}
		return x;
	}
	
	private void compute(float lambda, RealSignal gx, RealSignal gy, RealSignal gz, RealSignal tv) {
		int nxy = gx.nx * gy.ny;
		for(int k=0; k<gx.nz; k++)
		for(int i=0; i< nxy; i++)  {
			double dx = gx.data[k][i];
			double dy = gy.data[k][i];
			double dz = gz.data[k][i];
			tv.data[k][i] = (float)(1.0 / ( (dx+dy+dz) * lambda + 1.0));
		}
		//Log.info("Norm TV "+ Math.sqrt(norm));
	}

	public void gradientX(RealSignal signal, RealSignal output) {
		int nx = signal.nx;
		int ny = signal.ny;
		int nz = signal.nz;
		for(int k=0; k<nz; k++) 
		for(int j=0; j<ny; j++) 
		for(int i=0; i<nx-1; i++) {
			int index = i + signal.nx*j;
			output.data[k][index] = signal.data[k][index] - signal.data[k][index+1];
		}
	}

	public void gradientY(RealSignal signal, RealSignal output) {
		int nx = signal.nx;
		int ny = signal.ny;
		int nz = signal.nz;
		for(int k=0; k<nz; k++) 
		for(int j=0; j<ny-1; j++) 
		for(int i=0; i<nx; i++) {
			int index = i + signal.nx*j;
			output.data[k][index] = signal.data[k][index] - signal.data[k][index+nx];
		}
	}
	
	public void gradientZ(RealSignal signal, RealSignal output) {
		int nx = signal.nx;
		int ny = signal.ny;
		int nz = signal.nz;
		for(int k=0; k<nz-1; k++) 
		for(int j=0; j<ny; j++) 
		for(int i=0; i<nx; i++) {
			int index = i + signal.nx*j;
			output.data[k][index] = signal.data[k][index] - signal.data[k+1][index];
		}
	}

	public void normalize(RealSignal x, RealSignal y, RealSignal z) {
		int nx = x.nx;
		int ny = y.ny;
		int nz = z.nz;
		float e = (float)RealSignal.epsilon;
		for(int k=0; k<nz; k++) 
		for(int i=0; i<nx*ny; i++) {
			double norm = Math.sqrt(x.data[k][i] * x.data[k][i] + y.data[k][i] * y.data[k][i] + z.data[k][i] * z.data[k][i]);
			if (norm < e) {
				x.data[k][i] = e;
				y.data[k][i] = e;
				z.data[k][i] = e;
			}
			else {
				x.data[k][i] /= norm;
				y.data[k][i] /= norm;
				z.data[k][i] /= norm;
			}
		}
	}
	
	
	@Override
	public String getName() {
		return "Richardson-Lucy TV";
	}
	
	@Override
	public String getShortname() {
		return "RLTV";
	}

	@Override
	public boolean isRegularized() {
		return true;
	}
	
	@Override
	public boolean isStepControllable() {
		return true;
	}
	
	@Override
	public boolean isIterative() {
		return true;
	}
	
	@Override
	public boolean isWaveletsBased() {
		return false;
	}
	
	@Override
	public void setParameters(double[] params) {
		if (params == null)
			return;
		if (params.length > 0)
			controller.setIterationMax((int)Math.round(params[0]));
		if (params.length > 1)
			lambda = (float)params[1];
	}
	
	@Override
	public double[] getDefaultParameters() {
		return new double[] {10, 0.1};
	}
	
	@Override
	public double[] getParameters() {
		return new double[] {controller.getIterationMax(), lambda};
	}
	
	@Override
	public double getRegularizationFactor() {
		return lambda;
	}
	
	@Override
	public double getStepFactor() {
		return 0.0;
	}
}

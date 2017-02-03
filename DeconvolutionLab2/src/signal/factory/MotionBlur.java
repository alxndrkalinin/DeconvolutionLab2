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

package signal.factory;

import signal.RealSignal;

public class MotionBlur extends SignalFactory {

	private double sigma = 3.0;
	private double direction = 30.0;
	private double elongation = 3.0;

	public MotionBlur(double sigma, double direction, double elongation) {
		super(new double[] {sigma, direction, elongation});
	}

	@Override
	public String getName() {
		return "MotionBlur";
	}
	 
	@Override
	public String[] getParametersName() {
		return new String[] {"Sigma", "Direction", "Elongation"};
	}	

	@Override
	public void setParameters(double[] parameters) {
		if (parameters.length >= 1)
			this.sigma = parameters[0];
		if (parameters.length >= 2)
			this.direction = parameters[1];
		if (parameters.length >= 3)
			this.elongation = parameters[2];
	}

	@Override
	public double[] getParameters() {
		return new double[] {sigma, direction, elongation};
	}

	@Override
	public void fill(RealSignal signal) {
		double K1 = 0.5 / (sigma*sigma);
		double K2 = 0.5 / (sigma*sigma*elongation*elongation);
		double cosa = Math.cos(Math.toRadians(direction));
		double sina = Math.sin(Math.toRadians(direction));
		for(int x=0; x<nx; x++)
		for(int y=0; y<ny; y++) {		
			double dx = (x-xc);
			double dy = (y-yc);
			//double ps = (1.0 + (dx*cosa + dy*sina)/(dx*dx + dy*dy)) * 0.5;
			double K = K1 + dx * K2 /(dx*dx + dy*dy);
			double r2 = (x-xc)*(x-xc) + (y-yc)*(y-yc);
			for(int z=0; z<nz; z++) {
				signal.data[z][x+nx*y] = (float)((amplitude-background) * Math.exp(-r2*K) + background);
			}
		}
	}
}
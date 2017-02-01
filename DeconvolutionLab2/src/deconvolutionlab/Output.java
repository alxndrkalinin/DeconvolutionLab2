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

package deconvolutionlab;

import java.io.File;

import lab.tools.NumFormat;
import signal.Constraint;
import signal.RealSignal;
import deconvolution.algorithm.Controller;
import deconvolutionlab.monitor.Monitors;

public class Output {

	public enum View {
		STACK, SERIES, ORTHO, MIP, PLANAR, FIGURE
	};

	public enum Dynamic {
		INTACT, RESCALED, NORMALIZED, CLIPPED
	};

	private int					px			= 0;
	private int					py			= 0;
	private int					pz			= 0;
	private boolean				center		= true;

	private String				name		= "";
	private boolean				save		= true;
	private boolean				show		= true;

	private View				view		= View.STACK;
	private PlatformImager.Type	type		= PlatformImager.Type.FLOAT;
	private Dynamic				dynamic		= Dynamic.INTACT;

	private int					frequency	= 0;
	private String				path = "";
	
	public Output(View view, int frequency, String param) {
		String[] tokens = param.trim().split(" ");
		this.view = view;
		this.frequency = frequency;
		this.name = "";
		this.center = true;
		this.save = true;
		this.show = true;
		for (int i = 0; i < tokens.length; i++) {
			boolean found = false;
			String p = tokens[i].trim().toLowerCase();

			if (p.startsWith("@")) {
				found = true;
			}

			if (p.startsWith("noshow")) {
				show = false;
				found = true;
			}

			if (p.startsWith("nosave")) {
				save = false;
				found = true;
			}

			for (Dynamic d : Dynamic.values()) {
				if (p.toLowerCase().equals(d.name().toLowerCase())) {
					dynamic = d;
					found = true;
				}
			}
			for (View v : View.values()) {
				if (p.toLowerCase().equals(v.name().toLowerCase())) {
					view = v;
					found = true;
				}
			}
			for (PlatformImager.Type t : PlatformImager.Type.values()) {
				if (p.toLowerCase().equals(t.name().toLowerCase())) {
					type = t;
					found = true;
				}
			}
			if (p.startsWith("(") && p.endsWith(")")) {
				double pos[] = NumFormat.parseNumbers(p);
				if (pos.length > 0) px = (int) Math.round(pos[0]);
				if (pos.length > 1) py = (int) Math.round(pos[1]);
				if (pos.length > 2) pz = (int) Math.round(pos[2]);
				found = true;
				center = false;
			}
			if (!found) name += tokens[i] + " ";
			name = name.trim();
		}
	}

	public Output(View view, boolean show, boolean save, int frequency, String name, Dynamic dynamic, PlatformImager.Type type, boolean center) {
		this.name = name;
		this.show = show;
		this.save = save;
		this.view = view;
		this.type = type;
		this.dynamic = dynamic;
		this.center = center;
		this.frequency = frequency;
	}

	public Output(View view, boolean show, boolean save, int frequency, String name, Dynamic dynamic, PlatformImager.Type type, int px, int py, int pz) {
		this.name = name;
		this.show = show;
		this.save = save;
		this.view = view;
		this.type = type;
		this.dynamic = dynamic;
		this.center = false;
		this.px = px;
		this.py = py;
		this.pz = pz;
		this.frequency = frequency;
	}

	public boolean is(int iterations) {
		if (frequency == 0) return false;
		return iterations % frequency == 0;
	}

	public View getView() {
		return view;
	}

	public String getName() {
		return name;
	}
	
	public void setPath1(String path) {
		this.path = path;
	}

	public int extractFrequency(String param) {
		String line = param.trim();
		if (!line.startsWith("@")) line = "@0 " + line;
		String parts[] = line.split(" ");
		if (parts.length >= 1) {
			return (int) Math.round(NumFormat.parseNumber(parts[0], 0));
		}
		return 0;
	}

	public void setKeypoint(int px, int py, int pz) {
		this.px = px;
		this.py = py;
		this.pz = pz;
		this.center = false;
	}

	public String[] getAsString() {
		String t = (type == PlatformImager.Type.FLOAT ? "" : type.name().toLowerCase());
		String d = (dynamic == Dynamic.INTACT ? "" : dynamic.name().toLowerCase());
		String k = "";
		if (!center)
			k = " (" + px + "," + py + "," + pz + ")";
		else
			k = "";
		String sa = save ? "\u2713" : "";
		String sh = show ? "\u2713" : "";
		String fr = frequency > 0 ? " @" + frequency : "";
		return new String[] { view.name().toLowerCase() + fr, name, d, t, k, sh, sa, "" };
	}


	public void execute(Monitors monitors, RealSignal signal, Controller controller, boolean live) {
		if (signal == null) return;
		String title = name;
		if (controller != null && live) {
			if (controller.getIterations() > 0) {
				title += "@" + controller.getIterations();
			}
		}
		RealSignal x = null;
		Constraint constraint = new Constraint(monitors);
		switch (dynamic) {
		case RESCALED:
			x = signal.duplicate();
			constraint.rescaled(x, 0, 255);
			break;
		case CLIPPED:
			x = signal.duplicate();
			float[] stats = controller.getStatsInput();
			if (stats != null) constraint.clipped(x, stats[1], stats[2]);
			break;
		case NORMALIZED:
			x = signal.duplicate();
			float[] stats1 = controller.getStatsInput();
			if (stats1 != null) constraint.normalized(x, stats1[0], stats1[3]);
			break;
		default:
			x = signal;
		}
		String filename = path + File.separator + title + ".tif";
		String key = name + "-" + type.name() + "-" + view.name() + "-" + dynamic.name() + "-" + (px + py + pz);
		switch (view) {
		case STACK:
			if (show && !live) Lab.show(monitors, x, title, type, (center ? x.nz / 2 : pz));
			if (save && !live) Lab.save(monitors, x, filename, type);
			break;
		case SERIES:
			for (int k = 0; k < x.nz; k++) {
				RealSignal slice = x.getSlice(k);
				if (show && !live) Lab.show(monitors, slice, title, type);
				if (save && !live) Lab.save(monitors, slice, filename, type);
			}
			break;
		case ORTHO:
			orthoview(monitors, x, title, filename, live, key);
			break;
		case FIGURE:
			figure(monitors, x, title, filename, live, key);
			break;
		case MIP:
			mip(monitors, x, title, filename, live, key);
			break;
		case PLANAR:
			planar(monitors, x, title, filename, live, key);
			break;
		default:
			break;
		}
	}

	private void mip(Monitors monitors, RealSignal signal, String title, String filename, boolean live, String key) {
		RealSignal plane = signal.createMIP();
		if (show && live) Lab.appendShowLive(monitors, key, plane, title, type);
		if (show && !live) Lab.show(monitors, plane, title, type);
		if (save) Lab.save(monitors, plane, filename, type);
	}

	private void orthoview(Monitors monitors, RealSignal signal, String title, String filename, boolean live, String key) {
		int cx = px;
		int cy = py;
		int cz = pz;
		if (center) {
			cx = signal.nx / 2;
			cy = signal.ny / 2;
			cz = signal.nz / 2;
		}
		RealSignal plane = signal.createOrthoview(cx, cy, cz);
		if (show && live) Lab.appendShowLive(monitors, key, plane, title, type);
		if (show && !live) Lab.show(monitors, plane, title, type);
		if (save) Lab.save(monitors, plane, filename, type);
	}

	private void figure(Monitors monitors, RealSignal signal, String title, String filename, boolean live, String key) {
		int cx = px;
		int cy = py;
		int cz = pz;
		if (center) {
			cx = signal.nx / 2;
			cy = signal.ny / 2;
			cz = signal.nz / 2;
		}
		RealSignal plane = signal.createFigure(cx, cy, cz);
		if (show && live) Lab.appendShowLive(monitors, key, plane, title, type);
		if (show && !live) Lab.show(monitors, plane, title, type);
		if (save) Lab.save(monitors, plane, filename, type);
	}

	private void planar(Monitors monitors, RealSignal signal, String title, String filename, boolean live, String key) {
		RealSignal plane = signal.createMontage();
		if (show && live) Lab.appendShowLive(monitors, key, plane, title, type);
		if (show && !live) Lab.show(monitors, plane, title, type);
		if (save) Lab.save(monitors, plane, filename, type);
	}

	@Override
	public String toString() {
		String t = type.name().toLowerCase();
		String v = view.name().toLowerCase();
		String d = dynamic.name().toLowerCase();
		String f = frequency > 0 ? " every " + frequency + " iterations" : "";
		String k = (center ? "" : " keypoint = (" + px + "," + py + "," + pz + ")");
		return v + " " + name + " format = (" + d + ", " + t + ") " + k + f;
	}
}
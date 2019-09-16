//==============================================================================
//	
//	Copyright (c) 2017-
//	Authors:
//	* Dave Parker <d.a.parker@cs.bham.ac.uk> (University of Birmingham)
//	
//------------------------------------------------------------------------------
//	
//	This file is part of PRISM.
//	
//	PRISM is free software; you can redistribute it and/or modify
//	it under the terms of the GNU General Public License as published by
//	the Free Software Foundation; either version 2 of the License, or
//	(at your option) any later version.
//	
//	PRISM is distributed in the hope that it will be useful,
//	but WITHOUT ANY WARRANTY; without even the implied warranty of
//	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//	GNU General Public License for more details.
//	
//	You should have received a copy of the GNU General Public License
//	along with PRISM; if not, write to the Free Software Foundation,
//	Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//	
//==============================================================================

package server;

import prism.*;

/**
 * An example class demonstrating how to control PRISM programmatically,
 * through the functions exposed by the class prism.Prism.
 * <p>
 * This shows how to define a model programmatically using the {@link ModelGenerator}
 * interface - in this case a simple Markov chain model of a random walk.
 * <p>
 * See the README for how to link this to PRISM.
 */
public class CartpoleModelGeneratorPython {
	public static void main(String[] args) {
		new CartpoleModelGeneratorPython().run();
	}

	public void run() {
		try {
			// Create a log for PRISM output (hidden or stdout)
//			PrismLog mainLog = new PrismDevNullLog();
			PrismLog mainLog = new PrismFileLog("stdout");

			// Initialise PRISM engine 
			Prism prism = new Prism(mainLog);
			prism.initialise();

			// Create a model generator to specify the model that PRISM should build
			// (in this case a simple random walk)
			CartpolePythonWrapper modelGen = new CartpolePythonWrapper("tcp://localhost:5558");

			// Load the model generator into PRISM,
			// export the model to a dot file (which triggers its construction)
			prism.loadModelGenerator(modelGen);
			prism.setEngine(Prism.EXPLICIT);
//			prism.exportTransToFile(true, Prism.EXPORT_DOT_STATES, new File("dtmc.dot"));

			// Then do some model checking and print the result
			String[] props = new String[]{
					"P=?[F \"failed\"]",
					"P=?[F \"done\"]",
//					"P=?[F<=20 \"failed\"]",
//					"P=?[F<=40 \"done\"]",
//					"R=?[F \"done\"]",
					"R=?[F \"failed\"|\"done\"]",
			};
			for (String prop : props) {
				System.out.println(prop + ":");
				System.out.println(prism.modelCheck(prop).getResult());
			}

			// Close down PRISM
			prism.closeDown();

//		} catch (FileNotFoundException e) {
//			System.out.println("Error: " + e.getMessage());
//			System.exit(1);
		} catch (PrismException e) {
			System.out.println("Error: " + e.getMessage());
			System.exit(1);
		}
	}

}

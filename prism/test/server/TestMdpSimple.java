package server;

import explicit.Distribution;
import explicit.MDPSimple;
import parser.State;
import prism.PrismException;

public class TestMdpSimple {
	public static void main(String[] args) throws PrismException {
		TestPythonWrapper testPythonWrapper = new TestPythonWrapper();
		testPythonWrapper.testConnection();
	}

	public void testConnection() throws PrismException {
		MDPSimple mdpSimple = new MDPSimple();
		int s = mdpSimple.addState();
		final Distribution distr = new Distribution();
		distr.add(0, 0.8);
		distr.add(1, 0.2);
		mdpSimple.addChoice(s, distr);
//		mdpSimple.setAction();
	}
}

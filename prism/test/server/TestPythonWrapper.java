package server;

import parser.State;
import prism.PrismException;

public class TestPythonWrapper {
	public static void main(String[] args) throws PrismException {
		TestPythonWrapper testPythonWrapper = new TestPythonWrapper();
		testPythonWrapper.testConnection();
	}

	public void testConnection() throws PrismException {
		PythonWrapper server = new PythonWrapper("tcp://localhost:5558");
		System.out.println(server.getVarNames());
		System.out.println(server.getVarTypes());
		System.out.println(server.getLabelNames());
		System.out.println(server.createVarList());
		System.out.println(server.getInitialState());
		System.out.println(server.getNumChoices());
		System.out.println(server.getNumTransitions());
		System.out.println(server.getTransitionAction(0, 0));
		System.out.println(server.getTransitionProbability(0, 0));
		System.out.println(server.computeTransitionTarget(0, 0));
		System.out.println(server.isLabelTrue(0));
		System.out.println(server.getRewardStructNames());
		System.out.println(server.getStateReward(0, server.getInitialState()));
		System.out.println(server.getStateActionReward(0, server.getInitialState(), 0));
		System.out.println("No issues");
	}
}

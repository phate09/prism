package server;

import explicit.Distribution;
import explicit.MDPModelChecker;
import explicit.MDPSimple;
import explicit.ModelCheckerResult;
import explicit.rewards.MDPRewardsSimple;
import prism.PrismException;
import prism.PrismSettings;
import py4j.GatewayServer;

import java.util.BitSet;
import java.util.List;

public class MdpAbstract {
	private MDPSimple mdpSimple;

	public MdpAbstract() {
		mdpSimple = new MDPSimple();
	}

	public static void main(String[] args) {
		GatewayServer gatewayServer = new GatewayServer(new MdpAbstract());
		gatewayServer.start();
		System.out.println("MDP Server Started");
	}

	public MDPSimple getMdpSimple() {
		System.out.println("New connnection");
		return mdpSimple;
	}

	public Distribution generateDistribution(int successor1, int successor2) {
		Distribution distribution = new Distribution();
		distribution.add(successor1, 0.8);
		distribution.add(successor2, 0.2);
		return distribution;
	}

	public Distribution generateLinkSuccessor(int successor) {
		Distribution distribution = new Distribution();
		distribution.add(successor, 1.0);
		return distribution;
	}

	public Distribution newDistribution() {
		return new Distribution();
	}

	public void export_to_dot_file() {
		try {
			mdpSimple.exportToDotFile("./mdp_simple_dot.dot");
			System.out.println("File exported");
		} catch (PrismException e) {
			e.printStackTrace();
		}
	}

	public double[] check_property(int state_number) {
		try {
			if (mdpSimple.getNumInitialStates() == 0)
				mdpSimple.addInitialState(0);
			mdpSimple.findDeadlocks(true);
			MDPModelChecker checker = new MDPModelChecker(null);
			if (checker.getSettings() == null)
				checker.setSettings(new PrismSettings());
			BitSet bitSet = new BitSet();
			bitSet.set(state_number);
			final ModelCheckerResult modelCheckerResult = checker.computeReachProbs(mdpSimple, bitSet, true);
			return modelCheckerResult.soln;
		} catch (PrismException e) {
			e.printStackTrace();
		}
		return null;
	}

	public double[] check_state_list(int[] target_states_id) {
		try {
			if (mdpSimple.getNumInitialStates() == 0)
				mdpSimple.addInitialState(0);
			mdpSimple.findDeadlocks(true);
			MDPModelChecker checker = new MDPModelChecker(null);
			BitSet bitSet = new BitSet();
			for (int i : target_states_id) {
				bitSet.set(i);
			}
			final ModelCheckerResult modelCheckerResult = checker.computeReachProbs(mdpSimple, bitSet, true);
			return modelCheckerResult.soln;
		} catch (PrismException e) {
			e.printStackTrace();
		}
		return null;
	}
}

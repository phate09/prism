package server;

import explicit.Distribution;
import explicit.MDPModelChecker;
import explicit.MDPSimple;
import explicit.ModelCheckerResult;
import explicit.rewards.MDPRewardsSimple;
import prism.PrismException;
import py4j.GatewayServer;

import java.util.BitSet;

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

	public double[] check_property() {
		try {
			MDPModelChecker checker = new MDPModelChecker(null);
			BitSet bitSet = new BitSet();
			bitSet.set(3076);
			final ModelCheckerResult modelCheckerResult = checker.computeReachProbs(mdpSimple, bitSet, true);
			return modelCheckerResult.soln;
		} catch (PrismException e) {
			e.printStackTrace();
		}
		return null;
	}
}

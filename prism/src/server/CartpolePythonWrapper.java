package server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import parser.State;
import parser.VarList;
import parser.ast.Declaration;
import parser.ast.DeclarationInt;
import parser.ast.Expression;
import parser.type.Type;
import parser.type.TypeInt;
import prism.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ModelGenerator defining a discrete-time Markov chain (DTMC) model
 * of a simple random walk whose value varies between -n and n
 * and the probability of incrementing, rather than decrementing,
 * the value is p (n and p are both parameters).
 */
class CartpolePythonWrapper extends PythonWrapper implements ModelGenerator, RewardGenerator {

	public CartpolePythonWrapper(String _address) {
		super(_address);
	}

	// Methods for ModelGenerator interface (rather than superclass ModelInfo)

	@Override
	public VarList createVarList() {
		// Need to give the variable list containing the declaration of variable x
		VarList varList = new VarList();
		try {
			for (String x : getVarNames()) {
				varList.addVar(new Declaration(x, new DeclarationInt(Expression.Int(-10), Expression.Int(10))), 0, null);
			}
		} catch (PrismLangException e) {
			System.out.println(e.getMessage());
		}
		return varList;
	}

	@Override
	public State getInitialState() throws PrismException {
		State new_state = new State(getVarNames().size());
		socket_req.send("getInitialState");
		final byte[] recv = socket_req.recv();
		return parseState(recv);
	}

	@Override
	public void exploreState(State exploreState) throws PrismException {
		// Store the state (for reference, and because will clone/copy it later)
		StateRequest.CartPoleState stateFloat = stateToProtobuf(exploreState);
		socket_req.sendMore("exploreState");
		socket_req.send(stateFloat.toByteArray());
		socket_req.recv(); //discard acknowledge message

	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		socket_req.sendMore("computeTransitionTarget");
		socket_req.sendMore(String.valueOf(i));
		socket_req.send(String.valueOf(offset));
		final byte[] recv = socket_req.recv();//receives next state
		final State state = parseState(recv);
		return state;
	}

	@Override
	public double getStateReward(int r, State state) throws PrismException {
		// r will only ever be 0 (because there is one reward structure)
		// We assume it assigns 1 to all states.

		StateRequest.CartPoleState stateFloat = stateToProtobuf(state);
		socket_req.sendMore("getStateReward");
		socket_req.send(stateFloat.toByteArray());
		final String recv = socket_req.recvStr();
		return Double.parseDouble(recv);
	}

	@Override
	public double getStateActionReward(int r, State state, Object action) throws PrismException {
		// No action rewards
		StateRequest.CartPoleState stateFloat = stateToProtobuf(state);
		socket_req.sendMore("getStateActionReward");
		socket_req.sendMore(stateFloat.toByteArray());
		socket_req.send(action != null ? action.toString() : "null");
		final String recv = socket_req.recvStr();
		return Double.parseDouble(recv);
	}

	private StateRequest.CartPoleState stateToProtobuf(State exploreState) {
		StateRequest.CartPoleState.Builder builder = StateRequest.CartPoleState.newBuilder();
		builder.setT((int) exploreState.varValues[0]);
		for (int i = 1; i < exploreState.varValues.length; i++) {
			builder.addValue((int) (exploreState.varValues[i]));
		}
		StateRequest.CartPoleState stateFloat = builder.build();
		return stateFloat;
	}

	///parse a state from protobuf
	private State parseState(byte[] recv) {
		State new_state = new State(getVarNames().size());
		try {
			StateRequest.CartPoleState stateFloat = StateRequest.CartPoleState.parseFrom(recv);
			new_state.setValue(0, stateFloat.getT());//sets the time step value
			for (int j = 0; j < stateFloat.getValueCount(); j++) {
				new_state.setValue(j + 1, stateFloat.getValue(j));
			}
			return new_state;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return new_state;
	}
}

package server;

import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.ProtocolStringList;
import org.zeromq.SocketType;
import org.zeromq.ZMQ;
import parser.State;
import parser.VarList;
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
class PythonWrapper implements ModelGenerator, RewardGenerator {
	private ZMQ.Context _context;
	private ZMQ.Socket socket_req;
	private List<Type> _types = null;
	private List<String> _varNames;
	private List<String> _labelNames;
	private List<String> _rewardStructNames;

	public PythonWrapper(String _address) {
		_context = ZMQ.context(2);
		socket_req = _context.socket(SocketType.REQ);
		socket_req.connect(_address);
		StateRequest.StateFloat request = StateRequest.StateFloat.getDefaultInstance();
	}

	// Methods for ModelInfo interface

	// The model is a discrete-time Markov chain (DTMC)

	@Override
	public ModelType getModelType() {
		return ModelType.DTMC;
	}

	// The model's state comprises one, integer-valued variable, x

	@Override
	public List<String> getVarNames() {
		if (this._varNames == null) {
			socket_req.send("getVarNames");
			final byte[] recv = socket_req.recv();
			try {
				StateRequest.StringVarNames varNames = StateRequest.StringVarNames.parseFrom(recv);
				final ProtocolStringList valueList = varNames.getValueList();
				this._varNames = valueList;
				return valueList;
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		} else {
			return this._varNames;
		}
		return null;
	}

	@Override
	public List<Type> getVarTypes() {
		if (this._types == null) {
			socket_req.send("getVarTypes");
			final byte[] recv = socket_req.recv();
			try {
				StateRequest.StringVarNames varNames = StateRequest.StringVarNames.parseFrom(recv);
				final ProtocolStringList valueList = varNames.getValueList();
				List<Type> types = new ArrayList<>();
				for (String element : valueList) {
					switch (element) {
						case "TypeInt":
							types.add(TypeInt.getInstance());
					}
				}
				this._types = types;
				return types;
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		} else {
			return this._types;
		}
		return null;
	}

	// There are three labels: "end", "left" and "right" (x=-n|x=n, x=-n, x=n, respectively)

	@Override
	public List<String> getLabelNames() {
		if (this._labelNames == null) {
			socket_req.send("getLabelNames");
			final byte[] recv = socket_req.recv();
			try {
				StateRequest.StringVarNames varNames = StateRequest.StringVarNames.parseFrom(recv);
				final ProtocolStringList valueList = varNames.getValueList();
				this._labelNames = valueList;
				return valueList;
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		} else {
			return this._labelNames;
		}
		return null;
	}

	// Methods for ModelGenerator interface (rather than superclass ModelInfo)

	@Override
	public VarList createVarList() {
		// Need to give the variable list containing the declaration of variable x
		VarList varList = new VarList();
//
//		try {
//			varList.addVar(new Declaration("x", new DeclarationInt(Expression.Int(-n), Expression.Int(n))), 0, null);
//		} catch (PrismLangException e) {
//		}
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
		StateRequest.StateFloat stateFloat = stateToProtobuf(exploreState);
		socket_req.sendMore("exploreState");
		socket_req.send(stateFloat.toByteArray());
		socket_req.recv(); //discard acknowledge message

	}

	private StateRequest.StateFloat stateToProtobuf(State exploreState) {
		StateRequest.StateFloat stateFloat = StateRequest.StateFloat.getDefaultInstance();
		for (int i = 0; i < exploreState.varValues.length; i++) {
			stateFloat.getValueList().add((double) (exploreState.varValues[i])); //todo cast to double, needs to accommodate arbitrary type
		}
		return stateFloat;
	}

	@Override
	public int getNumChoices() throws PrismException {
		socket_req.send("getNumChoices");
		final String recv = socket_req.recvStr();
		return Integer.parseInt(recv);
	}

	@Override
	public int getNumTransitions(int i) throws PrismException {
		socket_req.sendMore("getNumTransitions");
		socket_req.send(String.valueOf(i));
		final String recv = socket_req.recvStr();
		return Integer.parseInt(recv);
	}

	@Override
	public Object getTransitionAction(int i, int offset) throws PrismException {
		// No action labels in this model
		return null;
	}

	@Override
	public double getTransitionProbability(int i, int offset) throws PrismException {
		socket_req.sendMore("getTransitionProbability");
		socket_req.send(String.valueOf(i));
		socket_req.send(String.valueOf(offset));
		final String recv = socket_req.recvStr();
		return Double.parseDouble(recv);
	}

	@Override
	public State computeTransitionTarget(int i, int offset) throws PrismException {
		socket_req.sendMore("computeTransitionTarget");
		socket_req.send(String.valueOf(i));
		socket_req.send(String.valueOf(offset));
		final byte[] recv = socket_req.recv();//receives next state
		return parseState(recv);
	}

	@Override
	public boolean isLabelTrue(int i) throws PrismException {
		socket_req.sendMore("computeTransitionTarget");
		socket_req.send(String.valueOf(i));
		final String recv = socket_req.recvStr();
		return Boolean.parseBoolean(recv);
	}

	///parse a state from protobuf
	private State parseState(byte[] recv) {
		State new_state = new State(getVarNames().size());
		try {
			StateRequest.StateFloat stateFloat = StateRequest.StateFloat.parseFrom(recv);
			for (int j = 0; j < stateFloat.getValueCount(); j++) {
				new_state.setValue(j, stateFloat.getValue(j));
			}
			return new_state;
		} catch (InvalidProtocolBufferException e) {
			e.printStackTrace();
		}
		return new_state;
	}
	// Methods for RewardGenerator interface (reward info stored separately from ModelInfo/ModelGenerator)

	// There is a single reward structure, r, which just assigns reward 1 to every state.
	// We can use this to reason about the expected number of steps that occur through the random walk.


	@Override
	public List<String> getRewardStructNames() {
		if (this._rewardStructNames == null) {
			socket_req.send("getRewardStructNames");
			final byte[] recv = socket_req.recv();
			try {
				StateRequest.StringVarNames varNames = StateRequest.StringVarNames.parseFrom(recv);
				final ProtocolStringList valueList = varNames.getValueList();
				this._rewardStructNames = valueList;
				return valueList;
			} catch (InvalidProtocolBufferException e) {
				e.printStackTrace();
			}
		} else {
			return this._rewardStructNames;
		}
		return null;
	}

	@Override
	public double getStateReward(int r, State state) throws PrismException {
		// r will only ever be 0 (because there is one reward structure)
		// We assume it assigns 1 to all states.

		StateRequest.StateFloat stateFloat = stateToProtobuf(state);
		socket_req.sendMore("getStateReward");
		socket_req.send(stateFloat.toByteArray());
		final String recv = socket_req.recvStr();
		return Double.parseDouble(recv);
	}

	@Override
	public double getStateActionReward(int r, State state, Object action) throws PrismException {
		// No action rewards
		StateRequest.StateFloat stateFloat = stateToProtobuf(state);
		socket_req.sendMore("getStateReward");
		socket_req.sendMore(stateFloat.toByteArray());
		socket_req.send(action.toString());
		final String recv = socket_req.recvStr();
		return Double.parseDouble(recv);
	}
}

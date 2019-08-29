package server;

public class TestPythonWrapper {
	public static void main(String[] args) {
		TestPythonWrapper testPythonWrapper = new TestPythonWrapper();
		testPythonWrapper.testConnection();
	}

	public void testConnection() {
		PythonWrapper server = new PythonWrapper("tcp://localhost:5558");
		System.out.println(server.getLabelNames());
		System.out.println(server.getVarNames());
	}
}

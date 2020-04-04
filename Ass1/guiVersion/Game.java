package guiVersion;

public class Game {

	public static void main(String[] args) {
		int threads = 3;
		int steps = 500;
		int balls = 100;
		SimulationViewer v = new SimulationViewer(620,620);
		new BouncingBalls(threads,balls,steps,v).run();

	}

}

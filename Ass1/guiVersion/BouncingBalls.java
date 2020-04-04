package guiVersion;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BouncingBalls {

	private int nWorkers;
	private int nBalls;
	private int nStep;
	private SharedContext context;
	private SimulationViewer view;

	public BouncingBalls(final int threads, final int balls, final int steps, SimulationViewer v) {
		nWorkers = threads;
		nBalls = balls;
		nStep = steps;
		context = SharedContext.getIstance();
		view = v;
	}

	public void run() {
		int j = 1;

		double vt = 0;
		double dt = 0.1;

		// Two indexes used to split balls between threads
		int perThread;
		int tmp = 0;
		List<Body> balls = generateBalls(nBalls);
		// A shared context with which threads will coordinate
		context.setBallList(balls);
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers; i++) {
			perThread = context.getBallsPerThread();
			workers.add(new Worker("Worker-" + i, nStep, context, tmp, tmp += perThread));
		}

		for (Worker w : workers) {
			w.start();
		}
		while (j++ <= nStep) {
			context.hitBarrier();
			vt = vt + dt;
			view.display(new ArrayList<Body>(balls), 0.1, j);
		}

	}

	private static List<Body> generateBalls(final int n) {
		Boundary bounds = new Boundary(-1.0, -1.0, 1.0, 1.0);
		Random rand = new Random(System.currentTimeMillis());
		ArrayList<Body> bodies = new ArrayList<Body>();
		for (int i = 0; i < n; i++) {
			double x = bounds.getX0() + rand.nextDouble() * (bounds.getX1() - bounds.getX0());
			double y = bounds.getX0() + rand.nextDouble() * (bounds.getX1() - bounds.getX0());
			double dx = -1 + rand.nextDouble() * 2;
			double speed = rand.nextDouble() * 0.05;
			Body b = new Body(new Position(x, y), new Velocity(dx * speed, Math.sqrt(1 - dx * dx) * speed), 0.01);
			bodies.add(b);
		}
		return bodies;
	}
}

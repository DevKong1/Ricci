package noGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBalls {

	public static void main(String []args) {

		int nWorkers = 3;
		int nBalls = 100;
		int nStep = 500;
		SharedContext context = SharedContext.getIstance();
		// Two indexes used to split balls between threads
		int perThread;
		int tmp = 0;
		List<Body> balls = generateBalls(nBalls,context.getBounds());
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
		
	}

	private static List<Body> generateBalls(final int n,Boundary bound) {
		final Boundary bounds = bound ;
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

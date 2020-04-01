package src.seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBalls {

	public static void main(String[] args) {

		int nWorkers = 3;
		int nBalls = 100;
		//Two indexes used to split balls between threads
		int perThread;
		int tmp = 0;
		//A shared context with which threads will coordinate
		SharedContext context = SharedContext.getIstance();
		context.setBallList(generateBalls(nBalls));
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers; i++) {
			perThread = context.getBallsPerThread();
			workers.add(new Worker("Worker-" + i, context.NSTEPS, context, context.getBallList().subList(tmp, tmp+=perThread)));
		}

		for (Worker w : workers) {
			w.start();
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

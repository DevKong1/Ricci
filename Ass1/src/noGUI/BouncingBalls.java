package noGUI;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBalls {

	public static void main(String[] args) {
		/**
		 * Input variables
		 */
		int nBalls = 5000;
		int nStep = 500;

		/**
		 * Computational variables
		 */
		SharedContext context = SharedContext.getIstance();
		int perThread;
		// Two indexes used to split balls between threads
		int tmp = 0;
		int counter = 0;
		//Numbers of thread to be used
		int nWorkers = SharedContext.getWorkers();
		List<Body> balls = generateBalls(nBalls, SharedContext.getBounds());
		// A shared context with which threads will coordinate
		context.setBallList(balls);
		List<Worker> workers = new ArrayList<Worker>();
		
		for (int i = 0; i < nWorkers; i++) {
			perThread = context.getBallsPerThread();
			workers.add(new Worker("Worker-" + i, context, tmp, tmp += perThread));
		}

		long c = System.currentTimeMillis(); 
		
		if (nStep > 0) {
			for (Worker w : workers) {
				w.start();
			}
			while (!context.getStop()) {
				context.hitBarrier();
				if (counter++ == nStep) {
					context.setStop(true);
				}
				context.hitBarrier();
			}
		}
		
		for(Worker b : workers){ 
			try { 
				b.join(); 
			} catch (InterruptedException e) { 
				e.printStackTrace(); 
			} 
		}
		
		long d = System.currentTimeMillis(); 
		System.out.println("Time: "+(d-c)); 	
	}

	private static List<Body> generateBalls(final int n, Boundary bound) {
		final Boundary bounds = bound;
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

package src.seq;

import java.util.List;
import java.util.Random;

public class Worker extends Thread {

	private SharedContext context;
	private final int nSteps;
	List<Body> threadBalls;
	double vt = 0;
	double dt = 0.1;

	/*
	 * name : thread 
	 * name context: shared context 
	 * threadBalls: balls assigned to this thread 
	 * allballs: every ball in the game
	 */
	public Worker(final String name, final int nSteps, final SharedContext context, final List<Body> threadBalls) {
		super(name);
		this.nSteps = nSteps;
		this.context = context;
		this.threadBalls = threadBalls;
	}

	public void run() {
		int i = 0;
		while(i < nSteps) {
			threadBalls.stream().forEach(x -> x.updatePos(dt));
			// System.out.println()
			context.waitNonConcurrentCalc();
			System.out.println("Thread-" + this.getName() + " ThreadBallSize:" + threadBalls.size());
			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(context.getBounds()));
			context.waitNonConcurrentCalc();
			i++;
		}

	}

	private void log(String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}

	private void waitFor(long ms) throws InterruptedException {
		Thread.sleep(ms);
	}
}

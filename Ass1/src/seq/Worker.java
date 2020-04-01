package src.seq;

import java.util.List;
import java.util.Random;

public class Worker extends Thread {

	private SharedContext context;
	List<Body> threadBalls;
	private int start;
	private int lastIndex;
	double vt = 0;
	double dt = 0.1;

	/*
	 * name : thread 
	 * name context: shared context 
	 * threadBalls: balls assigned to this thread 
	 * allballs: every ball in the game
	 */
	
	public Worker(final String name, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.context = context;		
		this.start = start;
		this.lastIndex = lastlIndex;
		this.threadBalls = context.getBallList().subList(start, lastIndex);
	}

	public void run() {
		threadBalls.stream().forEach(x -> x.updatePos(dt));
		// System.out.println()
		context.waitNonConcurrentCalc();
		System.out.println("Thread-" + this.getName() + " ThreadBallSize:" + threadBalls.size());
		threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(context.getBounds()));
		context.waitNonConcurrentCalc();

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

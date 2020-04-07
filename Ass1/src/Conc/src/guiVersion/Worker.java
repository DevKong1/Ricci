package guiVersion;

import java.util.ArrayList;
import java.util.List;


public class Worker extends Thread {

	private final SharedContext context;
	private List<Body> threadBalls;
	private final int start;
	private final int n;
	private final int lastIndex;
	private final double dt = 0.1;

	/*
	 * name : thread name context: shared context threadBalls: balls assigned to
	 * this thread allballs: every ball in the game
	 */
	public Worker(final String name, final int n, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.context = context;
		this.n = n;
		this.start = start;
		this.lastIndex = lastlIndex;
		this.threadBalls = new ArrayList<Body>(context.getBallList().subList(start, lastIndex));
	}

	public void run() {
		while (!context.getStop()) {
			// DEBUG TODO delete
			context.getMatrix().reset();

			threadBalls.forEach(x -> x.updatePos(dt));
			checkAndSolveInternalCollisions();
			context.lockUpdateSem();

			updateGlobalList();
			context.releaseUpdateSem();

			context.waitNonConcurrentCalc();

			log("EXTERNAL COLLISIONS");

			checkAndSolveExternalCollisions();

			context.waitNonConcurrentCalc();
			
			context.lockUpdateSem();
			updateGlobalList();
			context.releaseUpdateSem();

			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(SharedContext.getBounds()));
			context.lockUpdateSem();
			updateGlobalList();
			context.releaseUpdateSem();
			
			context.waitNonConcurrentCalc();
			context.hitBarrier();
		}
	}

	// Checks if there are any collisions between balls handled by a SINGLE
	// thread, if so, it solves them.
	private void checkAndSolveInternalCollisions() {
		for (int k = 0; k < threadBalls.size() - 1; k++) {
			Body b1 = threadBalls.get(k);
			for (int j = k + 1; j < threadBalls.size(); j++) {
				Body b2 = threadBalls.get(j);
				if (b1.collideWith(b2)) {
					Body.solveCollision(b1, b2);
				}
			}
		}
	}

	private void updateGlobalList() {
		for (int m = 0; m < threadBalls.size(); m++) {
			int k = start;
			context.updateBallList(threadBalls.get(m), k++);
		}
	}

	private void checkAndSolveExternalCollisions() {
		int cycles = context.getWorkers() / 2;
		boolean isOdd = context.getWorkers() % 2 != 0;
		
		log("WaitTicket...");
		context.getTicketAndWait();
		log("Got ticket");
		
		List<Body> tmp = context.getBallList();
		//the number of iterations required is the number of threads/2
		for(int i = 1; i <= cycles; i++) {			
			//for each ball of this thread
			for(int j = start; j < lastIndex; j++) {
				context.lockBall(j);
				Body b1 = tmp.get(j);
				//if the number of cycles is even and its the last iteration the threads may try to do the same check
				if (!isOdd && i == cycles) {
					if(!context.getMatrix().checkAndSet(n, (n+i) % context.getWorkers())) {
						checkAndSolve(b1,i,tmp);
					}
				} else {
					checkAndSolve(b1,i,tmp);					
				}
				context.releaseBall(j);
			}
			
			
			log(n + " FINISHED CYCLE " + i);
		}

		context.releaseTicket();
		log("Released ticket");
	}
	
	private void checkAndSolve(Body b1, int i, List<Body> tmp) {
		int ballsToSkip = context.getBallsPerThread() * i;
		int last = start + ballsToSkip >= tmp.size() ? ballsToSkip - 1 + (tmp.size() % context.getWorkers()): lastIndex + ballsToSkip - 1;
		int first =  start + ballsToSkip >= tmp.size() ? last - context.getBallsPerThread() + 1 - (tmp.size() % context.getWorkers()) : last - context.getBallsPerThread() + 1;

		for(; last >= first; last--) {
			context.lockBall(last);
			Body b2 = tmp.get(last);
			if (b1.collideWith(b2)) {
				Body.solveCollision(b1, b2);
				log(last + " Velocity:" + b1.getVel().getX() + "  ---- " + b1.getVel().getY() + "  ---- " + b2.getVel().getX() + "  ---- " + b2.getVel().getY());	   
			}
			context.releaseBall(last);
		}
	}

	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
}

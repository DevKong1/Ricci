package guiVersion;

import java.util.ArrayList;
import java.util.List;

public class Worker extends Thread {

	private final SharedContext context;
	private List<Body> threadBalls;
	private final int start;
	private final int lastIndex;
	private final double dt = 0.1;

	/*
	 * name : thread name context: shared context threadBalls: balls assigned to
	 * this thread allballs: every ball in the game
	 */
	public Worker(final String name, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.context = context;
		this.start = start;
		this.lastIndex = lastlIndex;
		this.threadBalls = new ArrayList<Body>(context.getBallList().subList(start, lastIndex));
	}

	public void run() {
		while (!context.getStop()) {
			// DEBUG TODO delete
			// context.resetPrint();

			threadBalls.forEach(x -> x.updatePos(dt));
			checkAndSolveInternalCollisions();
			context.lockUpdateSem();

			updateGlobalList();
			context.releaseUpdateSem();

			context.waitNonConcurrentCalc();

			log("EXTERNAL COLLISIONS");

			checkAndSolveExternalCollisions();

			context.waitNonConcurrentCalc();

			// System.out.println()
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
					// if a collision is detected the bodies are locked and the
					// context updated
					// log("two balls are colliding at X:"+b1.getPos().getX()+",
					// y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+",
					// y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX()
					// +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX()
					// +"y:"+b2.getVel().getY()+"\n");
					Body.solveCollision(b1, b2);
					// log(k + " Local Velocity:" + b1.getVel().getX() + " ----
					// " + b1.getVel().getY());
					// log(j + " Local Velocity:" + b2.getVel().getX() + " ----
					// " + b2.getVel().getY());
					// log("two balls are colliding at X:"+b1.getPos().getX()+",
					// y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+",
					// y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX()
					// +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX()
					// +"y:"+b2.getVel().getY()+"\n");

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

		// get ticket to avoid deadlock
		log("WaitTicket...");
		context.getTicketAndWait();
		log("Got ticket");

		rightCheck();

		context.releaseTicket();
		log("Released ticket");
	}

	private void rightCheck() {
		int size = threadBalls.size();
		int limit = context.getBallList().size();
		int pos = Integer.parseInt(getName().substring(getName().length()-1)) +1 ;
		int maxInternalRange = threadBalls.size()*pos;
		int maxExternalRange;
		int i = start;
		int k;
		int j = 0;
		
		if (lastIndex >= context.getBallList().size()) {
			k = 1;
			maxExternalRange = size-1;
		} else {
			k = lastIndex + 1;
			 maxExternalRange = lastIndex+size;
		}

		while (j++ < SharedContext.getWorkers()-1) {

			for (i=start; i < maxInternalRange; i++) {
				context.waitNonConcurrentCalc();
				context.lockBall(i);
				Body b1 = context.getBallList().get(i);
				context.waitNonConcurrentCalc();
				for (; k < maxExternalRange ; k++) {
					context.lockBall(k);
					Body b2 = context.getBallList().get(k);
					if (b1.collideWith(b2)) {
						Body.solveCollision(b1, b2);
					}
					context.releaseBall(k);
					log("Locked my ball "+i +" and checked collision with "+k );
				}
				context.releaseBall(i);

			}
			k = maxExternalRange+1;		
			maxExternalRange = (maxExternalRange + size) >= limit+1? size : maxExternalRange + size;
			if(j == SharedContext.getWorkers() -3){
				log("HELLOOO");
			}
		}

	}

	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
}

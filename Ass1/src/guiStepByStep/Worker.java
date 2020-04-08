package src.guiStepByStep;

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
			context.hitBarrier();

			threadBalls.forEach(x -> x.updatePos(dt));
			checkAndSolveInternalCollisions();
			context.lockUpdateSem();

			updateGlobalList();
			context.releaseUpdateSem();

			context.waitNonConcurrentCalc();

			log("EXTERNAL COLLISIONS");

			checkAndSolveExternalCollisions();

			//context.waitNonConcurrentCalc();
			//threadBalls = new ArrayList<Body>(context.getBallList().subList(start, lastIndex));

			// System.out.println()
			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(SharedContext.getBounds()));
			
			context.lockUpdateSem();
			updateGlobalList();
			context.releaseUpdateSem();
			
			context.waitNonConcurrentCalc();
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
		/*List<Body> tmp = context.getBallList();
	    for (int i = 0; i < threadBalls.size() - 1; i++) {
	    	Body b1 = tmp.get(i);
	        for (int j = i + 1; j < tmp.size(); j++) {
	        	Body b2 = tmp.get(j);
	            if (b1.collideWith(b2)) {
	            	Body.solveCollision(b1, b2);
	            	//System.out.println(i + " Local Velocity:" + b1.getVel().getX() + "  ---- " + b1.getVel().getY());	   
	            	//System.out.println(j + " Local Velocity:" + b2.getVel().getX() + "  ---- " + b2.getVel().getY());	
	            }
	        }
        }
	    threadBalls = tmp.subList(start, lastIndex);*/
		
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
		int tmp1;
		int tmp2;
		int i = start;
		int k;
		int j = 0;
		
		if (lastIndex >= context.getBallList().size()) {
			k = 1;
			maxExternalRange = size;
		} else {
			k = lastIndex + 1;
			 maxExternalRange = lastIndex+size;
		}
		tmp1=k;
		tmp2=start;
		while (j++ < SharedContext.getWorkers()-1) {

			for (; i < maxInternalRange; i++) {
				context.waitNonConcurrentCalc();
				context.lockBall(i);
				Body b1 = context.getBallList().get(i);
				for (; k < maxExternalRange ; k++) {
					context.waitNonConcurrentCalc();
					context.lockBall(k);
					Body b2 = context.getBallList().get(k);
					if (b1.collideWith(b2)) {
						Body.solveCollision(b1, b2);
					}
					context.releaseBall(k);
					log("Locked my ball "+i +" and checked collision with "+k );
				}
				context.releaseBall(i);
				
				if (tmp1+size-1 > context.getBallList().size()) {
					k = 1;
					i = start;
				} else {
					k = tmp1 +1;
					tmp1++;
				}
				if(i == 2 || i == 252 || i == 502 || i == 752){
					log("helo");
				}

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

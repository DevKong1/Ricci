package src.guiVersion;

import java.util.ArrayList;
import java.util.List;


public class Worker extends Thread {

	private final SharedContext context;
	private List<Body> threadBalls;
	private final int start;
	private final int lastIndex;
	private final double dt = 0.1;

	/*
	 * name : thread name 
	 * context: shared context 
	 * start: index of the first ball assigned to the thread (included)
	 * lastIndex: index of the last ball assigned to the thread (not included)
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

			//each cycle move the balls according to its speed
			updateInternalPos();
			context.waitNonConcurrentCalc();			
			
			//check and solve collisions assigned to the thread
			checkAndSolveCollisions();
			context.waitNonConcurrentCalc();

			//check and solve boundary collisions
			solveBoundaryCollision();
			context.waitNonConcurrentCalc();
			context.hitBarrier();
		}
	}
	
	//method to move each assigned ball accordingly to its velocity
	private void updateInternalPos() {
		
		for(int i = 0; i < threadBalls.size(); i++) {
			threadBalls.get(i).updatePos(dt);
		}
		
		context.lockUpdateSem();
		updateGlobalList();
		context.releaseUpdateSem();
	}
	
	//method to check and solve collisions relative to balls assigned to the thread
	private void checkAndSolveCollisions() {
		
		//instantiate a copy of the global ball list
		List<Body> tmp = new ArrayList<>();
		context.lockUpdateSem();
    	tmp = new ArrayList<>(context.getBallList());
    	context.releaseUpdateSem();
    	
    	//for each assigned ball
	    for (int i = start; i < lastIndex - 1; i++) {	    	
	    	Body b1 = new Body(tmp.get(i));
	    	
	    	//for each subsequent ball
	        for (int j = i+1; j < tmp.size(); j++) {        	
	        	Body b2 = new Body(tmp.get(j));
	        	
	            if (b1.collideWith(b2)) {            	
	            	Body.solveCollision(b1, b2);
	            	threadBalls.set(i-start, b1);
	            	context.lockUpdateSem();
	            	updateAfterCollision(b1, b2, i, j);
	            	context.releaseUpdateSem();
	            }
	        }
        }
	}
	
	//update global ball list
	private void updateAfterCollision(Body b1, Body b2, int i, int j) {
		context.updateBallList(b1, i);
		context.updateBallList(b2, j);
	}
	
	//check and directly update local collisions
	private void solveBoundaryCollision() {
		for(int i = 0; i < threadBalls.size(); i++) {
			threadBalls.get(i).checkAndSolveBoundaryCollision(SharedContext.getBounds());
		}
		context.lockUpdateSem();
		updateGlobalList();
		context.releaseUpdateSem();
	}

	//set the global list to match our local
	private void updateGlobalList() {
		int k = start;
		for (int m = 0; m < threadBalls.size(); m++) {
			context.updateBallList(threadBalls.get(m), k++);
		}
	}
}

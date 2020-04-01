package src.seq;

import java.util.List;

public class Worker extends Thread {

	private final SharedContext context;
	private final int nSteps;
	List<Body> threadBalls;
	private final int start;
	private final int lastIndex;
	private final double vt = 0;
	private final double dt = 0.1;

	/*
	 * name : thread 
	 * name context: shared context 
	 * threadBalls: balls assigned to this thread 
	 * allballs: every ball in the game
	 */
	public Worker(final String name, final int nSteps, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.nSteps = nSteps;
		this.context = context;		
		this.start = start;
		this.lastIndex = lastlIndex;
		this.threadBalls = context.getBallList().subList(start, lastIndex);
	}

	public void run() {
		int i = 0;
		while(i++ < nSteps) {
			context.lockUpdateSem();
			// DEBUG : Old x position print
			/*log("I'm thread " +this.getName()+" Before Updating Pos");
			int z = 0;
			for(Body b : context.getBallList()){
				
				System.out.print(i++ + " " +b.getPos().getX()+" - ");
			}
			z=0;
			System.out.println("\n");  */
			context.getBallList().subList(start, lastIndex).forEach(x -> x.updatePos(dt));
			//DEBUG :  new x positions
			/*log("Before Updating Pos");
			for(Body b : context.getBallList()){
				
				System.out.print(i++ + " " +b.getPos().getX()+" - ");
			}
			z=0;
			System.out.println("\n");  */
			context.releaseUpdateSem();		
			context.waitNonConcurrentCalc();
			
			context.lockUpdateSem();		
			checkAndSolveInternalCollisions();
			context.releaseUpdateSem();	
			context.waitNonConcurrentCalc();

			
			// System.out.println()
			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(context.getBounds()));
			context.waitNonConcurrentCalc();
		}

	}
	
	private void checkAndSolveInternalCollisions(){
		for (int k = start; k < lastIndex - 1; k++) {
	    	Body b1 = context.getBallList().get(k);
	        for (int j = k + 1; j < lastIndex; j++) {
	        	Body b2 = context.getBallList().get(j);
	            if (b1.collideWith(b2)) {		       
	            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
	            	Body.solveCollision(b1, b2);		    
	            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
	            }
	        }
        }
	}
	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
	private void waitFor(long ms) throws InterruptedException {
		Thread.sleep(ms);
	}
}

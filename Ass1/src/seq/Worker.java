package src.seq;

import java.util.List;

public class Worker extends Thread {

	private SharedContext context;
	private final int nSteps;
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
			/*System.out.println("I'm thread " +this.getName()+" Before Updating Pos");
			int k = 0;
			for(Body b : context.getBallList()){
				
				System.out.print(i++ + " " +b.getPos().getX()+" - ");
			}
			i=0;
			System.out.println("\n");  */
			context.getBallList().subList(start, lastIndex).forEach(x -> x.updatePos(dt));
			//DEBUG :  new x positions
			/*System.out.println("I'm thread " +this.getName()+" Before Updating Pos");
			for(Body b : context.getBallList()){
				
				System.out.print(i++ + " " +b.getPos().getX()+" - ");
			}
			k=0;
			System.out.println("\n");  */
			context.releaseUpdateSem();		
			context.waitNonConcurrentCalc();
			// System.out.println()
			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(context.getBounds()));
			context.waitNonConcurrentCalc();
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

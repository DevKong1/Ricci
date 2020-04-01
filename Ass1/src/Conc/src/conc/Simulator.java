package conc;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;

public class Simulator {
	/* get number of available cores */
	private final int nCore = Runtime.getRuntime().availableProcessors();
	
	/* bodies in the field */ 
	ArrayList<Body> bodies;
	
	/* boundary of the field */
	private Boundary bounds;
	
	/* Nsteps */
	private long nSteps; 
	
	/* barrier to synchronize threads */
	CyclicBarrier barrier;
	
	public Simulator(long nSteps){
		this.nSteps = nSteps;
		
		/* initializing boundary and bodies */
		
	    bounds = new Boundary(-1.0,-1.0,1.0,1.0);
		
		Random rand = new Random(System.currentTimeMillis());
	    bodies = new ArrayList<Body>();
	    for (int i = 0; i < 100; i++) {
	        double x = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
	        double y = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
	        double dx = -1 + rand.nextDouble()*2;
	        double speed = rand.nextDouble()*0.05;
	        Body b = new Body(new Position(x, y), new Velocity(dx*speed,Math.sqrt(1 - dx*dx)*speed), 0.01);
	        bodies.add(b);
	    }
	}
	
	public void execute() {
		// number of balls
		int nWorkers = bodies.size();
		barrier = new CyclicBarrier(nWorkers);
		//variables used to split the balls into different threads
		int counter = 0;
		List<Body> consideredBodies;
		
		for(int i = 0; i < nCore; i++) {			
			consideredBodies = new ArrayList<Body>();	
			
			//each core gets an amount of balls to track
			for (int j = 0; j < nWorkers / (nCore + 1); i++) {
				consideredBodies.add(bodies.get(counter++));
			}
	
			for (Worker w: workers) {
				w.start();
			}
		}
	}
}

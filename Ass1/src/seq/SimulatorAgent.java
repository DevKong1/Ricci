package src.seq;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class SimulatorAgent extends Thread{

	private CyclicBarrier barier;
	private Boundary bounds;
	ArrayList<Body> bodies;
	private final int nSteps;
	
	public SimulatorAgent(final int nBodies, final int nSteps, final int nThreads) {
		this.bounds = new Boundary(-1.0,-1.0,1.0,1.0);
		this.nSteps = nSteps;
		this.barier = new CyclicBarrier(nThreads);
		this.createBodies(nBodies);
	}
	
	public void run() {        
        double dt = 0.1;
		int step = 0;
		while (step < nSteps) {
			try {
				while(updatePositions(dt)) {
					try {
						System.out.println("Ciao pagliaccio");
						barier.await();
						System.out.println("Sono entrato");
					} catch (InterruptedException e) {
	                	e.printStackTrace();
	                } catch (BrokenBarrierException br) {
	                	br.printStackTrace();
	                }
				}
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
	}
	
	private void createBodies(final int nBodies) {
		Random rand = new Random(System.currentTimeMillis());
		bodies = new ArrayList<Body>();
        for (int i = 0; i < nBodies; i++) {
            double x = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double y = bounds.getX0() + rand.nextDouble()*(bounds.getX1() - bounds.getX0());
            double dx = -1 + rand.nextDouble()*2;
            double speed = rand.nextDouble()*0.05;
            Body b = new Body(new Position(x, y), new Velocity(dx*speed,Math.sqrt(1 - dx*dx)*speed), 0.01);
            bodies.add(b);
        }
	}
	
	private boolean updatePositions(final double dt) {
		for (Body b: bodies) {
    		b.updatePos(dt);
	    }
		return true;
	}
}

package src.seq;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	private static final double X0 = -1.0;
	private static final double Y0 = -1.0;
	private static final double X1 = 1.0;
	private static final double Y1 = 1.0;
	private static final int SEMAPHORE_PERMITS = 1;
	//Used to divide balls correctly between threads
	private boolean isOdd;
	//Number of threads available
	private static final int THREADS = 3;//Runtime.getRuntime().availableProcessors() + 1 ;
	private List<Body> balls;
	private CyclicBarrier barrier;
	private Semaphore updateSemaphore;
	
	private TicketSemaphore ticketSemaphore;
	
	private Vector<Semaphore> collisionSemaphore;
	private Boundary bounds;
	
	private SharedCollisionsMatrix matrix; //matrix to check if a collision has already been solved
	
	// Private constructor for Singleton
	private SharedContext() {
		barrier = new CyclicBarrier(THREADS);
		bounds = new Boundary(X0,Y0,X1,Y1);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
		matrix = new SharedCollisionsMatrix();
		ticketSemaphore = new TicketSemaphore(THREADS);
	}

	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}


	// lets thread synchronize on a cyclic barrier:
	// First time when every thread calculated new balls positions'
	// Second time when every thread checks collision with boundaries
	public void waitNonConcurrentCalc() {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
	
	public void lockUpdateSem(){
		try {
			updateSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	public void releaseUpdateSem(){
		updateSemaphore.release();
	}
	
	public void getTicketAndWait() {
		while(!ticketSemaphore.tryEnqueue()) {
			ticketSemaphore.lockTicket();
		}
	}	
	
	public void releaseTicket() {
		ticketSemaphore.releaseTicket();
	}	
	
	// Returns ballList
	public List<Body> getBallList(){
		return this.balls;
	}
	// Creates the array of bodies
	public void setBallList(final List<Body> balls) {
		this.balls = balls;
		matrix.init(balls.size());
		initCollisonVector();
		
		if(balls.size() % THREADS  != 0) {
			isOdd = true;
		}
	}
	
	private void initCollisonVector() {
		collisionSemaphore = new Vector<Semaphore>(balls.size());
		
		for(int i = 0; i < balls.size(); i++) {
			collisionSemaphore.add(new Semaphore(SEMAPHORE_PERMITS));
		}
	}
	
	//Lock 2 balls
	public void lockBall(int b1){
		try {
			collisionSemaphore.get(b1).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	//Release 2 balls
	public void releaseBall(int b1){
		collisionSemaphore.get(b1).release();
	}
	
	//Returns map boundaries
	public Boundary getBounds(){
		return this.bounds;
	}
	
	//Returns collision Matrix
	public SharedCollisionsMatrix getMatrix(){
		return this.matrix;
	}
	
	public void updateBallList(Body b,int index){
		balls.set(index, b);
	}
	//Returns how many balls should a SINGLE thread handle.
	public int getBallsPerThread(){
		if(isOdd){
			isOdd = false;
			return (balls.size() / THREADS) + (balls.size() % THREADS);
		}
		return balls.size() / THREADS;
	}
	
	//TESTING METHOD
	public void printVel(int index) {
		System.out.println(index + " Global Velocity: " + balls.get(index).getVel().getX() + "  ---- " + balls.get(index).getVel().getY());
	}

}

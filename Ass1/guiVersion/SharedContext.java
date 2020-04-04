package guiVersion;

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
	private static final double dt = 0.1;
	private static final int SEMAPHORE_PERMITS = 1;
	private static final int THREADS = 3;//Runtime.getRuntime().availableProcessors() + 1 ;
	//Used to divide balls correctly between threads
	private boolean isOdd;
	//Number of threads available
	private List<Body> balls;
	private CyclicBarrier barrier;
	private Semaphore updateSemaphore;
	private CyclicBarrier guiSemaphore;
	
	private TicketSemaphore ticketSemaphore;
	
	private Vector<Semaphore> collisionSemaphore;
	private Boundary bounds;
	
	private SharedCollisionsMatrix matrix; //matrix to check if a collision has already been solved

	//boolean used to updatepositions only once each step
	private Boolean canUpdate = true;
	
	//TESING VARIABLE TODO DELETE
	private Boolean printreset = true;
	
	// Private constructor for Singleton
	private SharedContext() {
		barrier = new CyclicBarrier(THREADS);
		bounds = new Boundary(X0,Y0,X1,Y1);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
		matrix = new SharedCollisionsMatrix();
		ticketSemaphore = new TicketSemaphore(THREADS);
		guiSemaphore = new CyclicBarrier(THREADS+1);
	}

	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}

	/** 
	 * LOCK & UNLOCK METHODS, used access items in shared context concurrently.
	 */
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
		ticketSemaphore.lockTicket();
	}	
	
	public void releaseTicket() {
		ticketSemaphore.releaseTicket();
	}	
	
	//Lock 2 balls
	public void lockBall(final int b1){
		try {
			collisionSemaphore.get(b1).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	//Release 2 balls
	public void releaseBall(final int b1){
		collisionSemaphore.get(b1).release();
	}
	public void hitBarrier(){
		try {
			this.guiSemaphore.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Getter & Setter
	 *
	 */
	// Returns ballList
	public List<Body> getBallList(){
		return this.balls;
	}
	// Creates the array of bodies
	public void setBallList(final List<Body> balls) {
		this.balls = new ArrayList<Body>(balls);
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
	
	//Returns map boundaries
	public Boundary getBounds(){
		return this.bounds;
	}
	
	//Returns collision Matrix
	public SharedCollisionsMatrix getMatrix(){
		return this.matrix;
	}
	
	public void updateBallList(final Body b,final int index){
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
	public static int getWorkers(){
		return THREADS;
	}
	
	/**
	 * Update Positions methods
	 *
	 */
	public void updatePositions(){
		if(!canUpdate) {
			return;
		}
		
		for (Body b: balls) {
    		b.updatePos(dt);
	    }	
		canUpdate = false;
	}
	
	public void resetUpdate(){
		canUpdate = true;
	}
	
	/**
	 * Testing methods
	 * 
	 */
	//TESTING METHOD
	public void printVel(final int index) {
		Body considered = balls.get(index);
		System.out.println(index + "Position: "+ considered.getPos().getX()+ "-" + considered.getPos().getY() +" Global Velocity: " + balls.get(index).getVel().getX() + "  ---- " + balls.get(index).getVel().getY());
	}
	
	public void printMatrix(){
		if(!printreset) {
			return;
		}
		
		for(int i = 0; i < balls.size(); i++) {
			printVel(i);
		}		
		printreset = false;
	}
	
	public void resetPrint(){
		printreset = true;
	}

}

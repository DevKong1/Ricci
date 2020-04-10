package noGUI;

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
	private static final int THREADS = 4 ;
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	
	//Used to divide balls correctly between threads
	private boolean isOdd;
	private boolean stop;
	//Number of threads available
	private List<Body> balls;
	private CyclicBarrier barrier;
	private CyclicBarrier controlSemaphore;
	private Semaphore updateSemaphore;	
	private Vector<Semaphore> collisionSemaphore;
	
	
	// Private constructor for Singleton
	private SharedContext() {
		stop=false;
		barrier = new CyclicBarrier(THREADS);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
		controlSemaphore = new CyclicBarrier(THREADS+1);
	}

	/** 
	 * Synchronizing methods, used access items in shared context concurrently.
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
	// Used to update shared variables
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
	
	//Locks a balls
	public void lockBall(final int b1){
		try {
			collisionSemaphore.get(b1).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	//Releases a balls
	public void releaseBall(final int b1){
		collisionSemaphore.get(b1).release();
	}
	/*
	 * Used to let main thread and worker threads synchronize
	 */
	public void hitBarrier(){
		try {
			this.controlSemaphore.await();
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
	// Sets the array of shared bodies
	public void setBallList(final List<Body> balls) {
		this.balls = new ArrayList<Body>(balls);
		initCollisonVector();
		
		if(balls.size() % THREADS  != 0) {
			isOdd = true;
		}
	}
	//Used to update global ball list
	public void updateBallList(final Body b,final int index){
		balls.set(index, b);
	}
	//Returns how many balls should a SINGLE thread handle.
	//If the number is odd, one thread gets more ball than the others
	public int getBallsPerThread(){
		if(isOdd){
			isOdd = false;
			return (balls.size() / THREADS) + (balls.size() % THREADS);
		}
		return balls.size() / THREADS;
	}
	//Used to let workers know when calculation should be over and they should terminate
	public boolean getStop(){
		return stop;
	}
	//Used to let workers know when calculation is over and they should terminate
	public void setStop(boolean val){
		stop = val;
	}
	/**
	 * private methods
	 */
	private void initCollisonVector() {
		collisionSemaphore = new Vector<Semaphore>(balls.size());
		
		for(int i = 0; i < balls.size(); i++) {
			collisionSemaphore.add(new Semaphore(SEMAPHORE_PERMITS));
		}
	}
	/**
	 * Static methods
	 * 
	 */
	//Returns map boundaries
	public static Boundary getBounds(){
		return BOUNDS;
	}
	//Returns number of threads to be used.
	public static int getWorkers(){
		return THREADS;
	}
	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}

}

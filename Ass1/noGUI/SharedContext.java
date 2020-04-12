package noGUI;

import java.util.ArrayList;
import java.util.List;
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
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	private static final int MAIN_THREAD = 1;

	private static int THREADS;
	
	//Used to divide balls correctly between threads
	private boolean isOdd;
	private boolean stop;
	//Number of threads available
	private List<Body> balls;
	private CyclicBarrier barrier;
	private CyclicBarrier controlSemaphore;
	private Semaphore updateSemaphore;
	
	
	// Private constructor for Singleton
	private SharedContext() {
	}
	
	public void init(final List<Body> balls) {		
		// To avoid program to be sequential
		if(Runtime.getRuntime().availableProcessors() < 3) {
			THREADS= 3;
		} else {
			THREADS = Runtime.getRuntime().availableProcessors();
		}
		
		setBallList(balls);
		if(THREADS > getBallList().size()) {
			THREADS = getBallList().size();
		}

		stop=false;
		barrier = new CyclicBarrier(THREADS);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
		controlSemaphore = new CyclicBarrier(THREADS+MAIN_THREAD);
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

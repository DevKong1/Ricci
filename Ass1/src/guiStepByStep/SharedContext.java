package src.guiStepByStep;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public final class SharedContext {

	//Shared Variable between Workers
	private static final SharedContext SINGLETON = new SharedContext();
	private static final double X0 = -1.0;
	private static final double Y0 = -1.0;
	private static final double X1 = 1.0;
	private static final double Y1 = 1.0;
	private static final int SEMAPHORE_PERMITS = 1;
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	
	//Number of Workers
	private static int THREADS;
	
	private boolean stop = false;
	//Used to divide balls correctly between threads
	private boolean isOdd;
	//Number of threads available
	private List<Body> balls;
	private CyclicBarrier barrier;
	private CyclicBarrier guiSemaphore;
	private Semaphore updateSemaphore;
	
	
	// Private constructor for Singleton
	private SharedContext() {
		THREADS = Runtime.getRuntime().availableProcessors() + 1 >= 3 ? Runtime.getRuntime().availableProcessors() +1 : 3 ;
		barrier = new CyclicBarrier(THREADS);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
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
	
	//Method for lock Semaphore between Workers 
	public void lockUpdateSem(){
		try {
			updateSemaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	//Method for release Semaphore between Workers 
	public void releaseUpdateSem(){
		updateSemaphore.release();
	}
	
	//Method for wait GUI
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
		
		if(balls.size() % THREADS  != 0) {
			isOdd = true;
		}else{
			isOdd = false;
		}
	}
	
	//Returns map boundaries
	public static Boundary getBounds(){
		return BOUNDS;
	}
	
	//Update global list
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
	
	//Return number of thread workers
	public static int getWorkers(){
		return THREADS;
	}
	
	//Set stop simulation
	public void setStop(final boolean val){
		stop = val;
	}
	
	//Return value of stop
	public boolean getStop(){
		return stop;
	}

}

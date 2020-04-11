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
	private static final int SEMAPHORE_PERMITS = 1;
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	private static final int GUI_THREAD = 1 ;
	
	private static int THREADS;
	
	private boolean isEnded = false;
	
	private boolean stop = false;
	//Used to divide balls correctly between threads
	private boolean isOdd;
	//Number of threads available
	private List<Body> balls;
	private final CyclicBarrier barrier;
	private final Semaphore updateSemaphore;
	private final CyclicBarrier guiSemaphore;
	
	
	private Vector<Semaphore> collisionSemaphore;
	
	// Private constructor for Singleton
	private SharedContext() {
		THREADS = Runtime.getRuntime().availableProcessors() + 1;
		barrier = new CyclicBarrier(THREADS);
		updateSemaphore = new Semaphore(SEMAPHORE_PERMITS);
		guiSemaphore = new CyclicBarrier(THREADS+GUI_THREAD);
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
	
	//Lock a ball
	public void lockBall(final int b1){
		try {
			collisionSemaphore.get(b1).acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	//Releases a ball
	public void releaseBall(final int b1){
		collisionSemaphore.get(b1).release();
	}
	/*
	 * Used to let main thread and worker threads synchronize
	 */
	public void hitBarrier(){
		try {
			this.guiSemaphore.await();
		} catch (InterruptedException | BrokenBarrierException e) {
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
		}else{
			isOdd = false;
		}
	}
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
	
	public void setStop(final boolean val){
		this.stop = val;
	}
	public boolean getStop(){
		return stop;
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
	 * static methods
	 */
	//Return numbers of thread
	public static int getWorkers(){
		return THREADS;
	}

	// returns Singleton instance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}
	
	//Returns map boundaries
	public static Boundary getBounds(){
		return BOUNDS;
	}
	
	public boolean isEnded() {
		return isEnded;
	}
	
	public void setEnd(boolean b) {
		isEnded = b;
	}

}

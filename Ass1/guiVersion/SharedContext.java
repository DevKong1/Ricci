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
	private static final int THREADS = 4;//Runtime.getRuntime().availableProcessors() + 1 ;
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	
	
	private boolean stop = false;
	//Used to divide balls correctly between threads
	private boolean isOdd;
	//Number of threads available
	private List<Body> balls;
	private CyclicBarrier barrier;
	private Semaphore updateSemaphore;
	private CyclicBarrier guiSemaphore;
	
	
	private Vector<Semaphore> collisionSemaphore;

	
	//TESING VARIABLE TODO DELETE
	private Boolean printreset = true;
	
	// Private constructor for Singleton
	private SharedContext() {
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
		initCollisonVector();
		
		if(balls.size() % THREADS  != 0) {
			isOdd = true;
		}else{
			isOdd = false;
		}
	}

	
	private void initCollisonVector() {
		collisionSemaphore = new Vector<Semaphore>(balls.size());
		
		for(int i = 0; i < balls.size(); i++) {
			collisionSemaphore.add(new Semaphore(SEMAPHORE_PERMITS));
		}
	}
	
	//Returns map boundaries
	public static Boundary getBounds(){
		return BOUNDS;
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
	public void setStop(final boolean val){
		this.stop = val;
	}
	public boolean getStop(){
		return stop;
	}

}

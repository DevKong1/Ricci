package src.seq;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	
	//Used to divide balls correctly between threads
	private boolean isOdd = true;
	//Number of threads available
	private static final int THREADS = 3;//Runtime.getRuntime().availableProcessors() + 1 ;
	private List<Body> balls;
	private CyclicBarrier barrier;
	private List<Semaphore> collisionSemaphore;
	private Boundary bounds;
	
	// Private constructor for Singleton
	private SharedContext() {
		barrier = new CyclicBarrier(THREADS);
		bounds = new Boundary(-1.0,-1.0,1.0,1.0);
	}

	// returns Singleton istance
	public static SharedContext getIstance() {
		return SharedContext.SINGLETON;
	}


	// lets thread synchronize on a ciclyc barrier:
	// First time when every thread calculated new balls positions'
	// Second time when every thread checks collision with boundaries
	public void waitNonConcurrentCalc() {
		try {
			barrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
	
	//TODO Understand how collision should be implemented concurrently
	public void waitCollisionMutex(final int ball1, final int ball2){
		while(!collisionSemaphore.get(ball1).tryAcquire() && !collisionSemaphore.get(ball2).tryAcquire()){
			try {
				collisionSemaphore.get(ball1).acquire();
				collisionSemaphore.get(ball2).acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	// Returns ballList
	public List<Body> getBallList(){
		return this.balls;
	}
	// Creates the array of bodies
	public void setBallList(final List<Body> balls) {
		this.balls = balls;
		collisionSemaphore = new ArrayList<Semaphore>(balls.size());
	}
	//Returns map boundaries
	public Boundary getBounds(){
		return this.bounds;
	}
	
	//Returns how many balls should a SINGLE thread handle.
	public int getBallsPerThread(){
		if(balls.size() % THREADS  != 0 && isOdd){
			isOdd = false;
			return (balls.size() / THREADS) + (balls.size() % THREADS);
		}
		return balls.size() / THREADS;
	}
}

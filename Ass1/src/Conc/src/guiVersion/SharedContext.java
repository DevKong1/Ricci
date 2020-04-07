package guiVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Semaphore;

public final class SharedContext {

	private static final SharedContext SINGLETON = new SharedContext();
	private static final double X0 = -1.0;
	private static final double Y0 = -1.0;
	private static final double X1 = 1.0;
	private static final double Y1 = 1.0;
	private static final int THREADS = 13;//Runtime.getRuntime().availableProcessors() + 1 ;
	private static final Boundary BOUNDS = new Boundary(X0,Y0,X1,Y1);
	

	private List<Body> balls;
	private Vector<Pair<Integer, Integer>> buffer = new Vector<Pair<Integer, Integer>>();
	private Semaphore nAvailItems = new Semaphore(THREADS);
	private Semaphore consumedSem = new Semaphore(1);
	private boolean stop = false;
	
	// Private constructor for Singleton
	private SharedContext() {
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
	}
	
	//Returns map boundaries
	public static Boundary getBounds(){
		return BOUNDS;
	}
	
	
	public void updateBallList(final Body b,final int index){
		balls.set(index, b);
	}
	
	public static int getWorkers(){
		return THREADS;
	}
	
	public Vector<Pair<Integer, Integer>> getBuffer(){
		synchronized(buffer) {
			return this.buffer;
		}
	}
	
	public void setStop(final boolean val){
		stop = val;
	}
	
	public boolean getStop(){
		return stop;
	}

	public void addCollision(int i, int j) {
		synchronized(buffer) {
			buffer.add(new Pair<Integer, Integer>(i,j));
		}
		signalAvailItem();
	}
	
	private void signalAvailItem() {
		nAvailItems.notify();
	}
	
	public void waitAvailable() {
		try {
			nAvailItems.wait();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void waitEmptyBuffer() {
		try {
			while(!buffer.isEmpty()) {
				consumedSem.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public Pair<Integer, Integer> consumeItem() {
		synchronized(buffer) {
			Pair<Integer, Integer> result = buffer.firstElement();
			buffer.remove(0);
			return result;
		}
	}

}

package guiVersion;

import java.util.Vector;
import java.util.concurrent.Semaphore;

public class SharedCollisionsMatrix {

	private Vector<Vector<Boolean>> matrix;
	private static final int WORKERS = SharedContext.getWorkers();
	private final Semaphore sem = new Semaphore(1);
	
	public SharedCollisionsMatrix() {	
	}
	
	public void init(final int size) {
		reset();
	}
	
	public synchronized void reset() {
        int innerSize = 1;
        matrix = new Vector<Vector<Boolean>>();
        for (int i = 0; i < WORKERS; i++) {
            Vector<Boolean> tmp = new Vector<>();
            for(int k = 0 ; k < innerSize; k++){
                    tmp.add(false);
            }
            matrix.add(tmp);
            innerSize++;
        }

    }
	
	
	public synchronized Boolean checkAndSet(int th1, int th2) {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Since the matrix is triangular, "biggest" item needs to be first
		if(th2 > th1){
			int tmp = th1;
			th1 = th2;
			th2 = tmp;
		}
		
		Boolean r1 = matrix.get(th1).get(th2);
		
		if(!r1) {
			matrix.get(th1).set(th2, true);
		} 
		
		sem.release();
		
		return r1;
	}
}

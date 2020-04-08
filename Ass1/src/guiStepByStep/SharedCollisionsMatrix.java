package src.guiStepByStep;

import java.util.Vector;
import java.util.concurrent.Semaphore;

public class SharedCollisionsMatrix {

	private Vector<Vector<Boolean>> matrix;
	private int size;
	private static final int WORKERS = SharedContext.getWorkers();
	private final Semaphore sem = new Semaphore(1);
	
	public SharedCollisionsMatrix() {	
	}
	
	public void init(final int size) {
		this.size = size;
		reset();
	}
	
	public synchronized void reset() {
		int innerSize = 1;
		matrix = new Vector<Vector<Boolean>>();
		for (int i = 0; i < size; i++) {
			Vector<Boolean> tmp = new Vector<>();
			for(int k = 0 ; k < innerSize; k++){
					tmp.add(false);
			}
			matrix.add(tmp);	
			innerSize++;
		}	   
	}
	public void printMatrix(){
		for (int i = 0; i < matrix.size(); i++) {
		    for (int j = 0; j < matrix.get(i).size(); j++) {
		        System.out.print(matrix.get(i).get(j) + " ");
		    }
		    System.out.println();
		}
	}
	
	
	public synchronized Boolean checkAndSet(int ball1, int ball2) {
		try {
			sem.acquire();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//Since the matrix is triangular, "biggest" item needs to be first
		if(ball2 > ball1){
			int tmp = ball1;
			ball1 = ball2;
			ball2 = tmp;
		}
		
		Boolean r1 = matrix.get(ball1).get(ball2);
		
		if(!r1) {
			matrix.get(ball1).set(ball2, true);
		} 
		
		sem.release();
		
		return r1;
	}
}

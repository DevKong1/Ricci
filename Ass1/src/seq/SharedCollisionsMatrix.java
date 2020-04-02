package src.seq;

import java.util.Vector;

public class SharedCollisionsMatrix {

	private Vector<Vector<Boolean>> matrix;
	private boolean available;
	private static final int WORKERS = 3;
	
	public SharedCollisionsMatrix() {
		available = false;
	}
	
	public synchronized void init(int size) {
		available = true;
		reset(size);
		notifyAll();
	}
	
	public synchronized void reset(int size) {
		 int alreadyCheked = 0;
		 int innerSize = size-1;
		matrix = new Vector<Vector<Boolean>>();
		for (int i = 0; i < size; i++) {
			Vector<Boolean> tmp = new Vector<>();
			for(int k = size ; k > innerSize; k--){
				if(k <= innerSize + alreadyCheked+1){
					tmp.add(true);
				}else{
					tmp.add(false);
				}
			}
			if(alreadyCheked >= WORKERS){
				alreadyCheked = 0;
			}else{
				alreadyCheked = alreadyCheked +1;
			}
			innerSize--;
			matrix.add(tmp);			
		}	   
	}
	
	public synchronized Boolean checkAndSet(int ball1, int ball2) {
		while (!available){
			try {
				wait();
			} catch (InterruptedException ex){}
		}
		//Since the matrix is triangular, biggest item needs to be first
		if(ball2 > ball1){
			int tmp = ball1;
			ball1 = ball2;
			ball2 = tmp;
		}
		
		Boolean r1 = matrix.get(ball1).get(ball2);
		
		if(!r1) {
				matrix.get(ball1).set(ball2, true);
		} 
		
		notifyAll();
		return r1;
	}
}

package src.seq;

import java.util.Vector;

public class SharedCollisionsMatrix {

	private Vector<Vector<Boolean>> matrix;
	private boolean available;
	
	public SharedCollisionsMatrix() {
		available = false;
	}
	
	public synchronized void init(int size) {
		available = true;
		reset(size);
		notifyAll();  
	}
	
	public synchronized void reset(int size) {
		matrix= new Vector<Vector<Boolean>>();	
		for(int i=0;i<size;i++) {
	        Vector<Boolean> r=new Vector<>();
	        for(int j=0;j<size;j++){
	            r.add(false);
	        }
	        matrix.add(r);
	    }	    
	}
	
	public synchronized Boolean checkAndSet(int ball1, int ball2) {
		while (!available){
			try {
				wait();
			} catch (InterruptedException ex){}
		}
		
		Boolean r1 = matrix.get(ball1).get(ball2);
		Boolean r2 = matrix.get(ball2).get(ball1);
		Boolean result = r1 || r2;
		
		if(!r1) {
			matrix.get(ball1).set(ball2, true);
		} 
		
		if(!r2) {
			matrix.get(ball2).set(ball1, true);			
		}
		
		return result;
	}
}

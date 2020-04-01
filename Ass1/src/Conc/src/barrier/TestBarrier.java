package barrier;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;

public class TestBarrier {

	public static void main(String[] args) {
		
		int nWorkers = 10;
		
		CyclicBarrier barrier = new CyclicBarrier(nWorkers);
		
		List<Worker> workers = new ArrayList<Worker>();
		for (int i = 0; i < nWorkers; i++) {
			workers.add(new Worker("Worker-"+i, barrier));
		}

		for (Worker w: workers) {
			w.start();
		}
		
	}
}

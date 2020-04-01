package conc;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Worker extends Thread {

	private CyclicBarrier barrier;
	
	public Worker(String name, CyclicBarrier barrier) {
		super(name);
		this.barrier = barrier;
	}
	
	public void run() {
		for(int i = 0;i < 3;i++) {
			Random gen = new Random(System.nanoTime());
			try {
				waitFor(gen.nextInt(3000));
				log("before");
				barrier.await();
				log("after");
				
			} catch (InterruptedException ex) {
				log("Interrupted!");
			} catch (BrokenBarrierException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void log(String msg) {
		synchronized(System.out) {
			System.out.println("[ "+getName()+" ] "+msg);
		}
	}
	
	private void waitFor(long ms) throws InterruptedException{
		Thread.sleep(ms);
	}
}

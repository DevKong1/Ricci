package src.seq;

import java.util.concurrent.Semaphore;

public class TicketSemaphore {

	private static final int SEMAPHORE_PERMITS = 1;
	private Semaphore ticket;
	private int queued;
	private final int max;
	
	public TicketSemaphore(int m) {
		ticket = new Semaphore(SEMAPHORE_PERMITS);
		queued = 0;
		max = m;
	}
	
	public Boolean tryEnqueue() {
		if(queued >= max - 1) {
			return false;
		}
		queued++;
		return true;
	}
	
	public void lockTicket(){
		try {
			ticket.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public void releaseTicket(){
		ticket.release();
		queued--;
	}
}

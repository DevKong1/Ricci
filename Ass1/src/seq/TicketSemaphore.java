package src.seq;

import java.util.concurrent.Semaphore;

public class TicketSemaphore {

	private Semaphore ticket;
	
	public TicketSemaphore(int m) {
		ticket = new Semaphore(m - 1);
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
	}
}

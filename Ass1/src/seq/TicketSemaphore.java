package src.seq;

import java.util.concurrent.Semaphore;

public class TicketSemaphore {

	private static final int SEMAPHORE_PERMITS = 1;
	private Semaphore ticket;
	private int turn;
	private int last;
	
	public TicketSemaphore() {
		ticket = new Semaphore(SEMAPHORE_PERMITS);
		reset();
	}
	
	public void reset() {
		turn = 0;
		last = 0;
	}
	
	public synchronized int getTicket() {
		return last++;
	}
	
	public synchronized int getTurn() {
		return turn;
	}
	
	public synchronized Semaphore getSemaphore() {
		return ticket;
	}
	
	
	public synchronized void lockUpdateSem(){
		try {
			ticket.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public synchronized void releaseUpdateSem(){
		ticket.release();
		turn++;
		ticket.notifyAll();
	}
}

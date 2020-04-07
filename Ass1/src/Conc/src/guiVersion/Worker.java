package guiVersion;

import java.util.ArrayList;
import java.util.List;


public class Worker extends Thread {

	private final SharedContext context;

	/*
	 * name : thread name context: shared context threadBalls: balls assigned to
	 * this thread allballs: every ball in the game
	 */
	public Worker(final String name, final int n, final SharedContext context) {
		super(name);
		this.context = context;
	}

	public void run() {
		while (!context.getStop()) {
			while(context.getBuffer().isEmpty()) {
				context.waitAvailable();
			}
			Pair<Integer, Integer> consumed = context.consumeItem();
			
		}
	}

	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
}

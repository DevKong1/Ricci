package guiVersion;

public class Producer extends Thread {

	private final SharedContext context;
	private final double dt = 0.1;

	public Producer(final String name, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.context = context;
	}

	public void run() {
		while (!context.getStop()) {			
			findCollisions();	
			context.waitEmptyBuffer();
			solveBoundaryCollisions();
		}
	}
	
	private void solveBoundaryCollisions() {
		for (Body b: context.getBallList()) {
	    	b.checkAndSolveBoundaryCollision(context.getBounds());
	    }
	}

	private void findCollisions() {
		 for (int i = 0; i < context.getBallList().size() - 1; i++) {
		    	Body b1 = context.getBallList().get(i);
		        for (int j = i + 1; j < context.getBallList().size(); j++) {
		        	Body b2 = context.getBallList().get(j);
		            if (b1.collideWith(b2)) {
		            	context.addCollision(i,j);
		            }
		        }
	        }
	}
	
	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
}

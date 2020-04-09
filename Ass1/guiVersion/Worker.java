package guiVersion;

import java.util.ArrayList;
import java.util.List;

import guiVersion.Body;
import guiVersion.SharedContext;

public class Worker extends Thread {

	private final SharedContext context;
	private List<Body> threadBalls;
	private final int start;
	private final int lastIndex;
	private final double dt = 0.1;

	/*
	 * name : thread 
	 * name context: shared context 
	 * threadBalls: balls assigned to this thread 
	 * allballs: every ball in the game
	 */
	public Worker(final String name, final SharedContext context, final int start, final int lastlIndex) {
		super(name);
		this.context = context;		
		this.start = start;
		this.lastIndex = lastlIndex;
		this.threadBalls =  new ArrayList<Body>(context.getBallList().subList(start, lastIndex));
	}

	public void run() {
		while(!context.getStop()) {
			//DEBUG TODO delete
			//log(""+i);
			//context.resetPrint();
			
			threadBalls.forEach(x -> x.updatePos(dt));
			checkAndSolveInternalCollisions();
			context.lockUpdateSem();
			// DEBUG : Old x position print
			/*log("I'm thread " +this.getName()+" Before Updating Pos");
			int z = 0;
			for(Body b : context.getBallList()){
				
				System.out.print(z++ + " " +b.getPos().getX()+" - ");
			}
			z=0;		
			System.out.println("\n");  */
			updateGlobalList();
			//DEBUG :  new x positions
			/*log("After Updating Pos");
			for(Body b : context.getBallList()){
				
				System.out.print(z++ + " " +b.getPos().getX()+" - ");
			}
			z=0;
			System.out.println("\n");  */
			context.releaseUpdateSem();	
			
			context.waitNonConcurrentCalc();	
			
			//log("EXTERNAL COLLISIONS");
			
			checkAndSolveExternalCollisions();
			
			context.waitNonConcurrentCalc();
		
			// System.out.println()
			threadBalls.stream().forEach(x -> x.checkAndSolveBoundaryCollision(SharedContext.getBounds()));
			
			context.lockUpdateSem();
			updateGlobalList();
			context.releaseUpdateSem();	
			
			context.waitNonConcurrentCalc();
			context.hitBarrier();
			context.hitBarrier();
		}		
	}
	
	//Checks if there are any collisions between balls handled by a SINGLE thread, if so, it solves them.
	private void checkAndSolveInternalCollisions(){
		for (int k = 0; k < threadBalls.size()  - 1; k++) {
	    	Body b1 = threadBalls.get(k);
	        for (int j = k + 1; j < threadBalls.size(); j++) {
	        	Body b2 = threadBalls.get(j);
	            if (b1.collideWith(b2)) {	
	            	//if a collision is detected the bodies are locked and the context updated    
	            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
	            	Body.solveCollision(b1, b2);		    
	            	//log(k + " Local Velocity:" + b1.getVel().getX() + "  ---- " + b1.getVel().getY());
	            	//log(j + " Local Velocity:" + b2.getVel().getX() + "  ---- " + b2.getVel().getY());
	            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
	            	
	            }
	        }
        }
	}
	
	private void updateGlobalList(){
		for(int m = 0; m<threadBalls.size();m++){
			int k = start;
			context.updateBallList(threadBalls.get(m), k++);
		}
	}
	private void checkAndSolveExternalCollisions(){
		
		//get ticket to avoid deadlock
		//log("WaitTicket...");
    	context.getTicketAndWait();
		//log("Got ticket");
		
    	rightCheck();
    	
    	context.releaseTicket();
		//log("Released ticket");
	}

	private void rightCheck() {	
		for (int k = start; k < lastIndex; k++) {
			//lock on first ball to check
			//log("Getting " + k + " ...");
			context.lockBall(k);
			//log("Got " + k );
			
	    	Body b1 = context.getBallList().get(k);
	    	
	        for (int j = lastIndex+1; j < context.getBallList().size(); j++) {
        		//lock on second ball to check
    			//log("Getting " + j + " ...");
    			context.lockBall(j);
    			//log("Got " + j);
    			
        		//check if the collision has already been solved
	        	//if(!context.getMatrix().checkAndSet(k, j)) {
		        	Body b2 = context.getBallList().get(j);
		        	
		            if (b1.collideWith(b2)) {		
		            	
		            	//if a collision is detected the bodies are locked and the context updated
		    			//context.lockBalls(k, j);	   		          
		            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
		            	
		            	Body.solveCollision(b1, b2);	
		            	//log(k + " Local Velocity:" + b1.getVel().getX() + "  ---- " + b1.getVel().getY());	   
		            	//log(j + " Local Velocity:" + b2.getVel().getX() + "  ---- " + b2.getVel().getY());	    
		            	//context.printVel(k);   
		            	//context.printVel(j);
		            	
		            	//log("two balls are colliding at X:"+b1.getPos().getX()+", y="+b1.getPos().getY() +" Y:"+b2.getPos().getX()+", y="+b2.getPos().getY()+" VEL1: x:"+b1.getVel().getX() +"y:"+b1.getVel().getY()+" VEL2: x:"+b2.getVel().getX() +"y:"+b2.getVel().getY()+"\n");
		            }
	            //}
	        	
    			//log("Releasing  " + j);
    			context.releaseBall(j);		          			
	        }
	        
			//log("Releasing  " + k);
	        context.releaseBall(k);
        }
		
		
	}
	private void log(final String msg) {
		synchronized (System.out) {
			System.out.println("[ " + getName() + " ] " + msg);
		}
	}
}

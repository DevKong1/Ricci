package src.noguiVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BouncingBalls {

	private int nStep;
	private SharedContext context;
	private int j = 1;
	private double vt = 0;
	private double dt = 0.01;
	private List<Worker> workers;

	public BouncingBalls(final int nBalls, final int nStep) {
		
		this.nStep = nStep;
		context = SharedContext.getIstance();
		context.setSteps(nStep);
		workers = new ArrayList<Worker>();
		
		// Two indexes used to split balls between threads	
		int perThread;
		int tmp = 0;
		List<Body> balls = generateBalls(nBalls);
		// A shared context with which threads will coordinate
		context.setBallList(balls);

		for (int i = 0; i < SharedContext.getWorkers(); i++) {
			perThread = context.getBallsPerThread();
			workers.add(new Worker("Worker-" + i, context, tmp, tmp += perThread));
		}
	}
	
	/**
     * Start the workers and wait for them to finish
     * @return
     */
	public void begin(){
		
		for (Worker b : workers) {
			b.start();
		}
		
		//each step update the time and display the balls
		while (j++ <= nStep) {
			context.hitBarrier();
			vt = vt + dt;
		}
		stop();
		
		for(Worker b : workers){
			try {
				b.join();
				System.out.println("joined");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
     * Stops the execution
     * @return
     */
	public void stop(){
		context.setStop(true);
	}
	
	private static List<Body> generateBalls(final int n) {
		Boundary bounds = new Boundary(-1.0, -1.0, 1.0, 1.0);
		Random rand = new Random(System.currentTimeMillis());
		ArrayList<Body> bodies = new ArrayList<Body>();
		for (int i = 0; i < n; i++) {
			double x = bounds.getX0() + rand.nextDouble() * (bounds.getX1() - bounds.getX0());
			double y = bounds.getX0() + rand.nextDouble() * (bounds.getX1() - bounds.getX0());
			double dx = -1 + rand.nextDouble() * 2;
			double speed = rand.nextDouble() * 0.05;
			Body b = new Body(new Position(x, y), new Velocity(dx * speed, Math.sqrt(1 - dx * dx) * speed), 0.01);
			bodies.add(b);
		}
		return bodies;
	}
}

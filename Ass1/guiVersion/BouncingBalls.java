package guiVersion;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class BouncingBalls {

	private int nStep;
	private SharedContext context;
	private SimulationViewer view;
	private int j = 1;
	private double vt = 0;
	private double dt = 0.1;
	private List<Worker> workers;
	private boolean end;

	public BouncingBalls(final int nBalls, final int nStep) {
		this.nStep = nStep;
		end = false;
		context = SharedContext.getIstance();
		workers = new ArrayList<Worker>();
		// Two indexes used to split balls between threads
		int perThread;
		int tmp = 0;
		List<Body> balls = generateBalls(nBalls);
		// A shared context with which threads will coordinate
		context.init(balls);
		
		for (int i = 0; i < SharedContext.getWorkers(); i++) {
			if(i < balls.size()) {
				perThread = context.getBallsPerThread();
				workers.add(new Worker("Worker-" + i, context, tmp, tmp += perThread));
			}
		}
	}
	//When this class is initiated by this method, it launches the GUI.
	public void init(){
		view = new SimulationViewer(620,620,this);
	}

	
	public void begin(){
		long c = System.currentTimeMillis();
		for (Worker b : workers) {
			b.start();
		}
		while (j++ < nStep) {
			context.hitBarrier();
			while(context.getStop()) {
				try {
					this.wait();
				} catch(Exception ex) {}
			}
			vt = vt + dt;
			view.display(new ArrayList<Body>(context.getBallList()), vt, j);
			context.hitBarrier();
		}
		this.end = true;
		end();
		long d = System.currentTimeMillis();
		System.out.println(""+(d-c));
		context.hitBarrier();
		context.hitBarrier();
		for(Worker b : workers){
			try {
				b.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	//Method called by the GUI to stop the computation
	public void stop(){
		context.setStop(true);
	}
	
	public void resume() {
		context.setStop(false);
	}
	
	public void end() {
		context.setEnd(true);
	}
	
	public boolean isEnded() {
		return this.end;
	}
	
	private List<Body> generateBalls(final int n) {
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

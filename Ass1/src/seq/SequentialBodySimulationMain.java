package src.seq;

/**
 * Bodies simulation - legacy code: sequential, unstructured
 * 
 * @author aricci
 */
public class SequentialBodySimulationMain {
	
	private static final int nBodies = 10;
	private static final int nSteps = 30;
	private static int nThreads;

    public static void main(String[] args) {
    	
    	if(Runtime.getRuntime().availableProcessors() +1 > 3) {
    		nThreads = Runtime.getRuntime().availableProcessors() + 1;
    	} else {
    		nThreads = 3;
    	}
    	
    	int i = 0;
    	while(i < nThreads-2) {
    		new SimulatorAgent(nBodies, nSteps, nThreads-2).start();
    		i++;
    	}
    	
    	/*SimulationViewer viewer = new SimulationViewer(620,620);
        
    	Simulator sim = new Simulator(viewer);
        sim.execute();*/

    }
}

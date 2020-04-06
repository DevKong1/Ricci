package guiVersion;

public class Game {
	
	private static SharedContext context;

	public static void main(String[] args) {
		new BouncingBalls(SharedContext.getWorkers(), SharedContext.getNballs(), SharedContext.getNsteps()).init();
	}

}

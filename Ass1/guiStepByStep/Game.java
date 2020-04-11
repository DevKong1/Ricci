package guiStepByStep;

public class Game {

	public static void main(String[] args) {
		final int balls = 1000;
		final int nSteps = 1000;
		new BouncingBalls(balls, nSteps).init();
	}

}

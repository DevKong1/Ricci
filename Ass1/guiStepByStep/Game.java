package guiStepByStep;

public class Game {

	public static void main(String[] args) {
		//int threads = 4;
		final int steps = 1000;
		final int balls = 1000;
		new BouncingBalls(balls,steps).init();
				

	}

}

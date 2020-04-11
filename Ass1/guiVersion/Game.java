package guiVersion;

public class Game {

	public static void main(String[] args) {
		//int threads = 4;
		int steps = 1000;
		int balls = 1000;
		new BouncingBalls(balls,steps).init();
	}

}

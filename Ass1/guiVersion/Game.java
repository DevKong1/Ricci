package guiVersion;

public class Game {

	public static void main(String[] args) {
		//int threads = 4;
		int steps = 5000;
		int balls = 5000;
		new BouncingBalls(balls,steps).init();
	}

}

package src.guiVersion;

public class Game {

	public static void main(String[] args) {
		//int threads = 4;
		int steps = 5000;
		int balls = 3000;
		new BouncingBalls(balls,steps).init();
				

	}

}
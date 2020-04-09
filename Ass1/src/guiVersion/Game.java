package src.guiVersion;

public class Game {

	public static void main(String[] args) {
		int steps = 500;
		int balls = 5000;
		new BouncingBalls(balls,steps).init();				
	}
}

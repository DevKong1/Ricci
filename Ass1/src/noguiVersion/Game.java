package src.noguiVersion;

public class Game {

	public static void main(String[] args) {
		int steps = 1000;
		int balls = 1000;
		new BouncingBalls(balls,steps).begin();			
	}
}

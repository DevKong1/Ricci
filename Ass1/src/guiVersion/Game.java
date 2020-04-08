package src.guiVersion;

//Main class of the project
public class Game {

	public static void main(String[] args) {
		int steps = 5000;
		int balls = 1000;
		new BouncingBalls(balls,steps).init();
	}

}

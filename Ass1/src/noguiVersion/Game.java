package src.noguiVersion;

public class Game {

	public static void main(String[] args) {
		int steps = 1000;
		int balls = 1000;
		long c = System.currentTimeMillis();
		new BouncingBalls(balls,steps).begin();		
		long d = System.currentTimeMillis();
		System.out.println(""+(d-c));
	}
}

package src.guiStepByStep;

public class Position { 

    private double x, y;

    public Position(final double x, final double y){
        this.x = x;
        this.y = y;
    }

    public void change(final double x, final double y){
    	this.x = x;
    	this.y = y;
    }
    
    public double getX() {
    	return x;
    }

    public double getY() {
    	return y;
    }
}

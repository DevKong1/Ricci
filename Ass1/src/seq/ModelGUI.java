package src.seq;

import java.util.ArrayList;
import java.util.List;

public class ModelGUI {

	private List<ModelObserver> observers;
	private int state;
	
	public ModelGUI(){
		state = 0;
		observers = new ArrayList<ModelObserver>();
	}
	
	public synchronized void update(){
		state++;
		notifyObservers();
	}
	
	public synchronized int getState(){
		return state;
	}
	
	public void addObserver(ModelObserver obs){
		observers.add(obs);
	}
	
	private void notifyObservers(){
		for (ModelObserver obs: observers){
			obs.modelUpdated(this);
		}
	}
}

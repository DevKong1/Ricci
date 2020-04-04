package src.seq;

public class AgentGUI extends Thread {

	private ModelGUI model;
	
	public AgentGUI(ModelGUI model){
		this.model = model;
	}
	
	public void run(){
		while (true){
			try {
				model.update();
				Thread.sleep(500);
			} catch (Exception ex){
			}
		}
	}
}

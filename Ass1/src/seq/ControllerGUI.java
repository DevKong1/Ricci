package src.seq;


public class ControllerGUI {
	
	private ModelGUI model;
	public ControllerGUI(ModelGUI model){
		this.model = model;
	}
	public void processEvent(String event) {
		try {
			new Thread(() -> {
				try {
					System.out.println("[Controller] Processing the event "+event+" ...");
					Thread.sleep(1000);
					model.update();
					System.out.println("[Controller] Processing the event done.");
				} catch (Exception ex){
					ex.printStackTrace();
				}
			}).start();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}

package guiStepByStep;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DecimalFormat;
import java.util.ArrayList;
import javax.swing.*;

/**
 * Simulation view
 * 
 * @author aricci
 *
 */
public class SimulationViewer extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private VisualiserPanel panel;
	private Container mainContainer;
	private JPanel componentPane;
	private JPanel buttonPane;
	private BouncingBalls game;
	private boolean begin;
	JButton step;
	JButton stop = new JButton("Stop");
	JLabel label1 = new JLabel();
	 private static DecimalFormat df2;
	/**
	 * Creates a view of the specified size (in pixels)
	 * 
	 * @param w
	 * @param h
	 */
	public SimulationViewer(int w, int h, BouncingBalls controller) {
		begin=false;
		this.game = controller;
		setTitle("Bodies Simulation");
		setSize(w, h);
		setResizable(false);
		df2 = new DecimalFormat("#.##");
		JFrame myFrame = new JFrame();
		myFrame.setSize(new Dimension(w, h + 75));
		myFrame.setLayout(new BorderLayout());

		panel = new VisualiserPanel(w, h);
		mainContainer = getContentPane();
		componentPane = new JPanel();
		buttonPane = new JPanel();
		mainContainer.setLayout(new BoxLayout(mainContainer, BoxLayout.Y_AXIS));
		componentPane.setLayout(new BoxLayout(componentPane, BoxLayout.X_AXIS));
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.Y_AXIS));
		buttonPane.setSize(componentPane.getSize());
		step = new JButton("Start");
		stop = new JButton("Stop");
		stop.setSize(step.getSize());
		label1 = new JLabel("Bodies: " + " - vt: " + " - nIter: ");
		label1.setAlignmentX(Component.TOP_ALIGNMENT);
		label1.setMaximumSize(new Dimension(200, 10));
		step.addActionListener(e -> {
			new Thread(() -> {
				if (!begin) {
					step.setText("Step");
					begin = true;
					game.begin();
				}else{
					game.step();
				}
			}).start();
		});
		stop.addActionListener(e -> {
        	new Thread(() ->{
            	game.stop();
        	}).start();
        });
		buttonPane.add(step);
		buttonPane.add(stop);
		componentPane.add(label1);
		componentPane.add(Box.createRigidArea(new Dimension(200, 0)));
		componentPane.add(buttonPane);
		mainContainer.add(componentPane);
		mainContainer.add(Box.createRigidArea(new Dimension(0, 10)));
		mainContainer.add(panel);
		myFrame.addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent ev) {
				System.exit(-1);
			}

			public void windowClosed(WindowEvent ev) {
				System.exit(-1);
			}
		});
		myFrame.getContentPane().add(mainContainer);
		myFrame.setVisible(true);
	}

	public void display(ArrayList<Body> bodies, double vt, long iter) {
		panel.display(bodies);
		label1.setText("Bodies: " + bodies.size() + " - vt: " + df2.format(vt) + " - nIter: " + iter);
	}

	public static class VisualiserPanel extends JPanel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private ArrayList<Body> bodies = new ArrayList<Body>();
		private long dx;
		private long dy;

		public VisualiserPanel(int w, int h) {
			setSize(w, h);
			dx = w / 2 - 20;
			dy = h / 2 - 20;
		}

		public void paint(Graphics g) {
			Graphics2D g2 = (Graphics2D) g;

			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
			g2.clearRect(0, 0, this.getWidth(), this.getHeight());

			bodies.forEach(b -> {
				Position p = b.getPos();
				double rad = b.getRadius();
				int x0 = (int) (dx + p.getX() * dx);
				int y0 = (int) (dy - p.getY() * dy);
				g2.drawOval(x0, y0, (int) (rad * dx * 2), (int) (rad * dy * 2));
			});
		}

		public void display(ArrayList<Body> bodies) {
			this.bodies = bodies;
			repaint();
		}
	}
}

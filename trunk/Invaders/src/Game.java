import javax.swing.*;
import java.awt.*;

import de.pxlab.pxl.*;
import java.awt.image.BufferStrategy;

public class Game extends JFrame implements Runnable {
	private static final long serialVersionUID = -2669437745998537325L;

	private final int WINDOW_WIDTH = 647;
	private final int WINDOW_HEIGHT = 480;

	private Thread updateThread;
	private Base base;
	private Insets insets;
	private Dimension dim;
	private Shield shield[];
	private Aliens aliens;
	
	private void createSprites()
	{
		int i;
		
		base = new Base(0, 0, 0, "base", 1, 0, 32, 22, dim);
		aliens = new Aliens( dim, insets);
		shield = new Shield[3];
		
		for(i=0; i<3; i++)
		{
			shield[i] = new Shield(100+(i*200)+insets.left, 375);
		}
	}
	
	public Game() {
		//
		// Create the window
		//
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setBackground(Color.BLACK);
		setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		setResizable(false);

		setVisible(true);
		createBufferStrategy(2);

		//
		// Size and insets not valid until visible.
		//
		dim = getSize();
		insets = getInsets();

		//
		// Things that are reset per game
		//
		createSprites();

		addKeyListener(base);
		
		//
		// Create a new thread to run the main game loop
		//
		updateThread = new Thread(this);
		updateThread.start();
	}

	public static void main(String[] args) {
		new Game();
		System.out.println("Hello World!");
	}

	@Override
	public void update(Graphics g) {
		super.update(g);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
	}

	@Override
	public void run() {
		int speed = 70;
		Score score;
		Lives lives;
		Font font;
		int i;
		int level = 0;
		BufferStrategy bf = getBufferStrategy();
		Graphics2D g = (Graphics2D) bf.getDrawGraphics();
				
		g.setColor(Color.GREEN);
		score = new Score(insets.left + 100, insets.top + 20);		
		lives = new Lives(400, insets.top + 20);

		while ((Thread.currentThread() == updateThread) &&
				   (lives.get() > 0)) {
			g.clearRect(insets.left, insets.top, dim.width - insets.left - insets.right, dim.height - insets.bottom - insets.top);

			base.reset();
			aliens.reset();
			
			if(speed >= 10)
				speed -= 10;
			aliens.setAlienSpeed(speed);
			
			for(i=0; i<3; i++)
			{
				shield[i].reset();
				shield[i].draw(g);
			}
			
			font = new Font("Verdana", Font.BOLD, 32);
			g.setFont(font);
			level++;
			g.drawString(String.format("Level %d", level), 250, 200);

			font = new Font("Verdana", Font.BOLD, 24);
			g.setFont(font);	
			score.draw(g);
			lives.draw(g);
			
			bf.show();
			
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			while ((Thread.currentThread() == updateThread) &&
				   (aliens.numAlive() != 0) &&
				   (lives.get() > 0)) {
	
				g.clearRect(insets.left, insets.top, dim.width - insets.left - insets.right, dim.height - insets.bottom - insets.top);
	
				base.updatePosition();
				aliens.updatePosition();
				aliens.updateMissiles();
	
				//
				// Collision checking
				// 
				score.add(base.collision(aliens));
				
				for(i=0; i<3; i++)
				{
					base.collision(shield[i]);
					aliens.collision(shield[i]);
					shield[i].draw(g);
				}
	
				if (aliens.collision(base))
				{
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					lives.decrement();
				}
				
				aliens.draw(g);
				base.draw(g);
				score.draw(g);
				lives.draw(g);
	
				// Wait for vertical retrace then update the screen
				VideoSync.waitForBeginOfVerticalBlank();
				// Make the buffer visible
				bf.show();
				// Force the buffer to the screen
				Toolkit.getDefaultToolkit().sync();
	/*			
				try {
					// Give other things time to do stuff.
					Thread.sleep(10);
				} catch (InterruptedException e) {
					break;
				}
	*/
			}
		}
		g.setColor(Color.RED);
		font = new Font("Verdana", Font.BOLD, 32);
		g.setFont(font);
		g.drawString("Game Over", 250, 200);
		bf.show();
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		g.dispose();
	}
}

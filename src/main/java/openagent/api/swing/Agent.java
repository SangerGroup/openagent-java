package openagent.api.swing;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.*;

import dorkbox.systemTray.SystemTray;
import openagent.api.AgentException;
import openagent.api.Command;
import openagent.api.FilenameImage;
import openagent.api.UncheckedAgentException;

import com.sun.speech.freetts.VoiceManager;

/**
 * The Agent class is the main class of the OpenAgent API.
 * When instantiated and shown, it creates a virtual character
 * onscreen. The character can then be commanded to speak, play
 * animations, and other things.<br><br>
 * 
 * --Creating your own character--<br>
 * To create your own working OpenAgent character, follow this
 * structure:<br>
 * idle.png (the image shown when no animations are playing, and
 * is reverted to this when the stop() method is called)<br>
 * animations/<br>
 * 	foo/<br>
 * 		0.png<br>
 * 		1.png<br>
 * 		2.png<br>
 * 	bar/<br>
 * 		0.png<br>
 * 		1.png<br>
 * 		2.png<br><br>
 * 
 * All images must be in PNG format, with the .png extension.
 * In the animation folders, each frame must be numbered from
 * 0.png to the end of the animation. (e.g. 0.png, 1.png, 2.png)<br>
 * 
 * To run animations, call animate() with the name of an
 * animation folder as an argument. (You can experiment with the optional
 * delay parameter to find the best speed for your animation.)<br>
 * 
 * @author githubcyclist
 * @version beta 1.1
*/
public class Agent {
	
	/*
	 * Declare fields
	*/
	
	private JWindow internalFrame = new JWindow();
	
	private Image idleImage = null;
	
	private Voice v;
	
	private JPopupMenu cm = new JPopupMenu();
	
	private int aI = 0;
	
	private boolean animated = false;
	
	private boolean usingTransparencyHack = false;
	
	private JLabel speechLabel = new JLabel();
	
	private JFrame bubbleWindow = new JFrame();
	
	private ImagePanel character;
	
	private String dataPath;
	
	private boolean animatingNow = false;
	
	private AnimationPriority priority = AnimationPriority.NOT_PLAYING;
	
	private String currentAnimation = "none";
	
	/**
	 * Creates a new {@code Agent} object,
	 * with the data path set to [current
	 * working directory]/character
	 * (which, in JAR files, will be the "character"
	 * folder in the same folder as the JAR file).
	 * @throws AgentException When the agent's idle image
	 * (placed under [data path]/idle.png) is not found.
	*/
	public Agent() throws AgentException {
		this(Paths.get(".").toAbsolutePath().normalize().toString()
				+ File.separator + "character", true);
	}
	
	/**
	 * Creates a new {@code Agent} object with TTS
	 * output enabled.
	 * @param dataPath - The path to the character data
	 * folder, including the folder name.
	 * @throws AgentException When the agent's idle image
	 * (placed under [data path]/idle.png) is not found.
	*/
	public Agent(String dataPath) throws AgentException {
		this(dataPath, true);
	}
	
	/**
	 * Creates a new {@code Agent} object.
	 * @param dataPath - The path to the character data folder.
	 * @param useTTS - Property that determines whether this
	 * {@code Agent} will use TTS for speech.
	 * @throws AgentException When the agent's idle image
	 * (placed under [data path]/idle.png) is not found.
	*/
	public Agent(String dataPath, boolean useTTS) throws AgentException {
		this.dataPath = dataPath;
		this.useTTS = useTTS;
		bubbleWindow.setUndecorated(true);
		bubbleWindow.setAlwaysOnTop(true);
		bubbleWindow.setSize(240, 145);
		
		bubbleWindow.add(speechLabel);
		
		internalFrame.setSize(150, 140);
		internalFrame.addMouseMotionListener(new MouseMotionListener() {
		    private int mx, my;

		    @Override
		    public void mouseMoved(MouseEvent e) {
		        mx = e.getXOnScreen();
		        my = e.getYOnScreen();
		    }

		    @Override
		    public void mouseDragged(MouseEvent e) {
		        Point p = internalFrame.getLocation();
		        p.x += e.getXOnScreen() - mx;
		        p.y += e.getYOnScreen() - my;
		        mx = e.getXOnScreen();
		        my = e.getYOnScreen();
		        internalFrame.setLocation(p);
		    }
		});
		internalFrame.setLayout(new BorderLayout());
		BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
		try {
			img = ImageIO.read(
					new File(dataPath + File.separator + "idle.png"));
			idleImage = img;
		} catch (IOException e1) {
			throw new AgentException("Couldn't find the agent's idle image."
					+ " Place it under <character path>/idle.png.");
		}
		character = new ImagePanel(img);
		//character.setBackground(new Color(0,0,0,0));
		//character.setOpaque(false);
		internalFrame.setName("OpenAgent");
		internalFrame.add(character, BorderLayout.CENTER);
		internalFrame.addMouseListener(new MouseAdapter() {
			
		    public void mousePressed(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseClicked(MouseEvent e) {
				checkPopup(e);
			}

			public void mouseReleased(MouseEvent e) {
				checkPopup(e);
			}

			private void checkPopup(MouseEvent e) {
			    if(e.isPopupTrigger()) {
			    	cm.show(internalFrame, e.getX(), e.getY());
				}
			}
			
		});
		//internalFrame.setBackground(new Color(0,0,0,0));
		//internalFrame.getContentPane().setBackground(new Color(0,0,0,0));
		internalFrame.setAlwaysOnTop(true);
		internalFrame.setSize(img.getWidth(), img.getHeight());
		SystemTray tray = SystemTray.get();
		tray.setImage(dataPath + File.separator + "tray.png");
		tray.setTooltip("OpenAgent");
		JMenuItem hideItem = new JMenuItem("Hide");
		hideItem.addActionListener((e) -> {
			this.hide();
		}); 
		this.getMenu().add(hideItem);
		this.getMenu().addSeparator();
		System.setProperty("FreeTTSSynthEngineCentral", "com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
        System.setProperty("freetts.voices", "com.sun.speech.freetts.en.us.cmu_us_kal.KevinVoiceDirectory");
        //Central.registerEngineCentral("com.sun.speech.freetts.jsapi.FreeTTSEngineCentral");
        if(useTTS) {
        	v = new Voice("kevin16");
        }
		new Thread(() -> {
			while(true) {
				if(!(this.supportsAnimation("breathe"))) Thread.currentThread().interrupt();
				if(!(animatingNow) && animated && canBreathe()) {
					this.animate("breathe", 100, AnimationPriority.LOW /* Everything interrupts breathing, except other breathing */);
				}
				try {
					Thread.sleep(3000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					throw new UncheckedAgentException("Breathing thread interrupted");
				}
			}
		}).start();
		String s = File.separator;
		String idlePath = dataPath + s + "animations" + s + "idle";
		File idleAnimationsFolder = new File(idlePath);
		ArrayList<String> possibleIdleAnimations = new ArrayList<>();
		for(File file : idleAnimationsFolder.listFiles()) {
			if(file.isDirectory() && file.getName().startsWith("idle_")) possibleIdleAnimations.add(file.getName());
		}
		File rareIdleAnimationsFolder = new File(idlePath + s + "rare");
		ArrayList<String> possibleRareIdleAnimations = new ArrayList<>();
		for(File file : rareIdleAnimationsFolder.listFiles()) {
			if(file.isDirectory() && file.getName().startsWith("rare_")) possibleRareIdleAnimations.add(file.getName());
		}
		new Thread(() -> {
			while(true) {
				try {
					Thread.sleep(new Random().nextInt(20000) + 10000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(!(this.supportsIdle())) Thread.currentThread().interrupt();
				if(animated && canPlayIdle()) {
					if(new Random().nextInt(20) + 1 == 10) {
						this.animate(possibleRareIdleAnimations.get(new Random().nextInt(possibleRareIdleAnimations.size())), 
								100, AnimationPriority.IDLE);
					} else {
						this.animate(possibleIdleAnimations.get(new Random().nextInt(possibleIdleAnimations.size())), 100, AnimationPriority.IDLE);
					}
				}
			}
		}).start();
	}
	
	private boolean canPlayIdle() {
		return !(priority.equals(AnimationPriority.NORMAL) || priority.equals(AnimationPriority.TOP));
	}
	
	private boolean canBreathe() {
		// Can only breathe when no animation is playing, not even breathing
		return priority.equals(AnimationPriority.NOT_PLAYING);
	}

	class ImagePanel extends JPanel {

	      private Image image;
	      private Image background;

	      ImagePanel(Image image) {
	          this.image = image;
	      }
	      
	      public void setImage(Image val) {
	    	  this.image = val;
	    	  internalFrame.revalidate();
	    	  this.repaint();
	      }
	      
	      public Image getImage() {
	    	  return image;
	      }

	      @Override
	      public void paintComponent(Graphics g) {
	          super.paintComponent(g);
	    	  g.drawImage(image,0,0,getWidth(),getHeight(),null);
	      }

	}
	
	private boolean alreadyStarted = false;
	
	public void setBackgroundColor(Color color) {
		this.internalFrame.setBackground(color);
		this.internalFrame.getContentPane().setBackground(color);
		this.character.setBackground(color);
	}
	
	public Color getBackgroundColor() {
		return internalFrame.getBackground();
	}
	
	public void setOpaque(boolean flag) {
		character.setOpaque(flag);
	}
	  
	/**
	 * Enables or disables a transparency hack
	 * which uses screenshots to work.<br><br>
	 * Enabling this property may degrade performance.<br>
	 * Note that this method was created for use under Linux,
	 * which has issues with Swing and transparency.
	 * This method will print a warning if used 
	 * @param flag - Value to set property to
	*/
	public void useTransparencyHack(boolean flag) {
		if(System.getProperty("os.name").contains("Windows")
				|| System.getProperty("os.name").contains("Mac")) {
				System.out.println("WARNING: Do not use useTransparencyHack(boolean) on Windows or Mac."
					+ " Instead use setTransparent(boolean).");
		} else {
			/*if(flag) {
				if(alreadyStarted) transparencyThread.notify();
				else {
					System.out.println("started transparency thread");
					transparencyThread.start();
				}
			} else {
				try {
					transparencyThread.wait();
					alreadyStarted = true;
				} catch (InterruptedException e) {
					throw new UncheckedAgentException("Thread interrupted");
				}
			}
			this.usingTransparencyHack = flag;*/
		}
	}
	
	private boolean useTTS = true;
	
	/**
	 * This method sets whether this {@code Agent}
	 * will use TTS.
	 * @param flag - Boolean value to set property to
	*/
	public void setUseTTS(boolean flag) {
		this.useTTS = flag;
	}
	
	/**
	 * @return A boolean indicating
	 * whether this {@code Agent} uses
	 * TTS for speech.
	*/
	public boolean usesTTS() {
		return useTTS;
	}
	  
	/**
	 * This method sets whether the character
	 * will use animations to be more expressive
	 * or not.<br><br>
	 * To use this method, the character
	 * must have implemented the animations
	 * "show", "hide", and (optionally) "breathe".
	 * If the character has not implemented the animation
	 * "breathe", then the breathing thread will exit
	 * upon startup.
	 * 
	 * @param animated - The boolean flag
	*/
	public void setAnimated(boolean animated) {
		this.animated = animated;
	}
	
	/**
	 * Stops all animations and brings character back to idle state.
	 * This method stops the animation timer, sets
	 * the image to the idle image, hides the speech bubble, and
	 * stops TTS output.
	*/
	public void stop() {
		timer.stop();
		character.setImage(idleImage);
		bubbleWindow.setVisible(false);
		v.systemVoice.getAudioPlayer().cancel();
	}
	
	/**
	 * Stops all animations without
	 * stopping TTS output.
	*/
	public void stopAnimating() {
		timer.stop();
		character.setImage(idleImage);
	}
	
	/**
	 * Checks if this {@code Agent}'s current character
	 * can play the given animation.
	 * 
	 * Note that for an idle animation (in the "idle" folder
	 * under the "animations" folder)
	 * to be recognized as supported, the name has to start with "idle_".
	 * 
	 * @return A boolean flag indicating whether the current
	 * character supports the given animation.
	*/
	public boolean supportsAnimation(String name) {
		String s = File.separator;
		String pathPart = dataPath + s + "animations" + s;
		if(!name.equals("idle")) {
			if(name.startsWith("idle_")) {
				return new File(pathPart + s + "idle" + s + name).exists();
			} else {
				return new File(pathPart + name).exists();
			}
		} else {
			return false;
		}
	}
	
	/**
	 * Checks if this {@code Agent} supports idle
	 * animations, which are played at random times.
	 * @return A {@code boolean} flag indicating this.
	*/
	public boolean supportsIdle() {
		String s = File.separator;
		File animationsFile = new File(dataPath + s + "animations" + s + "idle");
		return (animationsFile.exists() && animationsFile.listFiles().length > 0);
	}
	  
	/**
	 * Makes this {@code Agent} play an animation.
	 * This method is a convenience method. It is equivalent to calling
	 * {@code animate(animationName, 100, false)}.
	 * @param animationName - The name of the animation to play.
	 * 
	*/
	public void animate(String animationName) {
		animate(animationName, 100, AnimationPriority.NORMAL);
	}
	
	Timer timer = new Timer(100, null);
	
	ActionListener act = new ActionListener(){@Override public void actionPerformed(ActionEvent e) {}};
	
	public enum AnimationType {
		NORMAL, IDLE, RARE_IDLE, UNDETERMINED
	}
	
	public enum AnimationPriority {
		NOT_PLAYING, // Every animation will play at this priority.
		LOW, // Used by breathing. Breathing never interrupts other breathing
		IDLE, // Only breathing will not interrupt these.
		NORMAL, // Idle or breathing never interrupts these.
		TOP // No animations interrupt this priority.
	}
	  
	/**
	 * Makes this {@code Agent} play an animation.
	 * @param animationName - The name of the animation to play.
	 * @param delay - The time between animation frames.
	*/
	public void animate(String animationName, int delay, AnimationPriority animationPriority) {
		String oldPriority = priority.toString();
		if(animationPriority.equals(AnimationPriority.IDLE) || animationPriority.equals(AnimationPriority.LOW)
		   && priority.equals(AnimationPriority.NORMAL)) {
			return;
		}
		if(priority.equals(AnimationPriority.TOP) && !(animationPriority.equals(AnimationPriority.TOP))) return;
		priority = animationPriority;
		String s = File.separator;
		try {
			this.stopAnimating();
			//if(oldPriority != "NOT_PLAYING")
			System.out.println("Am I bothering you? " + animatingNow + " (is " + animationPriority.toString() + " bothering " + oldPriority + ")");
				System.err.println("animation of priority " + oldPriority + " (" + currentAnimation + 
						") interrupted by priority " + animationPriority.toString() 
					+ " (" + animationName + ")");
			this.currentAnimation = animationName;
			String pathPart = "";
			if(animationName.startsWith("idle_")) pathPart = "idle" + s + animationName;
			else if(animationName.startsWith("rare_")) pathPart = "idle" + s + "rare" + s + animationName;
			else pathPart = animationName;
			String path = dataPath + s + "animations" + s + pathPart;
			if(!(new File(path).exists())) throw new UncheckedAgentException("Unsupported animation");
			ArrayList<FilenameImage> framesList = new ArrayList<>();
			File[] files = new File(path).listFiles();
			int i1 = 0;
			for(int i = 0; i < files.length; i++) {
				File imgFile = new File(path + File.separator + i + ".png");
				if(imgFile.exists()) framesList.add(new FilenameImage(ImageIO.read(imgFile), imgFile.getName()));
				i1++;
			}
			System.out.println(i1 + " (correct length) vs " + framesList.size() + " (actual length)");
			aI = 0;
			timer.removeActionListener(act);
			act = (e) -> {
				if(aI >= framesList.size()) {
					internalFrame.setSize(idleImage.getWidth(null), idleImage.getHeight(null));
					character.setImage(idleImage);
					timer.stop();
					animatingNow = false;
					priority = AnimationPriority.NOT_PLAYING;
					currentAnimation = "none";
					return;
				}
				FilenameImage bi = framesList.get(aI);
				internalFrame.setSize(bi.getImage().getWidth(), bi.getImage().getHeight());
				character.setImage(bi.getImage());
				internalFrame.validate();
				internalFrame.repaint();
				aI++;
			};
			timer.addActionListener(act);
			timer.start();
		} catch (Exception e) {
			animatingNow = false;
			priority = AnimationPriority.NOT_PLAYING;
			currentAnimation = "none";
			character.setImage(idleImage);
			e.printStackTrace();
			throw new UncheckedAgentException("Error processing animation. " + e.getMessage());
		}
	}
	
	/**
	 * Get this {@code Agent}'s context menu.
	 * @return A {@code JPopupMenu} representing
	 * this {@code Agent}'s context menu.
	*/
	public JPopupMenu getMenu() {
		return cm;
	}
	
	/**
	 * Adds a left click listener to this {@code Agent}.
	 * @param c - The {@code Command} to set as the left click listener.
	*/
	public void setOnLeftClick(Command c) {
		internalFrame.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() == MouseEvent.BUTTON1) c.onInvoke();
				internalFrame.repaint();
			}

			@Override public void mouseClicked(MouseEvent arg0) {}
			@Override public void mouseEntered(MouseEvent arg0) {}
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			
		});
	}
	
	/**
	 * Adds a middle click listener to this {@code Agent}.
	 * @param c - The {@code Command} to set as the middle click listener.
	*/
	public void setOnMiddleClick(Command c) {
		internalFrame.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() == MouseEvent.BUTTON2) c.onInvoke();
			}

			@Override public void mouseClicked(MouseEvent arg0) {}
			@Override public void mouseEntered(MouseEvent arg0) {}
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			
		});
	}
	
	/**
	 * Adds a right click listener to this {@code Agent}.
	 * @param c - The {@code Command} to set as the right click listener.
	*/
	public void setOnRightClick(Command c) {
		internalFrame.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() == MouseEvent.BUTTON3) c.onInvoke();
			}

			@Override public void mouseClicked(MouseEvent arg0) {}
			@Override public void mouseEntered(MouseEvent arg0) {}
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			
		});
	}
	
	/**
	 * Adds left and right click listeners to this {@code Agent}.
	 * @param left - The {@code Command} to set as the left click listener.
	 * @param right - The {@code Command} to set as the right click listener.
	*/
	public void setOnLeftRightClick(Command left, Command right) {
		internalFrame.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent arg0) {
				if(arg0.getButton() == MouseEvent.BUTTON1) left.onInvoke();
				else if(arg0.getButton() == MouseEvent.BUTTON3) right.onInvoke();
			}

			@Override public void mouseClicked(MouseEvent arg0) {}
			@Override public void mouseEntered(MouseEvent arg0) {}
			@Override public void mouseExited(MouseEvent arg0) {}
			@Override public void mousePressed(MouseEvent arg0) {}
			
		});
	}
	
	/**
	 * Makes this {@code Agent} speak the given text.
	 * @param msg - The text to speak.
	*/
	public void say(String msg) {
		int x = (int) (internalFrame.getLocation().x + internalFrame.getWidth()
			- idleImage.getWidth(null));
		int y = (internalFrame.getLocation().y - internalFrame.getHeight()) - 40;
		bubbleWindow.setLocation(x, y);
		speechLabel.setText(null);
		bubbleWindow.setVisible(true);
		speechLabel.setText(msg);
		speechLabel.paintImmediately(speechLabel.getVisibleRect());
		if(v != null) {
			float oldVol = v.systemVoice.getAudioPlayer().getVolume();
			if(!useTTS) v.systemVoice.getAudioPlayer().setVolume(0.0f);
			if(useTTS) v.say(msg);
			v.systemVoice.getAudioPlayer().setVolume(oldVol);
		}
		try { Thread.sleep( 600); } catch (InterruptedException e) {
			throw new UncheckedAgentException("Agent thread interrupted");
		}
		bubbleWindow.setVisible(false);
	}
	
	/**
	 * Change whether this {@code Agent} stays on top of
	 * other windows or not.
	 * @param b - Boolean to set property to
	*/
	public void setAlwaysOnTop(boolean b) {
		internalFrame.setAlwaysOnTop(b);
	}
	
	/**
	 * Moves this {@code Agent} to the given spot on the screen.
	 * @param x - The X co-ordinate to move to
	 * @param y - The Y co-ordinate to move to
	*/
	public void move(int x, int y) {
		internalFrame.setLocation(x, y);
	}
	
	/**
	 * Moves this {@code Agent} to the given {@code Point} on the screen.
	 * @param p - The {@code Point} to move to.
	*/
	public void move(Point p) {
		internalFrame.setLocation(p);
	}
	
	/**
	 * @deprecated
	 * Sets the idle image of the character.
	 * This method was deprecated because the correct
	 * way to set the idle image now is to create a file
	 * called idle.png in the character root.
	 * @param val - The image to set to.
	*/
	@Deprecated
	public void setIdleImage(Image val) {
		this.idleImage = val;
		internalFrame.setSize(idleImage.getWidth(null),
				idleImage.getHeight(null));
	}
	
	/**
	 * Gets the idle image of the character.
	*/
	public Image getIdleImage() { return idleImage; }
	
	/**
	 * @deprecated
	 * This method was originally meant
	 * to handle lip-syncing, but was
	 * never implemented.
	 * @throws UnsupportedOperationException Always.
	*/
	@Deprecated
	public void setTalkingImages(Image[] vals) {
		throw new UnsupportedOperationException();
	}
	
	/**
	 * Shows this {@code Agent}. If this
	 * {@code Agent} is animated, this method
	 * will play the "show" animation.
	*/
	public void show() {
		character.setImage(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB));
		internalFrame.setVisible(true);
		if(animated) this.animate("show", 100, AnimationPriority.TOP);
	}
	
	/**
	 * Hides this {@code Agent}. If this
	 * {@code Agent} is animated, this method
	 * will play the "hide" animation.
	*/
	public void hide() {
		if(animated) this.animate("hide", 100, AnimationPriority.TOP);
		internalFrame.setVisible(false);
	}
	
	class Voice
	{
	    private String name;
	    private com.sun.speech.freetts.Voice systemVoice;

	    public Voice(String name)
	    {
	        this.name = name;
	        this.systemVoice = VoiceManager.getInstance().getVoice(this.name);
	        this.systemVoice.allocate();
	    }

	    public void say(String text)
	    {
	        this.systemVoice.speak(text);
	    }

	    public void dispose()
	    {
	        this.systemVoice.deallocate();
	    }
	}

}
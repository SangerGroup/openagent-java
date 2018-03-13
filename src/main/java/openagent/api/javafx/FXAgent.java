package openagent.api.javafx;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioInputStream;
import javax.swing.JOptionPane;

import com.sun.speech.freetts.VoiceManager;

import dorkbox.systemTray.SystemTray;
import openagent.api.javafx.OpenAgent;
import openagent.api.swing.Agent.AnimationPriority;
import openagent.api.tetyys.SAPIVoice;
import openagent.api.FilenameImage;
import openagent.api.TTS;
import openagent.api.UncheckedAgentException;
import openagent.api.javafx.Callback;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextArea;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;
import marytts.LocalMaryInterface;
import marytts.MaryInterface;
import marytts.exceptions.MaryConfigurationException;
import marytts.exceptions.SynthesisException;
import marytts.util.data.audio.AudioPlayer;
import javafx.scene.Scene;
import javafx.scene.control.ContextMenu;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class FXAgent extends Application {
	
	private String dataPath = Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "character";
	
	private Image idleImage;
	
	private ImageView charView = new ImageView();
	
	private Stage internalStage;
	
	private Stage bubbleStage;
	
	private Scene scene;
	
	private TextArea speechLabel;
	
	private ContextMenu menu;
	
	private FreeTTSVoice freeTTSVoice;
	
	private MaryTTSVoice maryTTSVoice;
	
	private SAPIVoice sapiVoice = SAPIVoice.bonziInstance();
	
	private boolean animated = true;
	
	private Callback finishListener;
	
	private static final boolean DEBUG = true;
	
	private String charName = "FXAgent";
	
	private Logger log = Logger.getLogger("FXAgent:" + charName);
	
	private TTS toUse = TTS.SAPI4;
	
	/**
	 * @deprecated
	 * WARNING: This constructor is called
	 * by JavaFX internally and should never
	 * be called by the user.
	*/
	@Deprecated
	public FXAgent() {
		this.dataPath = Paths.get(".").toAbsolutePath().normalize().toString() + File.separator + "character";
		AgentManager.setAgent(this);
	}
	
	FXAgent(String dataPath, String charName) {
		Application.launch(FXAgent.class, dataPath, "false");
	}
	
	FXAgent(String dataPath, boolean animated, String charName) {
		new Thread(() -> Application.launch(FXAgent.class, dataPath, String.valueOf(animated))).start();
	}
	
	public static void main(String[] args) {
		OpenAgent.loadAgent(Paths.get(".").toAbsolutePath().normalize().toString() 
				+ File.separator + "res" + File.separator + "peedy", true,
				(agent) -> {
					javafx.scene.control.MenuItem mItem = new javafx.scene.control.MenuItem("Animate");
					mItem.setOnAction(e -> {
						agent.animate(JOptionPane.showInputDialog("Animation?"), 100, AnimationPriority.TOP);
					});
					agent.getMenu().getItems().add(mItem);
				},
				(agent) -> {
					agent.say("Hello World! This is a very long sentence for testing the speech bubble in JavaFX.");
				});
	}
	
	private boolean canPlayIdle() {
		return !(priority.equals(AnimationPriority.NORMAL)
					|| priority.equals(AnimationPriority.TOP) || priority.equals(AnimationPriority.IDLE))
					&& internalStage.isShowing();
	}
	
	private boolean canBreathe() {
		// Can only breathe when no animation is playing, not even breathing
		System.out.println("is priority NOT_PLAYING? " + priority.equals(AnimationPriority.NOT_PLAYING));
		return priority.equals(AnimationPriority.NOT_PLAYING);
	}
	
	private double xOffset = 0;
	private double yOffset = 0;

	@Override
	public void start(Stage unusedStage) throws Exception {
		unusedStage.initStyle(StageStyle.UTILITY);
		info("start");
		// Get parameters (data path, animated)
		List<String> params = getParameters().getRaw();
		this.dataPath = params.get(0);
		this.animated = Boolean.parseBoolean(params.get(1));
		
		// Set internal stage variable
		internalStage = new Stage();
		
		// Begin window configuration
		idleImage = new Image("file:///" + dataPath + File.separator + "idle.png");
		internalStage.initStyle(StageStyle.TRANSPARENT);
		StackPane sp = new StackPane();
		scene = new Scene(sp);
		scene.setFill(Color.TRANSPARENT);
		sp.getChildren().add(charView);
		
		// Make stage draggable even though it is undecorated
		sp.setOnMousePressed(e -> {
        	xOffset = e.getSceneX();
            yOffset = e.getSceneY();
        });
        sp.setOnMouseDragged(e -> {
                internalStage.setX(e.getScreenX() - xOffset);
                internalStage.setY(e.getScreenY() - yOffset);
                int x = (int) (internalStage.getX() + internalStage.getWidth()
				- idleImage.getWidth());
				int y = (int) ((internalStage.getY() - internalStage.getHeight()) - 80);
				bubbleStage.setX(x);
				bubbleStage.setY(y);
        });
        
        // Begin context menu configuration
        menu = new ContextMenu();
        sp.setStyle("-fx-background-color: transparent;"); // Keep transparent color
        MenuItem hideItem = new MenuItem("Hide");
        hideItem.setOnAction(e -> hide());
        MenuItem speakItem = new MenuItem("Speak");
        speakItem.setOnAction(e -> this.say("Test test"));
        menu.getItems().addAll(hideItem, new SeparatorMenuItem());
        charView.setOnContextMenuRequested(e -> 
        	menu.show(charView, e.getScreenX(), e.getScreenY())); // Make context menu appear on right click
        
        // System tray configuration
        new Thread(() -> {
        	 SystemTray tray = SystemTray.get();
        	 tray.setEnabled(true);
             System.err.println("trying to read file " + dataPath + File.separator + "tray.png");
             try {
				tray.setImage(ImageIO.read(new File(dataPath + File.separator + "tray.png")));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
             dorkbox.systemTray.MenuItem showItem = new dorkbox.systemTray.MenuItem("Show");
             showItem.setCallback(e -> Platform.runLater(() -> this.show()));
             dorkbox.systemTray.MenuItem hideItem1 = new dorkbox.systemTray.MenuItem("Hide");
             hideItem1.setCallback(e -> Platform.runLater(() -> { if(internalStage.isShowing()) hide(); }));
             tray.getMenu().add(showItem);
             tray.getMenu().add(hideItem1);
             tray.setStatus("OpenAgent");
        }).start();
        
        // Final window configuration
		internalStage.setScene(scene);
		internalStage.setTitle("OpenAgent");
		internalStage.setAlwaysOnTop(true);
		internalStage.initModality(Modality.APPLICATION_MODAL);
		internalStage.initOwner(unusedStage);
		
		// Begin bubble window configuration
		bubbleStage = new Stage(StageStyle.UNDECORATED);
		StackPane bubblePane = new StackPane();
		Scene bubbleScene = new Scene(bubblePane, 200, 200);
		StackPane bubbleView = new StackPane();
		Image image = new Image("file:///" + dataPath + File.separator + "bubble.png");
		bubbleView.setBackground(new Background(new BackgroundImage(image, null, null, null, null)));
		bubbleView.setMaxWidth(Double.MAX_VALUE);
		bubbleView.setMaxHeight(Double.MAX_VALUE);
		Image img = bubbleView.getBackground().getImages().get(0).getImage();
		bubbleStage.setMinWidth(img.getWidth());
		bubbleStage.setMinHeight(img.getHeight());
		speechLabel = new TextArea();
		speechLabel.setEditable(false);
		speechLabel.setWrapText(true);
		bubbleView.getChildren().add(speechLabel);
		bubblePane.getChildren().add(bubbleView);
		bubbleStage.setScene(bubbleScene);
		bubbleStage.setAlwaysOnTop(true);
		
		Platform.setImplicitExit(false);
		
		
		// Start threads
		
		// Breathing thread
		new Thread(() -> {
			info("Breathing thread started");
			while(true) {
				if(!(this.supportsAnimation("breathe"))) Thread.currentThread().interrupt();
				if(animated && canBreathe() && internalStage.isShowing()) {
					this.animate("breathe", 100, AnimationPriority.LOW); //Everything interrupts breathing, except other breathing
				}
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
					throw new UncheckedAgentException("Breathing thread interrupted");
				}
			}
		}).start();
		
		// Set up idle and rare idle animations
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
		
		// Start idle and rare idle animation thread
		new Thread(() -> {
			info("Idle thread started");
			while(true) {
				/*System.out.println("Idle thread FTW! Available idle animations are:");
				for(String idleName : possibleIdleAnimations) System.out.println(idleName);
				System.out.println("Rare idle animations:");
				for(String rareIdleName : possibleRareIdleAnimations) System.out.println(rareIdleName);*/
				try {
					int timeout = new Random().nextInt(15000) + 5000;
					info("sleeping for " + timeout + " seconds");
					Thread.sleep(timeout);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				if(!(this.supportsIdle())) Thread.currentThread().interrupt();
				if(animated && canPlayIdle()) {
					info("Can play idle. animated = " + animated + ", & canPlayIdle() = " + canPlayIdle());
					if((new Random().nextInt(10) + 1) == 5) {
						System.out.println("Rare idle animation");
						this.animate(possibleRareIdleAnimations.get(new Random().nextInt(possibleRareIdleAnimations.size())), 
								100, AnimationPriority.IDLE);
					} else {
						info("Reg. idle animation");
						this.animate(possibleIdleAnimations.get(new Random().nextInt(possibleIdleAnimations.size())), 100, AnimationPriority.IDLE);
					}
				} else {
					info("Can't play idle. animated = " + animated + ", & canPlayIdle() = " + canPlayIdle());
				}
			}
		}).start();
		
		// Final steps
		charView.setImage(idleImage);
		if(finishListener != null) finishListener.finished(this);
	}
	
	private void info(String msg) {
		if(DEBUG) log.info(msg);
	}
	
	/**
	 * Shows this {@code FXAgent}.
	*/
	public void show() {
		if(internalStage != null) {
			internalStage.show();
			if(animated) this.animate("show", 100, AnimationPriority.TOP);
		}
	}
	
	/**
	 * Shows this {@code FXAgent}.
	 * @param afterShow - Code to be executed
	 * after the agent shows
	*/
	public void show(ShortCallback afterShow) {
		if(internalStage != null) {
			internalStage.show();
			if(animated) this.animate("show", 100, AnimationPriority.TOP, () -> afterShow.finished());
		}
	}
	
	/**
	 * Hides this {@code FXAgent}.
	*/
	public void hide() {
		if(internalStage != null) {
			if(animated) this.animate("hide", 100, AnimationPriority.TOP, () -> internalStage.hide());
		}
	}
	
	/**
	 * @return A boolean indicating whether
	 * this {@code FXAgent} is displayed or not.
	*/
	public boolean isShown() {
		return internalStage.isShowing();
	}
	
	/**
	 * Makes this {@code FXAgent} speak the given text.
	 * @param msg - The text to speak.
	*/
	public void say(String msg) {
		say(msg, msg, () -> {});
	}
	
	/**
	 * Makes this {@code FXAgent} speak the given text.
	 * @param msg - The text to speak.
	 * @param afterSay - The code to run after the {@code FXAgent}
	 * speaks the given text.
	*/
	public void say(String msg, ShortCallback afterSay) {
		say(msg, msg, afterSay);
	}
	
	/**
	 * Makes this {@code FXAgent} speak the given text.
	 * @param msg - The text to speak.
	 * @param bubbleText - The text to set the speech bubble to.
	 * @param afterSay - The code to run after the {@code FXAgent}
	 * speaks the given text.
	*/
	@SuppressWarnings("deprecation")
	public void say(String msg, String bubbleText, ShortCallback afterSay) {
		new Thread(() -> {
			if(internalStage != null) {
				if(toUse.equals(TTS.FREETTS) && freeTTSVoice != null)
					freeTTSVoice.systemVoice.getAudioPlayer().cancel(); // Stop already-playing noises
				else if(toUse.equals(TTS.MARYTTS) && maryTTSVoice != null)
					maryTTSVoice.ap.cancel(); // Stop already-playing noises
				Platform.runLater(() -> {
					int x = (int) (internalStage.getX() + internalStage.getWidth()
					- idleImage.getWidth());
					int y = (int) ((internalStage.getY() - internalStage.getHeight()) - 80);
					bubbleStage.setX(x);
					bubbleStage.setY(y);
					speechLabel.setText(null);
					speechLabel.setText(bubbleText);
				}); 
				
				if(toUse.equals(TTS.FREETTS)) {
					Platform.runLater(() -> bubbleStage.show()); 
					if(freeTTSVoice == null) freeTTSVoice = new FreeTTSVoice("kevin16");
					freeTTSVoice.say(msg);
					Platform.runLater(() -> bubbleStage.hide());
					afterSay.finished();
				} else if(toUse.equals(TTS.MARYTTS)) {
					if(maryTTSVoice == null) maryTTSVoice = new MaryTTSVoice("cmu-bdl-hsmm");
					Platform.runLater(() -> bubbleStage.show()); 
					maryTTSVoice.say(msg, () -> {
						try { Thread.sleep(600); } catch (InterruptedException e) {
							throw new UncheckedAgentException("Speaking interrupted");
						}
						Platform.runLater(() -> bubbleStage.hide());
						afterSay.finished();
					});
				} else if(toUse.equals(TTS.SAPI4)) {
					new Thread(() -> {
						double origTimeout = sapiVoice.speak(msg);
						Platform.runLater(() -> bubbleStage.show()); 
						double timeout = origTimeout + ((double) origTimeout / 15.7);
						try { Thread.sleep((int) timeout); } catch (InterruptedException e) {
							throw new UncheckedAgentException("Speaking interrupted");
						}
						Platform.runLater(() -> bubbleStage.hide());
						afterSay.finished();
					}).start();
				}
				
			} 
		}).start();
	}
	
	/**
	 * Makes this {@code FXAgent} play an animation.<br><br>
	 * 
	 * Calling <code>animate([animationName])</code>
	 * is equivalent to calling:<br> <code>animate([animationName],
	 * 100, AnimationPriority.NORMAL, () -> {})</code>.
	 * 
	 * @param animationName - The name of the animation to play.
	*/
	public void animate(String animationName) {
		animate(animationName, 100, AnimationPriority.NORMAL, () -> {});
	}
	
	/**
	 * Makes this {@code FXAgent} play an animation.
	 * @param animationName - The name of the animation to play.
	 * @param delay - The time between animation frames.
	 * @param animationPriority - The priority of the animation.
	*/
	public void animate(String animationName, int delay, AnimationPriority animationPriority) {
		animate(animationName, delay, animationPriority, () -> {});
	}
	
	public enum AnimationType {
		NORMAL, IDLE, RARE_IDLE, UNDETERMINED
	}
	
	private AnimationPriority priority = AnimationPriority.NOT_PLAYING;
	
	private String currentAnimation = "";
	
	Timeline timeline = new Timeline();
	
	private int aI = 0;
	
	/**
	 * Makes this {@code FXAgent} play an animation.
	 * @param animationName - The name of the animation to play.
	 * @param delay - The time between animation frames.
	 * @param animationPriority - The priority of the animation.
	 * @param afterAnimation - The code to run after the animation finishes.
	*/
	public void animate(String animationName, int delay, AnimationPriority animationPriority, ShortCallback afterAnimation) {
		String oldPriority = priority.toString();
		System.out.print("The animation " + animationName + " is playing...");
		if((animationPriority.equals(AnimationPriority.IDLE) || animationPriority.equals(AnimationPriority.LOW))
		   && (priority.equals(AnimationPriority.NORMAL) || priority.equals(AnimationPriority.TOP))) {
			info("Returning: animationPriority = " + animationPriority + ", & priority = " + priority);
			return;
		}
		if(priority.equals(AnimationPriority.TOP) && !(animationPriority.equals(AnimationPriority.TOP))) return;
		this.stopAnimating(animationName, animationPriority);
		priority = animationPriority;
		System.out.println("priority of animation " + animationName + " is " + animationPriority + ".");
		String s = File.separator;
		try {
			System.out.println("...and it bypassed the restrictions.");
			currentAnimation = animationName;
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
				if(imgFile.exists()) {
					FilenameImage imgFi = new FilenameImage(ImageIO.read(imgFile), imgFile.getName());
					framesList.add(imgFi);
				}
				i1++;
			}
			framesList.trimToSize();
			aI = 0;
			timeline.getKeyFrames().addAll(
					new KeyFrame(Duration.ZERO,
					 e -> {
						 if(aI < framesList.size()) {
							 FilenameImage bi = framesList.get(aI);
							 charView.setImage(SwingFXUtils.toFXImage(bi.getImage(), null));
							 aI++;
						 } else {
							 timeline.stop();
						 }
					 }),
					new KeyFrame(Duration.millis(delay))
			);
			timeline.setCycleCount(framesList.size());
			timeline.play();
			timeline.setOnFinished(e -> {
				charView.setImage(idleImage);
				priority = AnimationPriority.NOT_PLAYING;
				currentAnimation = "";
				afterAnimation.finished();
			});
		} catch (Exception e) {
			priority = AnimationPriority.NOT_PLAYING;
			currentAnimation = "";
			charView.setImage(idleImage);
			e.printStackTrace();
			throw new UncheckedAgentException("Error processing animation. " + e.getMessage());
		}
	}
	
	/**
	 * Stops all animations and
	 * reverts character back
	 * to idle.
	*/
	public void stopAnimating() {
		timeline.stop();
		timeline = new Timeline();
		charView.setImage(idleImage);
		currentAnimation = "";
		priority = AnimationPriority.NOT_PLAYING;
	}
	
	public void stopAnimating(String name, AnimationPriority priority) {
		System.out.println(name + " with priority " + priority + " stopped animation " + currentAnimation + " with priority " + priority);
		stopAnimating();
	}
	
	/**
	 * Stops all animations and talking
	 * and reverts the character back to
	 * idle state.
	*/
	@SuppressWarnings("deprecation")
	public void stop() {
		stopAnimating();
		Platform.runLater(() -> bubbleStage.hide());
		switch(toUse) {
		case MARYTTS:
			maryTTSVoice.ap.cancel();
			maryTTSVoice.ap = new AudioPlayer();
			// Fix freezing of animation
			charView.setImage(idleImage);
			priority = AnimationPriority.NOT_PLAYING;
			currentAnimation = "";
			break;
		case FREETTS:
			freeTTSVoice.systemVoice.getAudioPlayer().cancel();
			break;
		case SAPI4:
			sapiVoice.stopSpeaking();
			break;
		case NONE:
			break;
		}
	}
	
	/**
	 * Checks if this {@code FXAgent}'s current character
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
	 * Checks if this {@code FXAgent} supports idle
	 * animations, which are played at random times.
	 * @return A {@code boolean} flag indicating this.
	*/
	public boolean supportsIdle() {
		String s = File.separator;
		File animationsFile = new File(dataPath + s + "animations" + s + "idle");
		return (animationsFile.exists() && animationsFile.listFiles().length > 0);
	}
	
	/**
	 * This method sets whether the character
	 * will use animations to be more expressive
	 * or not. This property is enabled by default for
	 * all characters in the {@code Characters}
	 * class.<br><br>
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
	 * Sets the TTS engine this
	 * {@code FXAgent} will use for speech
	 * (with the {@code say(String)} method).
	 * @param engine - The TTS engine to use; 
	 * one of {@code FREETTS}, {@code MARYTTS}, or {@code SAPI4}.
	*/
	public void setTTS(TTS engine) {
		this.toUse = engine;
	}
	
	public ContextMenu getMenu() {
		return menu;
	}
	
	public void setOnFinish(Callback cb) {
		this.finishListener = cb;
	}
	
	public interface ClickListener {
		public void clicked(MouseEvent me);
	}
	
	/**
	 * Adds a click listener to this
	 * {@code FXAgent}.
	 * @param listener - the ClickListener
	 * to add to the agent
	*/
	public void addClickListener(ClickListener listener) {
		scene.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
		    @Override public void handle(MouseEvent mouseEvent) { listener.clicked(mouseEvent); }
		});
	}
	
	public interface ShortCallback {
		public void finished();
	}
	
	class FreeTTSVoice {
	    private String name;
	    private com.sun.speech.freetts.Voice systemVoice;

	    public FreeTTSVoice(String name) {
	        this.name = name;
	        this.systemVoice = VoiceManager.getInstance().getVoice(this.name);
	        try {
	        	this.systemVoice.allocate();
			} catch (Exception e) {
				System.err.println("Couldn't initialize TTS.");
				e.printStackTrace();
			}
	    }

	    public void say(String text) {
	    	try {
	    		this.systemVoice.speak(text);
	    	} catch(NullPointerException e) {}
	    }

	    public void dispose() {
	        this.systemVoice.deallocate();
	    }
	}
	
	static interface SayCallback {
    	public void finish();
    }
	
	class MaryTTSVoice
	{
	    private MaryInterface marytts;
	    private AudioPlayer ap;

	    public MaryTTSVoice(String voiceName)
	    {
	        try
	        {
	            marytts = new LocalMaryInterface();
	            marytts.setVoice(voiceName);
	            ap = new AudioPlayer();
	        }
	        catch (MaryConfigurationException ex)
	        {
	            ex.printStackTrace();
	        }
	    }

	    public void say(String input, SayCallback callback)
	    {
	        try
	        {
	        	ap.interrupt();
	        	ap = new AudioPlayer();
	            AudioInputStream audio = marytts.generateAudio(input);

	            ap.setAudio(audio);
	            ap.start();
				ap.join();
	            callback.finish();
	        }
	        catch (SynthesisException ex)
	        {
	            System.err.println("Error saying phrase.");
	            ex.printStackTrace();
	        } catch (InterruptedException e) {
				e.printStackTrace();
			}
	    }
	}
	
}

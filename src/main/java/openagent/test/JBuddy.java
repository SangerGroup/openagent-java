package openagent.test;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Platform;
import javafx.scene.control.MenuItem;
import javafx.scene.input.MouseButton;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.text.DefaultCaret;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;

import org.json.JSONObject;

import com.google.code.chatterbotapi.ChatterBot;
import com.google.code.chatterbotapi.ChatterBotFactory;
import com.google.code.chatterbotapi.ChatterBotSession;
import com.google.code.chatterbotapi.ChatterBotType;

import java.util.List;
import java.util.Scanner;

import dorkbox.systemTray.SystemTray;

import openagent.api.*;
import openagent.api.javafx.*;

public class JBuddy implements AgentApplication {
	
	public static void main(String[] args) throws AgentException {
		OpenAgent.launchApplication(Characters.PEEDY, JBuddy.class);
	}
	
	private File settingsFile = new PathBuilder().currentDir().sep().folderOrFile("res").sep().folderOrFile("settings.json").buildFile();
	
	private JFrame readFrame = new JFrame("Quick Read");
	
	@Override
	public void configure(JFrame mainFrame, FXAgent agent) {
		readFrame.setSize(285, 285);
		readFrame.setLayout(new BorderLayout());
		JTextArea readArea = new JTextArea();
		readArea.setBorder(new BevelBorder(BevelBorder.LOWERED));
		readArea.setWrapStyleWord(true);
		readArea.setLineWrap(true);
		JScrollPane readPane = new JScrollPane(readArea);
		JButton readBtn = new JButton("Read Text");
		readBtn.addActionListener((e) -> {
			agent.say(readArea.getText());
		});
		readFrame.getRootPane().setDefaultButton(readBtn);
		readFrame.add(readPane, BorderLayout.CENTER);
		readFrame.add(readBtn, BorderLayout.SOUTH);
		MenuItem quietItem = new MenuItem("Be Quiet");
		quietItem.setOnAction((e) -> agent.stop());
		MenuItem readItem = new MenuItem("Quick Read");
		readItem.setOnAction((e) -> readFrame.setVisible(true));
		MenuItem animItem = new MenuItem("Quick Animate");
		animItem.setOnAction((e) -> {
			String animationName = JOptionPane.showInputDialog("Animation to play?");
			agent.animate(animationName);
		});
		MenuItem jokeItem = new MenuItem("Tell Joke");
		jokeItem.setOnAction((e) -> FactJokeUtils.tellJoke(agent));
		MenuItem factItem = new MenuItem("Tell Fact");
		quietItem.setOnAction((e) -> FactJokeUtils.tellFact(agent));
		MenuItem openItem = new MenuItem("Open Program");
		openItem.setOnAction(e -> mainFrame.setVisible(true));
		MenuItem exitItem = new MenuItem("Exit Program");
		exitItem.setOnAction(e -> System.exit(0));
		agent.getMenu().getItems().addAll(quietItem, readItem, animItem, jokeItem, factItem, openItem, exitItem);
		agent.setTTS(TTS.MARYTTS);
		agent.addClickListener(me -> {
			if(!(me.isDragDetect()) && me.getClickCount() == 2) {
				mainFrame.setVisible(true);
				java.awt.EventQueue.invokeLater(() -> {
					mainFrame.setState(JFrame.NORMAL);
					mainFrame.toFront();
			        mainFrame.repaint();
				});
			} else if(me.getButton().equals(MouseButton.MIDDLE)) {
				initTextChat(agent);
			}
		});
	}
	
	private String name = "";
	private boolean intro = false;
	private boolean silenceOnStartup = true;
	
	boolean isGoing = false;
	
	String editing = "";
	
	@Override
	public void show(JFrame mainFrame, FXAgent agent) {
		SystemTray tray = SystemTray.get();
		tray.setEnabled(false);
		tray.setStatus("JBuddy");
		try {
			tray.setImage(ImageIO.read(new PathBuilder().currentDir().sep().folderOrFile("res").sep().folderOrFile("tray.png").buildFile()));
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			// Check if settings.json (main settings file) exists
			if(settingsFile.exists()) {
				read();
			} else {
				// Otherwise, create it and fill it with JSON
				settingsFile.createNewFile();
				Files.write(Paths.get(settingsFile.toURI()),
						"{ \"intro\":false,\"name\":\"\",\"silenceOnStartup\":true }".getBytes());
			}
		} catch(Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Sorry, but JBuddy hit a snag and can't continue. " + e.getMessage());
		}
		mainFrame.setTitle("JBuddy - Powered by OpenAgent");
		mainFrame.setSize(650, 400);
		mainFrame.setLayout(new BorderLayout());
		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// Initialized JTabbedPane
		JTabbedPane tabs = new JTabbedPane();
		
		// Basic tab
		JPanel basicPanel = new JPanel(new GridLayout(3, 3));
		JButton showHideBtn = new JButton("Show/Hide Character");
		showHideBtn.addActionListener(e -> {
			if(agent.isShown()) Platform.runLater(() -> agent.hide());
			else Platform.runLater(() -> agent.show());
		});
		JButton trayBtn = new JButton("System Tray");
		trayBtn.addActionListener(e -> {
			mainFrame.setVisible(false);
			tray.setEnabled(true);
		});
		JButton quietBtn = new JButton("Be Quiet (Character)");
		quietBtn.addActionListener(e -> agent.stop());
		JButton settingsBtn = new JButton("Settings");
		settingsBtn.addActionListener(e -> tabs.setSelectedIndex(2));
		JButton aboutBtn = new JButton("About JBuddy");
		aboutBtn.addActionListener(e -> {
			
		});
		JButton exitBtn = new JButton("Exit Program");
		exitBtn.addActionListener(e -> System.exit(0));
		basicPanel.add(showHideBtn);
		basicPanel.add(trayBtn);
		basicPanel.add(quietBtn);
		basicPanel.add(settingsBtn);
		basicPanel.add(aboutBtn);
		basicPanel.add(exitBtn);
		
		// Functions tab
		JPanel functionsPanel = new JPanel(new GridLayout(3, 3));
		JButton jokeBtn = new JButton("   Tell Joke (40 available)");
		jokeBtn.setIcon(new ImageIcon(JOKE_ICON));
		jokeBtn.addActionListener(e -> FactJokeUtils.tellJoke(agent));
		JButton factBtn = new JButton("   Tell Fact (13 available)");
		factBtn.setIcon(new ImageIcon(FACT_ICON));
		factBtn.addActionListener(e -> FactJokeUtils.tellFact(agent));
		JButton quickReadBtn = new JButton("   Quick Read");
		quickReadBtn.setIcon(new ImageIcon(READ_ICON));
		quickReadBtn.addActionListener(e -> readFrame.setVisible(true));
		JButton textChatBtn = new JButton("   Text Chat");
		textChatBtn.setIcon(new ImageIcon(CHAT_ICON));
		// Text chat button
		textChatBtn.addActionListener(e -> initTextChat(agent));
		// Voice chat button
		/*JButton voiceChatBtn = new JButton("   Voice Chat");
		voiceChatBtn.setIcon(new ImageIcon(VOICE_CHAT_ICON));
		voiceChatBtn.addActionListener(e -> {
			try {
				JFrame voiceChatFrame = new JFrame("JBuddy Voice Chat");
				voiceChatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				voiceChatFrame.setSize(100, 100);
				
				final Configuration configuration = new Configuration();
	
		        configuration.setAcousticModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us");
		        configuration.setDictionaryPath("resource:/edu/cmu/sphinx/models/en-us/cmudict-en-us.dict");
		        configuration.setLanguageModelPath("resource:/edu/cmu/sphinx/models/en-us/en-us.lm.bin");
		        
		        LiveSpeechRecognizer recognizer = new LiveSpeechRecognizer(configuration);
		        
				JButton startBtn = new JButton("");
				final ImageIcon VOICE_CHAT = new ImageIcon(VOICE_CHAT_ICON);
				startBtn.setIcon(VOICE_CHAT);
				startBtn.addActionListener(e1 -> {
					if(isGoing) {
						startBtn.setIcon(VOICE_CHAT);
						startBtn.setText("");
						SpeechResult result = recognizer.getResult();
						recognizer.stopRecognition();
						isGoing = false;
						JOptionPane.showInputDialog(null, "Message:", result.getHypothesis());
					} else {
						System.out.println("got here");
						startBtn.setIcon(null);
						startBtn.setText("⏺");
						startBtn.repaint();
						isGoing = true;
						recognizer.startRecognition(true);
					}
				});
				
				voiceChatFrame.add(startBtn);
				voiceChatFrame.setVisible(true);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		});*/
		functionsPanel.add(jokeBtn);
		functionsPanel.add(factBtn);
		functionsPanel.add(quickReadBtn);
		functionsPanel.add(textChatBtn);
		//functionsPanel.add(voiceChatBtn);
		
		// TTS Studio tab
		JPanel studioPanel = new JPanel(new BorderLayout());
		JPanel topPanel = new JPanel(new BorderLayout());
		
		// Create switcher
		JTabbedPane switcher = new JTabbedPane();
		switcher.setTabPlacement(JTabbedPane.BOTTOM);
		
		// Results pane
		JTextPane resultPane = new JTextPane();
		resultPane.setFont(new Font("Times New Roman", Font.PLAIN, 20));
		resultPane.setContentType("text/html");
		resultPane.setEditable(false);
		
		JScrollPane resultScroller = new JScrollPane(resultPane);
		
		// Source pane
		JTextArea sourcePane = new JTextArea();
		sourcePane.setFont(new Font("Courier New", Font.PLAIN, 18));
		sourcePane.setLineWrap(true);
		sourcePane.setWrapStyleWord(true);
		
		JScrollPane sourceScroller = new JScrollPane(sourcePane);
		
		switcher.add("Source", sourceScroller);
		switcher.add("Result", resultScroller);
		
		// Editing label, added to topPanel later
		JLabel editingLabel = new JLabel();
		editingLabel.setHorizontalAlignment(JLabel.CENTER);
		
		final JFileChooser chooser = new JFileChooser();
		JButton saveBtn = new JButton("Save");
		saveBtn.addActionListener(e -> {
			if(editing.equals("") && chooser.showSaveDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
				editing = chooser.getSelectedFile().getAbsolutePath();
				editingLabel.setText(editing);
				if(!(new File(editing).exists()))
					try {
						new File(editing).createNewFile();
					} catch (IOException e1) {
						e1.printStackTrace();
					}
			}
			try {
				Files.write(Paths.get(editing), sourcePane.getText().getBytes());
			} catch (IOException e1) {
				JOptionPane.showMessageDialog(mainFrame,
						"Couldn't save file. " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				e1.printStackTrace();
			}
		});
		
		// Buttons
		
		// Open
		JButton openBtn = new JButton("Open");
		openBtn.addActionListener(e -> {
			if(chooser.showOpenDialog(mainFrame) == JFileChooser.APPROVE_OPTION) {
				editing = chooser.getSelectedFile().getAbsolutePath();
				editingLabel.setText(editing);
				try {
					// Clear box in preparation for incoming data
					sourcePane.setText("");
					resultPane.setText("");
					List<String> lines = Files.readAllLines(Paths.get(editing));
					for(String line : lines) sourcePane.setText(sourcePane.getText() + line + "\n");
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(mainFrame,
							"Couldn't open file. " + e1.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					e1.printStackTrace();
				}
			}
		});
		
		// Speak
		JButton sayBtn = new JButton("Speak");
		sayBtn.addActionListener(e -> {
			resultPane.setText(sourcePane.getText());
			Html2Text htmlToText = new Html2Text();
			try {
				htmlToText.parse(new StringReader(resultPane.getText()
						/*.replace("<br>", "\n")
						.replace("<br />", "\n")
						.replace("<br/>", "\n")
						.replace("<br></br>", "\n")*/));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			agent.say(htmlToText.getText());
		});
		
		// Update Result
		JButton resultUpdateBtn = new JButton("Update Result");
		resultUpdateBtn.addActionListener(e -> resultPane.setText(sourcePane.getText()));
		
		JPanel controls = new JPanel();
		
		controls.add(openBtn);
		controls.add(saveBtn);
		controls.add(sayBtn);
		controls.add(resultUpdateBtn);
		topPanel.add(controls, BorderLayout.CENTER);
		topPanel.add(editingLabel, BorderLayout.NORTH);
		
		studioPanel.add(switcher, BorderLayout.CENTER);
		studioPanel.add(topPanel, BorderLayout.NORTH);
		
		JButton studioBtn = new JButton("   TTS Studio");
		studioBtn.setIcon(new ImageIcon(TTS_STUDIO_ICON));
		studioBtn.addActionListener(e -> {});
		
		functionsPanel.add(studioBtn);
		
		// Options tab
		JPanel optionsPanel = new JPanel(new GridLayout(3, 3));
		
		// Add tabs
		tabs.addTab("Home", basicPanel);
		tabs.addTab("Functions", functionsPanel);
		tabs.addTab("TTS Studio", studioPanel);
		tabs.addTab("Settings", optionsPanel);
		mainFrame.add(tabs, BorderLayout.CENTER);
		
		if(intro) {
			agent.say("Hello again, " + name + "!");
			mainFrame.setVisible(true);
		} else {
			String str = JOptionPane.showInputDialog("Enter your name to get started:");
			//new Thread(() -> agent.say("Hello! I don't think I've introduced myself. My name is Peedy. Enter yours in the box.")).start();
			name = str;
			intro = true;
			save();
			agent.say(name + ". That's a nice name.");
			mainFrame.setVisible(true);
		}
	}
	
	public class Html2Text extends HTMLEditorKit.ParserCallback {
	    StringBuffer s;

	    public Html2Text() {
	    }

	    public void parse(Reader in) throws IOException {
	        s = new StringBuffer();
	        ParserDelegator delegator = new ParserDelegator();
	        // the third parameter is TRUE to ignore charset directive
	        delegator.parse(in, this, Boolean.TRUE);
	    }

	    public void handleText(char[] text, int pos) {
	        s.append(text);
	    }

	    public String getText() {
	        return s.toString();
	    }

	}
	
	private void save() {
		try {
			Files.write(Paths.get(settingsFile.toURI()),
					("{\"intro\":" + intro + ",\"name\":\"" + name + "\",\"silenceOnStartup\":" + silenceOnStartup + "}").getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void read() {
		try {
			JSONObject json = new JSONObject(new Scanner(settingsFile).nextLine());
			name = json.getString("name");
			intro = json.getBoolean("intro");
			silenceOnStartup = json.getBoolean("silenceOnStartup");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private void initTextChat(FXAgent agent) {
		JFrame chatFrame = new JFrame("JBuddy Text Chat");
		chatFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		chatFrame.setSize(300, 150);
		chatFrame.setLayout(new BorderLayout());
		ChatterBotFactory factory = new ChatterBotFactory();
		ChatterBot bot2 = null;
		try {
			bot2 = factory.create(ChatterBotType.PANDORABOTS, "b0dafd24ee35a477");
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		final ChatterBotSession bot2session = bot2.createSession();
        JTextArea chatArea = new JTextArea();
        chatArea.setEditable(false);
        DefaultCaret caret = (DefaultCaret) chatArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
        JScrollPane chatScroll = new JScrollPane(chatArea);
        JPanel chatTextPanel = new JPanel();
        chatTextPanel.setLayout(new BorderLayout());
        JTextField enterTextField = new JTextField();
        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				try {
					// Think
					String thought = bot2session.think(enterTextField.getText());
					
					// Fix quirks
					thought = thought.replace("Chomsky", "Peedy");
					if(thought.contains("My name is")) thought = "My name is Peedy.";
					thought = thought
									// "I am one hell of a chatbot" doesn't really make sense in the context of Peedy.
									.replace("I am one hell of a chatbot.", "I am one hell of a parrot.")
									// Peedy already knows your name if you're using this chat!
									.replace("What is your name?", "")
									// Two spaces often appear instead of one; this fixes that
							        .replace("  ", " ");
					chatArea.setText(chatArea.getText() + 
							"You: " + enterTextField.getText() + "\n"
							+ "Peedy: " + thought
							+ "\n");
					agent.say(thought, () -> {
						chatFrame.toFront();
						chatFrame.requestFocus();
						enterTextField.requestFocus();
					});
					enterTextField.setText(null);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
        	
        });
        chatTextPanel.add(enterTextField, BorderLayout.CENTER);
        chatTextPanel.add(sendButton, BorderLayout.EAST);
        chatFrame.add(chatScroll, BorderLayout.CENTER);
        chatFrame.add(chatTextPanel, BorderLayout.SOUTH);
        chatFrame.getRootPane().setDefaultButton(sendButton);
        chatFrame.setVisible(true);
        enterTextField.requestFocus();
	}
	
	/**
	 * The icon used for the Tell Joke button.
	*/
	private static final BufferedImage JOKE_ICON = parse("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");
	
	/**
	 * The icon used for the Tell Fact button.
	*/
	private static final BufferedImage FACT_ICON = parse("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");
	
	/**
	 * The icon used for the Quick Read button.
	*/
	private static final BufferedImage READ_ICON = parse("iVBORw0KGgoAAAANSUhEUgAAACYAAAAmCAYAAACoPemuAAAEI0lEQVR4Ae1YA7fmSBDtNX7F2hzbtm"
			+ "3btm3btm3btj0TJx+S3K0a5I3nvW9znvucCm91birV6dslXjRz6yAxg/bqhDLfKsPydZQHZD0g980gy30zumTw3zKocv/MB+Uhubqokyv9BECY24aIV5o2t4EwVn"
			+ "f9TBlRoDw5HJd6pralHikRK9YzlUNEzyjD8lTTFzb9XJtW5RkpfUFTYW4Z/KkyLH9rqXdalcFxYr3SGMrQ3F31pa0/16ZXE+JR3a+FMiJ/Of9JRUhueP5at37mnJ"
			+ "pa9Vu5X6ajfCM+mNw3/VllbLGfhTI0T1sfcsrPnHPlwTl7CXlAll18IT4ZjdYTgkbh43hHrE8GS1B+Oe8Fdv8XUrd/oqxHCg43WWq+71338N3+9c4Zw1j2eaUfPn7"
			+ "/5wQRy8yO7xolUEYWgTazHoxVPWHtnITgkSUInV6P0NktCB5fBWvrGGhTa4CxTESdXBXW5pF0byVjCLsBwaNLYe2aAnNNX2izG0EZVYzwad9DLOX7iKWAubY/HPU+"
			+ "YIcA14EbDsA1JDjKPTjSbbj6Y8AJww3qCBycj8DeWXAtla7ZfI8xjCWfJ3BDFuC6YLyjPYS5aURkEeM3Cl3YAdhhBPbPhb64HbQp1aGMLg5lWAEoQ/NSNItCn9cM4"
			+ "Sv7nj0ULsLXj0Bf0IqjwhjC5qfj4lCnVIW+qC0Ce2aASYavHeRcioBYbyJ2cRdcU34W+q5/ReVH9xRsfMzXiWwJLzLqhArPsK/j2JexRJS/Qvj6Ych9/y+xMSXfk7"
			+ "ApIPfPCvveeTiPrkEelIvJvHMgKcMLxiKxAdmiiA1OJpZMLJlYMrFkYomA2Kjiz+fKv8n+ec3+fm1KyknYt+P8nSstFfrcptBm1IGxrDPMDUNgbRsLa8dEli7Q57f"
			+ "gSdwjpo4tzXi6N5wwEwg7jn1gLO/Cug7arPpwtUeRE2PhFzq3BdzcgOZpMtZafMzajLUVn9sPLnNkGQf7/kXvOmNgB/n4uW+YMeDGUknqkz4CYpQn+sI2CF89gOCJ1"
			+ "bC2juaIQZ/TGNq0WqzN+O0pMsM4UnjRHOkWY6HNbsgYxrJqhbG0I6wto1jZcp/cV+TSmk2mt2Kglye8f83U8eUQvnYI4ZvHSVpXeSvG8+2Rivvkvj8grft6i5GIjaM"
			+ "rD8xOiZ+Dj/1YW8Kv5ZunVn3oi3PPEnL/eLngPSnkobnjX4lgSO6+Qp1UnooqGeNRUSXDOXV86V/Fw1qfCSr9lKf/VnwoQ5nKsLy1bqfgauKcusJY2/1Tqvq0ob+9F"
			+ "pekiEMPY2XHL7Q59V6UOhtSqbPbZxS5ilR2PMnlx9gtdWY4pwzPV1Nf0PgLdWpl8Uoztw4WACjnKv5AydeZRuthcjCkPukg9fbXuE8KgEUlsBP0rF7quOI/A4GPzK1"
			+ "DPT7/AfaheYornLoLAAAAAElFTkSuQmCC");
	
	/**
	 * The icon used for the Text Chat button.
	*/
	private static final BufferedImage CHAT_ICON = parse("iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAABe0lEQVR4Ae3YNXLzUBRA4Z+ZmTFZR5jbM"
			+ "HNSujVK7gOb8D5ceQthZmYm5WTmhc1+Mt+ZrzIdk3RHd5KTnFibt1+/nHmKX/gt0Xc8gJTAdAxiHGMSTMCF77ICC7APTaIx/JYdeAwHrFCCYINTr8ADoQChPJeSkIH52"
			+ "BLyozHwA7KFD2ELFA94DQNUKG6osMEs2DzdV1BhRSqkBP7GGDSJ9lGgR+AaxkIwr3dgN36EcOqqw4GgS6ACiYejZGACBAr38Q0/0B1tgXdRhV6MYS3aAquwAE1AdATmI"
			+ "u9K3Cw6oUCFAa8jHWiGhjmU4K6b549ooBWLbuKiIjAPzajwPy78f5JnuC+eU0qgEzYoUGFHEe76sZZZ4cCxm2VBWqA7i6jAH3/XMtmB3+HCBMZu2ICGeZgw7sdaNo5BpM"
			+ "sKfIDv+H3DT1RhFhq2ceDnWvYLT6H7JY+7KMEcNCH4P1O4IuUHyo9URWBURi7CLgKjMrIC7SIwKiPv41WQgclJzinhaskq5VwFdQAAAABJRU5ErkJggg==");
	
	/**
	 * The icon used for the Voice Chat button.
	*/
	private static final BufferedImage VOICE_CHAT_ICON = parse("iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAYAAACM/rhtAAACKklEQVR4Ae3XA4ycYRDG8a+2zbOi2rYZ"
			+ "l9GFG9a2gxpx4yqoG9QON05t257+kzy1lh/S701+h9mZuef8rhP0E57w1GhQ35FKaCSVVPNFsCaYisO4IIdVa6Iez8J1wFl8gv3kkx7rqF7XwxUgCsM7HMMKOaaaqadAM64F"
			+ "LIk1MLzGHFSHI9VVew1Tb0k3A2bgAgxbURE/9Ki2DabeDDcDdsVLfMI41X7sgR77pN6ubgYcgHcyEH/qG4h3MsCrgAPwx74woD5QaTRCBiolGlCzGWiknSkL2AgncBMR9E0gY"
			+ "F/N3tSuRqkMmIErMMxINKBmTbsy/tuAM9EngT8zfTSbloANcR6GJeicwB/qzpo17WqYyoBVcBKG7cjFBRi2xfKvTjM7YNpVJXUB66NB/Q0wXEURVsV6WVBvkWZ5n10N6zkpCy"
			+ "hD8RqG+SiM+bpFr2ZMO4ak9PqlZVWxH4b7GIi2OPOXC+sZ9QzUjGlHVaTlktoDd2C4gpHIwEQcwgU5pFqGeq7ANNsjLZdXLS2BYjyB6fUmdEEGsiVDtU0/9RZrR1ovCqUwHld"
			+ "h8gxRHJCoaiZXNVMKrtxmSqAlNuPeX34G76mnpWYcV05N/QyhLJojgl34KLtVa66exH/m3LwjehZORuOjjFbNN+EaYC9M9qrmwJNQeZiFOViKU/gIk4+qLVXPLM24+vP2Fhajt"
			+ "24/aeqO87iKK3+jnvOacS1gBTRFRoyaasYJ2glPeD4Dz2Z8ZWVapfoAAAAASUVORK5CYII=");
	
	/**
	 * The icon used for the Launch TTS Studio icon.
	*/
	private static final BufferedImage TTS_STUDIO_ICON = parse("iVBORw0KGgoAAAANSUhEUgAAACgAAAAoCAQAAAAm93DmAAABSUlEQVR4Ae3QJVQsUBRG4f0eUHB3l76wPn"
			+ "l672Hi9AC9YQ13qzQSknB3K7gmbNzlLA6XOP/u3xXMRxHtrLFDFw1/wZUyyRcOT/PUm3IFzOIgWKf5c+fCwC3IQN8/AHLIAKrDyFVYUrXsykYSUMoU3WSFkZ+0gUPVN8MUeLhZD"
			+ "xBOTlKIkusnL8A5CCdnKQVMuDCymEIIBb944Unomb5wLoxMAwgHr7DSREvcmsgiyIU3RXY0eEw54uJyM5SCHpQ5PShyKfzTgqXMxOWKsJEhgEouj2EWBVDNDeBgWQDV3DcOlgRQ"
			+ "wWXS5+EEUMNBE484RFDFQQtPClDm9GASPRKnB0vYlTg9aOFN4vSgXeL0YDITAqcFw37wjX26KQUz0MINe0xgx0IJyWAK1gmQHhQmgwnwnFZKKf9xJVh5kcAPF3ms6oovCfxtCdA"
			+ "J1JjyUl1WVEEAAAAASUVORK5CYII=");
	
	private static BufferedImage parse(String base64) {
		byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(base64);
		ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes);
		try {
			return ImageIO.read(bis);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

}
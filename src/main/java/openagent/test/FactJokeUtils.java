package openagent.test;

import java.util.Random;

import javafx.application.Platform;

import openagent.api.javafx.FXAgent;

public abstract class FactJokeUtils {
	
	static String lineOne = "";
	static String punchline = "";
	
	public static void tellJoke(FXAgent toUse) {
		/*
		 * Joke template:
		 * case 1:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "";
			punchline = "";
			break;
		*/
		int randJoke = new Random().nextInt(39) + 1;
		switch(randJoke) {
		// Sources 
		case 1:
			lineOne = "Where do pencils go for vacation?";
			punchline = "Pencil-vania!";
			break;
		case 2:
			lineOne = "Why did the boy bring a ladder to school?";
			punchline = "Because he wanted to go to high school!";
			break;
		case 3:
			lineOne = "Why did the kid throw the butter out the window?";
			punchline = "Because he wanted to see a butterfly!";
			break;
		case 4:
			lineOne = "Why did the girl spread peanut butter on the road?";
			punchline = "To go with the traffic jam!";
			break;
		case 5:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What kind of dinosaur loves to sleep?";
			punchline = "A stega-snore-us!";
			break;
		case 6:
			// Source: globewalldesk
			lineOne = "26 cans each have a letter of the alphabet on them. Which one is the sweetest?";
			punchline = "Can D!";
			break;
		case 7:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you get when you cross a snake with a pie?";
			punchline = "";
			break;
		case 8:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you get when you cross a snake with a pie?";
			punchline = "A pie-thon!";
			break;
		case 9:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Where do sheep go to get their hair cut?";
			punchline = "The baa-baa shop!";
			break;
		case 10:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you call a group of disorganized cats?";
			punchline = "A cat-tastrophe!";
			break;
		case 11:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What's the most musical part of the chicken?";
			punchline = "The drumstick!";
			break;
		case 12:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Why did the banana go to the hospital?";
			punchline = "He was peeling bad.";
			break;
		case 13:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you call cheese that's not yours?";
			punchline = "Nach-o cheese!";
			break;
		case 14:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Where do hamburgers go to dance?";
			punchline = "The meat-ball!";
			break;
		case 15:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "How does the ocean say hello?";
			punchline = "It waves!";
			break;
		case 16:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What washes up on very small beaches?";
			punchline = "Micro-waves!";
			break;
		case 17:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What did the tree say to the wind?";
			punchline = "Leaf me alone!";
			break;
		case 18:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do elves learn at school?";
			punchline = "The elf-abet!";
			break;
		case 19:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What three candies can you find in every school?";
			punchline = "Nerds, DumDums, and Smarties.";
			break;
		case 20:
			lineOne = "Why couldn't the bike stand up?";
			punchline = "It was two-tired!";
			break;
		case 21:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Why did the computer go to the doctor?";
			punchline = "It had a virus.";
			break;
		case 22:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What is the longest word in the dictionary?";
			punchline = "\"Smiles,\" because there's a \"mile\" between each \"s.\"";
			break;
		case 23:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "When is a door not a door?";
			punchline = "When it's a-jar!";
			break;
		case 24:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What can be broken, but can't be held?";
			punchline = "A promise!";
			break;
		case 25:
			// Source: http://random-ize.com/bad-jokes/
			lineOne = "Hear about the two peanuts that walked through Central Park?";
			punchline = "One was a-salted.";
			break;
		case 26:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "I broke my arm in two places, you know what the doctor said?";
			punchline = "Stay out of those places!";
			break;
		case 27:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you call a can opener that doesn't work?";
			punchline = "A can't opener!";
			break;
		case 28:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do you call a king who is only 12 inches tall?";
			punchline = "A ruler!";
			break;
		case 29:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What's the tallest building in the world?";
			punchline = "The library, because it has the most stories!";
			break;
		case 30:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Why did the golfer wear two pairs of pants?";
			punchline = "In case he got a hole in one!";
			break;
		case 31:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What do call it when you help a lemon that's in trouble?";
			punchline = "Lemon aid!";
			break;
		case 32:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What kind of music do planets listen to?";
			punchline = "Nep-tunes!";
			break;
		case 33:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "When do doctors get mad?";
			punchline = "When they run out of patients!";
			break;
		case 34:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What kind of shoes do all spies wear?";
			punchline = "Sneakers!";
			break;
		case 35:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "Why were the teacher's eyes crossed?";
			punchline = "She couldn't control her pupils!";
			break;
		case 36:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What musical instrument is found in the bathroom?";
			punchline = "A tube a toothpaste!";
			break;
		case 37:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What never asks questions, but is often answered?";
			punchline = "A doorbell!";
			break;
		case 38:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What did the teddy bear say when he was offered seconds?";
			punchline = "No thank you, I'm stuffed.";
			break;
		case 39:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What kind of lion never roars?";
			punchline = "A dandy lion!";
			break;
		case 40:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "What runs around a yard without moving?";
			punchline = "A fence!";
			break;
		case 41:
			// Source: https://www.care.com/c/stories/3776/101-funny-jokes-for-kids/
			lineOne = "";
			punchline = "";
			break;
		default:
			// Placeholder joke; this one should never be told
			lineOne = "Why did the chicken cross the road?";
			punchline = "To get to the other side!";
			break;
		}
		new Thread(() -> {
			Platform.runLater(() -> toUse.say(lineOne, () -> {
				try {
					Thread.sleep(1200);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				toUse.say(punchline);
			}));
		}).start();		
	}
	
	public static void tellFact(FXAgent toUse) {
		/*
		 * Fact template:
		 * case 1:
		 	// Source: 
			toTell = "";
			break;
		*/
		int randFact = new Random().nextInt(13) + 1;
		String toTell = "";
		switch(randFact) {
		case 1:
			// Source: https://www.thefactsite.com/2011/07/top-100-random-funny-facts.html
			toTell = "If you lift a kangaroo's tail off the ground, it can't hop.";
			break;
		case 2:
			// Source: https://www.thefactsite.com/2011/07/top-100-random-funny-facts.html
			toTell = "Cherophobia is the fear of fun.";
			break;
		case 3:
			// Source: Wikipedia (Wandering Albatross)
			toTell = "The wandering albatross has the largest wingspan of any living bird, typically ranging from 2.51 to 3.5 meters.";
			break;
		case 4:
			// Source: http://scifun.chem.wisc.edu/homeexpts/cans.htm
			toTell = "Diet soda cans float and regular soda cans don't because regular soda has more dissolved sweetener, thus increasing its density.";
			break;
		case 5:
			// Unknown source
			toTell = "The Earth rotates around its axis at just over 1,000 miles per hour.";
			break;
		case 6:
			// Source: http://en.m.wikipedia.org/wiki/National_symbols_of_Scotland
			toTell = "The unicorn is the national animal of Scotland.";
			break;
		case 7:
			// Source: http://blog.nwf.org/2011/03/the-fascinating-things-about-creatures-that-swim/
			toTell = "The fastest flying bird is the spine-tailed swift of Siberia, which can fly at over 100 miles per hour."
					+ " The fastest swimming bird is the Gentoo penguin, which can swim at speeds of up to 22 miles per hour.";
			break;
		case 8:
			// Unknown source
			toTell = "Braces were invented in the 1800s, but people's obsession with straight teeth dates back to ancient Egyptian times.";
			break;
		case 9:
			// Source: http://m.kidshealth.org/kid/grow/boy/adams_apple.html
			toTell = "The Adam's apple in men is named after a piece of the forbidden fruit that got stuck in Adam's throat.";
			break;
		case 10:
			// Source: http://www.windows2universe.org/kids_space/sat.html
			toTell = "2,271 satellites currently orbit the earth.";
			break;
		case 11:
			// Source: http://www.brainline.org/content/2012/07/can-the-brain-itself-feel-pain.html
			toTell = "The brain itself cannot feel pain. Headaches are pains in the tissue surrounding the brain, not the brain itself.";
			break;
		case 12:
			// Source: Wikipedia - Shark (http://en.m.wikipedia.org/wiki/Shark)
			toTell = "Sharks cannot blink. They do not need to, because the surrounding water cleans their eyes.";
			break;
		default:
			break;
		}
		toUse.say(toTell);
	}

}

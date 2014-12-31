package com.belvain.soswe.util;

import java.util.Random;

public class NameGenerator {
	
	private static String[] names = {
	    "Face Sister",
	    "The Severed Goat",
	    "Super Desolation",
	    "The Crimson Poem",
	    "Door Bear",
	    "The White Dragon",
	    "Sister-Lad",
	    "Sale",
	    "Lion-Lass",
	    "The Wicked Crone",
	    "Storyteller-Woman",
	    "Captain Knowledge",
	    "Captain Thunder",
	    "Sentinel",
	    "Walrus",
	    "Witch Demon",
	    "Sergeant Rust",
	    "The Gothic Elephant",
	    "The Endless Door",
	    "Sun Sparrow",
	    "The Clear Rhinoceros",
	    "Professor Devotion",
	    "Commander Chicken",
	    "The Gothic Bread",
	    "Habit",
	    "The Evil Owl",
	    "Sunshine Girl",
	    "Fright",
	    "Moral",
	    "Captain Exuberance",
	    "The Happy Palace",
	    "Head Rhinoceros",
	    "The Green Fly",
	    "Super Fall",
	    "Super Qualm",
	    "Anxietylord",
	    "Lord Thief",
	    "Childhood Girl",
	    "Yesterday Woman",
	    "Warning",
	    "Super Disgust",
	    "Lie Boy",
	    "Sister Daze",
	    "Frog",
	    "Captain Brother",
	    "Unity-Man",
	    "Crown Ash",
	    "Captain Glass",
	    "Hippopotamus",
	    "The Tears",
	    "Super Shadow",
	    "Falcon Vampire",
	    "The Warm Whisper",
	    "The Sacred Father",
	    "Child Woman",
	    "Dolphin Shroud",
	    "Diamond",
	    "Grave-Lass",
	    "Horn Mouse",
	    "The Rainy Devil",
	    "Professor Darkness",
	    "Sergeant Earnestness",
	    "King Fly",
	    "Captain Hammer",
	    "Letter Face",
	    "Thunder Man",
	    "Man Sunshine",
	    "Super Ambassador",
	    "Power Wife",
	    "Storyteller Rat",
	    "Storm Girl",
	    "Power Door",
	    "The Dragon",
	    "Oyster-Man",
	    "The Crimson Ape",
	    "King Hobby",
	    "Gossip Boy",
	    "Penalty Boy",
	    "The Warm Table",
	    "Breeze Lass"
	};
	
	/**
	 * Generater funny name for a node
	 * @return Name for a node
	 */
	public static String Generate(){
		Random ran = new Random();
		String name = names[ran.nextInt(names.length)];
		return name;
	}

}

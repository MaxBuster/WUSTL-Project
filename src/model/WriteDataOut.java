package model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;

import org.apache.commons.math3.util.Pair;

import com.orsoncharts.util.json.JSONArray;
import com.orsoncharts.util.json.JSONObject;

public class WriteDataOut {
	public static void writeData(ArrayList<GameInfo> gameInfo) {
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(new File("data.csv")));
			for (GameInfo game : gameInfo) {
				// Game Info
				writer.write("// Game: " + game.getGameNum() + "\n");
				writer.write("// Budget: " + game.getBudget() + "\n");
				int[] dist = game.getDistribution();
				writer.write("// Distribution: " + implode(dist));
				int[] idealPts = game.getIdealPts();
				writer.write("// Ideal Points: " + implode(idealPts));
				// Player Info
				LinkedList<Player> players = game.getPlayers();
				for (Player player : players) {
					writer.write("// Player Number: " + player.getPlayerNumber() + "\n");
					writer.write("Ideal Point:, " + player.getIdealPt() + "\n");
					writer.write("Votes:, " + implode(player.getVotes()));
					
					ArrayList<Pair<Integer, Integer>> signals1 = new ArrayList<Pair<Integer, Integer>>();
					ArrayList<Pair<Integer, Integer>> signals2 = new ArrayList<Pair<Integer, Integer>>();
					for (int i=0; i<idealPts.length; i++) {
						Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> purchasedInfo = player.getInfoByRound(i);
						Pair<Integer, Integer> R1Signals = purchasedInfo.getFirst();
						signals1.add(R1Signals);
						Pair<Integer, Integer> R2Signals = purchasedInfo.getSecond();
						signals2.add(R2Signals);
					}
					
					writer.write("1st Ones:, " + implodeFirst(signals1));
					writer.write("1st Tokens:, " + implodeSecond(signals1));
					writer.write("2nd Ones:, " + implodeFirst(signals2));
					writer.write("2nd Tokens:, " + implodeSecond(signals2));
				}

				writer.write("\n");
			}
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static String implode(int[] input) {
		String output = "";
		for (int i=0; i<input.length-1; i++) {
			output += input[i] + ",";
		}
		output += input[input.length-1] + "\n";
		return output;
	}
	
	public static String implodeFirst(ArrayList<Pair<Integer, Integer>> input) {
		String output = "";
		for (int i=0; i<input.size()-1; i++) {
			output += input.get(i).getFirst() + ",";
		}
		output += input.get(input.size()-1).getFirst() + "\n";
		return output;
	}
	
	public static String implodeSecond(ArrayList<Pair<Integer, Integer>> input) {
		String output = "";
		for (int i=0; i<input.size()-1; i++) {
			output += input.get(i).getSecond() + ",";
		}
		output += input.get(input.size()-1).getSecond() + "\n";
		return output;
	}
}

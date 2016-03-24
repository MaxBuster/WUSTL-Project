package model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Vector;

public class ReadConfig {
	public static int multiplier;
	public static int intercept;

	public static String[] cleanArray(String[] input) {
		Vector<String> cleaned = new Vector<String>();
		for (String s : input) {
			if (!s.isEmpty()) {
				cleaned.add(s);
			}
		}
		String[] cleanedArray = new String[cleaned.size()];
		for (int i = 0; i<cleaned.size(); i++) {
			cleanedArray[i] = cleaned.get(i);
		}
		return cleanedArray;
	}

	public static ArrayList<GameInfo> readFile() {
		String fileName = "config.csv";
		File file = new File(fileName);
		if (file.canRead()) {
			try {
				ArrayList<GameInfo> games = new ArrayList<GameInfo>();
				FileReader fReader = new FileReader(file);
				BufferedReader bReader = new BufferedReader(fReader);
				int gameNum = 1;
				String line;
				if ((line = bReader.readLine()) == null  || line.isEmpty()) {
					bReader.close();
					return null;
				}
				line = line.replace(",", "");
				multiplier = Integer.parseInt(line);
				if ((line = bReader.readLine()) == null  || line.isEmpty()) {
					bReader.close();
					return null;
				}
				line = line.replace(",", "");
				intercept = Integer.parseInt(line);
				while ((line = bReader.readLine()) != null) {
					line = bReader.readLine();
					if (line == null || line.isEmpty()){
						bReader.close();
						return null;
					}
					String[] candidates = cleanArray(line.split("[ ]*,[ ]*"));
					int[] candidateNums = new int[candidates.length];
					for (int i=0; i<candidates.length; i++) {
						candidateNums[i] = Integer.parseInt(candidates[i]); // FIXME parse int check
					}
					if ((line = bReader.readLine()) == null || line.isEmpty()) {
						bReader.close();
						return null;
					}
					String[] parties = cleanArray(line.split("[ ]*,[ ]*"));
					char[] partyChars = new char[parties.length];
					for (int i=0; i<parties.length; i++) {
						partyChars[i] = parties[i].charAt(0);
					}
					if (partyChars.length != candidateNums.length) {
						bReader.close();
						return null;
					}
					if ((line = bReader.readLine()) == null  || line.isEmpty()) {
						bReader.close();
						return null;
					}
					String[] distribution = cleanArray(line.split(","));
					int[] distributionNums = new int[distribution.length];
					for (int i=0; i<distribution.length; i++) {
						distributionNums[i] = Integer.parseInt(distribution[i]);
					}
					if (distributionNums.length != 4) {
						bReader.close();
						return null;
					}
					if ((line = bReader.readLine()) == null  || line.isEmpty()) {
						bReader.close();
						return null;
					}
					line = line.replace(",", "");
					int budget = Integer.parseInt(line); // FIXME Catch errors thrown by this
					GameInfo game = new GameInfo(gameNum, candidateNums, partyChars, distributionNums, budget);
					games.add(game);
					gameNum++;
				}
				bReader.close();
				return games;
			} catch (FileNotFoundException e) {
				return null;
			} catch (IOException e) {
				return null;
			} catch (NumberFormatException e) {
				return null;
			}
		} else {
			return null;
		}
	}
}

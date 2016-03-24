package model;

import java.util.ArrayList;
import java.util.LinkedList;

public class GameInfo {
	private int gameNum;
	private int[] idealPts;
	private char[] parties;
	private int[] distribution;
	private int budget;
	private LinkedList<Player> players = new LinkedList<Player>();
	private ArrayList<Candidate> candidates = new ArrayList<Candidate>();
	
	public GameInfo(int gameNum, int[] idealPts, char[] parties, int[] distribution, 
			int budget) {
		this.gameNum = gameNum;
		this.idealPts = idealPts;
		this.parties = parties;
		this.distribution = distribution;
		this.budget = budget;
	}
	
	public synchronized int getGameNum() {
		return gameNum;
	}

	public int[] getIdealPts() {
		return idealPts;
	}

	public char[] getParties() {
		return parties;
	}

	public int[] getDistribution() {
		return distribution;
	}

	public int getBudget() {
		return budget;
	}
	
	public synchronized void addPlayer(Player player) {
		players.add(player);
	}
	
	public synchronized LinkedList<Player> getPlayers() {
		return players;
	}
	
	public void setCandidates(ArrayList<Candidate> newCandidates) {
		candidates = newCandidates;
	}
	
	public ArrayList<Candidate> getCandidates() {
		return candidates;
	}
	
	public int getNumCandidates() {
		return candidates.size();
	}
}

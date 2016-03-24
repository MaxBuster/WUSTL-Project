package model;

import org.apache.commons.math3.util.Pair;

public class Player {
	private int playerNum;
	private char playerParty;
	private int idealPt;
	private int budget;
	
	private int[][][] info;
	private int[] votes;
	
	private boolean doneWithRound;
	private String round;

	public Player(int playerNum, char playerParty, int idealPt, int budget, int numCandidates) {
		super();
		this.playerNum = playerNum;
		this.playerParty = playerParty;
		this.idealPt = idealPt;
		this.budget = budget;
		this.round = "buy1";
		this.doneWithRound = false;
		this.info = new int[numCandidates][2][2];
		this.votes = new int[3];
	}
	
	public Player(Player player) {
		this.playerNum = player.getPlayerNumber();
		this.playerParty = player.getParty();
		this.idealPt = player.getIdealPt();
		this.budget = player.getBudget();
		this.info = player.getInfo();
		this.votes = player.getVotes();
	}
	
	public void resetPlayer(char playerParty, int idealPt, int budget, int numCandidates) {
		this.playerParty = playerParty;
		this.idealPt = idealPt;
		this.budget = budget;
		this.round = "buy1";
		this.doneWithRound = false;
		this.info = new int[numCandidates][2][2];
		this.votes = new int[3];
	}

	public int getPlayerNumber() {
		return playerNum;
	}

	public char getParty() {
		return playerParty;
	}

	public int getIdealPt() {
		return idealPt;
	}

	public int getBudget() {
		return budget;
	}

	public void spendBudget(int amount) {
		budget -= amount;
	}
	
	public void addInfo(int candidateNum, int info) {
		int buyRound = round == "buy1" ? 0 : 1;
		this.info[candidateNum][buyRound][info]++;
	}
	
	public Pair<Integer, Integer> getInfo(int candidateNum) {
		int ones = 0;
		int total = 0;
		int[] roundOne = this.info[candidateNum][0];
		int[] roundTwo = this.info[candidateNum][1];
		// Get all ones
		ones += roundOne[1];
		ones += roundTwo[1];
		// Get all signals
		total += roundOne[0];
		total += roundOne[1];
		total += roundTwo[0];
		total += roundTwo[1];
		
		Pair<Integer, Integer> signals = new Pair<Integer, Integer>(ones, total);
		return signals;
	}
	
	public Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> getInfoByRound(int candidateNum) {
		int onesR1 = 0;
		int onesR2 = 0;
		int totalR1 = 0;
		int totalR2 = 0;
		int[] roundOne = this.info[candidateNum][0];
		int[] roundTwo = this.info[candidateNum][1];
		// Get all ones
		onesR1 += roundOne[1];
		onesR2 += roundTwo[1];
		// Get all signals
		totalR1 += roundOne[0];
		totalR1 += roundOne[1];
		totalR2 += roundTwo[0];
		totalR2 += roundTwo[1];
		
		Pair<Integer, Integer> R1Signals = new Pair<Integer, Integer>(onesR1, totalR1);
		Pair<Integer, Integer> R2Signals = new Pair<Integer, Integer>(onesR2, totalR2);
		
		Pair<Pair<Integer, Integer>, Pair<Integer, Integer>> signals = 
				new Pair<Pair<Integer, Integer>, Pair<Integer, Integer>>(R1Signals, R2Signals);
		
		return signals;
	}
	
	public int[][][] getInfo() {
		return this.info;
	}

	public synchronized void doneWithRound() {
		this.doneWithRound = true;
	}
	
	public void addVote(int candidateNum, int roundNum) {
		this.votes[roundNum] = candidateNum+1;
	}
	
	public int[] getVotes() {
		return this.votes;
	}
	
	public void setRound(String newRound) {
		this.round = newRound;
	}
	
	public String getRound() {
		return this.round;
	}

	public synchronized boolean checkIfDone() {
		return this.doneWithRound;
	}

	public void newRound() {
		this.doneWithRound = false;
	}
}

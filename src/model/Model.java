package model;

import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

public class Model {
	private PropertyChangeSupport PCS;
	private static int playerNumber;
	private static int candidateNumber;
	private int[] sumPoints;
	private int sumDataPoints;
	private ArrayList<Player> players;
	private boolean startGame = true;
	private int roundNum = 0;
	private String[] roundNames = new String[]{"First Buy", "Straw Vote", "First Vote", "Second Buy", "Second Vote", "Over"};
	private int gameNum = 0;
	private ArrayList<GameInfo> gameInfo;
	private int numDone = 0;
	
	public Model(final PropertyChangeSupport PCS, ArrayList<GameInfo> gameInfo) {
		this.PCS = PCS;
		this.gameInfo = gameInfo;
		players = new ArrayList<Player>();
		setNewGame(gameInfo.get(0));
	}
	
	public synchronized void addPlayerToGameObject(Player player, int currentGame) {
		gameInfo.get(currentGame).addPlayer(player);
	}
	
	public synchronized void getNewGame(int gameNum) {
		if (this.gameNum < gameNum) {
			PCS.firePropertyChange("New Round", null, roundNames[0]);
			PCS.firePropertyChange("New Game", null, gameNum);
			this.gameNum++;
			setNewGame(gameInfo.get(gameNum));
		}
	}
	
	public void setNewGame(GameInfo game) { 
		setGraphData(game.getDistribution());
		int[] candIdealPts = game.getIdealPts();
		char[] candParties = game.getParties();
		ArrayList<Candidate> candidates = new ArrayList<Candidate>();
		for (int i = 0; i < candIdealPts.length; i++) {
			Candidate candidate = new Candidate(i+1, candParties[i], candIdealPts[i]);
			candidates.add(candidate);
		}
		gameInfo.get(gameNum).setCandidates(candidates);
	}
	
	public void setGraphData(int[] graphData) {
		GetDistribution distribution = new GetDistribution(graphData);
		sumPoints = distribution.getSumData();
		sumDataPoints = distribution.getSum();
	}
	
	public int getBudget() {
		return gameInfo.get(gameNum).getBudget();
	}

	public synchronized boolean getStartGame() {
		return startGame;
	}
	
	public synchronized void setStartGame(boolean newStartGame) {
		startGame = newStartGame;
	}
	
	public int[] getData() {
		return gameInfo.get(gameNum).getDistribution();
	}

	public int getCandidateNumber() {
		candidateNumber++;
		return candidateNumber;
	}

	public ArrayList<Candidate> getCandidates() {
		return gameInfo.get(gameNum).getCandidates();
	}

	public synchronized Player newPlayer() {
		int playerNum = getPlayerNumber();
		int idealPt = getIdealPt();
		char party = getParty(idealPt);
		Player player = new Player(playerNum, party, idealPt, getBudget(), gameInfo.get(gameNum).getNumCandidates());
		players.add(player);
		PCS.firePropertyChange("New Player", null, playerNum);
		return player;
	}
	
	public synchronized void resetPlayer(Player player) {
		int idealPt = getIdealPt();
		char party = getParty(idealPt);
		player.resetPlayer(party, idealPt, getBudget(), gameInfo.get(gameNum).getNumCandidates());
	}
	
	public synchronized void removePlayer(Player player) {
		if (players.contains(player)) {
			players.remove(player);
		}
		PCS.firePropertyChange("Removed Player", null, player.getPlayerNumber());
	}

	public synchronized int getPlayerNumber() {
		playerNumber++;
		return playerNumber;
	}

	public int getIdealPt() {
		double randomNum = Math.random();
		double unroundedPt = randomNum * sumDataPoints;
		for (int i=0; i<100; i++) {
			if (sumPoints[i] > unroundedPt) {
				return i;
			}
		}
		return 100;
	}

	public char getParty(int idealPt) {
		if (idealPt < 50) {
			return 'R';
		} else {
			return 'D';
		}
	}

	public Candidate getCandidate(int candNum) {
		return gameInfo.get(gameNum).getCandidates().get(candNum);
	}
	
	public synchronized int getNumPlayers() {
		return players.size();
	}
	
	public synchronized Player getPlayer(int playerNum) {
		for (Player player : players) {
			if (player.getPlayerNumber() == playerNum) {
				return player;
			}
		}
		return null;
	}
	
	public ArrayList<Candidate> getSortedCandidates() {
		ArrayList<Candidate> tempCands = new ArrayList<Candidate>(gameInfo.get(gameNum).getCandidates());
		Collections.sort(tempCands, new Comparator<Candidate>() {
			public int compare(Candidate cand1, Candidate cand2) {
				return cand2.getFirstVotes() - cand1.getFirstVotes();
			}
		});
		return tempCands;
	}
	
	public Candidate getWinner(ArrayList<Candidate> candidates) {
		ArrayList<Candidate> tempCands = new ArrayList<Candidate>(candidates);
		Collections.sort(tempCands, new Comparator<Candidate>() {
			public int compare(Candidate cand1, Candidate cand2) {
				return cand2.getSecondVotes() - cand1.getSecondVotes();
			}
		});
		return tempCands.get(0);
	}

	public synchronized boolean checkEndRound() {
		boolean done = true;
		for (Player player : players) {
			if (!player.checkIfDone()) {
				done = false;
			}
		}
		if (done == true) {
			roundNum = (roundNum+1)%6;
			PCS.firePropertyChange("New Round", null, this.roundNames[roundNum]);
		}
		return done;
	}
	
	public int getNumGames() {
		return gameInfo.size();
	}
	
	public synchronized void writeDataOut() {
		numDone++;
		if (numDone == players.size()) {
			WriteDataOut.writeData(gameInfo);
		}
		PCS.firePropertyChange("Game Over", null, null);
	}
	
	public synchronized void writeDataPrematurely() {
		WriteDataOut.writeData(gameInfo);
	}
}

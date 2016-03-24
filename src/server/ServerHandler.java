package server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.commons.math3.util.Pair;

import model.Candidate;
import model.Model;
import model.Player;
import model.ReadConfig;

public class ServerHandler {
	private Model model;
	private Player player;
	private DataInputStream in;
	private DataOutputStream out;
	private static Object waitObject = new Object();
	private int gameNum = 0;

	public ServerHandler(Model model, DataInputStream in, DataOutputStream out) {
		this.model = model;
		this.player = model.newPlayer();
		this.in = in;
		this.out = out;
	}
	
	public int getPlayerNum() {
		return player.getPlayerNumber();
	}

	public void handleIO() {
		waitForGameStart();
		startGame();
		while (true) {
			String type = newMessage();
			int message;
			try {
				message = in.readInt(); 
			} catch (IOException e) {
				removePlayer();
				break; // End client
			}

			if (type.equals("Buy Info")) { // Bought info
				returnInfo(message);
			} else if (type.equals("End Buy")) { // Ended a buy round
				waitForNewRound(); // Waits for all players to be done with the round
				setRound(message); // Sets the round in the player's data
				startRound(message); // Sends out a starting round message
			} else if (type.equals("Vote")) {
				vote(message);
			} else {
				// Exceptions?
			}
		}
	}

	private void startGame() {
		writeMessage("Start Game");
		writeInt(player.getPlayerNumber());
		writeInt(player.getParty());
		writeInt(player.getIdealPt());
		writeInt(model.getBudget());
		writeInt(model.getNumGames());

		writeChartData();
		ArrayList<Candidate> candidates = model.getCandidates();
		writeMessage("Candidates");
		writeInt(candidates.size());
		for (int i = 0; i < candidates.size(); i++) {
			writeInt(candidates.get(i).getCandidateNumber());
			writeInt(candidates.get(i).getParty());
		}
	}

	private void returnInfo(int candidateToBuyFrom) {
		Candidate candidate = model.getCandidate(candidateToBuyFrom);
		int ideal = candidate.getIdealPt();
		int random = (int) (Math.random()*100);
		int signal = (random > ideal) ? 0 : 1;
		player.addInfo(candidateToBuyFrom, signal);
		Pair<Integer, Integer> info = player.getInfo(candidateToBuyFrom);
		writeMessage("Purchased Info");
		writeInt(candidateToBuyFrom);
		writeInt(info.getFirst()); // Ones
		writeInt(info.getSecond()); // Total Tokens
	}
	
	private void vote(int message) {
		ArrayList<Candidate> candidates = model.getCandidates();
		voteForCandidate(message);
		waitForNewRound();
		if (player.getRound() == "straw") {
			startFirstVote();
		} else if (player.getRound() == "first") {
			startSecondBuy();
		} else {
			sendWinner(candidates);
			Player clone = new Player(player);
			model.addPlayerToGameObject(clone, gameNum);
			gameNum++;
			if (gameNum < model.getNumGames()) {
				model.getNewGame(gameNum);
				model.resetPlayer(player);
				startGame();
			} else {
				writeMessage("Games Over");
				model.writeDataOut(); 
			}
		}
	}

	private void voteForCandidate(int candidateToVoteFor) {
		if (player.getRound() == "first") {
			model.getCandidate(candidateToVoteFor).voteFirst();
			player.addVote(candidateToVoteFor, 1);
		} else if (player.getRound() == "final") {
			model.getCandidate(candidateToVoteFor).voteSecond();
			player.addVote(candidateToVoteFor, 2);
		} else {
			model.getCandidate(candidateToVoteFor).voteStraw();
			player.addVote(candidateToVoteFor, 0);
		}
	}

	private void startFirstVote() {
		ArrayList<Candidate> candidates = model.getCandidates();
		player.setRound("first");
		writeMessage("Straw Results");
		writeInt(candidates.size());
		writeInt(0); // Start First Round FIXME Change to a var
		int numPlayers = model.getNumPlayers();
		for (Candidate candidate : candidates) {
			int numVotes = candidate.getStrawVotes();
			int percentVotes = ((numVotes*100)/numPlayers);
			writeInt(candidate.getCandidateNumber());
			writeInt(percentVotes);
		}
	}

	private void startSecondBuy() {
		player.setRound("buy2");
		ArrayList<Candidate> candidates = model.getSortedCandidates();
		writeMessage("First Results"); 
		writeInt(candidates.size());
		writeInt(1); // Start Last Round FIXME Change to a var
		int numPlayers = model.getNumPlayers();
		for (Candidate candidate : candidates) { // This writes the top candidates
			int numVotes = candidate.getFirstVotes();
			int percentVotes = ((numVotes*100)/numPlayers);
			writeInt(candidate.getCandidateNumber());
			writeInt(percentVotes);
		}
	}

	private void waitForGameStart() {
		synchronized (waitObject) {
			if (!model.getStartGame()) {
				try {
					waitObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			waitObject.notifyAll();
		}
	}

	public static void notifyWaiters() {
		synchronized (waitObject) {
			waitObject.notifyAll();
		}
	}

	private void waitForNewRound() {
		player.doneWithRound();
		synchronized (waitObject) {
			if (!model.checkEndRound()) {
				try {
					waitObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			waitObject.notifyAll();
		}
		player.newRound();
	}

	private void setRound(int whichBuyRound) {
		if (whichBuyRound == 1) {
			player.setRound("straw");
		} else {
			player.setRound("final");
		}
	}

	private void startRound(int whichBuyRound) {
		if (whichBuyRound == 1) { // FIXME Change number to var/enum
			writeMessage("Start Straw");
		} else {
			writeMessage("Start Final");
			player.setRound("final");
		}
	}

	private void writeChartData() {
		int[] chartData = model.getData();
		writeMessage("Chart Data");
		for (int i = 0; i < 4; i++) {
			writeInt(chartData[i]);
		}
	}

	private void sendWinner(ArrayList<Candidate> candidates) {
		Candidate winner = model.getWinner(candidates);
		writeMessage("Winner"); // Writes out the winner
		writeInt(winner.getCandidateNumber());
		int winnings = ReadConfig.intercept - (ReadConfig.multiplier*(Math.abs(winner.getIdealPt() - player.getIdealPt())));
		writeInt(winnings);
	}

	private String newMessage() {
		try {
			char c = (char) in.readByte();
			while (c != '!') {
				c = (char) in.readByte();
			}
			String type = in.readUTF();
			return type;
		} catch (IOException e) {
			removePlayer();
			return null;
		}
	}

	private void writeMessage(String type) {
		try {
			out.writeByte((int) '!');
			out.writeUTF(type);
		} catch (IOException e) {
			removePlayer();
		}
	}

	private void writeInt(int message) {
		try {
			out.writeInt(message);
		} catch (IOException e) {
			removePlayer();
		}
	}

	public void removePlayer() {
		model.removePlayer(player);
		if (model.checkEndRound()) {
			notifyWaiters();
		}
	}
}

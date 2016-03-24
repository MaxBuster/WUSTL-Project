package client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import javax.swing.JOptionPane;

import org.jfree.data.xy.IntervalXYDataset;

public class ClientHandler {
	private final PropertyChangeSupport PCS = new PropertyChangeSupport(this);
	private DataInputStream socketInputStream;
	private DataOutputStream socketOutputStream;
	private ClientJFrame gui;

	private final String[] TABLE2BUYNAMES = {"Candidate #", "Price", "Buy"};
	private final String[] TABLE2VOTENAMES = {"Candidate #", "Vote"};
	private final String[] TABLE1NAMES = {"Candidate #", "Party", "Best Guess", "Straw Votes", "First Round Votes"};

	private Object[][] TABLE2BUYDATA;
	private Object[][] TABLE2VOTEDATA;
	private Object[][] TABLE1DATA;

	private int playerNum;
	private int idealPt;
	private char party;
	private int budget;
	private int numGames;
	
	public ClientHandler(DataInputStream socketInputStream, DataOutputStream socketOutputStream) {
		PCS.addPropertyChangeListener(new ChangeListener());
		this.socketInputStream = socketInputStream;
		this.socketOutputStream = socketOutputStream;
		gui = new ClientJFrame(PCS);
		gui.setVisible(true);
	}

	public void handleIO() {
		while (true) {
			String type = newMessage();
			if (type.equals("Start Game")) { // Getting labels
				getAndSetLabels();
			} else if (type.equals("Chart Data")) { // Getting graph data
				getAndSetChart();
			} else if (type.equals("Candidates")) { // Getting candidates and starting the game
				getAndSetCandidateInfo();
			} else if (type.equals("Purchased Info")) { // Got info
				getAndSetPurchasedInfo();
			} else if (type.equals("Start Straw")) { // Starting the straw vote
				startStraw();
			} else if (type.equals("Straw Results") || type.equals("First Results")) {
				startRoundAfterVote();
			} else if (type.equals("Start Final")) {
				startFinal();
			} else if (type.equals("Winner")) {
				endCurrentGame();
			} else if (type.equals("Games Over")) {
				JOptionPane.showMessageDialog(null, "Game Over \nWinnings: " + gui.getWinnings());
				gui.allowClose();
			} else {
				// Read the rest and ignore
			}
		} 
	}

	private void getAndSetLabels() {
		playerNum = readInt();
		party = (char) readInt();
		idealPt = readInt();
		budget = readInt();
		numGames = readInt();
		gui.addLabels(playerNum, party, idealPt, budget, numGames);
	}

	private void getAndSetChart() {
		int[] chartInfo = new int[4];
		for (int i = 0; i < 4; i++) {
			chartInfo[i] = readInt();
		}
		double[] chartData = BiModalDist.getData(chartInfo);
		gui.addChart(chartData, idealPt);
	}

	private void getAndSetCandidateInfo() {
		int numCandidates = readInt();
		TABLE2BUYDATA = new Object[numCandidates][3];
		TABLE2VOTEDATA = new Object[numCandidates][2];
		TABLE1DATA = new Object[numCandidates][5];

		for (int i = 0; i < numCandidates; i++) {
			int candidateNumber = readInt();
			char candidateParty = (char) readInt();
			int infoPrice;
			if (candidateParty == party) { 
				infoPrice = 10;
			}
			else {
				infoPrice = 20;
			}
			double[] candidateExpectations  = BetaDist.getBetaDist(0, 0);
			for (int candidate = 0; candidate < numCandidates; candidate++) {
				IntervalXYDataset dataset = MakeChart.createDataset(candidateExpectations, "Candidate " + (candidate+1));
				gui.addDataset(candidate, dataset);
			}
			TABLE2BUYDATA[i] = new Object[] {candidateNumber, infoPrice, "Buy"}; 
			TABLE2VOTEDATA[i] = new Object[] {candidateNumber, "Vote"};
			TABLE1DATA[i] = new Object[] {candidateNumber, candidateParty, "50", "-------", "-------"};
		}

		String gameDescription = "There will five rounds: buy info, straw vote, first vote, buy info, second vote \n"
				+ "The goal is to try to get the candidate to win with the closest ideal point to you \n"
				+ "It is currently the first buy round. Candidates with the opposite party cost twice as much";
		gui.setTextPane(gameDescription);
		gui.setScrollPane1(TABLE1NAMES, TABLE1DATA);
		gui.setScrollPane2(TABLE2BUYNAMES, TABLE2BUYDATA, "Buy");
		gui.addEndRoundBtn(1);
		gui.updateGUI();
	}

	private void getAndSetPurchasedInfo() {
		int candidate = readInt();
		int signals = readInt();
		int tokens = readInt();

		int expected = ((signals+1)*100)/(tokens+2);
		double[] beta = BetaDist.getBetaDist(tokens, signals);
		IntervalXYDataset data = MakeChart.createDataset(beta, "Candidate " + (candidate+1));

		addToTable1Data(candidate, 2, expected);
		gui.removeScrollPane1();
		gui.setScrollPane1(TABLE1NAMES, TABLE1DATA);
		gui.addDataset(candidate, data);
		gui.updateGUI();
	}
	
	private void startStraw() {
		String strawVoteDescription = "It is now the straw vote. \n"
				+ "Vote based on the information that you bought from the previous round. \n"
				+ "This round will have no effect on the final winner, and is meant to give information about the other voters.";
		gui.setTextPane(strawVoteDescription);
		gui.removeScrollPane2();
		gui.setScrollPane2(TABLE2VOTENAMES, TABLE2VOTEDATA, "Vote");
		gui.updateGUI();
	}

	private void addToTable1Data(int candidateNumber, int position, Object data) {
		for (int i=0; i<TABLE1DATA.length; i++) {
			if (TABLE1DATA[i][0].equals(candidateNumber+1)) {
				TABLE1DATA[i][position] = data;
			}
		}
	}

	private void startRoundAfterVote() {
		int numCandidates = readInt();
		int round = readInt();
		// If its 0 then do straw if its 1 then start buy after
		int[] candNums = new int[2];
		for (int i = 0; i < numCandidates; i++) {
			int candNum = readInt();
			int numVotes = readInt();
			addToTable1Data(candNum-1, round+3, numVotes+"%"); 

			if (i < 2) {
				candNums[i] = candNum;
			}
		}
		Arrays.sort(candNums);
		if (round == 0) {
			String firstVoteDescription = "This is the first real vote. \n"
					+ "The top two candidates from this round will continue to the final vote.";
			gui.setTextPane(firstVoteDescription);
			gui.removeScrollPane1();
			gui.setScrollPane1(TABLE1NAMES, TABLE1DATA);
			gui.setScrollPane2(TABLE2VOTENAMES, TABLE2VOTEDATA, "Vote");
			gui.updateGUI();
		} else if (round == 1) {
			Object[][] TempTable2BuyData = new Object[2][]; 
			Object[][] TempTable2VoteData = new Object[2][];
			TempTable2BuyData[0] = TABLE2BUYDATA[candNums[0]-1];
			TempTable2VoteData[0] = TABLE2VOTEDATA[candNums[0]-1];
			TempTable2BuyData[1] = TABLE2BUYDATA[candNums[1]-1];
			TempTable2VoteData[1] = TABLE2VOTEDATA[candNums[1]-1];
			TABLE2BUYDATA = TempTable2BuyData;
			TABLE2VOTEDATA = TempTable2VoteData;

			String secondBuyDescription = "This is the final information purchase round.";
			gui.setTextPane(secondBuyDescription);
			gui.setScrollPane1(TABLE1NAMES, TABLE1DATA);
			gui.setScrollPane2(TABLE2BUYNAMES, TABLE2BUYDATA, "Buy");
			gui.addEndRoundBtn(0);
			gui.updateGUI();
		}
	}
	
	private void startFinal() {
		String finalVoteDescription = "This is the final vote round. \n"
				+ "Whoever wins this round will win the election.";
		gui.setTextPane(finalVoteDescription);
		gui.setScrollPane2(TABLE2VOTENAMES, TABLE2VOTEDATA, "Vote");
		gui.updateGUI();
	}
	
	private void endCurrentGame() {
		int winningCandidate = readInt();
		int winnings = readInt();
		gui.increaseWinnings(winnings);
		gui.setTextPane("The winner is: " + winningCandidate);
		sleep();
	}

	private String newMessage() {
		try {
			char c = (char) socketInputStream.readByte();
			while (c != '!') {
				c = (char) socketInputStream.readByte();
			}
			String type = socketInputStream.readUTF();
			return type;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Removed from game due to IOException with server.");
			gui.allowClose();
			return null;
		}
	}

	private void writeMessage(String type, int message) {
		try {
			socketOutputStream.writeChar((int) '!');
			socketOutputStream.writeUTF(type); 
			socketOutputStream.writeInt(message);
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Removed from game due to IOException with server.");
			gui.allowClose();
		}
	}

	private int readInt() {
		try {
			int message = socketInputStream.readInt();
			return message;
		} catch (IOException e) {
			JOptionPane.showMessageDialog(null, "Removed from game due to IOException with server.");
			gui.allowClose();
			return -1;
		}
	}

	private void sleep() {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	class ChangeListener implements PropertyChangeListener {
		@Override
		public void propertyChange(PropertyChangeEvent PCE) {
			if (PCE.getPropertyName() == "Buy Info") {
				int candidateToBuyFrom = (Integer) PCE.getOldValue();
				writeMessage("Buy Info", candidateToBuyFrom);
			} 
			else if (PCE.getPropertyName() == "Vote") {
				int candidateToVoteFor = (Integer) PCE.getOldValue();
				writeMessage("Vote", candidateToVoteFor);
			} 
			else if (PCE.getPropertyName() == "End Round") {
				int whichRound = (Integer) PCE.getOldValue();
				writeMessage("End Buy", whichRound);
			}
		}
	}
}

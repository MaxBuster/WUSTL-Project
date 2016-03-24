package client;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeSupport;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import net.miginfocom.swing.MigLayout;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JTable;
import javax.swing.UIManager;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.ui.RectangleAnchor;
import org.jfree.ui.TextAnchor;

public class ClientJFrame extends JFrame {
	private PropertyChangeSupport PCS;
	private JPanel contentPane;
	private char party = 'X';
	private int budget = 4;
	private int currentGame = 1;
	private int numGames;
	private JLabel lblPlayerNumber;
	private JLabel lblParty;
	private JLabel lblIdealPoint;
	private JLabel lblGameNum;
	private JLabel lblBudget;
	private int winnings = 0;
	private JLabel lblWinnings;
	private JTextPane genericTextPane;

	private JScrollPane scrollPane1;
	private JTable table1;

	private JScrollPane scrollPane2;
	private JTable table2;

	private MakeChart chart;
	private JFreeChart graph;
	private ChartPanel pane;

	private static final String WAITING_MESSAGE = "We are waiting for everyone to finish this round.";

	/**
	 * Create the frame.
	 */
	public ClientJFrame(PropertyChangeSupport PCS) {
		this.PCS = PCS;
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new MigLayout("", "[grow,right][grow,fill][grow,fill][grow,fill][grow,fill][grow,fill][grow,left]", "[grow,top][fill][fill][100px,grow,fill][100px,grow,fill][grow,fill][grow][][grow,bottom]"));
		genericTextPane = new JTextPane();
		genericTextPane.setEditable(false);
		contentPane.add(genericTextPane, "cell 1 2 5 1,grow");
		String startingMessage = "Please wait for the game to begin";
		genericTextPane.setText(startingMessage);

		table1 = new JTable();
		table2 = new JTable();

		lblPlayerNumber = new JLabel("Player Number");
		lblPlayerNumber.setVisible(false);
		contentPane.add(lblPlayerNumber, "cell 1 1");
		lblParty = new JLabel("Party");
		lblParty.setVisible(false);
		contentPane.add(lblParty, "cell 2 1");
		lblIdealPoint = new JLabel("Ideal Point");
		lblIdealPoint.setVisible(false);
		contentPane.add(lblIdealPoint, "cell 3 1");
		lblBudget = new JLabel("Budget");
		lblBudget.setVisible(false);
		contentPane.add(lblBudget, "cell 4 1");
		lblGameNum = new JLabel("Game");
		lblGameNum.setVisible(false);
		contentPane.add(lblGameNum, "cell 3 0");
		lblWinnings = new JLabel("Winnings");
		lblWinnings.setVisible(false);
		contentPane.add(lblWinnings, "cell 5 1");

		chart = new MakeChart("New", new double[]{0});
		pane = new ChartPanel(graph);
		pane.setVisible(false);
		contentPane.add(pane, "cell 1 5 5 1,grow");
	}

	public void addLabels(int playerNum, char party, int idealPt, int budget, int numGames) {
		lblPlayerNumber.setText("Player Number: " + playerNum);
		lblPlayerNumber.setVisible(true);

		lblParty.setText("Party: " + party);
		lblParty.setVisible(true);
		this.party = party;

		lblIdealPoint.setText("Ideal Point: " + idealPt);
		lblIdealPoint.setVisible(true);

		lblBudget.setText("Budget: " + budget);
		lblBudget.setVisible(true);
		this.budget = budget;
		
		lblGameNum.setText("Game: " + currentGame + "/" + numGames);
		lblGameNum.setVisible(true);
		this.numGames = numGames;
		currentGame++;

		lblWinnings.setText("Winnings: " + winnings);
		lblWinnings.setVisible(true);
	}

	public void setTextPane(String text) {
		genericTextPane.setText(text);
		genericTextPane.revalidate();
		genericTextPane.repaint();
	}

	public void addChart(double[] chartData, int idealPoint) {
		chart = new MakeChart("Distribution of Voters", chartData);
		graph = chart.getChart();

		ValueMarker marker = new ValueMarker(idealPoint);
		marker.setPaint(Color.black);
		marker.setLabel("You");
		marker.setLabelTextAnchor(TextAnchor.TOP_RIGHT);
		graph.getXYPlot().addDomainMarker(marker);

		pane.setChart(graph);
		pane.setVisible(true);
	}

	public void addDataset(int candidate, IntervalXYDataset data) {
		graph.getXYPlot().setDataset(candidate+1, data);

		XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(); 

		renderer.setSeriesShapesVisible(0, false);
		Color[] colors = new Color[]{new Color(0, 0 ,153), new Color(0, 153, 0), new Color(255, 102, 0), 
				new Color(255, 255, 0), new Color(153, 0, 153), new Color(255,0,204), 
				new Color(255, 204, 204), new Color(0, 255, 255), new Color(102,0,51), 
				new Color(139,69,19)};
		renderer.setSeriesPaint(0, colors[candidate]);
		graph.getXYPlot().setRenderer(candidate+1, renderer); 
	}

	public void updateGUI() {
		contentPane.revalidate();
		contentPane.repaint();
	}

	public void setScrollPane1(String[] columnNames, Object[][] data) {
		DefaultTableModel table1Model = new DefaultTableModel(data, columnNames) {
			public boolean isCellEditable(int row, int column)
			{
				return false;
			}
		};
		table1 = new JTable(table1Model);
		scrollPane1 = new JScrollPane(table1);
		contentPane.add(scrollPane1, "cell 1 3 5 1");
	}

	public void setScrollPane2(String[] columnNames, Object[][] data, String buttonColumn) {
		DefaultTableModel table2Model = new DefaultTableModel(data, columnNames){
			public boolean isCellEditable(int row, int column)
			{
				if (table2.getColumnCount() == 3 && column == 2) {
					return true;
				} else if (table2.getColumnCount() == 2 && column == 1) {
					return true;
				}
				return false;
			}
		};
		table2 = new JTable(table2Model);

		table2.getColumn(buttonColumn).setCellRenderer(new ButtonRenderer());
		table2.getColumn(buttonColumn).setCellEditor(new ButtonEditor(new JCheckBox()));

		scrollPane2 = new JScrollPane(table2);
		contentPane.add(scrollPane2, "cell 1 4 5 1");
	}

	public void addEndRoundBtn(final int roundNum) {
		final JButton btnEndRound = new JButton("End Round");
		btnEndRound.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				btnEndRound.setVisible(false);
				removeScrollPane2();
				contentPane.remove(btnEndRound);
				PCS.firePropertyChange("End Round", roundNum, null); // Sends which buy round ended
				setTextPane(WAITING_MESSAGE);
			}
		});
		contentPane.add(btnEndRound, "cell 5 6"); 
	}

	public void removeScrollPane1() {
		contentPane.remove(scrollPane1);
	}

	public void removeScrollPane2() {
		contentPane.remove(scrollPane2);
	}

	public void increaseWinnings(int roundWinnings) {
		winnings += roundWinnings; // FIXME make these amounts conditional
		winnings += budget;
		lblWinnings.setText("Winnings: " + winnings);
		updateGUI();
	}

	public int getWinnings() {
		return winnings;
	}

	public void allowClose() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void closeWithIOException() {
		this.setVisible(false);
		this.dispose();
	}

	class ButtonRenderer extends JButton implements TableCellRenderer {

		public ButtonRenderer() {
			setOpaque(true);
		}

		public Component getTableCellRendererComponent(JTable table,
				Object value, boolean isSelected, boolean hasFocus, int row,
				int column) {
			if (isSelected) {
				setForeground(table.getSelectionForeground());
				setBackground(table.getSelectionBackground());
			} else {
				setForeground(table.getForeground());
				setBackground(UIManager.getColor("Button.background"));
			}
			setText((value == null) ? "" : value.toString());
			return this;
		}
	}

	/**
	 * @version 1.0 11/09/98
	 */
	class ButtonEditor extends DefaultCellEditor {
		protected JButton button;

		private String label;

		private boolean isPushed;

		public ButtonEditor(JCheckBox checkBox) {
			super(checkBox);
			button = new JButton();
			button.setOpaque(true);
			button.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (label.equals("Buy")) {
						fireBuy();
					} else if (label.equals("Vote")) {
						fireVote();
					}
					fireEditingStopped();
				}
			});
		}

		public void fireBuy() {
			int selectedRow = table2.getSelectedRow();
			int price = (Integer) table2.getModel().getValueAt(selectedRow, 1);
			if (budget >= price) {
				int selectedCand = (Integer) table2.getModel().getValueAt(selectedRow, 0) - 1;
				PCS.firePropertyChange("Buy Info", selectedCand, true); // Same party
				budget -= price;
				lblBudget.setText("Budget: " + budget);
			} else {
				JOptionPane.showMessageDialog(null, "You don't have enough money to buy that info.");
			}
		}

		public void fireVote() {
			int selectedRow = table2.getSelectedRow();
			int selectedCand = (Integer) table2.getModel().getValueAt(selectedRow, 0) - 1;
			setTextPane(WAITING_MESSAGE);
			contentPane.remove(scrollPane2);
			contentPane.remove(scrollPane1);
			contentPane.revalidate();
			contentPane.repaint();
			PCS.firePropertyChange("Vote", selectedCand, null);
		}

		public Component getTableCellEditorComponent(JTable table,
				Object value, boolean isSelected, int row, int column) {
			if (isSelected) {
				button.setForeground(table.getSelectionForeground());
				button.setBackground(table.getSelectionBackground());
			} else {
				button.setForeground(table.getForeground());
				button.setBackground(table.getBackground());
			}
			label = (value == null) ? "" : value.toString();
			button.setText(label);
			isPushed = true;
			return button;
		}

		public Object getCellEditorValue() {
			EventQueue.invokeLater(new Runnable() {
				@Override
				public void run() {
					if (isPushed) {
						isPushed = false;
					}
				}
			});
			return new String(label);
		}

		public boolean stopCellEditing() {
			isPushed = false;
			return super.stopCellEditing();
		}

		protected void fireEditingStopped() {
			super.fireEditingStopped();
		}
	}
}

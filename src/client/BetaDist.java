package client;

import org.apache.commons.math3.distribution.BetaDistribution;

public class BetaDist {
	
	public static double[] getBetaDist(int totalTokens, int numberOnes) {
		BetaDistribution beta = new BetaDistribution(numberOnes+1, totalTokens-numberOnes+1);
		double[] data = new double[100];
		for (int i = 0; i < 100; i++) {
			double num = beta.density((double)(i+.001)/100);
			data[i] = num;
		}
		return data;
	}
}

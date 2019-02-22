package common;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class TestUtil {
	public static double getAverageOfTotalEpisodes(TestResult[] testResults) {
        return Arrays.stream(testResults)
        		.flatMapToInt(testResult -> IntStream.of(testResult.totalEpisodes))
        		.summaryStatistics().getAverage();
	}
	public static double getAverageOfProcessTimes(TestResult[] testResults) {
        return Arrays.stream(testResults)
        		.flatMapToLong(testResult -> LongStream.of(testResult.totalProcessTime))
        		.summaryStatistics().getAverage();
	}
	
	public static String formatTestResults(int minNumAgents, int maxNumAgents, 
			TestResult[] singleAgentTestResults, TestResult[][] parallelTestResultsTable) {
		StringBuilder sb = new StringBuilder(maxNumAgents * 100);
    	sb.append("Episodes\n");
		// row of num of agents
		sb.append("Number of agents,1,");
        for (int i = minNumAgents; i <= maxNumAgents; i+=2) {
        	sb.append(i + (i < maxNumAgents? "," : "\n"));
        }
        int iteration = (maxNumAgents - minNumAgents) / 2 + 1;
        int numTrials = singleAgentTestResults.length;
        // rows of data
        for (int i = 0; i < numTrials; i++) {
        	sb.append("," + singleAgentTestResults[i].totalEpisodes + ",");
        	for (int j = 0; j < iteration; j++) {
            	sb.append(parallelTestResultsTable[j][i].totalEpisodes);
            	sb.append(j < iteration - 1? "," : "\n");
        	}
        }
        //average of single agent:
        sb.append("Average (total episodes),");
        double singleAgentEpisodesAverage = TestUtil.getAverageOfTotalEpisodes(singleAgentTestResults);
        sb.append(singleAgentEpisodesAverage + ",");
        //average of parallel agents
        double[] averages = new double[iteration];
    	for (int i = 0; i < iteration; i++) {
    		averages[i] = getAverageOfTotalEpisodes(parallelTestResultsTable[i]);
        	sb.append(averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
    	//speed-ups
    	sb.append("Speed-ups,1,");
    	for (int i = 0; i < iteration; i++) {
        	sb.append(String.format("%.2f", singleAgentEpisodesAverage / averages[i]));
        	sb.append(i < iteration - 1? "," : "\n");
    	}
        //average (total episodes) / numAgents
        sb.append("Average (total episodes) / numAgents,");
        sb.append(singleAgentEpisodesAverage + ",");
        for (int i = 0; i < iteration; i++) {
            int numAgents = minNumAgents + i * 2;
            sb.append(String.format("%.2f", averages[i] / numAgents));
            sb.append(i < iteration - 1? "," : "\n");
        }
        //speed-ups / numAgents
        sb.append("Speed-ups * numAgents,1,");
        for (int i = 0; i < iteration; i++) {
            int numAgents = minNumAgents + i * 2;
            sb.append(String.format("%.2f", singleAgentEpisodesAverage / averages[i] * numAgents));
            sb.append(i < iteration - 1? "," : "\n");
        }
    	
    	
    	sb.append("\nProcess Times\n");
		// row of num of agents
		sb.append("Number of agents,1,");
        for (int i = minNumAgents; i <= maxNumAgents; i+=2) {
        	sb.append(i + (i < maxNumAgents? "," : "\n"));
        }
        for (int i = 0; i < numTrials; i++) {
        	sb.append("," + singleAgentTestResults[i].totalProcessTime + ",");
        	for (int j = 0; j < iteration; j++) {
            	sb.append(parallelTestResultsTable[j][i].totalProcessTime);
            	sb.append(j < iteration - 1? "," : "\n");
        	}
        }
        //average of single agent:
        sb.append("Average (process times),");
        double singleAgentProcessTimesAverage = getAverageOfProcessTimes(singleAgentTestResults);
        sb.append(singleAgentProcessTimesAverage + ",");
        //average of stop walk agents 
    	for (int i = 0; i < iteration; i++) {
    		averages[i] = getAverageOfProcessTimes(parallelTestResultsTable[i]);
        	sb.append(averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
    	//speed-ups
    	sb.append("Speed-ups,1,");
    	for (int i = 0; i < iteration; i++) {
        	sb.append(String.format("%.2f", singleAgentProcessTimesAverage / averages[i]));
        	sb.append(i < iteration - 1? "," : "\n");
    	}

        sb.append("Average (process times) / numAgents,");
        sb.append(singleAgentProcessTimesAverage + ",");
        //average of stop walk agents 
        for (int i = 0; i < iteration; i++) {
            int numAgents = minNumAgents + i * 2;
            sb.append(String.format("%.2f", averages[i] / numAgents));
            sb.append(i < iteration - 1? "," : "\n");
        }
        //speed-ups
        sb.append("Speed-ups * numAgents,1,");
        for (int i = 0; i < iteration; i++) {
            int numAgents = minNumAgents + i * 2;
            sb.append(String.format("%.2f", singleAgentProcessTimesAverage / averages[i] * numAgents));
            sb.append(i < iteration - 1? "," : "\n");
        }
    	
    	return sb.toString();
	}
}

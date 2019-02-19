package common;

import java.util.Arrays;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

public class TestUtil {
	public static double getAverageOfEpisodes(TestResult[] testResults) {
        return Arrays.stream(testResults)
        		.flatMapToInt(testResult -> IntStream.of(testResult.optimumEpisode))
        		.summaryStatistics().getAverage();
	}
	public static double getAverageOfProcessingTimes(TestResult[] testResults) {
        return Arrays.stream(testResults)
        		.flatMapToLong(testResult -> LongStream.of(testResult.processingTime))
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
        	sb.append("," + singleAgentTestResults[i].optimumEpisode + ",");
        	for (int j = 0; j < iteration; j++) {
            	sb.append(parallelTestResultsTable[j][i].optimumEpisode);
            	sb.append(j < iteration - 1? "," : "\n");
        	}
        }
        //average of single agent:
        sb.append("Average (episodes),");
        double singleAgentEpisodesAverage = TestUtil.getAverageOfEpisodes(singleAgentTestResults);
        sb.append(singleAgentEpisodesAverage + ",");
        //average of parallel agents
        double[] averages = new double[iteration];
    	for (int i = 0; i < iteration; i++) {
    		averages[i] = getAverageOfEpisodes(parallelTestResultsTable[i]);
        	sb.append(averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
    	//speed-ups
    	sb.append("Speed-ups,1,");
    	for (int i = 0; i < iteration; i++) {
        	sb.append(singleAgentEpisodesAverage / averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
    	
    	
    	sb.append("\nProcessing Times\n");
		// row of num of agents
		sb.append("Number of agents,1,");
        for (int i = minNumAgents; i <= maxNumAgents; i+=2) {
        	sb.append(i + (i < maxNumAgents? "," : "\n"));
        }
        for (int i = 0; i < numTrials; i++) {
        	sb.append("," + singleAgentTestResults[i].processingTime + ",");
        	for (int j = 0; j < iteration; j++) {
            	sb.append(parallelTestResultsTable[j][i].processingTime);
            	sb.append(j < iteration - 1? "," : "\n");
        	}
        }
        //average of single agent:
        sb.append("Average (processing times),");
        double singleAgentProcessingTimesAverage = getAverageOfProcessingTimes(singleAgentTestResults);
        sb.append(singleAgentProcessingTimesAverage + ",");
        //average of stop walk agents 
    	for (int i = 0; i < iteration; i++) {
    		averages[i] = getAverageOfProcessingTimes(parallelTestResultsTable[i]);
        	sb.append(averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
    	//speed-ups
    	sb.append("Speed-ups,1,");
    	for (int i = 0; i < iteration; i++) {
        	sb.append(singleAgentProcessingTimesAverage / averages[i]);
        	sb.append(i < iteration - 1? "," : "\n");
    	}
        return sb.toString();
	}
}

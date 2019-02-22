package common;

public class TestResult {
    public int totalEpisodes;
    public int totalTicks;
	public int optimumEpisode;
	public long totalProcessTime;
	public TestResult(int totalEpisodes, int totalTicks, long totalProcessTime) {
	    this.totalEpisodes = totalEpisodes;
	    this.totalTicks = totalTicks;
		this.totalProcessTime = totalProcessTime;
	}
}

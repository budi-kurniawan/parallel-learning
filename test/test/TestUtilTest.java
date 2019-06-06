package test;

import java.util.stream.IntStream;

import org.apache.commons.math3.distribution.EnumeratedIntegerDistribution;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;

import common.TestResult;
import common.TestUtil;

public class TestUtilTest {
    public void test1() {
        StandardDeviation sd = new StandardDeviation();
        System.out.println(sd.evaluate(new double[] {95, 61, 111, 66, 79, 81, 107, 52, 85, 156})); // should be 30.173756
        System.out.println(sd.evaluate(new double[] {158,97,77,107,74,103,178,108,64,88})); // should be 36.411841
    }
    
    public void test2() {
        
    }
    public static void main(String[] args) {
//        TestResult[] testResults = IntStream.of(1, 2, 3, 4, 5).mapToObj(i -> new TestResult(i, 0, 0)).toArray(TestResult[]::new);
//        double sd = TestUtil.getStandardDeviationOfTotalEpisodes(testResults);
//        System.out.println(sd);
//        
//        TestUtilTest test = new TestUtilTest();
//        test.test1();
        
        //int[] data = {0, 1, 2, 30};
        int[] data = IntStream.range(0, 4).toArray();
        double[] probs = {0.1, 0.1, 0.5, 0.3};
        EnumeratedIntegerDistribution dist = new EnumeratedIntegerDistribution(data, probs);
        for (int i = 0; i < 10; i++) {
            System.out.println(dist.sample());
        }
    }
}

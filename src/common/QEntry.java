package common;

public class QEntry {
    public double value;
    public int counter;
    
    public void update(double value, int counter) {
        this.value = value;
        this.counter = counter;
    }
    
    @Override
    public String toString() {
        return value + " (count:" + counter + ")";
    }
}

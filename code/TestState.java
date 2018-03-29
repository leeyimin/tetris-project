public class TestState {

    public int[][] field;
    public int[] top;

    public int lastCleared;

    public TestState(int[][] field, int[] top, int lastCleared) {
        this.field = field;
        this.top = top;
        this.lastCleared = lastCleared;
    }

}

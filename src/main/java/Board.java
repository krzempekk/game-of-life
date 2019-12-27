import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Board {
    private Vector2D lowerLeft;
    private Vector2D upperRight;
    private Map<Vector2D, Cell> cells;
    public List<Integer> birthNeighbourCount;
    public List<Integer> surviveNeighbourCount;

    int randomInt(int a, int b) {
        return (int) (Math.random() * (b - a + 1) + a);
    }

    public Board(Vector2D lowerLeft, Vector2D upperRight, List<Integer> birthNeighbourCount, List<Integer> surviveNeighbourCount) {
        this.lowerLeft = lowerLeft;
        this.upperRight = upperRight;
        this.cells = new HashMap<>();
        for(int x = lowerLeft.x; x <= upperRight.x; x++) {
            for(int y = lowerLeft.y; y <= upperRight.y; y++) {
                Vector2D position = new Vector2D(x, y);
                this.cells.put(position, new Cell(position, CellState.DEAD, this));
            }
        }
        this.birthNeighbourCount = birthNeighbourCount;
        this.surviveNeighbourCount = surviveNeighbourCount;
    }

    public void setBoardBirthRule(int amount, boolean add) {
        if(add) this.birthNeighbourCount.add(amount);
        else this.birthNeighbourCount.remove((Integer) amount);
    }

    public void setBoardSurviveRule(int amount, boolean add) {
        if(add) this.surviveNeighbourCount.add(amount);
        else this.surviveNeighbourCount.remove((Integer) amount);
    }

    public void setCellState(Vector2D position, CellState state) {
        this.cells.get(position).setState(state);
    }

    public CellState getCellState(Vector2D position) {
        if(position.precedes(this.lowerLeft) || position.follows(this.upperRight)) return CellState.DEAD;
        Cell cell = this.cells.get(position);
        return cell.getCurrentState();
    }

    public void reset() {
        for(int x = this.lowerLeft.x; x <= this.upperRight.x; x++) {
            for(int y = this.lowerLeft.y; y <= this.upperRight.y; y++) {
                Vector2D position = new Vector2D(x, y);
                this.cells.get(position).setState(CellState.DEAD);
            }
        }
    }

    public void step() {
        for(Cell cell: this.cells.values()) {
            cell.calculateNextState();
        }
        for(Cell cell: this.cells.values()) {
            cell.enterNextState();
        }
    }

    public void setRandomCellsAlive(int amount) {
        for(int i = 0; i < amount; i++) {
            Cell cell;
            do {
                cell = (Cell) this.cells.values().toArray()[this.randomInt(0, this.cells.size() - 1)];
            } while(cell.getCurrentState() != CellState.DEAD);
            cell.setState(CellState.ALIVE);
        }
    }
}

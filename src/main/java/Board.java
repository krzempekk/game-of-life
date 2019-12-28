
import java.util.*;

public class Board implements IuiActionObserver {
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
        Cell cellToChange = this.cells.get(position);
        if(cellToChange == null) {
            this.cells.put(position, new Cell(position, state, this));
        } else {
            this.cells.get(position).setState(state);
        }
    }

    public CellState getCellState(Vector2D position) {
        if(position.precedes(this.lowerLeft) || position.follows(this.upperRight)) return CellState.DEAD;
        Cell cell = this.cells.get(position);
        if(cell == null) return CellState.DEAD;
        return cell.getCurrentState();
    }

    public void reset() {
        this.cells.clear();
    }

    public void step() {
        Set<Vector2D> candidatePositions = new HashSet<>();
        for(Vector2D position: this.cells.keySet()) {
            for(BoardDirection direction: BoardDirection.values()) {
                Vector2D newPosition = position.add(direction.getUnitVector());
                if(this.cells.get(newPosition) == null) {
                    candidatePositions.add(newPosition);
                }
            }
        }
        for(Vector2D position: candidatePositions) {
            this.cells.put(position, new Cell(position, CellState.DEAD, this));
        }
        for(Cell cell: this.cells.values()) {
            cell.calculateNextState();
        }
        for(Cell cell: this.cells.values()) {
            cell.enterNextState();
        }
        this.cells.values().removeIf(cell -> cell.getCurrentState() == CellState.DEAD);
    }



    @Override
    public void uiActionPerformed(uiActionType actionType, Object... args) {
        switch (actionType) {
            case STEP_SKIP:
                for(int i = 0; i < (int) args[0]; i++) {
                   this.step();
                }
                break;
            case RESET:
                this.reset();
                break;
        }
    }
}

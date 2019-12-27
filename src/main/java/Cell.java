public class Cell {
    private Vector2D position;
    private CellState currentState;
    private CellState nextState;
    private Board board;

    public Cell(Vector2D position, CellState state, Board board) {
        this.position = position;
        this.currentState = state;
        this.board = board;
    }

    public void setState(CellState currentState) { this.currentState = currentState; }

    public CellState getCurrentState() {
        return this.currentState;
    }

    public void calculateNextState() {
        int aliveNeighbours = 0;
        for(BoardDirection direction: BoardDirection.values()) {
            Vector2D testedPosition = this.position.add(direction.getUnitVector());
            if(this.board.getCellState(testedPosition) != CellState.DEAD) aliveNeighbours++;
        }
        if(this.currentState == CellState.DEAD) {
            this.nextState = this.board.birthNeighbourCount.contains(aliveNeighbours) ? CellState.ALIVE : CellState.DEAD;
        } else {
            this.nextState = this.board.surviveNeighbourCount.contains(aliveNeighbours) ? CellState.ALIVE : CellState.DEAD;
        }
    }

    public void enterNextState() {
        this.currentState = this.nextState;
        this.nextState = null;
    }
}

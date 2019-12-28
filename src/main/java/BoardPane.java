import javafx.application.Platform;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

public class BoardPane extends Pane implements IuiActionObserver {
    int boardWidth;
    int boardHeight;
    Board board;
    Canvas canvas;
    int cellSize = 25;
    Vector2D upperLeft = new Vector2D(0, 0);
    private int maxCellSize = 100;
    private int minCellSize = 3;
    private double scaleFactor = 0.75;
    boolean displayGrid = true;

    public BoardPane(int width, int height, Board board) {
        this.board = board;
        this.setMinWidth(width);
        this.setMinHeight(height);
        this.boardWidth = width;
        this.boardHeight = height;
        this.canvas = new Canvas(width, height);
        this.canvas.setOnMouseClicked(event -> {
            int boardX = (int) event.getX();
            int boardY = (int) event.getY();
            Vector2D position = new Vector2D(boardX / this.cellSize, -boardY / this.cellSize);
            this.board.setCellState(this.upperLeft.add(position), CellState.ALIVE);
            this.update();
        });
        this.getChildren().add(this.canvas);
    }

    public void changeDimensions(int width, int height) {
        this.setMinWidth(width);
        this.setMinHeight(height);
        this.boardWidth = width;
        this.boardHeight = height;
        this.canvas.setWidth(width);
        this.canvas.setHeight(height);
        this.update();
    }

    public void setKeyHandlers() {
        this.getScene().addEventFilter(KeyEvent.KEY_PRESSED, (KeyEvent event) -> {
            Vector2D offset = null;
            KeyCode keyCode = event.getCode();
            switch (keyCode) {
                case UP:
                    offset = new Vector2D(0, 1);
                    break;
                case DOWN:
                    offset = new Vector2D(0, -1);
                    break;
                case LEFT:
                    offset = new Vector2D(-1, 0);
                    break;
                case RIGHT:
                    offset = new Vector2D(1, 0);
                    break;
            }
            if(keyCode == KeyCode.ADD || keyCode == KeyCode.SUBTRACT) {
                double multiplier = keyCode == KeyCode.ADD ? 1 / this.scaleFactor : this.scaleFactor;
                int newCellSize = (int) (this.cellSize * multiplier);
                if(newCellSize >= this.minCellSize && newCellSize <= this.maxCellSize) {
                    int offsetX = (int) ((this.boardWidth / this.cellSize - this.boardWidth / (this.cellSize * multiplier)) / 2);
                    int offsetY = (int) ((this.boardHeight / this.cellSize - this.boardHeight / (this.cellSize * multiplier)) / 2);
                    offset = new Vector2D(offsetX, -offsetY);
                    this.cellSize *= multiplier;
                }
            }
            if(offset != null) {
                this.upperLeft = this.upperLeft.add(offset);
                this.update();
            }
            event.consume();
        });
    }

    @Override
    protected void layoutChildren() {
        Platform.runLater(()->{
            super.layoutChildren();

            GraphicsContext gc = this.canvas.getGraphicsContext2D();
            gc.clearRect(0, 0, this.boardWidth, this.boardHeight);

            if(this.displayGrid) {
                // vertical lines
                gc.setStroke(Color.BLACK);
                for(int i = 0 ; i < this.boardWidth ; i += this.cellSize){
                    gc.strokeLine(i, 0, i, this.boardHeight);
                }

                // horizontal lines
                gc.setStroke(Color.BLACK);
                for(int i = 0 ; i < this.boardHeight ; i += this.cellSize){
                    gc.strokeLine(0, i, this.boardWidth, i);
                }
            }

            int width = this.boardWidth / this.cellSize, height = this.boardHeight / this.cellSize;

            for(int x = 0; x < width; x++) {
                for(int y = 0; y < height; y++) {
                    gc.setFill(Color.BLACK);
                    if(this.board.getCellState(this.upperLeft.add(new Vector2D(x, -y))) != CellState.DEAD) {
                        gc.fillRect(x * this.cellSize, y * this.cellSize, this.cellSize, this.cellSize);
                    }
                }
            }
        });
    }

    public void update() {
        this.layoutChildren();
    }

    @Override
    public void uiActionPerformed(uiActionType actionType, Object... args) {
        switch (actionType) {
            case HIDE_GRID:
                this.displayGrid = false;
                break;
            case DISPLAY_GRID:
                this.displayGrid = true;
                break;
        }
        this.update();
    }
}

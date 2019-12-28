import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.scene.Scene;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Main extends Application implements IuiActionObserver {
    private Board board;
    private Map<Vector2D, Rectangle> map = new HashMap<>();
    private AtomicBoolean paused;
    private Thread threadObject;
    private int boardWidth = 500;
    private int boardHeight = 500;
    private int sidebarWidth = 400;
    private int stepPause;
    private BoardPane boardPane;

    List<Integer> initialBirthNeighbourCount = new ArrayList<>();
    List<Integer> initialSurviveNeighbourCount = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) {
        this.boardPane = new BoardPane(this.boardWidth, this.boardHeight, this.board);

        SidePane sidePane = new SidePane(this.board, this.initialBirthNeighbourCount, this.initialSurviveNeighbourCount, this.stepPause);
        sidePane.addObserver(this);
        sidePane.addObserver(this.boardPane);
        sidePane.addObserver(this.board);
        sidePane.setPrefWidth(this.sidebarWidth);

        HBox root = new HBox(this.boardPane, sidePane);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, this.boardWidth + this.sidebarWidth, this.boardHeight));
        primaryStage.show();

        ChangeListener<Number> stageSizeListener = (observable, oldValue, newValue) -> this.boardPane.changeDimensions((int) primaryStage.getWidth() - this.sidebarWidth, (int) primaryStage.getHeight());

        primaryStage.widthProperty().addListener(stageSizeListener);
        primaryStage.heightProperty().addListener(stageSizeListener);

        this.boardPane.setKeyHandlers();
        this.boardPane.update();
        this.run();
    }

    public void run() {
        Runnable runnable = () -> {
            while (true) {
                if (Main.this.paused.get()) {
                    synchronized (this.threadObject) {
                        try {
                            this.threadObject.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                }

                this.board.step();
                this.boardPane.update();

                try {
                    Thread.sleep(this.stepPause);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        this.threadObject = new Thread(runnable);
        this.threadObject.start();
    }

    public Main() {
        this.readParameters();
        this.board = new Board(new Vector2D(-1000, -1000), new Vector2D(1000, 1000), this.initialBirthNeighbourCount, this.initialSurviveNeighbourCount);
        this.board.setCellState(new Vector2D(5, -5), CellState.ALIVE);
        this.board.setCellState(new Vector2D(5, -6), CellState.ALIVE);
        this.board.setCellState(new Vector2D(5, -7), CellState.ALIVE);
        this.paused = new AtomicBoolean(false);
    }

    private String getParametersFile() {
        StringBuilder contentBuilder = new StringBuilder();
        try (Stream<String> stream = Files.lines(Paths.get(this.getClass().getResource("parameters.json").toURI()), StandardCharsets.UTF_8)) {
            stream.forEach(s -> contentBuilder.append(s).append("\n"));
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
        return contentBuilder.toString();
    }

    private void readParameters() {
        String paramsFile = this.getParametersFile();
        JSONObject params = new JSONObject(paramsFile);

//        this.width = params.getInt("width");
//        this.height = params.getInt("height");
        this.initialBirthNeighbourCount = new ArrayList<>();
        this.initialSurviveNeighbourCount = new ArrayList<>();
        for(Object val: params.getJSONArray("birthNeighbourCount")) {
            this.initialBirthNeighbourCount.add((Integer) val);
        }
        for(Object val: params.getJSONArray("surviveNeighbourCount")) {
            this.initialSurviveNeighbourCount.add((Integer) val);
        }
        this.stepPause = params.getInt("stepPause");
    }


    public static void main(String[] args) {
        Main main = new Main();
        launch(args);
    }

    @Override
    public void uiActionPerformed(uiActionType actionType, Object... args) {
        switch (actionType) {
            case RESUME:
                this.paused.set(false);
                synchronized(this.threadObject) {
                    this.threadObject.notify();
                }
                break;
            case PAUSE:
                this.paused.set(true);
                break;
            case STEP_PAUSE_CHANGED:
                this.stepPause = (int) args[0];
                break;
        }
    }
}

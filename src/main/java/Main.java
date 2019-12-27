import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class Main extends Application {
    private Board board;
    private int width;
    private int height;
    private Map<Vector2D, Rectangle> map = new HashMap<>();
    private AtomicBoolean paused;
    private Thread threadObject;
    private int boardWidth = 500;
    private int boardHeight = 500;
    private int stepPause;

    List<Integer> initialBirthNeighbourCount = new ArrayList<>();
    List<Integer> initialSurviveNeighbourCount = new ArrayList<>();

    @Override
    public void start(Stage primaryStage) throws Exception {
//        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));

        GridPane gridPane = new GridPane();
        gridPane.setGridLinesVisible(true);
        gridPane.setMinSize(this.boardWidth, this.boardHeight);

        for (int i = 0; i < this.width; i++) {
            ColumnConstraints colConst = new ColumnConstraints();
            colConst.setPercentWidth(100.0 / this.width);
            gridPane.getColumnConstraints().add(colConst);
        }
        for (int i = 0; i < this.height; i++) {
            RowConstraints rowConst = new RowConstraints();
            rowConst.setPercentHeight(100.0 / this.height);
            gridPane.getRowConstraints().add(rowConst);
        }

        for(int x = 0; x < this.width; x++) {
            for(int y = 0; y < this.height; y++) {
                Rectangle rect = new Rectangle();
                rect.setFill(Color.WHITE);
                rect.setWidth((double) this.boardWidth / this.width - 1);
                rect.setHeight((double) this.boardHeight / this.height - 1);
                gridPane.add(rect, x, y);
                this.map.put(new Vector2D(x, y), rect);
                rect.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
                    @Override
                    public void handle(MouseEvent mouseEvent) {
                        int x = GridPane.getColumnIndex((Node) mouseEvent.getTarget());
                        int y = GridPane.getRowIndex((Node) mouseEvent.getTarget());
                        CellState state = Main.this.board.getCellState(new Vector2D(x, y));
                        Main.this.board.setCellState(new Vector2D(x, y), state == CellState.ALIVE ? CellState.DEAD : CellState.ALIVE);
                        Main.this.updateGrid();
                    }
                });
            }
        }

        FlowPane flowPane = new FlowPane();
        flowPane.setOrientation(Orientation.VERTICAL);

        Button stepButton = new Button("Next step");

        stepButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Main.this.step();
            }
        });

        Button pauseButton = new Button("Pause");

        pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                if(Main.this.paused.get()) {
                    pauseButton.setText("Pause");
                    Main.this.paused.set(false);
                    synchronized(Main.this.threadObject) {
                        Main.this.threadObject.notify();
                    }
                } else {
                    pauseButton.setText("Resume");
                    Main.this.paused.set(true);
                }
            }
        });

        Button resetButton = new Button("Reset");

        resetButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Main.this.board.reset();
                Main.this.updateGrid();
            }
        });

        Button addCellsButton = new Button("Add random cells");

        addCellsButton.addEventHandler(MouseEvent.MOUSE_CLICKED, new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent mouseEvent) {
                Main.this.board.setRandomCellsAlive(100);
                Main.this.updateGrid();
            }
        });

        Slider speedSlider = new Slider(0, 1000, this.stepPause);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(10);


        speedSlider.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) {
                Main.this.stepPause = newValue.intValue();
            }
        });

        List<CheckBox> survivalCheckBoxList = new ArrayList<>();
        List<CheckBox> birthCheckBoxList = new ArrayList<>();

        for(int i = 0; i <= 8; i++) {
            CheckBox checkBox = new CheckBox("" + i);
            if(this.initialSurviveNeighbourCount.contains(i)) checkBox.setSelected(true);
            int number = i;
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    Main.this.board.setBoardSurviveRule(number, newValue);
                }
            });
            survivalCheckBoxList.add(checkBox);
        }

        for(int i = 0; i <= 8; i++) {
            CheckBox checkBox = new CheckBox("" + i);
            if(this.initialBirthNeighbourCount.contains(i)) checkBox.setSelected(true);
            int number = i;
            checkBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
                @Override
                public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                    Main.this.board.setBoardBirthRule(number, newValue);
                }
            });
            birthCheckBoxList.add(checkBox);
        }

        flowPane.getChildren().addAll(stepButton, pauseButton, resetButton, addCellsButton, speedSlider);
        flowPane.getChildren().add(new Text("Survival when"));
        flowPane.getChildren().addAll(survivalCheckBoxList);
        flowPane.getChildren().add(new Text("Birth when"));
        flowPane.getChildren().addAll(birthCheckBoxList);

        HBox root = new HBox(gridPane, flowPane);

        primaryStage.setTitle("Hello World");
        primaryStage.setScene(new Scene(root, 600, 500));
        primaryStage.show();

        this.updateGrid();
        this.run();
    }

    public void updateGrid() {
        for(int x = 0; x < this.width; x++) {
            for(int y = 0; y < this.height; y++) {
                Vector2D position = new Vector2D(x, y);
                if(this.board.getCellState(position) == CellState.DEAD) {
                    this.map.get(position).setFill(Color.WHITE);
                } else {
                    this.map.get(position).setFill(Color.BLACK);
                }
            }
        }
    }

    public void step() {
        this.board.step();
        this.updateGrid();
    }

    public void run() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Main.this.paused.get()) {
                        synchronized (Main.this.threadObject) {
                            try {
                                Main.this.threadObject.wait();
                            } catch (InterruptedException ignored) {
                            }
                        }
                    }

                    Main.this.step();

                    try {
                        Thread.sleep(Main.this.stepPause);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        this.threadObject = new Thread(runnable);
        this.threadObject.start();
    }

    public Main() {
        this.readParameters();
        this.board = new Board(new Vector2D(0, 0), new Vector2D(this.width - 1, this.height - 1), this.initialBirthNeighbourCount, this.initialSurviveNeighbourCount);
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

        this.width = params.getInt("width");
        this.height = params.getInt("height");
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
}

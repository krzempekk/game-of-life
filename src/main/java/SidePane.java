import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;

public class SidePane extends FlowPane {
    Board board;
    List<IuiActionObserver> observers;
    ToolBar buttonToolBar;

    public SidePane(Board board, List<Integer> initialBirthNeighbourCount, List<Integer> initialSurviveNeighbourCount, int stepPause) {
        this.board = board;
        this.observers = new ArrayList<>();
        this.setOrientation(Orientation.VERTICAL);
        this.setVgap(8);
        this.buttonToolBar = new ToolBar();
        this.buttonToolBar.prefWidthProperty().bind(this.prefWidthProperty());
        this.setPadding(new Insets(10, 0, 10,0));
        this.addTextLabel("Game of Life", 20);
        this.getChildren().add(this.buttonToolBar);
        this.addTextLabel("Step pause", 15);
        this.addStepButton();
        this.addPauseButton();
        this.addResetButton();
        this.addGridButton();
        this.addSpeedSlider(stepPause);
        this.addCheckboxes(initialBirthNeighbourCount, initialSurviveNeighbourCount);
    }

    public void addObserver(IuiActionObserver observer) {
        this.observers.add(observer);
    }

    public void notifyObservers(uiActionType actionType, Object... args) {
        for(IuiActionObserver observer: this.observers) {
            observer.uiActionPerformed(actionType, args);
        }
    }

    public void addTextLabel(String text, int fontSize) {
        Text textLabel = new Text(text);
        textLabel.setFont(Font.font("Verdana", fontSize));
        textLabel.setTextAlignment(TextAlignment.CENTER);
        textLabel.wrappingWidthProperty().bind(this.prefWidthProperty());
        this.getChildren().add(textLabel);
    }

    public void addStepButton() {
        Button stepButton = new Button("Next step");

        stepButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            this.notifyObservers(uiActionType.STEP_SKIP, 1);
        });

        this.buttonToolBar.getItems().add(stepButton);
    }

    public void addPauseButton() {
        Button pauseButton = new Button("Pause");

        pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            if(pauseButton.getText().equals("Pause")) {
                this.notifyObservers(uiActionType.PAUSE);
                pauseButton.setText("Resume");
            } else {
                this.notifyObservers(uiActionType.RESUME);
                pauseButton.setText("Pause");
            }
        });

        this.buttonToolBar.getItems().add(pauseButton);
    }

    public void addResetButton() {
        Button resetButton = new Button("Reset");

        resetButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            this.notifyObservers(uiActionType.RESET);
        });

        this.buttonToolBar.getItems().add(resetButton);
    }

    public void addGridButton() {
        Button pauseButton = new Button("Hide grid");

        pauseButton.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent mouseEvent) -> {
            if(pauseButton.getText().equals("Display grid")) {
                this.notifyObservers(uiActionType.DISPLAY_GRID);
                pauseButton.setText("Hide grid");
            } else {
                this.notifyObservers(uiActionType.HIDE_GRID);
                pauseButton.setText("Display grid");
            }
        });

        this.buttonToolBar.getItems().add(pauseButton);
    }

    public void addSpeedSlider(int stepPause) {
        Slider speedSlider = new Slider(0, 1000, stepPause);
        speedSlider.setShowTickLabels(true);
        speedSlider.setMajorTickUnit(10);

        speedSlider.valueProperty().addListener((ObservableValue<? extends Number> observableValue, Number oldValue, Number newValue) -> {
            this.notifyObservers(uiActionType.STEP_PAUSE_CHANGED, newValue.intValue());
        });

        this.getChildren().add(speedSlider);
    }

    public void addCheckboxes(List<Integer> initialBirthNeighbourCount, List<Integer> initialSurviveNeighbourCount) {
        List<VBox> survivalCheckBoxList = new ArrayList<>();
        List<VBox> birthCheckBoxList = new ArrayList<>();

        for(int i = 0; i <= 8; i++) {
            VBox box = new VBox();
            Text text = new Text("" + i);
            text.setTextAlignment(TextAlignment.CENTER);
            CheckBox checkBox = new CheckBox();
            text.wrappingWidthProperty().bind(checkBox.widthProperty());
            box.getChildren().addAll(checkBox, text);

            if(initialSurviveNeighbourCount.contains(i)) checkBox.setSelected(true);
            int number = i;
            checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                this.board.setBoardSurviveRule(number, newValue);
            });
            survivalCheckBoxList.add(box);
        }

        for(int i = 0; i <= 8; i++) {
            VBox box = new VBox();
            Text text = new Text("" + i);
            text.setTextAlignment(TextAlignment.CENTER);
            CheckBox checkBox = new CheckBox();
            text.wrappingWidthProperty().bind(checkBox.widthProperty());
            box.getChildren().addAll(checkBox, text);

            if(initialBirthNeighbourCount.contains(i)) checkBox.setSelected(true);
            int number = i;
            checkBox.selectedProperty().addListener((ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                this.board.setBoardBirthRule(number, newValue);
            });
            birthCheckBoxList.add(box);
        }

        FlowPane survivalCheckBoxes = new FlowPane();
        FlowPane birthCheckboxes = new FlowPane();

        survivalCheckBoxes.setHgap(3);
        survivalCheckBoxes.setPadding(new Insets(0, 10, 0, 10));
        survivalCheckBoxes.getChildren().add(new Text("Survival when"));
        survivalCheckBoxes.getChildren().addAll(survivalCheckBoxList);
        this.getChildren().add(survivalCheckBoxes);

        birthCheckboxes.setHgap(3);
        birthCheckboxes.setPadding(new Insets(0, 10, 0, 10));
        birthCheckboxes.getChildren().add(new Text("Birth when"));
        birthCheckboxes.getChildren().addAll(birthCheckBoxList);
        this.getChildren().add(birthCheckboxes);
    }
}

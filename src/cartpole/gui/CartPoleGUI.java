package cartpole.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cartpole.CartPoleEngine;
import cartpole.gui.listener.CartPoleLearningView;
import gui.NumberField;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import rl.QEntry;
import rl.Util;

public class CartPoleGUI extends Application {
    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 800;
    private static final int INITIAL_LEFT_MARGIN = 40;
    private static final int INITIAL_TOP_MARGIN = 40;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    private Button startButton = new Button("Start");
    private Spinner<Integer> numRowsSpinner = new Spinner<>(4, 45, 8);
    private Spinner<Integer> numColsSpinner = new Spinner<>(4, 45, 8);
    private NumberField numEpisodesField = new NumberField("500");
    private CheckBox concurrentCb = new CheckBox("Concurrent");
    private ComboBox<String> learningTypeCombo = new ComboBox<>();
    private int numAgents = 2;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Parallel Q-Learning");
        Group root = new Group();
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        Label label1 = new Label("#Episodes:");
        learningTypeCombo.getItems().addAll("1. Normal Learning", "2. Policy View", "3. Policy View 2");
        
        numRowsSpinner.setPrefWidth(60);
        numColsSpinner.setPrefWidth(60);
        numEpisodesField.setPrefWidth(70);
        
        hbox.getChildren().addAll(new Label("#Rows:"), numRowsSpinner, new Label("#Columns"),
                numColsSpinner, label1, numEpisodesField, learningTypeCombo, concurrentCb, startButton);
        root.getChildren().add(canvas);
        root.getChildren().add(hbox);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        startButton.setOnAction(event -> {
            executeLearningType1();
//            if (validateInput()) {
//                Util.numRows = numRowsSpinner.getValue();
//                Util.numCols = numColsSpinner.getValue();
//                String learningType = learningTypeCombo.getValue();
//                if (learningType.startsWith("1.")) {
//                    executeLearningType1();
//                } else if (learningType.startsWith("2.")) {
//                    executeLearningType2();
//                } else if (learningType.startsWith("3.")) {
//                    executeLearningType3();
//                }
//            }
        });
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
    }
    
    private boolean validateInput() {
        if (learningTypeCombo.getValue() == null) {
            Alert alert = new Alert(AlertType.WARNING, "Please select a learning type", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        try {
            Util.numEpisodes = 300;//Integer.parseInt(numEpisodesField.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            Alert alert = new Alert(AlertType.WARNING, "Please enter the number of episodes", ButtonType.OK);
            alert.showAndWait();
        }
        return false;
    }
    
    private void executeLearningType1() {
        System.out.println("executeLearningType1 in CartpoleGUI");
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;
        
        CartPoleLearningView learningView = new CartPoleLearningView(canvas.getGraphicsContext2D(), leftMargin, topMargin);

        Util.numEpisodes = 20000;
        Util.MAX_TICKS = 400;
        QEntry[][] q = new QEntry[163][2];
        for (int i = 0; i < 163; i++) {
            for (int j = 0; j < 2; j++) {
                q[i][j] = new QEntry();
            }
        }
        int[] actions = {0, 1};
        int[][] stateActions = new int[163][2];
        for (int i = 0; i < 163; i++) {
            stateActions[i] = actions;
        }

        CartPoleEngine engine = new CartPoleEngine(q, stateActions);
        engine.addTickListeners(learningView);
        engine.addEpisodeListeners(learningView);
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));

        executorService.submit(engine);
    }

    private void executeLearningType2() {
    }

    private void executeLearningType3() {
    }
}
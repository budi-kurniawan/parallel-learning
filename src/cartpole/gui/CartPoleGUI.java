package cartpole.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cartpole.CartpoleEngine;
import cartpole.CartpoleUtil;
import cartpole.gui.listener.CartPoleLearningView;
import common.CommonUtil;
import common.QEntry;
import common.gui.NumberField;
import javafx.application.Application;
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

public class CartPoleGUI extends Application {
    private static final int CANVAS_WIDTH = 1200;
    private static final int CANVAS_HEIGHT = 800;
    private static final int INITIAL_LEFT_MARGIN = 40;
    private static final int INITIAL_TOP_MARGIN = 40;
    private ExecutorService executorService = Executors.newFixedThreadPool(10);
    private Canvas canvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
    private Button startButton = new Button("Start");
//    private Spinner<Integer> numRowsSpinner = new Spinner<>(4, 45, 8);
//    private Spinner<Integer> numColsSpinner = new Spinner<>(4, 45, 8);
    private NumberField numEpisodesField = new NumberField("2000");
    private CheckBox concurrentCb = new CheckBox("Concurrent");
    private ComboBox<String> learningTypeCombo = new ComboBox<>();

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
        
//        numRowsSpinner.setPrefWidth(60);
//        numColsSpinner.setPrefWidth(60);
        numEpisodesField.setPrefWidth(70);
        
        hbox.getChildren().addAll(label1, numEpisodesField, learningTypeCombo, concurrentCb, startButton);
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
            CommonUtil.numEpisodes = 300;//Integer.parseInt(numEpisodesField.getText().trim());
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
        

        CommonUtil.numEpisodes = 2000;
        CommonUtil.MAX_TICKS = 100_000;
        QEntry[][] q = CartpoleUtil.createInitialQ();
        CartpoleEngine engine = new CartpoleEngine(q);
        CartPoleLearningView learningView = new CartPoleLearningView(canvas.getGraphicsContext2D(), leftMargin, topMargin, q);
        engine.addTickListeners(learningView);
        engine.addEpisodeListeners(learningView);
        engine.addTrialListeners(learningView);
//        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
//                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
        executorService.submit(engine);
    }

    private void executeLearningType2() {
    }

    private void executeLearningType3() {
    }
}
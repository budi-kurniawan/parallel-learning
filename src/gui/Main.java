package gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.listener.LearningView;
import gui.listener.ParallelPolicyView;
import gui.listener.PolicyView;
import gui.listener.TestParallelPolicyView;
import gui.listener.TestPolicyView;
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
import rl.Engine;
import rl.QEntry;
import rl.Util;
import rl.parallel.ParallelEngine;

public class Main extends Application {
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
            if (validateInput()) {
                Util.numRows = numRowsSpinner.getValue();
                Util.numCols = numColsSpinner.getValue();
                String learningType = learningTypeCombo.getValue();
                if (learningType.startsWith("1.")) {
                    executeLearningType1();
                } else if (learningType.startsWith("2.")) {
                    executeLearningType2();
                } else if (learningType.startsWith("3.")) {
                    executeLearningType3();
                }
            }
        });
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
    }
    
    private boolean validateInput() {
        System.out.println(learningTypeCombo.getValue());
        if (learningTypeCombo.getValue() == null) {
            Alert alert = new Alert(AlertType.WARNING, "Please select a learning type", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        try {
            Util.numEpisodes = Integer.parseInt(numEpisodesField.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            Alert alert = new Alert(AlertType.WARNING, "Please enter the number of episodes", ButtonType.OK);
            alert.showAndWait();
        }
        return false;
    }
    
    private void executeLearningType1() {
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;        
        LearningView learningView1 = new LearningView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
        PolicyView policyView1 = new PolicyView(leftMargin, topMargin, canvas.getGraphicsContext2D());
        Engine engine = new Engine();
        engine.addTickListeners(learningView1, policyView1);
        engine.addEpisodeListeners(learningView1, policyView1);
        engine.addTrialListeners(policyView1);
        
        leftMargin = INITIAL_LEFT_MARGIN;
        topMargin += (Util.numRows + 1) * LearningView.cellHeight;
        LearningView learningView2a = new LearningView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
        LearningView learningView2b = new LearningView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        
        leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
        ParallelPolicyView policyView2 = new ParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        ParallelEngine parallelEngine1 = new ParallelEngine(1, q1, q2);
        ParallelEngine parallelEngine2 = new ParallelEngine(2, q2, q1);
        parallelEngine1.addTickListeners(learningView2a);
        parallelEngine1.addEpisodeListeners(learningView2a);
        parallelEngine1.addEpisodeListeners(policyView2);
        parallelEngine1.addTrialListeners(policyView2);

        parallelEngine2.addTickListeners(learningView2b);
        parallelEngine2.addEpisodeListeners(learningView2b);
        parallelEngine2.addEpisodeListeners(policyView2);
        parallelEngine2.addTrialListeners(policyView2);
        
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));

        if (concurrentCb.isSelected()) {
            executorService.submit(engine);
            executorService.submit(parallelEngine1);
            executorService.submit(parallelEngine2);
        } else {
            executorService.execute(() -> {
                try {
                    executorService.submit(engine).get();
                    executorService.submit(parallelEngine1);
                    executorService.submit(parallelEngine2);
                } catch (Exception e) {
                    
                }
            });
        }
        
    }
    private void executeLearningType2() {
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;
        TestPolicyView testPolicyView = new TestPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        Engine engine = new Engine();
        engine.addEpisodeListeners(testPolicyView);

        topMargin += (Util.numRows + 1) * LearningView.cellHeight;
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        QEntry[][] q2 = Util.createInitialQ(Util.numRows,  Util.numCols);
        TestParallelPolicyView policyView2 = new TestParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());

        ParallelEngine parallelEngine1 = new ParallelEngine(1, q1, q2);
        ParallelEngine parallelEngine2 = new ParallelEngine(2, q2, q1);
        parallelEngine1.addEpisodeListeners(policyView2);
        parallelEngine2.addEpisodeListeners(policyView2);
        
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
        if (concurrentCb.isSelected()) {
            executorService.submit(engine);
            executorService.submit(parallelEngine1);
            executorService.submit(parallelEngine2);
        } else {
            executorService.execute(() -> {
                try {
                    executorService.submit(engine).get();
                    executorService.submit(parallelEngine1);
                    executorService.submit(parallelEngine2);
                } catch (Exception e) {
                    
                }
            });
        }
    }

    private void executeLearningType3() {
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;
        TestPolicyView testPolicyView = new TestPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        Engine engine = new Engine();
        engine.addEpisodeListeners(testPolicyView);

        topMargin += (Util.numRows + 1) * LearningView.cellHeight;
        QEntry[][] q1 = Util.createInitialQ(Util.numRows,  Util.numCols);
        TestParallelPolicyView policyView2 = new TestParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());

        ParallelEngine parallelEngine1 = new ParallelEngine(1, q1, q1);
        ParallelEngine parallelEngine2 = new ParallelEngine(2, q1, q1);
        parallelEngine1.addEpisodeListeners(policyView2);
        parallelEngine2.addEpisodeListeners(policyView2);
        
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
        if (concurrentCb.isSelected()) {
            executorService.submit(engine);
            executorService.submit(parallelEngine1);
            executorService.submit(parallelEngine2);
        } else {
            executorService.execute(() -> {
                try {
                    executorService.submit(engine).get();
                    executorService.submit(parallelEngine1);
                    executorService.submit(parallelEngine2);
                } catch (Exception e) {
                }
            });
        }
    }
}
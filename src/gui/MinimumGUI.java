package gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import gui.listener.LearningView;
import gui.listener.ParallelPolicyView;
import gui.listener.PolicyView;
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
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import rl.Util;

public class MinimumGUI extends Application {
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
    private CheckBox runParallelCb = new CheckBox("Run parallel agents too");

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
        hbox.getChildren().addAll(new Label("#Rows:"), numRowsSpinner, new Label("#Columns"), numColsSpinner, label1,
                numEpisodesField, runParallelCb, startButton);
        root.getChildren().add(canvas);
        root.getChildren().add(hbox);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        startButton.setOnAction(event -> {
            if (validateInput()) {
                Util.numRows = numRowsSpinner.getValue();
                Util.numCols = numColsSpinner.getValue();
                int leftMargin = INITIAL_LEFT_MARGIN;
                int topMargin = INITIAL_TOP_MARGIN;
                LearningView learningView1 = new LearningView(leftMargin, topMargin,
                        canvas.getGraphicsContext2D());
                leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
                PolicyView policyView1 = new PolicyView(leftMargin, topMargin, canvas.getGraphicsContext2D());
                QLearningTask task1 = new QLearningTask();
                task1.addTrialListener(policyView1);

                ParallelQLearningTask task2 = null;
                if (runParallelCb.isSelected()) {
                    leftMargin = INITIAL_LEFT_MARGIN;
                    topMargin += (Util.numRows + 1) * LearningView.cellHeight;
                    LearningView learningView2a = new LearningView(leftMargin, topMargin,
                            canvas.getGraphicsContext2D());
                    leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
                    LearningView learningView2b = new LearningView(leftMargin, topMargin,
                            canvas.getGraphicsContext2D());
                    task2 = new ParallelQLearningTask(executorService);
                    leftMargin += (Util.numCols + 1) * LearningView.cellWidth;
                    ParallelPolicyView policyView2 = new ParallelPolicyView(leftMargin, topMargin,
                            canvas.getGraphicsContext2D());
                    task2.addTickListenerForAgent1(learningView2a);
                    task2.addTickListenerForAgent2(learningView2b);
                    task2.addTickListenerForBothAgents(policyView2);
                    
                }
                
                Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                        0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
                executorService.execute(task1);
                if (runParallelCb.isSelected()) {
                    executorService.execute(task2);
                }
            }
        });
    }

    @Override
    public void stop() {
        executorService.shutdownNow();
    }
    
    private boolean validateInput() {
        try {
            Util.numEpisodes = Integer.parseInt(numEpisodesField.getText().trim());
            return true;
        } catch (NumberFormatException e) {
            Alert alert = new Alert(AlertType.WARNING, "Please enter the number of episodes", ButtonType.OK);
            alert.showAndWait();
        }
        return false;
    }
}
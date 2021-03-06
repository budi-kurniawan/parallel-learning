package gridworld.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import common.CommonUtil;
import common.Engine;
import common.Factory;
import common.QEntry;
import common.gui.NumberField;
import gridworld.GridworldFactory;
import gridworld.GridworldUtil;
import gridworld.gui.listener.LearningView;
import gridworld.gui.listener.ParallelPolicyView;
import gridworld.gui.listener.PolicyView;
import gridworld.gui.listener.TestParallelPolicyView;
import gridworld.gui.listener.TestPolicyView;
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

public class GridworldGUI extends Application {
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
                GridworldUtil.numRows = numRowsSpinner.getValue();
                GridworldUtil.numCols = numColsSpinner.getValue();
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
        if (learningTypeCombo.getValue() == null) {
            Alert alert = new Alert(AlertType.WARNING, "Please select a learning type", ButtonType.OK);
            alert.showAndWait();
            return false;
        }
        try {
            CommonUtil.numEpisodes = Integer.parseInt(numEpisodesField.getText().trim());
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
        leftMargin += (GridworldUtil.numCols + 1) * LearningView.cellWidth;
        PolicyView policyView1 = new PolicyView(leftMargin, topMargin, canvas.getGraphicsContext2D());
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        Engine engine = new Engine(factory);
        engine.addTickListeners(learningView1, policyView1);
        engine.addEpisodeListeners(learningView1, policyView1);
        engine.addTrialListeners(policyView1);
        
        leftMargin = INITIAL_LEFT_MARGIN;
        topMargin += (GridworldUtil.numRows + 1) * LearningView.cellHeight;
        LearningView learningView2a = new LearningView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        leftMargin += (GridworldUtil.numCols + 1) * LearningView.cellWidth;
        LearningView learningView2b = new LearningView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        
        leftMargin += (GridworldUtil.numCols + 1) * LearningView.cellWidth;
        ParallelPolicyView policyView2 = new ParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        QEntry[][] q1 = GridworldUtil.createInitialQ();
        Factory factory1 = new GridworldFactory(q1);
        QEntry[][] q2 = GridworldUtil.createInitialQ();
        Factory factory2 = new GridworldFactory(q2);
        Engine parallelEngine1 = new Engine(0, factory1);
        Engine parallelEngine2 = new Engine(1, factory2);
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
                    e.printStackTrace();
                }
            });
        }
        
    }

    private void executeLearningType2() {
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;
        TestPolicyView testPolicyView = new TestPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        Engine engine = new Engine(factory);
        engine.addEpisodeListeners(testPolicyView);

        topMargin += (GridworldUtil.numRows + 1) * LearningView.cellHeight;
        QEntry[][] q1 = GridworldUtil.createInitialQ();
        QEntry[][] q2 = GridworldUtil.createInitialQ();
        Factory factory1 = new GridworldFactory(q1);
        Factory factory2 = new GridworldFactory(q2);
        TestParallelPolicyView policyView2 = new TestParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());

        Engine[] parallelEngines = new Engine[2];
        parallelEngines[0] = new Engine(0, factory1);
        parallelEngines[1] = new Engine(1, factory2);
        
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
        if (concurrentCb.isSelected()) {
            executorService.submit(engine);
            for (Engine parallelEngine : parallelEngines) {
                executorService.submit(parallelEngine);
            }
        } else {
            executorService.execute(() -> {
                try {
                    executorService.submit(engine).get();
                    for (Engine parallelEngine : parallelEngines) {
                        executorService.submit(parallelEngine);
                    }
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
        QEntry[][] q = GridworldUtil.createInitialQ();
        Factory factory = new GridworldFactory(q);
        Engine engine = new Engine(factory);
        engine.addEpisodeListeners(testPolicyView);

        topMargin += (GridworldUtil.numRows + 1) * LearningView.cellHeight;
        QEntry[][] q1 = GridworldUtil.createInitialQ();
        Factory factory1 = new GridworldFactory(q1);
        TestParallelPolicyView policyView2 = new TestParallelPolicyView(leftMargin, topMargin,
                canvas.getGraphicsContext2D());

        Engine[] parallelEngines = new Engine[2];
        parallelEngines[0] = new Engine(0, factory1);
        parallelEngines[0].addEpisodeListeners(policyView2);
        parallelEngines[1] = new Engine(1, factory1);
        parallelEngines[1].addEpisodeListeners(policyView2);
        Platform.runLater(() -> canvas.getGraphicsContext2D().clearRect(
                0, 0, CANVAS_WIDTH, CANVAS_HEIGHT));
        if (concurrentCb.isSelected()) {
            executorService.submit(engine);
            for (Engine parallelEngine : parallelEngines) {
                executorService.submit(parallelEngine);
            }
        } else {
            executorService.execute(() -> {
                try {
                    executorService.submit(engine).get();
                    for (Engine parallelEngine : parallelEngines) {
                        executorService.submit(parallelEngine);
                    }
                } catch (Exception e) {
                }
            });
        }
    }
}
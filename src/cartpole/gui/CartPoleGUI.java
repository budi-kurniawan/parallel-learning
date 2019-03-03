package cartpole.gui;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cartpole.ActorCriticCartpoleFactory;
import cartpole.CartpoleFactory;
import cartpole.CartpoleUtil;
import cartpole.QLearningCartpoleFactory;
import cartpole.gui.listener.CartPoleLearningView;
import common.CommonUtil;
import common.Engine;
import common.QEntry;
import common.agent.QLearningAgent;
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
        primaryStage.setTitle("Cartpole");
        Group root = new Group();
        HBox hbox = new HBox();
        hbox.setSpacing(10);
        Label label1 = new Label("#Episodes:");
        learningTypeCombo.getItems().addAll("1. Q-Learning", "2. Actor Critic");
        learningTypeCombo.getSelectionModel().selectFirst();
        
//        numRowsSpinner.setPrefWidth(60);
//        numColsSpinner.setPrefWidth(60);
        numEpisodesField.setPrefWidth(70);
        
        hbox.getChildren().addAll(label1, numEpisodesField, learningTypeCombo, /*concurrentCb,*/ startButton);
        root.getChildren().add(canvas);
        root.getChildren().add(hbox);
        primaryStage.setScene(new Scene(root));
        primaryStage.show();

        startButton.setOnAction(event -> {
            if (validateInput()) {
                String learningType = learningTypeCombo.getValue();
                if (learningType.startsWith("1.")) {
                    executeQLearning();
                } else if (learningType.startsWith("2.")) {
                    executeActorCriticLearning();
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
    
    private void executeQLearning() {
        System.out.println("q learning");
        QLearningAgent.ALPHA = 0.1F;
        QLearningAgent.GAMMA = 0.99F;
        QEntry[][] q = CartpoleUtil.createInitialQ();
        CartpoleFactory factory = new QLearningCartpoleFactory(q);
        doExecuteLearning(factory);
    }
    
    private void executeActorCriticLearning() {
        System.out.println("actor critic");
        CartpoleFactory factory = new ActorCriticCartpoleFactory();
        doExecuteLearning(factory);
    }
    
    private void doExecuteLearning(CartpoleFactory factory) {
        CommonUtil.MAX_TICKS = 100_000;
        int leftMargin = INITIAL_LEFT_MARGIN;
        int topMargin = INITIAL_TOP_MARGIN;
        
        Engine engine = new Engine(factory);
        CartPoleLearningView learningView = new CartPoleLearningView(canvas.getGraphicsContext2D(), 
                leftMargin, topMargin);
        engine.addTickListeners(learningView);
        engine.addEpisodeListeners(learningView);
        engine.addTrialListeners(learningView);
        executorService.submit(engine);
    }
}
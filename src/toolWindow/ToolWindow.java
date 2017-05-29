package toolWindow;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.Utils;

/**
 * Created by Jed on 29-May-17.
 */
public class ToolWindow extends Application {

    private static Stage primaryStage;
    private static Light.Point light;
    private static Circle circle;
    private static Utils.Vector2f globalLightPos;
    private static NumericField xPosBox, yPosBox;

    @Override
    public void start(Stage primaryStage) throws Exception {
        ToolWindow.primaryStage = primaryStage;
        primaryStage.setTitle("RTI Viewer");

        Scene scene = createScene(primaryStage);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.show();
    }


    private static Scene createScene(Stage primaryStage){
        FlowPane flowPane = new FlowPane();
        Scene scene = new Scene(flowPane, 400, 600);

        MenuBar menuBar = createMenuBar(primaryStage);
        flowPane.getChildren().add(menuBar);

        Pane lightControlGroup = createLightControl(primaryStage, flowPane);
        flowPane.getChildren().add(lightControlGroup);

        ComboBox<String> comboBox = createFilterChoiceBox();
        flowPane.getChildren().add(comboBox);

        flowPane.setMargin(lightControlGroup, new Insets(20, 0, 0, 20));
        flowPane.setMargin(comboBox, new Insets(20, 0, 0, (scene.getWidth() / 2) - (comboBox.getPrefWidth() / 2) ));
        return scene;
    }


    private static MenuBar createMenuBar(Stage primaryStage){
        MenuBar menuBar = new MenuBar();
        Menu menuFile = new Menu("File");
        MenuItem open = new MenuItem("Open");
        menuFile.getItems().add(open);

        Menu menuEdit = new Menu("Edit");
        Menu menuView = new Menu("View");
        menuBar.getMenus().addAll(menuFile, menuEdit, menuView);
        menuBar.prefWidthProperty().bind(primaryStage.widthProperty());

        return menuBar;
    }


    private static Pane createLightControl(Stage primaryStage, FlowPane parent){
        Pane pane = new Pane();

        Circle circle = createSpecularBall(primaryStage, pane);

        pane.getChildren().add(circle);
        circle.setLayoutX(circle.getRadius());
        circle.setLayoutY(circle.getRadius());

        Label xPosLabel = new Label("Light X:");
        xPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        xPosLabel.setLayoutY(40);

        Label yPosLabel = new Label("Light Y:");
        yPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        yPosLabel.setLayoutY(70);

        xPosBox = new NumericField("0.0") {
            @Override
            public void updateAction(String stringValue) {
                updateGlobalLightPos(new Utils.Vector2f(Float.parseFloat(stringValue), globalLightPos.y));
                updateLightControls();
            }
        };
        xPosBox.setPrefWidth(60);
        xPosBox.setLayoutX(2 * circle.getRadius() + 90);
        xPosBox.setLayoutY(37);

        yPosBox = new NumericField("0.0") {
            @Override
            public void updateAction(String stringValue) {
                updateGlobalLightPos(new Utils.Vector2f(globalLightPos.x, Float.parseFloat(stringValue)));
                updateLightControls();
            }
        };
        yPosBox.setPrefWidth(60);
        yPosBox.setLayoutX(2 * circle.getRadius() + 90);
        yPosBox.setLayoutY(67);

        pane.getChildren().addAll(xPosLabel, xPosBox, yPosLabel, yPosBox);

        return pane;
    }


    private static Circle createSpecularBall(Stage primaryStage, Pane parentGroup){
        circle = new Circle();
        circle.setRadius(100.0);
        circle.setFill(Paint.valueOf("#d3d3d3"));
        light = new Light.Point();
        light.setColor(Color.WHITE);

        light.setX(circle.getRadius());
        light.setY(circle.getRadius());
        light.setZ(25);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        circle.setEffect(lighting);


        circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getX() > circle.getCenterX() - circle.getRadius()
                        && event.getX() < circle.getCenterX() + circle.getRadius()
                        && event.getY() > circle.getCenterY() - circle.getRadius()
                        && event.getY() < circle.getCenterY() + circle.getRadius()) {
                    light.setX(event.getX() + 100);
                    light.setY(event.getY() + 100);
                    calculateNormalisedLight(light, circle);
                    updateLightControls();
                }
            }
        });

        circle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getX() > circle.getCenterX() - circle.getRadius()
                    && event.getX() < circle.getCenterX() + circle.getRadius()
                    && event.getY() > circle.getCenterY() - circle.getRadius()
                    && event.getY() < circle.getCenterY() + circle.getRadius()) {
                    light.setX(event.getX() + 100);
                    light.setY(event.getY() + 100);
                }else{
                    double theta = Math.atan2(event.getY(), event.getX()) + Math.PI;
                    double x = circle.getRadius() - (circle.getRadius() * Math.cos(theta));
                    double y = circle.getRadius() - (circle.getRadius() * Math.sin(theta));
                    light.setX(x);
                    light.setY(y);
                }
                calculateNormalisedLight(light, circle);
                updateLightControls();
            }
        });

        return circle;
    }


    private static void updateGlobalLightPos(Utils.Vector2f newLight){
        if(newLight.length() > 1.0) {
            globalLightPos = newLight.normalise();
        }else{
            globalLightPos = newLight;
        }
    }

    private static void updateLightControls(){
        light.setX(globalLightPos.x * circle.getRadius() + circle.getLayoutX());
        light.setY(globalLightPos.y * circle.getRadius() + circle.getLayoutY());
        xPosBox.setText(String.valueOf(globalLightPos.x));
        yPosBox.setText(String.valueOf(globalLightPos.y));
    }

    private static void calculateNormalisedLight(Light.Point light, Circle circle){
        double x = (light.getX() - circle.getRadius()) / circle.getRadius();
        double y = (light.getY() - circle.getRadius()) / circle.getRadius();
        updateGlobalLightPos(new Utils.Vector2f((float)x, (float)y));
    }


    private static ComboBox<String> createFilterChoiceBox(){
        ObservableList<String> options = FXCollections.observableArrayList(
                "Default view",
                "Normals visualisation",
                "Diffuse gain",
                "Specular enhancement"
        );
        ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().select(0);
        comboBox.setPrefWidth(180);

        //return selectorBox;
        return comboBox;
    }


    public static void main(String[] args) {
        launch(args);
    }
}

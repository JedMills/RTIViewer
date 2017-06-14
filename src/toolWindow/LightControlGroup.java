package toolWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import utils.Utils;

/**
 * Created by Jed on 14-Jun-17.
 */
public class LightControlGroup extends Pane {


    private RTIViewer rtiViewer;
    private Pane parent;
    private Stage primaryStgae;
    private Light.Point light;
    private Circle circle;
    private Spinner<Double> xPosBox, yPosBox;
    public enum LightEditor{CIRCLE, XSPINNER, YSPINNER, RESIZE;}


    public LightControlGroup(RTIViewer rtiViewer, Stage primaryStage, Pane parent) {
        this.rtiViewer = rtiViewer;
        this.parent = parent;
        this.primaryStgae = primaryStage;

        createLightControl();
    }


    private Circle createSpecularBall(){
        circle = new Circle();
        circle.setFill(Paint.valueOf("#d3d3d3"));
        light = new Light.Point();
        light.setColor(Color.WHITE);

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
                    light.setX(event.getX() + circle.getLayoutX());
                    light.setY(event.getY() + circle.getLayoutY());
                    Utils.Vector2f normalised = calculateNormalisedLight(light, circle);
                    updateGlobalLightPos(normalised, LightEditor.CIRCLE);
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
                    light.setX(event.getX() + circle.getLayoutX());
                    light.setY(event.getY() + circle.getLayoutY());
                }else{
                    double theta = Math.atan2(event.getY(), event.getX()) + Math.PI;
                    double x = circle.getLayoutX() - (circle.getRadius() * Math.cos(theta));
                    double y = circle.getLayoutY() - (circle.getRadius() * Math.sin(theta));
                    light.setX(x);
                    light.setY(y);
                }
                Utils.Vector2f normalised = calculateNormalisedLight(light, circle);
                updateGlobalLightPos(normalised, LightEditor.CIRCLE);
            }
        });

        return circle;
    }


    private void createLightControl(){
        Circle circle = createSpecularBall();

        getChildren().add(circle);
        circle.setLayoutX(circle.getRadius());
        circle.setLayoutY(circle.getRadius());

        Label xPosLabel = new Label("Light X:");
        xPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        xPosLabel.setLayoutY(40);

        Label yPosLabel = new Label("Light Y:");
        yPosLabel.setLayoutX(2 * circle.getRadius() + 40);
        yPosLabel.setLayoutY(70);

        xPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        xPosBox.setEditable(true);
        xPosBox.setPrefWidth(80);
        xPosBox.setLayoutX(2 * circle.getRadius() + 90);
        xPosBox.setLayoutY(37);
        xPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Float value = Float.parseFloat(xPosBox.getEditor().getText());
                    updateGlobalLightPos(new Utils.Vector2f(value, RTIViewer.globalLightPos.y), LightEditor.XSPINNER);
                }catch(NumberFormatException e){
                    RTIViewer.entryAlert.setContentText("Invalid entry for light X position.");
                    RTIViewer.entryAlert.showAndWait();
                }
            }
        });

        xPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                Float value = Float.parseFloat(xPosBox.getEditor().getText());
                updateGlobalLightPos(new Utils.Vector2f(value, RTIViewer.globalLightPos.y), LightEditor.XSPINNER);
            }
        });

        yPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        yPosBox.setEditable(true);
        yPosBox.setPrefWidth(80);
        yPosBox.setLayoutX(2 * circle.getRadius() + 90);
        yPosBox.setLayoutY(67);
        yPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    Float value = Float.parseFloat(yPosBox.getEditor().getText());
                    updateGlobalLightPos(new Utils.Vector2f(RTIViewer.globalLightPos.x, value), LightEditor.YSPINNER);
                }catch(NumberFormatException e){
                    RTIViewer.entryAlert.setContentText("Invalid entry for light Y position.");
                    RTIViewer.entryAlert.showAndWait();
                }
            }
        });

        yPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            @Override
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                Float value = Float.parseFloat(yPosBox.getEditor().getText());
                updateGlobalLightPos(new Utils.Vector2f(RTIViewer.globalLightPos.x, value), LightEditor.YSPINNER);
            }
        });

        getChildren().addAll(xPosLabel, xPosBox, yPosLabel, yPosBox);
    }


    private void updateGlobalLightPos(Utils.Vector2f newLight, LightEditor source){
        if(newLight.length() > 1.0) {
            RTIViewer.globalLightPos = newLight.normalise();
        }else{
            RTIViewer.globalLightPos = newLight;
        }
        updateLightControls(source);
    }

    private Utils.Vector2f calculateNormalisedLight(Light.Point light, Circle circle){
        double x = (light.getX() - circle.getRadius()) / circle.getRadius();
        double y = -(light.getY() - circle.getRadius()) / circle.getRadius();
        return new Utils.Vector2f((float)x, (float)y);
    }

    private void updateLightControls(LightEditor source){
        if(!source.equals(LightEditor.CIRCLE)) {
            light.setX(RTIViewer.globalLightPos.x * circle.getRadius() + circle.getLayoutX());
            light.setY(-RTIViewer.globalLightPos.y * circle.getRadius() + circle.getLayoutY());
        }
        if(!source.equals(LightEditor.XSPINNER)){
            xPosBox.getValueFactory().setValue((double)RTIViewer.globalLightPos.x);
        }
        else if(!source.equals(LightEditor.YSPINNER)){
            yPosBox.getValueFactory().setValue((double)RTIViewer.globalLightPos.y);
        }
    }


    public void updateSize(double width, double height){
        setWidth(width);
        setHeight(height);

        if(width < 516){
            circle.setRadius(width / 4.5);
        }else{
            circle.setRadius(height / 2);
        }
        circle.setLayoutX(circle.getRadius() / 2);
        circle.setLayoutY(circle.getRadius());

        updateGlobalLightPos(new Utils.Vector2f(0,0), LightEditor.RESIZE);
    }
}

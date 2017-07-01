package toolWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.effect.Light;
import javafx.scene.effect.Lighting;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Material;
import javafx.scene.paint.Paint;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import openGLWindow.RTIWindow;
import utils.Utils;

import static java.lang.Math.*;

/**
 * Created by Jed on 14-Jun-17.
 */
public class LightControlGroup extends StackPane {


    private RTIViewer rtiViewer;
    private Pane parent;
    private Stage primaryStgae;
    private Light.Point light;
    private Circle circle;
    private Spinner<Double> xPosBox, yPosBox;
    public enum LightEditor{CIRCLE, XSPINNER, YSPINNER, RESIZE;}

    private StackPane stackPane;
    private HBox hBox;
    private GridPane gridPane;
    private VBox vBox;

    private Label xPosLabel;
    private Label yPosLabel;

    private boolean wasVertical = true;



    public LightControlGroup(RTIViewer rtiViewer, Stage primaryStage, Pane parent) {
        this.rtiViewer = rtiViewer;
        this.parent = parent;
        this.primaryStgae = primaryStage;

        createLightControl();
        setId("lightControlGroup");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(5, 0, 5, 0));
    }


    private Circle createSpecularBall(){
        circle = new Circle();
        circle.setFill(Paint.valueOf("#d3d3d3"));

        light = new Light.Point();
        light.setColor(Color.WHITE);
        circle.setRadius(75);

        light.setX(circle.getRadius());
        light.setY(circle.getRadius());
        light.setZ(25);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        circle.setEffect(lighting);

        circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                light.setX(event.getX() + circle.getRadius());
                light.setY(event.getY() + circle.getRadius());
                Utils.Vector2f normalised = new Utils.Vector2f((float)(event.getX() / circle.getRadius()),
                        (float)(-event.getY() / circle.getRadius()));
                updateGlobalLightPos(normalised, LightEditor.CIRCLE);
            }
        });

        circle.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                if(event.getX() >  - circle.getRadius()
                        && event.getX() <  circle.getRadius()
                        && event.getY() >  - circle.getRadius()
                        && event.getY() <  circle.getRadius()) {
                    light.setX(event.getX() + circle.getRadius());
                    light.setY(event.getY() + circle.getRadius());
                }else{
                    double theta = Math.atan2(event.getY(), event.getX());
                    double x = circle.getRadius() + (circle.getRadius() * Math.cos(theta));
                    double y = circle.getRadius() + (circle.getRadius() * Math.sin(theta));
                    light.setX(x);
                    light.setY(y);
                }
                Utils.Vector2f normalised = new Utils.Vector2f((float)(event.getX() / circle.getRadius()),
                        (float)(-event.getY() / circle.getRadius()));
                updateGlobalLightPos(normalised, LightEditor.CIRCLE);
            }
        });

        return circle;
    }


    private void createLightControl(){
        hBox = new HBox();
        vBox = new VBox();
        stackPane = new StackPane();

        stackPane.setAlignment(Pos.TOP_CENTER);
        gridPane = new GridPane();

        Circle circle = createSpecularBall();

        stackPane.getChildren().add(circle);

        xPosLabel = new Label("Light X:");
        yPosLabel = new Label("Light Y:");

        xPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        xPosBox.setEditable(true);
        xPosBox.setMinHeight(0);

        xPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateLightFromBox(xPosBox, RTIViewer.globalLightPos.y, LightEditor.XSPINNER);
            }
        });

        xPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                updateLightFromBox(xPosBox, RTIViewer.globalLightPos.y, LightEditor.XSPINNER);
            }
        });


        xPosBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    updateLightFromBox(xPosBox, RTIViewer.globalLightPos.y, LightEditor.XSPINNER);
                }
            }
        });

        yPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        yPosBox.setEditable(true);
        yPosBox.setMinHeight(0);

        yPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                updateLightFromBox(yPosBox, RTIViewer.globalLightPos.x, LightEditor.YSPINNER);
            }
        });

        yPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                updateLightFromBox(yPosBox, RTIViewer.globalLightPos.x, LightEditor.YSPINNER);
            }
        });


        yPosBox.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                if(!newValue){
                    updateLightFromBox(yPosBox, RTIViewer.globalLightPos.x, LightEditor.YSPINNER);
                }
            }
        });


        gridPane.setAlignment(Pos.CENTER);
        gridPane.getChildren().addAll(xPosLabel, xPosBox, yPosLabel, yPosBox);
    }


    private void updateLightFromBox(Spinner<Double> spinner, float constVal, LightEditor lightEditor){
        try {
            Float value = Float.parseFloat(spinner.getEditor().getText());
            if(lightEditor.equals(LightEditor.XSPINNER)) {
                updateGlobalLightPos(new Utils.Vector2f(value, constVal), lightEditor);
            }else{
                updateGlobalLightPos(new Utils.Vector2f(constVal, value), lightEditor);
            }
        }catch(NumberFormatException e){
            if(lightEditor.equals(LightEditor.XSPINNER)) {
                RTIViewer.entryAlert.setContentText("Invalid entry for light X position.");
            }else{
                RTIViewer.entryAlert.setContentText("Invalid entry for light Y position.");
            }
            RTIViewer.entryAlert.showAndWait();
        }
    }



    private void updateGlobalLightPos(Utils.Vector2f newLight, LightEditor source){
        if(newLight.length() > 1.0) {
            RTIViewer.globalLightPos = newLight.normalise();
        }else{
            RTIViewer.globalLightPos = newLight;
        }
        updateLightControls(source);
    }


    public void updateLightControls(LightEditor source){
        if(!source.equals(LightEditor.CIRCLE)){
            light.setX(circle.getRadius() + (circle.getRadius() * RTIViewer.globalLightPos.x));
            light.setY(circle.getRadius() -(circle.getRadius() * RTIViewer.globalLightPos.y));
        }
        if(!source.equals(LightEditor.XSPINNER)){
            xPosBox.getValueFactory().setValue((double)RTIViewer.globalLightPos.x);
        }
        if(!source.equals(LightEditor.YSPINNER)){
            yPosBox.getValueFactory().setValue((double)RTIViewer.globalLightPos.y);
        }
    }


    public void updateSize(double width, double height){
        setPrefWidth(width - 20);

        updateGlobalLightPos(new Utils.Vector2f(RTIViewer.globalLightPos.x,
                                                RTIViewer.globalLightPos.y),
                                                LightEditor.RESIZE);

        updateComponentSizes(width, height);
    }

    private void updateComponentSizes(double width, double height){
        if(height < 700){
            light.setZ(20);
            circle.setRadius(50);
        }else if(height < 800 ||Double.isNaN(height)){
            light.setZ(25);
            circle.setRadius(75);
        }else{
            light.setZ(30);
            circle.setRadius(90);
        }

        if(width < 335){
            setSpinnerSizesForVertical(width);
            if(!wasVertical) {
                setVerticalAlignment();
                setSpinnerSizesForVertical(width);
                wasVertical = true;
            }
        }else if(width >= 335){
            setSpinnerSizes(16, 16, 30,(int) width / 5);
            if(wasVertical) {
                setHorizontalAlignment();
                wasVertical = false;
            }
        }
    }


    private void setSpinnerSizesForVertical(double height){
        if(height < 700){
            setSpinnerSizes(10, 10, 20, 60);
        }else if(height < 800){
            setSpinnerSizes(12, 12, 25, 60);
        }else{
            setSpinnerSizes(14, 14, 30, 60);
        }
    }



    private void setSpinnerSizes(int labelFontSize, int spinnerFontSize, int spinnerHeight, int spinnerWidth){
        xPosBox.setPrefWidth(spinnerWidth);
        xPosBox.setPrefHeight(spinnerHeight);
        yPosBox.setPrefWidth(spinnerWidth);
        yPosBox.setPrefHeight(spinnerHeight);

        xPosBox.getEditor().setFont(Font.font(spinnerFontSize));
        yPosBox.getEditor().setFont(Font.font(spinnerFontSize));

        xPosLabel.setFont(Font.font(labelFontSize));
        yPosLabel.setFont(Font.font(labelFontSize));
    }

    private void setVerticalAlignment(){
        hBox.getChildren().removeAll(stackPane, gridPane);
        vBox.getChildren().removeAll(stackPane, gridPane);
        getChildren().removeAll(hBox, vBox);
        GridPane.setConstraints(xPosLabel, 0, 0, 1, 1);
        GridPane.setConstraints(xPosBox, 1, 0, 1, 1);
        GridPane.setConstraints(yPosLabel, 2, 0, 1, 1);
        GridPane.setConstraints(yPosBox, 3, 0, 1, 1);
        vBox.setMargin(gridPane, new Insets(5, 0, 0, 0));

        gridPane.setVgap(0);
        gridPane.setHgap(5);
        gridPane.setAlignment(Pos.CENTER);

        vBox.getChildren().addAll(stackPane, gridPane);
        getChildren().add(vBox);
    }


    private void setHorizontalAlignment(){
        hBox.getChildren().removeAll(stackPane, gridPane);
        vBox.getChildren().removeAll(stackPane, gridPane);
        getChildren().removeAll(hBox, vBox);
        GridPane.setConstraints(xPosLabel, 0, 0, 1, 1);
        GridPane.setConstraints(xPosBox, 1, 0, 1, 1);
        GridPane.setConstraints(yPosLabel, 0, 1, 1, 1);
        GridPane.setConstraints(yPosBox, 1, 1, 1, 1);


        hBox.setMargin(gridPane, new Insets(0, 0, 0, 10));
        gridPane.setAlignment(Pos.CENTER);
        hBox.setAlignment(Pos.CENTER);

        gridPane.setVgap(10);
        gridPane.setHgap(5);

        hBox.getChildren().addAll(stackPane, gridPane);
        getChildren().add(hBox);
    }
}

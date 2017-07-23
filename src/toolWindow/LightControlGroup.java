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
 * The pane containing the specular ball control and the x and y light position boxes.
 */
public class LightControlGroup extends StackPane {

    /** The light thatshines on the specular ball and is moved by the user*/
    private Light.Point light;

    /** The specular ball that the light shines on*/
    private Circle circle;

    /** The spinner for th light x position */
    private Spinner<Double> xPosBox;

    /** The spinner for the light y position */
    private Spinner<Double> yPosBox;

    /** Used to prevent feedback loop in updating the light from the spinners and ball */
    public enum LightEditor{CIRCLE, XSPINNER, YSPINNER, RESIZE;}

    /** StackPane containing the specular ball and the light  */
    private StackPane stackPane;

    /** HBox containing the ball and spinners in the horizontal layout */
    private HBox hBox;

    /** GridPane for the spinners */
    private GridPane gridPane;

    /** HBox containing the ball and spinners in the vertical layout */
    private VBox vBox;

    /** Label for the x spinner */
    private Label xPosLabel;

    /** Label for the y spinner*/
    private Label yPosLabel;

    /** Used to change between vertical and horizontal layouts */
    private boolean wasVertical = true;


    /**
     * Creates a new LightControlGroup.
     */
    public LightControlGroup() {
        createLightControl();
        setId("lightControlGroup");
        setAlignment(Pos.CENTER);
        setPadding(new Insets(5, 0, 5, 0));
    }


    /**
     * Creates the grey specular ball, and the light that shines on it.Also adds the listeners to mouse events
     * to make the light position change with the mouse.
     *
     * @return  the grey specular ball widget
     */
    private Circle createSpecularBall(){
        //the ball, a nice grey colour
        circle = new Circle();
        circle.setFill(Paint.valueOf("#d3d3d3"));

        //the light that shines from above
        light = new Light.Point();
        light.setColor(Color.WHITE);
        circle.setRadius(75);

        //set the light in the center, and 25px away is quite a good distance
        light.setX(circle.getRadius());
        light.setY(circle.getRadius());
        light.setZ(25);

        Lighting lighting = new Lighting();
        lighting.setLight(light);
        circle.setEffect(lighting);

        //move the light to the mouse position on click
        circle.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                light.setX(event.getX() + circle.getRadius());
                light.setY(event.getY() + circle.getRadius());
                Utils.Vector2f normalised = new Utils.Vector2f((float)(event.getX() / circle.getRadius()),
                        (float)(-event.getY() / circle.getRadius()));
                //update the global light position for rendering
                updateGlobalLightPos(normalised, LightEditor.CIRCLE);
            }
        });

        //move the light to the mouse position on mouse drag
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
                    //if the mouse is outside the circle, constrain it using polar coordinates
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


    /**
     * Creates the light control layout, with the specular ball and linked x and y spinners.
     */
    private void createLightControl(){
        //hbox for horizontal layout
        hBox = new HBox();
        //vbox for vertical layout
        vBox = new VBox();
        //stacjkpane for circle and light
        stackPane = new StackPane();

        stackPane.setAlignment(Pos.TOP_CENTER);
        gridPane = new GridPane();

        //the specular circle
        Circle circle = createSpecularBall();

        stackPane.getChildren().add(circle);

        xPosLabel = new Label("Light X:");
        yPosLabel = new Label("Light Y:");

        xPosBox = new Spinner<Double>(-1.0, 1.0, 0.0, 0.01);
        xPosBox.setEditable(true);
        xPosBox.setMinHeight(0);

        //update the global light position from the x spinner when typed in
        xPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                updateLightFromBox(xPosBox, RTIViewer.globalLightPos.y, LightEditor.XSPINNER);
            }
        });

        //update the global light position from the x spinner when clicked
        xPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                updateLightFromBox(xPosBox, RTIViewer.globalLightPos.y, LightEditor.XSPINNER);
            }
        });

        //if the x position box is unfocused, update the light position from what's in it
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

        //update the global light position from the y spinner when typed in
        yPosBox.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            public void handle(ActionEvent event) {
                updateLightFromBox(yPosBox, RTIViewer.globalLightPos.x, LightEditor.YSPINNER);
            }
        });

        //update the global light position from the y spinner when clicked
        yPosBox.valueProperty().addListener(new ChangeListener<Double>() {
            public void changed(ObservableValue<? extends Double> observable, Double oldValue, Double newValue) {
                updateLightFromBox(yPosBox, RTIViewer.globalLightPos.x, LightEditor.YSPINNER);
            }
        });


        //if the y position box is unfocused, update the light position from what's in it
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


    /**
     * Updates the light position from a spinner.
     *
     * @param spinner           spinner that caused the change
     * @param constVal          the value that is not the one to be changed
     * @param lightEditor       wether the thing that changed the light position was a spinner or the ball widget
     */
    private void updateLightFromBox(Spinner<Double> spinner, float constVal, LightEditor lightEditor){
        try {
            Float value = Float.parseFloat(spinner.getEditor().getText());
            //update the global position of the light
            if(lightEditor.equals(LightEditor.XSPINNER)) {
                updateGlobalLightPos(new Utils.Vector2f(value, constVal), lightEditor);
            }else{
                updateGlobalLightPos(new Utils.Vector2f(constVal, value), lightEditor);
            }
        }catch(NumberFormatException e){
            //the user typed in a non number
            if(lightEditor.equals(LightEditor.XSPINNER)) {
                RTIViewer.entryAlert.setContentText("Invalid entry for light X position.");
            }else{
                RTIViewer.entryAlert.setContentText("Invalid entry for light Y position.");
            }
            RTIViewer.entryAlert.showAndWait();
        }
    }


    /**
     * Used for updating the {@link RTIViewer#globalLightPos} from the light control widgets.
     *
     * @param newLight      the light position to change to
     * @param source        the widget that caused the light position to change
     */
    private void updateGlobalLightPos(Utils.Vector2f newLight, LightEditor source){
        //the light can never be greater than one in length, as PTM and SH assumes <= 1
        if(newLight.length() > 1.0) {
            RTIViewer.globalLightPos = newLight.normalise();
        }else{
            RTIViewer.globalLightPos = newLight;
        }
        //update all the other light control widgets with the changed light
        updateLightControls(source);
    }


    /**
     * Updates all the light controls with the {@link RTIViewer#globalLightPos}. Used when one widget changes the
     * position of the light and the other widgets need to be updated.
     *
     * @param source    the widget that chaged the light position
     */
    public void updateLightControls(LightEditor source){
        //update only the widgets that didn't cause the change in light pos so there isn't a feedback
        //loop with a widget keeps changing itself
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


    /**
     * Updates the size of the light control widget pane. Can cause change in layout from horizontal to vertical.
     *
     * @param width         width of the main window the pane is in
     * @param height        height of the main window the pane is in
     */
    public void updateSize(double width, double height){
        setPrefWidth(width - 20);

        //make the light position stay in the same place as the specular ball moves about
        updateGlobalLightPos(new Utils.Vector2f(RTIViewer.globalLightPos.x,
                                                RTIViewer.globalLightPos.y),
                                                LightEditor.RESIZE);

        //update the size of the widget, maybe change to vertical/horizontal layout
        updateComponentSizes(width, height);
    }

    /**
     * Updates the size of the widgets in the light control pane and switches between horizontal and vertical layout
     * when the width and height are past certain values.
     *
     * @param width         width of the main window the pane is in
     * @param height        height of the main window the pane is in
     */
    private void updateComponentSizes(double width, double height){
        //makes the specular ball bigger and move the light back so that it scales nicely with the ball
        if(height < 700){
            light.setZ(20);
            circle.setRadius(50);
        }else if(height < 800 || Double.isNaN(height)){
            light.setZ(25);
            circle.setRadius(75);
        }else{
            light.setZ(30);
            circle.setRadius(90);
        }

        //switching between horizontal and vertical layout, wasVertical bool needed because otherwise
        //the layout sometimess flip or rearrange when resizing without changing layout
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


    /**
     * Sets the sizes of spinners for alignment of specular ball and spinner.
     *
     * @param height    height of the main toolbar
     */
    private void setSpinnerSizesForVertical(double height){
        if(height < 700){
            setSpinnerSizes(10, 10, 20, 60);
        }else if(height < 800){
            setSpinnerSizes(12, 12, 25, 60);
        }else{
            setSpinnerSizes(14, 14, 30, 60);
        }
    }


    /**
     * convenience method to set the sizes of all the spinners and fonts in the light control widget pane.
     *
     * @param labelFontSize         size of the XPos and YPos labels
     * @param spinnerFontSize       size of the font in the spinners
     * @param spinnerHeight         height to set the spinners
     * @param spinnerWidth          width to set the spinners
     */
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



    /**
     * Sets the light control widget group to have the vertical alignment, with spinners below the specular ball.
     */
    private void setVerticalAlignment(){
        //remove everything from the horizontal alignment box
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

        //add them to the vertical alignment
        vBox.getChildren().addAll(stackPane, gridPane);
        getChildren().add(vBox);
    }



    /**
     * Sets the light control widget group to have the horizontal alignment, with spinners to the right of the
     * specular ball.
     */
    private void setHorizontalAlignment(){
        //remove the widgets from the vertical alignment
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

        //add them to horizontal alignment
        hBox.getChildren().addAll(stackPane, gridPane);
        getChildren().add(hBox);
    }
}

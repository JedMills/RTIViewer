package toolWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

/**
 * Created by Jed on 29-May-17.
 */
public class FilterParamsPane extends Pane {

    private RTIViewer toolWindow;
    private ComboBox<String> filterChoiceBox;
    private Scene parent;
    private int width;
    private int height;

    public static final double INITIAL_DIFF_GAIN_VAL = 0.0;
    public static final double INITIAL_DIFF_COLOUR_VAL = 0.0;
    public static final double INITIAL_SPEC_VAL = 0.0;
    public static final double INITIAL_HIGHLIGHT_VAL = 0.0;

    private Label gainLabel;
    private Slider gainSlider;
    private Spinner<Double> gainSpinner;

    private Label seColourLabel;
    private Slider seColourSlider;
    private Spinner<Double> seColourSpinner;

    private Label seSpecLabel;
    private Slider seSpecSlider;
    private Spinner<Double> seSpecSpinner;

    private Label seHighlightLabel;
    private Slider seHighlightSlider;
    private Spinner<Double> seHighlightSpinner;

    private Control[] allControls;

    public FilterParamsPane(RTIViewer toolWindow, Scene parent, ComboBox<String> filterChoiceBox) {
        super();
        this.filterChoiceBox = filterChoiceBox;
        this.parent = parent;
        this.toolWindow = toolWindow;

        setWidth(parent.getWidth());
        setHeight(200);
        createComponents();

         allControls = new Control[]{gainLabel, gainSlider, gainSpinner, seColourLabel, seColourSlider,
                seColourSpinner, seSpecLabel, seSpecSlider, seSpecSpinner,
                seHighlightLabel, seHighlightSlider, seHighlightSpinner};

        hideAllItems();
    }

    public FilterParamsPane(Node... children) {
        super(children);
    }

    private void createComponents(){
        GridPane gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        createDiffGainComponents(gridPane);
        createSpecularEnhanceComponents(gridPane);

        getChildren().add(gridPane);
    }


    private void createDiffGainComponents(GridPane gridPane){
        gainLabel = new Label("Gain");

        gainSlider = new Slider(0.0, 100.0, INITIAL_DIFF_GAIN_VAL);
        gainSpinner = new Spinner<>(0.0, 100.0, INITIAL_DIFF_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(gainSlider, gainSpinner, "Invalid entry for diffuse gain spinner.",
                RTIViewer.GlobalParam.DIFF_GAIN);

        GridPane.setConstraints(gainLabel, 0, 0);
        GridPane.setConstraints(gainSlider, 1, 0);
        GridPane.setConstraints(gainSpinner, 2, 0);

        gridPane.getChildren().addAll(gainLabel, gainSlider, gainSpinner);
    }


    private void createSpecularEnhanceComponents(GridPane gridPane){
        seColourLabel = new Label("Diffuse colour");
        seColourSlider = new Slider(0.0, 100.0, INITIAL_DIFF_COLOUR_VAL);
        seColourSpinner = new Spinner<>(0.0, 100.0, INITIAL_DIFF_COLOUR_VAL, 1.0);
        setupSliderSpinnerPair(seColourSlider, seColourSpinner, "Invalid entry for diffuse colour spinner.",
                RTIViewer.GlobalParam.DIFF_COLOUR);

        seSpecLabel = new Label("Specularity");
        seSpecSlider = new Slider(0.0, 100.0, INITIAL_SPEC_VAL);
        seSpecSpinner = new Spinner<>(0.0, 100.0, INITIAL_SPEC_VAL, 1.0);
        setupSliderSpinnerPair(seSpecSlider, seSpecSpinner, "Invalid entry for specularity spinner.",
                RTIViewer.GlobalParam.SPECULARITY);

        seHighlightLabel = new Label("Highlight size");
        seHighlightSlider = new Slider(0.0, 100.0, INITIAL_HIGHLIGHT_VAL);
        seHighlightSpinner = new Spinner<>(0.0, 100.0, INITIAL_HIGHLIGHT_VAL, 1.0);
        setupSliderSpinnerPair(seHighlightSlider, seHighlightSpinner, "Invalid entry for highlight size spinner.",
                RTIViewer.GlobalParam.HIGHTLIGHT_SIZE);

        GridPane.setConstraints(seColourLabel, 0, 0);
        GridPane.setConstraints(seColourSlider, 1, 0);
        GridPane.setConstraints(seColourSpinner, 2, 0);

        GridPane.setConstraints(seSpecLabel, 0, 1);
        GridPane.setConstraints(seSpecSlider, 1, 1);
        GridPane.setConstraints(seSpecSpinner, 2, 1);

        GridPane.setConstraints(seHighlightLabel, 0, 2);
        GridPane.setConstraints(seHighlightSlider, 1, 2);
        GridPane.setConstraints(seHighlightSpinner, 2, 2);

        gridPane.getChildren().addAll(seColourLabel,    seColourSlider,     seColourSpinner,
                                      seSpecLabel,      seSpecSlider,       seSpecSpinner,
                                      seHighlightLabel, seHighlightSlider,  seHighlightSpinner);
    }


    private void setupSliderSpinnerPair(Slider slider, Spinner spinner, String warningText,
                                        RTIViewer.GlobalParam globalParam){

        slider.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                spinner.getEditor().setText(String.valueOf(slider.getValue()));
                toolWindow.setGlobalVal(globalParam, slider.getValue());
            }
        });
        spinner.setEditable(true);
        spinner.setPrefWidth(75);
        spinner.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    Double value = Double.parseDouble(spinner.getEditor().getText());
                    slider.setValue(value);
                    toolWindow.setGlobalVal(globalParam, value);
                }catch(NumberFormatException e){
                    toolWindow.entryAlert.setContentText(warningText);
                    toolWindow.entryAlert.showAndWait();
                }
            }
        });
        spinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Double value = Double.parseDouble(spinner.getEditor().getText());
                slider.setValue(value);
                toolWindow.setGlobalVal(globalParam, value);
            }
        });
    }

    public void setCurrentFilter(String filterType){
        hideAllItems();
        if(filterType.equals("Default view")){}
        else if(filterType.equals("Normals visualisation")){}
        else if(filterType.equals("Diffuse gain")){
            gainLabel.setVisible(true);
            gainSlider.setVisible(true);
            gainSpinner.setVisible(true);
        }
        else if(filterType.equals("Specular enhancement")){
            for(int i = 3; i <= 11; i++){
                allControls[i].setVisible(true);
            }
        }

    }

    private void hideAllItems(){
        for(Control control : allControls){
            control.setVisible(false);
        }
    }
}

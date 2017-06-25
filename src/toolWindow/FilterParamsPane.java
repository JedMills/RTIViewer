package toolWindow;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * Created by Jed on 29-May-17.
 */
public class FilterParamsPane extends Pane {

    private RTIViewer toolWindow;
    private ComboBox<String> filterChoiceBox;
    private Scene parent;
    private int width;
    private int height;
    private GridPane gridPane;

    public static final double INITIAL_DIFF_GAIN_VAL = 0.0;
    public static final double INITIAL_DIFF_COLOUR_VAL = 0.0;
    public static final double INITIAL_SPEC_VAL = 0.0;
    public static final double INITIAL_HIGHLIGHT_VAL = 0.0;
    public static final double INITIAL_NORM_UN_MASK_GAIN_VAL = 0.0;
    public static final double INITIAL_NORM_UN_MASK_ENV_VAL = 0.0;
    public static final double INITIAL_IMG_UN_MASK_GAIN_VAL = 0.0;
    public static final double INITIAL_COEFF_UN_MASK_GAIN_VAL = 0.0;

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

    private Label normUnMaskGainLabel;
    private Slider normUnMaskGainSlider;
    private Spinner<Double> normUnMaskGainSpinner;

    private Label normUnMaskEnvLabel;
    private Slider normUnMaskEnvSlider;
    private Spinner<Double> normUnMaskEnvSpinner;

    private Label imgUnMaskGainLabel;
    private Slider imgUnMaskGainSlider;
    private Spinner<Double> imgUnMaskGainSpinner;

    private Label coeffUnMaskGainLabel;
    private Slider coeffUnMaskGainSlider;
    private Spinner<Double> coeffUnMaskGainSpinner;

    private Control[] allControls;

    public FilterParamsPane(RTIViewer toolWindow, Scene parent) {
        super();
        this.parent = parent;
        this.toolWindow = toolWindow;

        setWidth(parent.getWidth());
        setHeight(200);
        createComponents();

        allControls = new Control[]{    gainLabel,              gainSlider,             gainSpinner,
                                        seColourLabel,          seColourSlider,         seColourSpinner,
                                        seSpecLabel,            seSpecSlider,           seSpecSpinner,
                                        seHighlightLabel,       seHighlightSlider,      seHighlightSpinner,
                                        normUnMaskGainLabel,    normUnMaskGainSlider,   normUnMaskGainSpinner,
                                        normUnMaskEnvLabel,     normUnMaskEnvSlider,    normUnMaskEnvSpinner,
                                        imgUnMaskGainLabel,     imgUnMaskGainSlider,    imgUnMaskGainSpinner,
                                        coeffUnMaskGainLabel,   coeffUnMaskGainSlider,  coeffUnMaskGainSpinner};

        hideAllItems();
        setId("filterParamsPane");
    }

    private void createComponents(){
        VBox vBox = new VBox();
        gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(20);
        gridPane.setAlignment(Pos.TOP_CENTER);

        createComboBox(vBox);
        createDiffGainComponents(gridPane);
        createSpecularEnhanceComponents(gridPane);
        createNormUnsharpMaskComponents(gridPane);
        createImgUnsharpMaskComponents(gridPane);
        createCoeffUnsharpMaskComponents(gridPane);

        vBox.getChildren().add(gridPane);

        //getChildren().add(gridPane);
        getChildren().add(vBox);
        vBox.setAlignment(Pos.TOP_CENTER);

        gridPane.setPadding(new Insets(0, 5, 5, 5));
        setPadding(new Insets(0, -1, 0, -1));
    }


    private void createComboBox(VBox vBox){
        ObservableList<String> options = FXCollections.observableArrayList(
                "Default view",
                "Normals visualisation",
                "Diffuse gain",
                "Specular enhancement",
                "Normal unsharp masking",
                "Image unsharp masking",
                "Coefficient unsharp masking"
        );
        ComboBox<String> comboBox = new ComboBox<>(options);
        comboBox.getSelectionModel().select(0);

        comboBox.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setCurrentFilter(comboBox.getSelectionModel().getSelectedItem());
                RTIViewer.updateWindowFilter(comboBox.getSelectionModel().getSelectedItem());
            }
        });


        vBox.setMargin(comboBox, new Insets(5, 0 , 5, 0));
        vBox.getChildren().add(comboBox);
    }


    private void createDiffGainComponents(GridPane gridPane){
        gainLabel = new Label("Gain");

        gainSlider = new Slider(0.0, 100.0, INITIAL_DIFF_GAIN_VAL);
        gainSpinner = new Spinner<>(0.0, 100.0, INITIAL_DIFF_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(gainSlider, gainSpinner, "Invalid entry for diffuse gain spinner.",
                RTIViewer.GlobalParam.DIFF_GAIN);

        GridPane.setConstraints(gainLabel, 0, 1);
        GridPane.setConstraints(gainSlider, 1, 1);
        GridPane.setConstraints(gainSpinner, 2, 1);

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

        GridPane.setConstraints(seColourLabel, 0, 1);
        GridPane.setConstraints(seColourSlider, 1, 1);
        GridPane.setConstraints(seColourSpinner, 2, 1);

        GridPane.setConstraints(seSpecLabel, 0, 2);
        GridPane.setConstraints(seSpecSlider, 1, 2);
        GridPane.setConstraints(seSpecSpinner, 2, 2);

        GridPane.setConstraints(seHighlightLabel, 0, 3);
        GridPane.setConstraints(seHighlightSlider, 1, 3);
        GridPane.setConstraints(seHighlightSpinner, 2, 3);

        gridPane.getChildren().addAll(seColourLabel,    seColourSlider,     seColourSpinner,
                                      seSpecLabel,      seSpecSlider,       seSpecSpinner,
                                      seHighlightLabel, seHighlightSlider,  seHighlightSpinner);
    }


    private void createNormUnsharpMaskComponents(GridPane gridPane){
        normUnMaskGainLabel = new Label("Gain");
        normUnMaskGainSlider = new Slider(0.0, 100.0, INITIAL_NORM_UN_MASK_GAIN_VAL);
        normUnMaskGainSpinner = new Spinner<>(0.0, 100.0, INITIAL_NORM_UN_MASK_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(normUnMaskGainSlider, normUnMaskGainSpinner, "Invalid entry for normals unsharp masking gain slider",
                RTIViewer.GlobalParam.NORM_UN_MASK_GAIN);

        normUnMaskEnvLabel = new Label("Environment");
        normUnMaskEnvSlider = new Slider(0.0, 100.0, INITIAL_NORM_UN_MASK_GAIN_VAL);
        normUnMaskEnvSpinner = new Spinner<>(0.0, 100.0, INITIAL_NORM_UN_MASK_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(normUnMaskEnvSlider, normUnMaskEnvSpinner, "Invalid entry for normals unsharp masking environment slider",
                RTIViewer.GlobalParam.NORM_UN_MASK_ENV);

        GridPane.setConstraints(normUnMaskGainLabel, 0, 1);
        GridPane.setConstraints(normUnMaskGainSlider, 1, 1);
        GridPane.setConstraints(normUnMaskGainSpinner, 2, 1);

        GridPane.setConstraints(normUnMaskEnvLabel, 0, 2);
        GridPane.setConstraints(normUnMaskEnvSlider, 1, 2);
        GridPane.setConstraints(normUnMaskEnvSpinner, 2, 2);

        gridPane.getChildren().addAll(normUnMaskGainLabel,  normUnMaskGainSlider,   normUnMaskGainSpinner,
                                      normUnMaskEnvLabel,   normUnMaskEnvSlider,    normUnMaskEnvSpinner);
    }

    private void createImgUnsharpMaskComponents(GridPane gridPane){
        imgUnMaskGainLabel = new Label("Gain");
        imgUnMaskGainSlider = new Slider(0.0, 100.0, INITIAL_IMG_UN_MASK_GAIN_VAL);
        imgUnMaskGainSpinner = new Spinner<>(0.0, 100.0, INITIAL_IMG_UN_MASK_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(imgUnMaskGainSlider, imgUnMaskGainSpinner, "Invalid entry for image unsharp masking gain slider",
                RTIViewer.GlobalParam.IMG_UN_MASK_GAIN);

        GridPane.setConstraints(imgUnMaskGainLabel, 0, 1);
        GridPane.setConstraints(imgUnMaskGainSlider, 1, 1);
        GridPane.setConstraints(imgUnMaskGainSpinner, 2, 1);

        gridPane.getChildren().addAll(imgUnMaskGainLabel, imgUnMaskGainSlider, imgUnMaskGainSpinner);
    }

    private void createCoeffUnsharpMaskComponents(GridPane gridPane){
        coeffUnMaskGainLabel = new Label("Gain");
        coeffUnMaskGainSlider = new Slider(0.0, 100.0, INITIAL_COEFF_UN_MASK_GAIN_VAL);
        coeffUnMaskGainSpinner = new Spinner<>(0.0, 100.0, INITIAL_COEFF_UN_MASK_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(coeffUnMaskGainSlider, coeffUnMaskGainSpinner, "Invalid entry for coefficient unsharp masking gain slider",
                RTIViewer.GlobalParam.COEFF_UN_MASK_GAIN);

        GridPane.setConstraints(coeffUnMaskGainLabel, 0, 1);
        GridPane.setConstraints(coeffUnMaskGainSlider, 1, 1);
        GridPane.setConstraints(coeffUnMaskGainSpinner, 2, 1);

        gridPane.getChildren().addAll(coeffUnMaskGainLabel, coeffUnMaskGainSlider, coeffUnMaskGainSpinner);
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
        }else if(filterType.equals("Normal unsharp masking")){
            for(int i = 12; i <= 17; i++){
                allControls[i].setVisible(true);
            }
        }else if(filterType.equals("Image unsharp masking")){
            for(int i = 18; i <= 20; i++){
                allControls[i].setVisible(true);
            }
        }else if(filterType.equals("Coefficient unsharp masking")){
            for(int i = 21; i <= 23; i++){
                allControls[i].setVisible(true);
            }
        }

    }

    private void hideAllItems(){
        for(Control control : allControls){
            control.setVisible(false);
        }
    }

    public void updateSize(double width, double height){
        gridPane.setPrefWidth(width - 20);
        gridPane.setVgap(height / 40);
    }
}

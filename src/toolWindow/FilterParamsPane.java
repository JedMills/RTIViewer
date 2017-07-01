package toolWindow;

import javafx.beans.property.SimpleDoubleProperty;
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
    private GridPane gridPane;

    public static final double INITIAL_DIFF_GAIN_VAL = 0.0;
    public static final double INITIAL_DIFF_COLOUR_VAL = 0.0;
    public static final double INITIAL_SPEC_VAL = 0.0;
    public static final double INITIAL_HIGHLIGHT_VAL = 0.0;
    public static final double INITIAL_NORM_UN_MASK_GAIN_VAL = 0.0;
    public static final double INITIAL_NORM_UN_MASK_ENV_VAL = 0.0;
    public static final double INITIAL_IMG_UN_MASK_GAIN_VAL = 0.0;
    public static final double INITIAL_COEFF_UN_MASK_GAIN_VAL = 0.0;

    private ComboBox<String> filterChoice;

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


    private Label imgUnMaskGainLabel;
    private Slider imgUnMaskGainSlider;
    private Spinner<Double> imgUnMaskGainSpinner;



    private Control[] allControls;

    public FilterParamsPane(RTIViewer toolWindow, Scene parent) {
        super();
        this.toolWindow = toolWindow;

        setWidth(parent.getWidth());
        setHeight(200);
        createComponents();

        allControls = new Control[]{    gainLabel,              gainSlider,             gainSpinner,
                                        seColourLabel,          seColourSlider,         seColourSpinner,
                                        seSpecLabel,            seSpecSlider,           seSpecSpinner,
                                        seHighlightLabel,       seHighlightSlider,      seHighlightSpinner,
                                        imgUnMaskGainLabel,     imgUnMaskGainSlider,    imgUnMaskGainSpinner};

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
        createImgUnsharpMaskComponents(gridPane);

        vBox.getChildren().add(gridPane);

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
                "Image unsharp masking"
        );
        filterChoice = new ComboBox<>(options);
        filterChoice.getSelectionModel().select(0);

        filterChoice.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                setCurrentFilter(filterChoice.getSelectionModel().getSelectedItem());
                RTIViewer.updateWindowFilter(filterChoice.getSelectionModel().getSelectedItem());
            }
        });


        vBox.setMargin(filterChoice, new Insets(5, 0 , 5, 0));
        vBox.getChildren().add(filterChoice);
    }


    private void createDiffGainComponents(GridPane gridPane){
        gainLabel = new Label("Gain");

        gainSlider = new Slider(0.0, 100.0, INITIAL_DIFF_GAIN_VAL);
        gainSpinner = new Spinner<>(0.0, 100.0, INITIAL_DIFF_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(gainSlider, gainSpinner, "Invalid entry for diffuse gain spinner.",
                RTIViewer.globalDiffGainVal);

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
                RTIViewer.globalDiffColourVal);

        seSpecLabel = new Label("Specularity");
        seSpecSlider = new Slider(0.0, 100.0, INITIAL_SPEC_VAL);
        seSpecSpinner = new Spinner<>(0.0, 100.0, INITIAL_SPEC_VAL, 1.0);
        setupSliderSpinnerPair(seSpecSlider, seSpecSpinner, "Invalid entry for specularity spinner.",
                RTIViewer.globalSpecularityVal);

        seHighlightLabel = new Label("Highlight size");
        seHighlightSlider = new Slider(0.0, 100.0, INITIAL_HIGHLIGHT_VAL);
        seHighlightSpinner = new Spinner<>(0.0, 100.0, INITIAL_HIGHLIGHT_VAL, 1.0);
        setupSliderSpinnerPair(seHighlightSlider, seHighlightSpinner, "Invalid entry for highlight size spinner.",
                RTIViewer.globalHighlightSizeVal);

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


    private void createImgUnsharpMaskComponents(GridPane gridPane){
        imgUnMaskGainLabel = new Label("Gain");
        imgUnMaskGainSlider = new Slider(0.0, 100.0, INITIAL_IMG_UN_MASK_GAIN_VAL);
        imgUnMaskGainSpinner = new Spinner<>(0.0, 100.0, INITIAL_IMG_UN_MASK_GAIN_VAL, 1.0);
        setupSliderSpinnerPair(imgUnMaskGainSlider, imgUnMaskGainSpinner, "Invalid entry for image unsharp masking gain slider",
                RTIViewer.globalImgUnMaskGain);

        GridPane.setConstraints(imgUnMaskGainLabel, 0, 1);
        GridPane.setConstraints(imgUnMaskGainSlider, 1, 1);
        GridPane.setConstraints(imgUnMaskGainSpinner, 2, 1);

        gridPane.getChildren().addAll(imgUnMaskGainLabel, imgUnMaskGainSlider, imgUnMaskGainSpinner);
    }



    private void setupSliderSpinnerPair(Slider slider, Spinner spinner, String warningText,
                                        SimpleDoubleProperty globalParam){

        EventHandler<MouseEvent> sliderEdited = new EventHandler<MouseEvent>(){
            @Override
            public void handle(MouseEvent event) {
                spinner.getEditor().setText(String.valueOf(slider.getValue()));
                globalParam.set(slider.getValue());
            }
        };

        slider.setOnMouseDragged(sliderEdited);
        slider.setOnMouseClicked(sliderEdited);
        spinner.setEditable(true);
        spinner.setPrefWidth(75);

        globalParam.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                slider.setValue(globalParam.get());
                spinner.getEditor().setText(String.valueOf(globalParam.get()));
            }
        });

        spinner.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    Double value = Double.parseDouble(spinner.getEditor().getText());
                    slider.setValue(value);
                    globalParam.set(slider.getValue());
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
                globalParam.set(slider.getValue());
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
        }else if(filterType.equals("Image unsharp masking")){
            imgUnMaskGainLabel.setVisible(true);
            imgUnMaskGainSlider.setVisible(true);
            imgUnMaskGainSpinner.setVisible(true);
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


    public void updateFromBookmark(){
        String filterToSet = "";
        int index = 0;
        if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.DEFAULT)){
            filterToSet = "Default view";
            index = 0;
        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.NORMALS)){
            filterToSet = "Normals visualisation";
            index = 1;
        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.DIFF_GAIN)){
            filterToSet = "Diffuse gain";
            index = 2;
        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.SPEC_ENHANCE)){
            filterToSet = "Specular enhancement";
            index = 3;
        }else if(RTIViewer.currentProgram.equals(RTIViewer.ShaderProgram.IMG_UNSHARP_MASK)){
            filterToSet = "Image unsharp masking";
            index = 5;
        }

        filterChoice.getSelectionModel().select(index);
        setCurrentFilter(filterToSet);
    }
}

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
 * The pane that contains the rendering mode selection box, and the sliders and spinners that the user can use to change
 * the rendering parameters of the current rendering mode.
 */
public class FilterParamsPane extends Pane {

    /** The grid pane that contains the sliders, spinners and their labels for the rendering parameters */
    private GridPane gridPane;

    /** The initial value that the diffuse gain gain value is set to */
    public static final double INITIAL_DIFF_GAIN_VAL = 0.0;

    /** The initial value that the spec enhance diffuse colour value is set to */
    public static final double INITIAL_DIFF_COLOUR_VAL = 0.0;

    /** The initial value that the spec enhance specularity value is set to */
    public static final double INITIAL_SPEC_VAL = 0.0;

    /** The initial value that the spec enhance highlight size value is set to */
    public static final double INITIAL_HIGHLIGHT_VAL = 0.0;

    /** The initial value that the normals enhancement environment value is set to */
    public static final double INITIAL_NORM_UN_MASK_ENV_VAL = 0.0;

    /** The initial value that the normals enhancement gain value is set to */
    public static final double INITIAL_NORM_UN_MASK_GAIN_VAL = 0.0;

    /** The initial value that the image unsharp masking value is set to */
    public static final double INITIAL_IMG_UN_MASK_GAIN_VAL = 0.0;

    /** The dropdown menu for the different rendering modes */
    private ComboBox<String> filterChoice;

    /** Label for the diffuse gain rendering gain slider */
    private Label gainLabel;

    /** Slider for the diffuse gain rendering gain */
    private Slider gainSlider;

    /** Spinner for the diffuse gain gain value */
    private Spinner<Double> gainSpinner;

    /** Specular enhancement diffuse colour label*/
    private Label seColourLabel;

    /** Specular enhancement diffuse colour s;ider*/
    private Slider seColourSlider;

    /** Specular enhancement diffuse colour spinner*/
    private Spinner<Double> seColourSpinner;

    /** Specular enhancement specularity label*/
    private Label seSpecLabel;

    /** Specular enhancement specularity slider*/
    private Slider seSpecSlider;

    /** Specular enhancement specularity spinner*/
    private Spinner<Double> seSpecSpinner;

    /** Specular enhancement highlight label*/
    private Label seHighlightLabel;

    /** Specular enhancement highlight slider*/
    private Slider seHighlightSlider;

    /** Specular enhancement highlight spinner*/
    private Spinner<Double> seHighlightSpinner;

   /** Image unsharp masking gain label */
    private Label imgUnMaskGainLabel;

    /** Image unsharp masking gain slider */
    private Slider imgUnMaskGainSlider;

    /** Image unsharp masking gain spinner */
    private Spinner<Double> imgUnMaskGainSpinner;

    /** Contains all the controls so they can be easily hidden at once */
    private Control[] allControls;


    /**
     * Creates a new FilterParamsPane. Initialises all the controls.
     *
     * @param toolWindow
     * @param parent
     */
    public FilterParamsPane(RTIViewer toolWindow, Scene parent) {
        super();

        setWidth(parent.getWidth());
        setHeight(200);

        //create all the widgets
        createComponents();

        //used so the components can all be hidden when switching rendering mode
        allControls = new Control[]{    gainLabel,              gainSlider,             gainSpinner,
                                        seColourLabel,          seColourSlider,         seColourSpinner,
                                        seSpecLabel,            seSpecSlider,           seSpecSpinner,
                                        seHighlightLabel,       seHighlightSlider,      seHighlightSpinner,
                                        imgUnMaskGainLabel,     imgUnMaskGainSlider,    imgUnMaskGainSpinner};

        hideAllItems();
        setId("filterParamsPane");
        setMinHeight(162);
    }


    /**
     * Creates all the widgets in the pane, in a gridpane. Multiple widgets occupy the same cells in
     * the grid pane so the relevant ones for the current rendering mode need to be shown when switching
     * between rendering modes.
     */
    private void createComponents(){
        //vbx containing the rendering mode dropdown and the rendering params sliiders/spinners grid
        VBox vBox = new VBox();
        gridPane = new GridPane();
        gridPane.setHgap(20);
        gridPane.setVgap(15);
        gridPane.setAlignment(Pos.TOP_CENTER);

        //create the components for each rendering mode
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


    /**
     * Creates the combo ox for all the rendering modes.
     *
     * @param vBox the vBox containing the combo box
     */
    private void createComboBox(VBox vBox){
        //crete the combo box and the elements in it
        ObservableList<String> options = FXCollections.observableArrayList(
                "Default view",
                "Normals visualisation",
                "Diffuse gain (PTM) | Normals enhancement (HSH)",
                "Specular enhancement",
                "Image unsharp masking"
        );
        filterChoice = new ComboBox<>(options);
        //select the default view
        filterChoice.getSelectionModel().select(0);

        //when the selected rendering mode is changed, update everything
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


    /**
     * Adds the components for the diffuse gain rendering mode to the {@link FilterParamsPane#gridPane}.
     *
     * @param gridPane  gridpane to add the components to
     */
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


    /**
     * Adds the components for the specular enhancement rendering mode to the {@link FilterParamsPane#gridPane}.
     *
     * @param gridPane  gridpane to add the components to
     */
    private void createSpecularEnhanceComponents(GridPane gridPane){
        //components for the diffuse colour parameter
        seColourLabel = new Label("Diffuse colour");
        seColourSlider = new Slider(0.0, 100.0, INITIAL_DIFF_COLOUR_VAL);
        seColourSpinner = new Spinner<>(0.0, 100.0, INITIAL_DIFF_COLOUR_VAL, 1.0);
        setupSliderSpinnerPair(seColourSlider, seColourSpinner, "Invalid entry for diffuse colour spinner.",
                RTIViewer.globalDiffColourVal);

        //components for the specularity parameter
        seSpecLabel = new Label("Specularity");
        seSpecSlider = new Slider(0.0, 100.0, INITIAL_SPEC_VAL);
        seSpecSpinner = new Spinner<>(0.0, 100.0, INITIAL_SPEC_VAL, 1.0);
        setupSliderSpinnerPair(seSpecSlider, seSpecSpinner, "Invalid entry for specularity spinner.",
                RTIViewer.globalSpecularityVal);

        //components for the highlight size parameter
        seHighlightLabel = new Label("Highlight size");
        seHighlightSlider = new Slider(0.0, 100.0, INITIAL_HIGHLIGHT_VAL);
        seHighlightSpinner = new Spinner<>(0.0, 100.0, INITIAL_HIGHLIGHT_VAL, 1.0);
        setupSliderSpinnerPair(seHighlightSlider, seHighlightSpinner, "Invalid entry for highlight size spinner.",
                RTIViewer.globalHighlightSizeVal);

        //set out everything in the grid
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


    /**
     * Adds the components for the image unsharp masking rendering mode to the {@link FilterParamsPane#gridPane}.
     *
     * @param gridPane  gridpane to add the components to
     */
    private void createImgUnsharpMaskComponents(GridPane gridPane){
        //components for the gain rendering parameter
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


    /**
     * Sets uo a slider and a spinner so that changing one changes the other, and vice versa, and that both
     * always diaply the same value, and this value is updated to the global param passed.  Also sets the input
     * alert dialog to show if a bad value is entered into the spinner.
     *
     * @param slider            slider to link to spinner
     * @param spinner           spinner to link to slider
     * @param warningText       text to display
     * @param globalParam       global rendering parameter to link to this slider spinner pair
     */
    private void setupSliderSpinnerPair(Slider slider, Spinner spinner, String warningText,
                                        SimpleDoubleProperty globalParam){

        //when the slider is changed, update the global param
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

        //whe nthe global parameter is changed, update both the slider and spinner
        globalParam.addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                slider.setValue(globalParam.get());
                spinner.getEditor().setText(String.valueOf(globalParam.get()));
            }
        });

        //when the spinner ischanged, check it can be parsed to a float, and update the global value
        spinner.getEditor().setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try{
                    Double value = Double.parseDouble(spinner.getEditor().getText());
                    slider.setValue(value);
                    globalParam.set(slider.getValue());
                }catch(NumberFormatException e){
                    //if it can't show the entry alert
                    RTIViewer.entryAlert.setContentText(warningText);
                    RTIViewer.entryAlert.showAndWait();
                }
            }
        });

        //when the spinner is clicked, update everything
        spinner.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                Double value = Double.parseDouble(spinner.getEditor().getText());
                slider.setValue(value);
                globalParam.set(slider.getValue());
            }
        });
    }


    /**
     * Shows the relevant spinners and sliders for the selected rendering mode.
     *
     * @param filterType    the name of the selected rendering mode
     */
    public void setCurrentFilter(String filterType){
        //pretty self explanatory
        hideAllItems();
        if(filterType.equals("Default view")){}
        else if(filterType.equals("Normals visualisation")){}
        else if(filterType.equals("Diffuse gain (PTM) | Normals enhancement (HSH)")){
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


    /**
     * Hides all the spinners, sliders and labels for the rendering parameters.
     */
    private void hideAllItems(){
        for(Control control : allControls){
            control.setVisible(false);
        }
    }

    /**
     * Updates the width of the components in the pane/
     *
     * @param width     width of the primary stage
     * @param height    height of the primary stage
     */
    public void updateSize(double width, double height){
        filterChoice.setPrefWidth(width * 0.75);
        gridPane.setPrefWidth(width - 20);
    }


    /**
     * Updates the settings on the sliders and spinners from a selected bookmark in the bottom tab pane.
     */
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
            filterToSet = "Diffuse gain (PTM) | Normals enhancement (HSH)";
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

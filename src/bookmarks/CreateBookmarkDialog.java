package bookmarks;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import toolWindow.RTIViewer;
import utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a Dialog that is opened when the user clicks the 'Add' button in the
 * {@link toolWindow.BottomTabPane} to add a {@link Bookmark} to the currently selected {@link ptmCreation.RTIObject}.
 * The Dialog contains a text field to input the new {@link Bookmark} name, and 'Add' and 'Cancel' buttons.
 *
 * @see Bookmark
 * @see toolWindow.BottomTabPane
 *
 * @author Jed Mills
 */
public class CreateBookmarkDialog{

    /** The stage that the dialog exists in*/
    private Stage stage;

    /** Scene containing the text input box and buttons */
    private Scene scene;

    /** Label for the text box to input the bookmark name into */
    private Label createNameLabel;

    /** Where the user types the new bookmark name */
    private TextField bookmarkNameField;

    /** Button to add the new bookmark */
    private Button addButton;

    /** Button to cnacel adding  anew bookmark */
    private Button cancelButton;

    /** A list of all the names of bookmarks for the current rti object, used to check names aren't repeated */
    private List<String> currentBookmarkNames;


    /**
     * Creates s new BookmarkDialog. This creation involves making a new Stage for the dialog, creating the
     * layout, which contains a TextField to input the new {@link Bookmark} name, and 'Add' and 'Cancel' buttons.
     */
    public CreateBookmarkDialog(){
        //the stage (window) for the dialog
        stage = new Stage(StageStyle.UNIFIED);
        stage.setTitle("Create a bookmark");
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(RTIViewer.primaryStage);

        //will be updated with RTIObjects' bookmark names to ensure no duplicate bookmarks
        currentBookmarkNames = new ArrayList<>();

        //create the layout with buttons
        GridPane gridPane = createLayout();
        setButtonActions();

        //add the layout to the window and set it's min and max size
        scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setMaxHeight(200);
        stage.setMaxWidth(400);
        stage.setMinHeight(150);
        stage.setMinWidth(350);

        //bind resizing the GUI to the width hand height of the stage
        ChangeListener changeListener = new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        };
        stage.widthProperty().addListener(changeListener);
        stage.heightProperty().addListener(changeListener);
    }




    /**
     * Creates the component layout for the GUI of this dialog. The layout contains a label saying "Bookmark Name:",
     * a TextField for the user to input a new {@link Bookmark} name, an "Add" button to add the new bookmark, and
     * a "Cancel" button toclose the window without adding the bookmark.
     *
     * @return the generated layout
     */
    private GridPane createLayout(){
        //all pretty self-explanatory, just creating components and setting where they are in the grid
        GridPane gridPane = new GridPane();

            createNameLabel = new Label("Bookmark Name:");
            GridPane.setConstraints(createNameLabel, 0, 0, 1, 1);

            bookmarkNameField = new TextField("");
            bookmarkNameField.setEditable(true);
            GridPane.setConstraints(bookmarkNameField, 0, 1, 3, 1);

            addButton = new Button("Add");
            GridPane.setConstraints(addButton, 1, 2, 1, 1);

            cancelButton = new Button("Cancel");
            GridPane.setConstraints(cancelButton, 2, 2, 1, 1);

        //make the grid look nice
        gridPane.getChildren().addAll(createNameLabel, bookmarkNameField, addButton, cancelButton);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        return gridPane;
    }




    /**
     * Sets the actions for the 'Add' and 'Cancel' buttons. 'Add' checks that the input name is not in the
     * {@link CreateBookmarkDialog#currentBookmarkNames} list. If the name already exists, the button shows a
     * dialog to the user asking for another name. If it doesn't it gets the RTIWindow to create a new one. 'Cancel'
     * closes the dialog without doing anything.
     */
    private void setButtonActions(){
        //set the action for the 'Add' button
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(bookmarkNameField.getText().replaceAll("\\s+","").equals("")){
                    //if the name only contains spaces, it's invalid, so show a dialog
                    RTIViewer.entryAlert.setContentText("Please enter a valid bookmark name.");
                    bookmarkNameField.setText("");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            RTIViewer.entryAlert.showAndWait();
                        }
                    });

                }else if(Utils.checkIn(bookmarkNameField.getText(), currentBookmarkNames)){
                    //if a bookmark with the same name already exists, show a dialog asking for a new name
                    RTIViewer.entryAlert.setContentText("Please enter a unique bookmark name.");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            RTIViewer.entryAlert.showAndWait();
                        }
                    });

                }else{
                    //otherwise, the name is ok and we can create a new bookmark
                    RTIViewer.createBookmark(bookmarkNameField.getText());
                    stage.close();
                    bookmarkNameField.setText("");
                }
            }
        });

        //set the action for the 'Cancel' button
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //just close the dialog and reset the text field
                stage.close();
                bookmarkNameField.setText("");
            }
        });
    }




    /**
     * Makes the size of the text for the 'Bookmark Name:', and the buttons bigger if the dialog's above a certain
     * size.
     */
    private void resizeGUI(){
        int fontSize = 12;
        //if the dialog's big enough, make the font bigger
        if(stage.getHeight() >= 175 && stage.getWidth() >= 375){
            fontSize = 16;
        }
        //set the font size for all the components
        createNameLabel.setFont(Font.font(fontSize));
        bookmarkNameField.setFont(Font.font(fontSize));
        addButton.setFont(Font.font(fontSize));
        cancelButton.setFont(Font.font(fontSize));
    }




    /**
     * Shows the dialog.
     */
    public void show(){
        stage.showAndWait();
    }




    /**
     * Sets the {@link CreateBookmarkDialog#currentBookmarkNames} attribute. This should be called when adding
     * a new {@link Bookmark} so the dialog cancheck there ar eno duplicate {@link Bookmark}names.
     *
     * @param currentBookmarkNames  names of the bookmarks for the current RTIObject
     */
    public void setCurrentBookmarkNames(List<String> currentBookmarkNames){
        this.currentBookmarkNames = currentBookmarkNames;
    }

}
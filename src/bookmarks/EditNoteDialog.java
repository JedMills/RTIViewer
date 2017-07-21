package bookmarks;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import toolWindow.RTIViewer;

/**
 * This class represents the dialog that appears when the user wants to edit a {@link Bookmark.Note}. The dialog has
 * TextFields for editing the {@link Bookmark.Note#subject}, {@link Bookmark.Note#author}, and
 * {@link Bookmark.Note#comment} attributes of the target {@link Bookmark.Note}.
 *
 * @see Bookmark
 * @see Bookmark.Note
 * @see toolWindow.BottomTabPane
 */
public class EditNoteDialog {

    /** The stage (window) that contains this dialog */
    private Stage stage;

    /** The scene that the stage shows*/
    private Scene scene;

    /** 'Ok' button to set the target note's attributes as the ones written in the text fields of this dialog */
    private Button okButton;

    /** 'Cancel' button to close this dialog without doing anything */
    private Button cancelButton;

    /** For the user to write the target Note's subject in */
    private TextField subjectInput;

    /**For the user to write the target Note's author in */
    private TextField authorInput;

    /** For the user to type the target Note's comment in */
    private TextArea commentInput;

    /** The {@link Bookmark.Note} who's attributes will be changed by this dialog */
    private Bookmark.Note targetNote;

    /** Name fo the bookmark that the target {@link Bookmark.Note} belongs to */
    private String selectedBookmarkName;




    /**
     * Creates a new EditNoteDialog. Creates a new Stage for this dialog to exit in, and creates the layout
     * of text fields for the user to type into, and adds this layout to the stage.
     */
    public EditNoteDialog() {
        //create a new window for the dialog to exist in
        stage = new Stage(StageStyle.UNIFIED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(RTIViewer.primaryStage);

        //set it's min and max size
        stage.setMinWidth(250);
        stage.setMinHeight(200);
        stage.setMaxWidth(450);
        stage.setMaxHeight(300);

        //create the layout for the dialog to show
        VBox layout = createLayout();
        setupButtons();
        scene = new Scene(layout);
        stage.setScene(scene);
    }




    /**
     * Creates the layout for the dialog and returns it. The layout consists of three text input fields for the
     * {@link EditNoteDialog#targetNote}'s subject, author and comment attributes, arranged in a grid, and 'OK'
     * and 'Cancel' buttons.
     *
     * @return  the layout of components for the EditNoteDialog
     */
    private VBox createLayout(){
        VBox vBox = new VBox();

            //GridPane to contain the text fields and their labels
            GridPane gridPane = new GridPane();

                //create the text fields and their labels, and position them...
                Label subjectLabel = new Label("Subject:");
                GridPane.setConstraints(subjectLabel, 0, 0);

                subjectInput = new TextField();
                GridPane.setConstraints(subjectInput, 1, 0, 2, 1);

                Label authorLabel = new Label("Author:");
                GridPane.setConstraints(authorLabel, 0, 1);

                authorInput = new TextField();
                GridPane.setConstraints(authorInput, 1, 1, 2, 1);

                Label commentLabel = new Label("Comment:");
                GridPane.setConstraints(commentLabel, 0, 2);

                commentInput = new TextArea();
                GridPane.setConstraints(commentInput, 1, 2, 2, 2);

                okButton = new Button("OK");
                GridPane.setConstraints(okButton, 1, 4);

                cancelButton = new Button("Cancel");
                GridPane.setConstraints(cancelButton, 2, 4);

            gridPane.getChildren().addAll(  subjectLabel, subjectInput,
                                            authorLabel, authorInput,
                                            commentLabel, commentInput);
            gridPane.setVgap(5);
            gridPane.setHgap(5);

            //create a HBox to contain the OK and Cancel buttons at the bottom of the layout
            HBox hBox = new HBox();
            hBox.getChildren().addAll(okButton, cancelButton);
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setSpacing(5);

        //format everything nicely and add everything to the VBox
        vBox.getChildren().addAll(gridPane, hBox);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setFillWidth(true);

        return vBox;
    }




    /**
     * Sets the functions to be called when the {@link EditNoteDialog#okButton} and {@link EditNoteDialog#cancelButton}
     * buttons are pressed. The OK button sets the {@link EditNoteDialog#targetNote}'s attributes to the ones in the
     * input boxes, and the cancel button closes the dialog without doing anything.
     */
    private void setupButtons(){
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                //set the target note's attributes to those in the text fields
                targetNote.setAuthor(authorInput.getText());
                targetNote.setSubject(subjectInput.getText());
                targetNote.setComment(commentInput.getText());

                //get the RTIViewer to save the bookamarks with the new note info to the disk
                RTIViewer.updateBookmarks(RTIViewer.selectedWindow.rtiObject.getFilePath(),
                                            RTIViewer.selectedWindow.rtiObject.getBookmarks());
                RTIViewer.setSelectedBookmark(selectedBookmarkName);

                //reset the dialog pane
                stage.close();
                subjectInput.setText("");
                authorInput.setText("");
                commentInput.setText("");
            }
        });


        //the cancel button closes the window and resets the text fields without doing anything else
        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
                subjectInput.setText("");
                authorInput.setText("");
                commentInput.setText("");

            }
        });
    }




    /**
     * @param title Set the dialog stage's title.
     */
    public void setTitle(String title){
        stage.setTitle(title);
    }




    /**
     * Show the dialog.
     */
    public void show(){
        stage.showAndWait();
    }




    /**
     * @param targetNote set the {@link EditNoteDialog#targetNote} attribute
     */
    public void setTargetNote(Bookmark.Note targetNote) {
        this.targetNote = targetNote;
    }




    /**
     * @param selectedBookmarkName set the {@link EditNoteDialog#selectedBookmarkName} attribute
     */
    public void setTargetBookmark(String selectedBookmarkName) {
        this.selectedBookmarkName = selectedBookmarkName;
    }
}

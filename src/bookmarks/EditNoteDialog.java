package bookmarks;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
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
 * Created by Jed on 28-Jun-17.
 */
public class EditNoteDialog {

    private Stage stage;
    private Scene scene;

    private Button okButton;
    private Button cancelButton;

    private TextField subjectInput;
    private TextField authorInput;
    private TextArea commentInput;

    private Bookmark.Note targetNote;
    private String selectedBookmarkName;

    public EditNoteDialog() {
        stage = new Stage(StageStyle.UNIFIED);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(RTIViewer.primaryStage);

        stage.setMinWidth(250);
        stage.setMinHeight(200);
        stage.setMaxWidth(450);
        stage.setMaxHeight(400);

        VBox layout = createLayout();
        setupButtons();
        scene = new Scene(layout);
        stage.setScene(scene);
    }



    private VBox createLayout(){
        VBox vBox = new VBox();
        GridPane gridPane = new GridPane();

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

        HBox hBox = new HBox();
        hBox.getChildren().addAll(okButton, cancelButton);
        hBox.setAlignment(Pos.CENTER_RIGHT);
        hBox.setSpacing(5);

        vBox.getChildren().addAll(gridPane, hBox);
        vBox.setSpacing(5);
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setAlignment(Pos.TOP_CENTER);
        vBox.setFillWidth(true);
        return vBox;
    }



    private void setupButtons(){
        okButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                targetNote.setAuthor(authorInput.getText());
                targetNote.setSubject(subjectInput.getText());
                targetNote.setComment(commentInput.getText());

                RTIViewer.updateBookmarks(RTIViewer.selectedWindow.rtiObject.getFilePath(),
                                            RTIViewer.selectedWindow.rtiObject.getBookmarks());

                RTIViewer.setSelectedBookmark(selectedBookmarkName);

                stage.close();
                subjectInput.setText("");
                authorInput.setText("");
                commentInput.setText("");
            }
        });



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


    public void setTitle(String title){
        stage.setTitle(title);
    }


    public void show(){
        stage.showAndWait();
    }

    public void setTargetNote(Bookmark.Note targetNote) {
        this.targetNote = targetNote;
    }

    public void setTargetBookmark(String selectedBookmarkName) {
        this.selectedBookmarkName = selectedBookmarkName;
    }
}

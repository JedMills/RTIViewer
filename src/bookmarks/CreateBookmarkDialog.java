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

/**
 * Created by Jed on 25-Jun-17.
 */
public class CreateBookmarkDialog{

    private Stage stage;
    private Scene scene;
    private Label createNameLabel;
    private TextField bookmarkNameField;
    private Button addButton;
    private Button cancelButton;

    public CreateBookmarkDialog(){
        stage = new Stage(StageStyle.UNIFIED);
        stage.setTitle("Create a bookmark");
        stage.setAlwaysOnTop(true);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(RTIViewer.primaryStage);

        GridPane gridPane = createLayout();
        setButtonActions();

        scene = new Scene(gridPane);
        stage.setScene(scene);
        stage.setMaxHeight(200);
        stage.setMaxWidth(400);
        stage.setMinHeight(150);
        stage.setMinWidth(350);

        stage.widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });

        stage.heightProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                resizeGUI();
            }
        });
    }

    private GridPane createLayout(){
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

        gridPane.getChildren().addAll(createNameLabel, bookmarkNameField, addButton, cancelButton);
        gridPane.setAlignment(Pos.CENTER);
        gridPane.setHgap(10);
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(10, 10, 10, 10));

        return gridPane;
    }

    private void setButtonActions(){
        addButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                if(bookmarkNameField.getText().replaceAll("\\s+","").equals("")){
                    stage.close();
                    RTIViewer.entryAlert.setContentText("Please enter a valid bookmark name.");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            RTIViewer.entryAlert.showAndWait();
                        }
                    });
                }else{
                    BookmarkCreator.createNewBookmark(bookmarkNameField.getText());
                    RTIViewer.createBookmark(bookmarkNameField.getText());
                    stage.close();
                }


                bookmarkNameField.setText("");
            }
        });

        cancelButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                stage.close();
                bookmarkNameField.setText("");
            }
        });
    }


    private void resizeGUI(){
        int fontSize = 12;
        if(stage.getHeight() >= 175 && stage.getWidth() >= 375){
            fontSize = 16;
        }

        createNameLabel.setFont(Font.font(fontSize));
        bookmarkNameField.setFont(Font.font(fontSize));
        addButton.setFont(Font.font(fontSize));
        cancelButton.setFont(Font.font(fontSize));
    }


    public void show(){
        stage.showAndWait();
    }

}
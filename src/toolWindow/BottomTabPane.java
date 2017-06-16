package toolWindow;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;


/**
 * Created by Jed on 14-Jun-17.
 */
public class BottomTabPane extends TabPane {

    private RTIViewer rtiViewer;
    private Scene parent;
    private FlowPane previewFlowPane;
    private GridPane previewGridPane;
    private TextField fileName;
    private TextField imageWidthBox;
    private TextField imageHeightBox;
    private TextField imageFormat;
    private ImageView imagePreview;
    private BorderPane imageBorderPane;
    private Image defaultImage;

    private VBox vBox;

    public BottomTabPane(RTIViewer rtiViewer, Scene parent){
        super();
        this.rtiViewer = rtiViewer;
        this.parent = parent;

        defaultImage = new Image("file:rsc/images/exeterUniLogo.jpg");

        createComponenets();
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
    }

    private void createComponenets(){
        Tab previewTab = createPreviewTab();

        Tab bookmarksTab = new Tab("Bookmarks");
        bookmarksTab.setClosable(false);

        Tab saveImageTab = new Tab("Save image");
        saveImageTab.setClosable(false);

        getTabs().addAll(previewTab, bookmarksTab, saveImageTab);
    }


    private Tab createPreviewTab(){
        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);

        vBox = new VBox();

        previewGridPane = new GridPane();

        Label fileNameLabel = new Label("File:");
        fileName = new TextField("");
        fileName.setEditable(false);

        Label imageWidthLabel = new Label("Width:");
        imageWidthBox = new TextField("");
        imageWidthBox.setEditable(false);

        Label imageHeightLabel = new Label("Height:");
        imageHeightBox = new TextField("");
        imageHeightBox.setEditable(false);

        Label imageFormatLabel = new Label("Format:");
        imageFormat = new TextField("");
        imageFormat.setEditable(false);

        imageBorderPane = new BorderPane();

        imagePreview = new ImageView();
        imagePreview.setPreserveRatio(true);
        imageBorderPane.setCenter(imagePreview);

        imagePreview.fitWidthProperty().bind(imageBorderPane.widthProperty());
        imagePreview.fitHeightProperty().bind(imageBorderPane.heightProperty());

        imagePreview.setImage(defaultImage);

        GridPane.setConstraints(fileNameLabel, 0, 0, 1, 1);
        GridPane.setConstraints(fileName, 1, 0, 5, 1);

        GridPane.setConstraints(imageWidthLabel, 0, 1, 1, 1);
        GridPane.setConstraints(imageWidthBox, 1, 1, 1, 1);

        GridPane.setConstraints(imageHeightLabel, 2, 1, 1, 1);
        GridPane.setConstraints(imageHeightBox, 3, 1, 1, 1);

        GridPane.setConstraints(imageFormatLabel, 4, 1, 1, 1);
        GridPane.setConstraints(imageFormat, 5, 1, 1, 1);

        previewGridPane.getChildren().addAll(fileNameLabel, fileName, imageWidthLabel, imageWidthBox,
                imageHeightLabel, imageHeightBox, imageFormatLabel, imageFormat);

        previewGridPane.setAlignment(Pos.TOP_CENTER);
        previewGridPane.setVgap(5);
        previewGridPane.setHgap(5);

        vBox.getChildren().addAll(previewGridPane, imageBorderPane);
        vBox.setMargin(previewGridPane, new Insets(5, 0, 5, 0));
        previewTab.setContent(vBox);

        return previewTab;
    }


    public void setFileText(String text){
        fileName.setText(text);
    }

    public void setWidthText(String text){
        imageWidthBox.setText(text);
    }

    public void setHeightText(String text){
        imageHeightBox.setText(text);
    }

    public void setFormatText(String text){
        imageFormat.setText(text);
    }

    public void setPreviewImage(Image image){
        imagePreview.setImage(image);
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #000000;");
    }

    public void setDefaultImage(){
        imagePreview.setImage(defaultImage);
        updateSize(rtiViewer.primaryStage.getWidth(), rtiViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #ffffff;");
    }

    public void updateSize(double width, double height){
        setPrefWidth(width - 20);

        setPrefHeight(height - (getLayoutY() + 45));

        vBox.setPrefWidth(width - 20);
        previewGridPane.setPrefWidth(width - 20);

        imageWidthBox.setPrefWidth(width / 6);
        imageHeightBox.setPrefWidth(width / 6);
        imageFormat.setPrefWidth(width / 6);

        imageBorderPane.setMinWidth(0);
        imageBorderPane.setMinHeight(0);

        imageBorderPane.setPrefHeight(getPrefHeight() - 45);
    }
}

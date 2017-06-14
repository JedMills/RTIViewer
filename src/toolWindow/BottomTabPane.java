package toolWindow;

import javafx.geometry.Insets;
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
import javafx.scene.layout.Pane;


/**
 * Created by Jed on 14-Jun-17.
 */
public class BottomTabPane extends Pane {

    private RTIViewer rtiViewer;
    private Scene parent;
    private TextField fileName;
    private TextField imageWidth;
    private TextField imageHeight;
    private TextField imageFormat;
    private ImageView imagePreview;
    private BorderPane imageBorderPane;
    private Image defaultImage;

    public BottomTabPane(RTIViewer rtiViewer, Scene parent){
        super();
        this.rtiViewer = rtiViewer;
        this.parent = parent;

        setWidth(parent.getWidth());
        setHeight(400);

        defaultImage = new Image("file:rsc/images/exeterUniLogo.png");

        createComponenets();
    }

    private void createComponenets(){
        TabPane tabPane = new TabPane();

        Tab previewTab = createPreviewTab();

        Tab bookmarksTab = new Tab("Bookmarks");
        bookmarksTab.setClosable(false);

        Tab saveImageTab = new Tab("Save image");
        saveImageTab.setClosable(false);

        tabPane.getTabs().addAll(previewTab, bookmarksTab, saveImageTab);
        getChildren().add(tabPane);
    }


    private Tab createPreviewTab(){
        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);

        FlowPane flowPane = new FlowPane();

        GridPane gridPane = new GridPane();
        gridPane.setHgap(10);
        gridPane.setVgap(20);

        Label fileNameLabel = new Label("File:");
        fileName = new TextField("");
        fileName.setEditable(false);

        Label imageWidthLabel = new Label("Width:");
        imageWidth = new TextField("");
        imageWidth.setEditable(false);
        imageWidth.setPrefWidth(60);

        Label imageHeightLabel = new Label("Height:");
        imageHeight = new TextField("");
        imageHeight.setEditable(false);
        imageHeight.setPrefWidth(60);

        Label imageFormatLabel = new Label("Format:");
        imageFormat = new TextField("");
        imageFormat.setEditable(false);
        imageFormat.setPrefWidth(60);

        imageBorderPane = new BorderPane();
        imageBorderPane.setPrefWidth(400);
        imageBorderPane.setPrefHeight(250);
        imagePreview = new ImageView();
        imagePreview.setFitWidth(RTIViewer.width);
        imagePreview.setFitHeight(imageBorderPane.getPrefHeight());
        imagePreview.setPreserveRatio(true);
        imagePreview.setImage(defaultImage);
        imageBorderPane.setCenter(imagePreview);


        GridPane.setConstraints(fileNameLabel, 0, 0, 1, 1);
        GridPane.setConstraints(fileName, 1, 0, 5, 1);

        GridPane.setConstraints(imageWidthLabel, 0, 1, 1, 1);
        GridPane.setConstraints(imageWidth, 1, 1, 1, 1);

        GridPane.setConstraints(imageHeightLabel, 2, 1, 1, 1);
        GridPane.setConstraints(imageHeight, 3, 1, 1, 1);

        GridPane.setConstraints(imageFormatLabel, 4, 1, 1, 1);
        GridPane.setConstraints(imageFormat, 5, 1, 1, 1);


        gridPane.getChildren().addAll(fileNameLabel, fileName, imageWidthLabel, imageWidth,
                imageHeightLabel, imageHeight, imageFormatLabel, imageFormat);


        //previewTab.setContent(gridPane);

        flowPane.setMargin(gridPane, new Insets(10, 0,10,0));

        flowPane.getChildren().add(gridPane);
        flowPane.getChildren().add(imageBorderPane);

        previewTab.setContent(flowPane);

        return previewTab;
    }


    public void setFileText(String text){
        fileName.setText(text);
    }

    public void setWidthText(String text){
        imageWidth.setText(text);
    }

    public void setHeightText(String text){
        imageHeight.setText(text);
    }

    public void setFormatText(String text){
        imageFormat.setText(text);
    }

    public void setPreviewImage(Image image){
        imagePreview.setImage(image);
    }

    public void setDefaultImage(){
        imagePreview.setImage(defaultImage);
    }
}

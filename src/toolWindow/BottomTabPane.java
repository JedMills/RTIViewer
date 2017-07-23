package toolWindow;

import bookmarks.Bookmark;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import openGLWindow.RTIWindow;
import openGLWindow.RTIWindowHSH;
import openGLWindow.RTIWindowLRGB;
import openGLWindow.RTIWindowRGB;

import java.util.ArrayList;
import java.util.List;


/**
 * <p>
 *  This is the TabPane that exists at the bottom of the main {@link RTIViewer} tool window. It has a tab containing
 *  a preview of the currently selected {@link RTIWindow}'s {@link ptmCreation.RTIObject}, a tab with an interface
 *  to make and edit {@link Bookmark}s and {@link bookmarks.Bookmark.Note}s, and a tab to save snapshots of the
 *  currently selected RTIObject.
 * </p>
 *
 * @see Bookmark
 * @see RTIViewer
 * @see RTIWindow
 */
public class BottomTabPane extends TabPane {

    /** Used to determine if the selected RTIWindow has been changed */
    private RTIWindow currentRTIWindow;

    /** Contains the Labels and TextFields for the preview tab */
    private GridPane previewGridPane;

    /** Displays the name of the currently selected RTIObject */
    private TextField fileName;

    /** Displays the width of the currently selected RTIObject */
    private TextField imageWidthBox;

    /** Displays the height of the currently selected RTIObject  */
    private TextField imageHeightBox;

    /** Displays the format of the currently selected RTIObject */
    private TextField imageFormat;

    /** Contains the preview image of the currently selected RTIObject */
    private ImageView imagePreview;

    /** Contains the imagePreview node so it can have the black border around it */
    private BorderPane imageBorderPane;

    /** Contains all the nodes for the preview tab */
    private VBox previewVBox;

    /** The rectangle that shows the current viewing window on the preview image */
    private Rectangle previewWindowRect;

    /** The scale of the preview rect, 2 = half the size of the image, 3 = a third of the size etc. */
    private float previewRectScale = 1.0f;

    /** Diaplsy the bookmark names for the currently selected RTIObject in the bookmarks tab*/
    private ComboBox<String> bookmarkComboBox;

    /** The list for all the Notes of the currently selected Bookmark in the Bookmarks tab*/
    private ListView<Bookmark.Note> notesList;

    /** Button to select saving the red channel in the snapshot, in the save tab */
    RadioButton redChannelButton;

    /** Button to select saving the green channel in the snapshot, in the save tab */
    RadioButton greenChannelButton;

    /** Button to select saving the blue channel in the snapshot, in the save tab */
    RadioButton blueChannelButton;

    /** Selector the file format to save the snapshot in, eg .jpg/.png, in the save tab*/
    ComboBox<String> imageFormatsSelector;

    /** Selector for the colour model to save the snapshot in (colour/ greyscale), in the save tab*/
    ComboBox<String> colourModelSelector;


    /**
     * Creates a new BottomTabPane.
     */
    public BottomTabPane(){
        super();

        //initialise the listener to listen to events from widgets in this pane
        BottomTabPaneListener.init(this);
        createComponents();
        setId("bottomTabPane");

        setMinWidth(0);
        setMinHeight(0);
        setMaxHeight(Double.MAX_VALUE);

        //listen to change in width to change size of components in this pane
        widthProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                setWidth((Double)newValue);
                updateViewportOnSeparateThread();
            }
        });
    }


    /**
     * Called when this BottomTabPane is resized to make the rectangle in the image preview resize with
     * the changing size of the image view containing the image.
     */
    private void updateViewportOnSeparateThread(){
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if(RTIViewer.selectedWindow != null) {
                    //update the rectangle using the values from the selected RTIWindow as this
                    //is more robust than trying to scale the window inside the tab pane
                    updateViewportRect( RTIViewer.selectedWindow.getViewportX(),
                                        RTIViewer.selectedWindow.getViewportY(),
                                        RTIViewer.selectedWindow.getImageScale());
                }
            }
        });
    }

    /**
     * Creates and adds the three tabs for the BottomTabPane: the Preview, Bookmark and Save tabs.
     */
    private void createComponents(){
        //create the three tabs
        Tab previewTab = createPreviewTab();
        Tab bookmarksTab = createBookmarksTab();
        Tab saveImageTab = createSaveImageTab();

        //and add them to this tab pane, simples
        getTabs().addAll(previewTab, bookmarksTab, saveImageTab);
    }


    /**
     * Returns a newly created Preview tab, containing fields that display the currently selected
     * {@link ptmCreation.RTIObject} name, width, height, and format, as well as a preview window with a preview of
     * the image and a rectangle showing the current viewport for the active {@link RTIWindow}.
     *
     * @see RTIWindow
     *
     * @return  the preview tab
     */
    private Tab createPreviewTab(){
        Tab previewTab = new Tab("Preview");
        previewTab.setClosable(false);

        //box containing the text fields/ labels and  the image preview
        previewVBox = new VBox();

            //box containing the text fields
            previewGridPane = new GridPane();

                //text fields and their labels for the file name, dimensions and format
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

                previewGridPane.setPadding(new Insets(0, 3, 0 , 3));

                //create the image view and rectangle for the preview
                createImagePreview();

                //set the positions in the grid for all this stuff
                GridPane.setConstraints(fileNameLabel, 0, 0, 1, 1);
                GridPane.setConstraints(fileName, 1, 0, 5, 1);

                GridPane.setConstraints(imageWidthLabel, 0, 1, 1, 1);
                GridPane.setConstraints(imageWidthBox, 1, 1, 1, 1);

                GridPane.setConstraints(imageHeightLabel, 2, 1, 1, 1);
                GridPane.setConstraints(imageHeightBox, 3, 1, 1, 1);

                GridPane.setConstraints(imageFormatLabel, 4, 1, 1, 1);
                GridPane.setConstraints(imageFormat, 5, 1, 1, 1);

            //add everything and add some nice padding
            previewGridPane.getChildren().addAll(fileNameLabel, fileName, imageWidthLabel, imageWidthBox,
                    imageHeightLabel, imageHeightBox, imageFormatLabel, imageFormat);

            previewGridPane.setAlignment(Pos.TOP_CENTER);
            previewGridPane.setVgap(5);
            previewGridPane.setHgap(5);

        previewVBox.getChildren().addAll(previewGridPane, imageBorderPane);
        previewVBox.setMargin(imageBorderPane, new Insets(5, 5, 0, 5));
        previewVBox.setMargin(previewGridPane, new Insets(5, 0, 5, 0));
        previewTab.setContent(previewVBox);

        return previewTab;
    }


    /**
     * Creates the BorderPane, ImageView and Rectangle that make up the image preview pane at the bottom of the preview
     * tab for the BottomTabPane, and adds event handlers to deal with mouse interaction with the preview.
     */
    private void createImagePreview(){
        //put the preview in a border pane so black bars can be displayed at the top and bottom/size when the
        //preview box isn't the same aspect ratio as the preview image
        imageBorderPane = new BorderPane();

        //rectangle to show the viewport of the currently active RTIWindow
        previewWindowRect = new Rectangle(0, 0, 0 ,0);
        previewWindowRect.setFill(Color.TRANSPARENT);
        previewWindowRect.setStrokeWidth(2);

        //image view that actually displays the preview image
        imagePreview = new ImageView();
        imagePreview.setPreserveRatio(true);

        //stick the image view and rectangle ina  stack pane so the rectangle can be on top of the image
        StackPane imageContainerPane = new StackPane(imagePreview, previewWindowRect);
        imageContainerPane.setMinWidth(0);
        imageContainerPane.setMinHeight(0);

        imageBorderPane.setCenter(imageContainerPane);
        imageBorderPane.setMinWidth(0);
        imageBorderPane.setMinHeight(0);
        imageBorderPane.prefWidthProperty().bind(RTIViewer.primaryStage.widthProperty());
        imageBorderPane.prefHeightProperty().bind(previewVBox.heightProperty());

        //disablethe rectangle so that mouse events can be registered by the imageview underneath
        previewWindowRect.setDisable(true);

        //fit the preview to the size of the border pane so it doesn't expand the toolbar
        imagePreview.fitHeightProperty().bind(imageBorderPane.heightProperty());
        imagePreview.fitWidthProperty().bind(imageBorderPane.widthProperty());
        imagePreview.setSmooth(true);
        setDefaultImage();

        //for when the user clicks and drags the preview to change the viewport
        imagePreview.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                previewImageClicked(event.getX(), event.getY());
            }
        });

        imagePreview.setOnMouseClicked(new EventHandler<MouseEvent>() {
            public void handle(MouseEvent event) {
                previewImageClicked(event.getX(), event.getY());
            }
        });


        //when the user scrolls on the preview to change the RTIWindow's zoom
        imagePreview.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                //scroll up positive, scroll down negative
                previewRectScale += 0.01 * event.getDeltaY();
                if(previewRectScale > 10){previewRectScale = 10f;}
                else if(previewRectScale < 1){previewRectScale = 1;}

                //update the active RTIWindow's zoom
                if(RTIViewer.selectedWindow != null) {
                    RTIViewer.selectedWindow.updateViewportFromPreview(previewRectScale);
                }
            }
        });
    }


    /**
     * Called when the user clicks or drags on the preview image, to change the pan of the active RTIWindow.
     * Gets the selected RTIWindow to update its viewport from the
     *
     * @param x     x position in the preview image that was clicked
     * @param y     y position in the preview image that was clicked
     */
    private void previewImageClicked(double x, double y){
        //map the (x, y) position of the mouse click to the OpenGL space, with (0, 0) at the
        //center of the RTIwindow, and (-1, -1) in the top-left corner (1, 1) in bottom-right
        float normX = (float)((x / imagePreview.getBoundsInParent().getWidth()) - 0.5) * 2;
        float normY = (float)-(((y / imagePreview.getBoundsInParent().getHeight()) - 0.5) * 2);

        RTIViewer.selectedWindow.updateViewportFromPreview(normX, normY, previewRectScale);
    }


    /**
     * Sets the text for the {@link BottomTabPane#fileName} field.
     *
     * @param text  name of the file to display
     */
    public void setFileText(String text){
        fileName.setText(text);
    }


    /**
     * Sets the text for the {@link BottomTabPane#imageWidthBox} field.
     *
     * @param text  width of the file to display
     */
    public void setWidthText(String text){
        imageWidthBox.setText(text);
    }



    /**
     * Sets the text for the {@link BottomTabPane#imageHeightBox} field.
     *
     * @param text  height of the file to display
     */
    public void setHeightText(String text){
        imageHeightBox.setText(text);
    }


    /**
     * Sets the text for the {@link BottomTabPane#imageFormat} field.
     *
     * @param text  format of the file to display
     */
    public void setFormatText(String text){
        imageFormat.setText(text);
    }


    /**
     * Sets the image in the image preview box to the given image. Updates the size of the box. Makes the
     * preview viewport rectangle not transparent.
     *
     * @param image the image to display in the preview box
     */
    public void setPreviewImage(Image image){
        imagePreview.setImage(image);
        updateSize(RTIViewer.primaryStage.getWidth(), RTIViewer.primaryStage.getHeight());
        //make the border pane have black bars at the sides/top and bottom of the image
        imageBorderPane.setStyle("-fx-background-color: #000000;");
        //display the viewport rectangle by making it non transparent
        previewWindowRect.setStroke(Color.RED);
    }


    /**
     * Sets the image preview pane to be blank white space, and hides the preview viewport rectangle.
     */
    public void setDefaultImage(){
        imagePreview.setImage(null);
        updateSize(RTIViewer.primaryStage.getWidth(), RTIViewer.primaryStage.getHeight());
        imageBorderPane.setStyle("-fx-background-color: #ffffff;");
        //hide the viewport rectangle by making it transparent
        previewWindowRect.setStroke(Color.TRANSPARENT);
    }


    /**
     * Called by the RTIWindow when its stageis resized, in order to resize the layout of the tabs and the size
     * of the widgets inside them.
     *
     * @param width         width of the primary stage
     * @param height        height of the primary stage
     */
    public void updateSize(double width, double height){
        imageWidthBox.setPrefWidth(width / 6);
        imageHeightBox.setPrefWidth(width / 6);
        imageFormat.setPrefWidth(width / 6);

        //causes a null pointer exception on startup without this check as adding tabs to the
        //BottomTabPane resizes the primary stage
        if(bookmarkComboBox != null && notesList != null) {
            bookmarkComboBox.setPrefWidth(width / 2);
            notesList.setPrefWidth(width / 1.5);
        }
    }


    /**
     * Called when the user selects a different RTIWindow than the one stored in the
     * {@link BottomTabPane#currentRTIWindow}. This causes the details shown in the file name, image width,
     * image height and format boxes, and the preview image to be changed.
     *
     * @param rtiWindow     the RTIWindow that has been clicked
     */
    public void updateSelectedWindow(RTIWindow rtiWindow){
        //if the user has clicked the same window that is already active, we don't have to change anything
        if(rtiWindow == currentRTIWindow){return;}
        currentRTIWindow = rtiWindow;

        //set the values displayed in the info fields
        setFileText(rtiWindow.rtiObject.getFilePath());
        setWidthText(String.valueOf(rtiWindow.rtiObject.getWidth()));
        setHeightText(String.valueOf(rtiWindow.rtiObject.getHeight()));
        setBookmarks(rtiWindow.rtiObject.getBookmarks());

        if(rtiWindow instanceof RTIWindowRGB){
            setFormatText("PTM RGB");
        }else if(rtiWindow instanceof RTIWindowLRGB){
            setFormatText("PTM LRGB");
        }else if(rtiWindow instanceof RTIWindowHSH){
            setFormatText("HSH");
        }

        //set the preview image to that of the newly selected RTIWindow
        setPreviewImage(rtiWindow.rtiObject.previewImage);
    }




    /**
     * Moves and scales the viewport rectangle in the image preview to the specified x and y coordinates,
     * which areto be given as the coordinate system of the OpenGL window.
     *
     * @param x             x coordinate in the OpenGL window to move the preview rectangle to
     * @param y             y coordinate in the OpenGL window to move the preview rectangle to
     * @param imageScale    scale of the image in the OpenGL window to change the preview rectangle to
     */
    public void updateViewportRect(float x, float y, float imageScale){
        previewRectScale = imageScale;
        previewWindowRect.setWidth(imagePreview.getBoundsInParent().getWidth() / imageScale);
        previewWindowRect.setHeight(imagePreview.getBoundsInParent().getHeight() / imageScale);

        //map the x = -1 : +1 and y = -1 : +1 of the OpenGL window to the 0 : preview with
        //and 0 : preview height, of the preview image
        double mappedX = (x / imageScale) * imagePreview.getBoundsInParent().getWidth() / 2;
        double mappedY = -(y / imageScale) * imagePreview.getBoundsInParent().getHeight() / 2;

        previewWindowRect.setTranslateX(mappedX);
        previewWindowRect.setTranslateY(mappedY);
    }


    /**
     * Creates the bookmarks tab, with the bookmark selector, add and delete bookmark buttons,
     * the notes list, the add, edit and delete notes buttons, and the update bookmark button.
     *
     * @see Bookmark
     * @see bookmarks.Bookmark.Note
     *
     * @return  the Bookmarks tab
     */
    private Tab createBookmarksTab(){
        //the bookmarks tab
        Tab tab = new Tab("Bookmarks");
        tab.setClosable(false);

        //vbox containing all the stuff in the tab
        VBox vBox = new VBox();
        vBox.setPadding(new Insets(5, 5, 5, 5));
        vBox.setFillWidth(true);

            //gridpane containing the bookmark label, bookmark selector, and add and delete bookmark buttons
            GridPane bookmarkPane = new GridPane();
                bookmarkPane.setAlignment(Pos.CENTER);
                bookmarkPane.setHgap(5);
                bookmarkPane.setId("bottomBookmarkPane");
                bookmarkPane.setPadding(new Insets(5, 5, 5, 5));

                Label bookmarkLabel = new Label("Bookmark:");
                GridPane.setConstraints(bookmarkLabel, 0, 0, 1, 1);
                bookmarkComboBox = new ComboBox<>();
                bookmarkComboBox.setId("bookmarkComboBox");
                bookmarkComboBox.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(bookmarkComboBox, 1, 0, 1, 1);

                Button bookmarkAdd = new Button("Add");
                bookmarkAdd.setId("addBookmarkButton");
                bookmarkAdd.setMinWidth(Button.USE_PREF_SIZE);
                bookmarkAdd.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(bookmarkAdd, 2, 0, 1, 1);


                Button bookmarkDel = new Button("Del");
                bookmarkDel.setId("deleteBookmarkButton");
                bookmarkDel.setMinWidth(Button.USE_PREF_SIZE);
                bookmarkDel.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(bookmarkDel, 3, 0, 1, 1);

            bookmarkPane.getChildren().addAll(bookmarkLabel, bookmarkComboBox, bookmarkAdd, bookmarkDel);
            vBox.setMargin(bookmarkPane, new Insets(10, 0, 5, 0));

            //gridpane containing the notes list, add, edit and delete notes buttons, and the update
            //bookmark label and button
            GridPane bookmarkListPane = new GridPane();
            bookmarkListPane.setAlignment(Pos.CENTER);
            bookmarkListPane.setHgap(5);
            bookmarkListPane.setVgap(5);

                Label notesLabel = new Label("Notes:");
                GridPane.setConstraints(notesLabel, 0, 0, 1, 1);

                notesList = createNotesListView();
                notesList.setId("notesList");
                notesList.setMinHeight(0);
                GridPane.setConstraints(notesList, 0, 1, 2, 3);

                //button  to edit the currently selected note by brining up the edit note dialog
                Button notesEdit = new Button("Edit");
                notesEdit.setId("editNote");
                notesEdit.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(notesEdit, 2, 1, 1, 1);

                //button to bring up the edit note dialog with a newly created note
                Button notesAdd = new Button("Add");
                notesAdd.setId("addNote");
                notesAdd.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(notesAdd, 2, 2, 1, 1);

                //button to delete the currently selected note
                Button notesDel = new Button("Del");
                notesDel.setId("delNote");
                notesDel.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(notesDel, 2, 3, 1, 1);

                //button to update the selected bookmark with te current rendering parameters
                Label updateBookmarkLabel = new Label("Light, Zoom, Pan & Rendering:");
                GridPane.setConstraints(updateBookmarkLabel, 0, 4, 2, 1);
                Button updateBookmark = new Button("Update");
                updateBookmark.setId("updateBookmark");
                updateBookmark.setOnAction(BottomTabPaneListener.getInstance());
                GridPane.setConstraints(updateBookmark, 2, 4, 1, 1);

            //add everything and format the pane nicely
            bookmarkListPane.getChildren().addAll(  notesLabel, notesList,
                                                    notesEdit, notesAdd, notesDel,
                                                    updateBookmarkLabel, updateBookmark);
            bookmarkListPane.setId("bookmarkListPane");

            bookmarkListPane.setPadding(new Insets(5, 5, 5, 5));
            bookmarkListPane.setMinHeight(0);
            notesList.setMinHeight(0);
            notesEdit.setMinHeight(0);
            notesAdd.setMinHeight(0);
            notesDel.setMinHeight(0);
            updateBookmark.setMinHeight(0);

        vBox.setMargin(bookmarkListPane, new Insets(5, 0, 0, 0));

        vBox.getChildren().addAll(bookmarkPane, bookmarkListPane);
        tab.setContent(vBox);

        //tab's finally made, phew!
        return tab;
    }


    /**
     * Creates the ListView for the Notes using a custom cell factory to display the Note's name attribute as
     * the elements of the list, and create a tooltip when the mouse is hovered over the note.
     *
     * @see Bookmark.Note
     *
     * @return  the notes ListView
     */
    private ListView<Bookmark.Note> createNotesListView(){
        ListView<Bookmark.Note> notesList = new ListView<Bookmark.Note>();

        //make a cell factory that displays the note's name as the elements in the list, and a tooltip
        //when the mouse is hovered over the nte with the note's details in it
        notesList.setCellFactory(new Callback<ListView<Bookmark.Note>, ListCell<Bookmark.Note>>() {
            @Override
            public ListCell<Bookmark.Note> call(ListView<Bookmark.Note> param) {
                ListCell<Bookmark.Note> cell = new ListCell<Bookmark.Note>(){
                    @Override
                    protected void updateItem(Bookmark.Note item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item != null){
                            //text inthe list view is the note'ssubject
                            setText(item.getSubject());

                            //make a tooltip with all the note's details in it
                            Tooltip tooltip = new Tooltip();
                            String tooltipText = item.getSubject() + System.lineSeparator() + item.getAuthor() +
                                    System.lineSeparator() + item.getTimeStamp() + System.lineSeparator() +
                                    System.lineSeparator() + item.getComment();
                            tooltip.setText(tooltipText);
                            tooltip.setPrefWidth(200);
                            tooltip.setPrefHeight(200);
                            tooltip.setWrapText(true);
                            tooltip.setTextAlignment(TextAlignment.JUSTIFY);

                            setTooltip(tooltip);
                        }else{
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        });

        return notesList;
    }




    /**
     * Creates the Save tab. The save tab has radio buttons to select which colour channels to save to the snapshot,
     * a selctor to save the snapshot as .png/.jpg, a selector to save the snapshot as greyscale or colour, and a
     * save button.
     *
     * @return the Save tab.
     */
    private Tab createSaveImageTab(){
        Tab saveTab = new Tab("Save");
        saveTab.setClosable(false);

        //contains everything in the tab
        VBox wholeTabContent = new VBox();
            Label saveSnapshotTitle = new Label("Save Snapshot");

            //contains the colour channels selection box and the format selection box
            HBox hBoxForOptions = new HBox();

                //contains the colour channels radio buttons
                VBox vBoxForChannels = new VBox();
                    Label saveChannelsLabel = new Label("Save colour channels:");
                    redChannelButton = new RadioButton("Red");
                    greenChannelButton = new RadioButton("Green");
                    blueChannelButton = new RadioButton("Blue");
                vBoxForChannels.getChildren().addAll(saveChannelsLabel, redChannelButton,
                        greenChannelButton, blueChannelButton);
                vBoxForChannels.getStyleClass().add("defaultBorder");
                vBoxForChannels.setSpacing(10);
                vBoxForChannels.setPadding(new Insets(5, 5, 5, 5));
                vBoxForChannels.setAlignment(Pos.CENTER_LEFT);

                //contains the image format dropdown menus
                GridPane gridPaneForFormat = new GridPane();
                    Label formatLabel = new Label("Save as format:");
                    GridPane.setConstraints(formatLabel, 0, 0);

                    imageFormatsSelector = new ComboBox<>(FXCollections.observableArrayList("jpg", "png"));
                    imageFormatsSelector.getSelectionModel().select(0);
                    imageFormatsSelector.setMaxWidth(Double.MAX_VALUE);
                    GridPane.setConstraints(imageFormatsSelector, 1, 0);

                    Label greyscaleLabel = new Label("Colour model:");
                    GridPane.setConstraints(greyscaleLabel, 0, 1);

                    colourModelSelector = new ComboBox<>(FXCollections.observableArrayList("Colour",
                                                                                                "Greyscale"));
                    colourModelSelector.getSelectionModel().select(0);
                    GridPane.setConstraints(colourModelSelector, 1, 1);
                gridPaneForFormat.getChildren().addAll( formatLabel, imageFormatsSelector,
                                                        greyscaleLabel, colourModelSelector);
                gridPaneForFormat.setHgap(10);
                gridPaneForFormat.setVgap(10);
                gridPaneForFormat.setPadding(new Insets(5, 5, 5, 5));
                gridPaneForFormat.setAlignment(Pos.CENTER);
                gridPaneForFormat.getStyleClass().add("defaultBorder");

            hBoxForOptions.getChildren().addAll(vBoxForChannels, gridPaneForFormat);
            hBoxForOptions.setAlignment(Pos.CENTER);
            hBoxForOptions.setSpacing(10);

        Button saveAsButton = new Button("Save As...");
        saveAsButton.setOnAction(BottomTabPaneListener.getInstance());
        saveAsButton.setId("saveAsButton");

        wholeTabContent.getChildren().addAll(saveSnapshotTitle, hBoxForOptions, saveAsButton);
        wholeTabContent.setAlignment(Pos.CENTER);
        wholeTabContent.setSpacing(10);

        saveTab.setContent(wholeTabContent);

        //another big formatting method there too
        return saveTab;
    }


    /**
     * Bookmarks to display in the bookmarks combo box.
     *
     * @see Bookmark
     *
     * @param bookmarks the bookmarks to display
     */
    public void setBookmarks(ArrayList<Bookmark> bookmarks){
        //get rid of the existing bookmarks
        bookmarkComboBox.getItems().clear();
        notesList.getItems().clear();

        if(bookmarks == null){return;}

        //add the names to the list
        for(Bookmark bookmark : bookmarks){
            bookmarkComboBox.getItems().add(bookmark.getName());
        }
    }


    /**
     * Finds all the Notes attacthed to the current {@link Bookmark} object, and adds them to the
     * {@link BottomTabPane#notesList} to be displayed.
     *
     * @see Bookmark
     * @see bookmarks.Bookmark.Note
     *
     * @param bookmarkName
     */
    public void showNotes(String bookmarkName){
        notesList.getItems().clear();

        ArrayList<Bookmark> bookmarks = currentRTIWindow.rtiObject.getBookmarks();

        //get the bookmark with the same name as the currently selected one, and find all its notes and add them
        for(Bookmark bookmark : bookmarks){
            if(bookmark.getName().equals(bookmarkName)){
                for(Bookmark.Note note : bookmark.getNotes()){
                    notesList.getItems().add(note);
                }
                break;
            }
        }
    }


    /**
     * Sets all the text in the text fields to blank, sets the diaplayed preview image to blank, and
     * removes all bookmarks and notes from the notes list.
     */
    public void setNoFocusedWindow(){
        setFileText("");
        setWidthText("");
        setHeightText("");
        setFormatText("");
        setDefaultImage();

        setBookmarks(null);
    }


    /**
     * @return the list of bookmark names in the {@link BottomTabPane#bookmarkComboBox}
     */
    public List<String> getCurrentBookmarkNames(){
        return bookmarkComboBox.getItems();
    }


    /**
     * @return  {@link BottomTabPane#bookmarkComboBox}
     */
    public ComboBox<String> getBookmarkComboBox() {
        return bookmarkComboBox;
    }


    /**
     * Sets the currently selected bookmark in the dropdown to the one wit hte same name as the passed name.
     *
     * @param bookmarkName  the bookmark to select
     */
    public void setSelectedBookmark(String bookmarkName){
        bookmarkComboBox.getSelectionModel().select(bookmarkName);
    }


    /**
     * @return {@link BottomTabPane#notesList}
     */
    public ListView<Bookmark.Note> getNotesList() {
        return notesList;
    }
}

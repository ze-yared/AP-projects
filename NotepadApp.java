import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.geometry.Orientation;
import java.io.*;

public class NotepadApp extends Application {
    
    private TextArea textArea;
    private Stage primaryStage;
    private File currentFile = null;
    private boolean textChanged = false;
    
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("JavaFX Notepad - Untitled");
        
        BorderPane root = new BorderPane();
        
        textArea = new TextArea();
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Monospaced", FontPosture.REGULAR, 14));
        
        textArea.textProperty().addListener((obs, oldText, newText) -> {
            textChanged = true;
            updateTitle();
        });
        
        root.setCenter(textArea);
        
        MenuBar menuBar = new MenuBar();
        
        Menu fileMenu = new Menu("File");
        
        MenuItem newFile = new MenuItem("New");
        newFile.setAccelerator(KeyCombination.keyCombination("Ctrl+N"));
        newFile.setOnAction(e -> newFile());
        
        MenuItem openFile = new MenuItem("Open...");
        openFile.setAccelerator(KeyCombination.keyCombination("Ctrl+O"));
        openFile.setOnAction(e -> openFile());
        
        MenuItem saveFile = new MenuItem("Save");
        saveFile.setAccelerator(KeyCombination.keyCombination("Ctrl+S"));
        saveFile.setOnAction(e -> saveFile());
        
        MenuItem saveAsFile = new MenuItem("Save As...");
        saveAsFile.setAccelerator(KeyCombination.keyCombination("Ctrl+Shift+S"));
        saveAsFile.setOnAction(e -> saveAsFile());
        
        SeparatorMenuItem separator1 = new SeparatorMenuItem();
        
        MenuItem exit = new MenuItem("Exit");
        exit.setAccelerator(KeyCombination.keyCombination("Ctrl+Q"));
        exit.setOnAction(e -> exitApplication());
        
        fileMenu.getItems().addAll(newFile, openFile, saveFile, saveAsFile, separator1, exit);
        
        Menu editMenu = new Menu("Edit");
        
        MenuItem undo = new MenuItem("Undo");
        undo.setAccelerator(KeyCombination.keyCombination("Ctrl+Z"));
        undo.setOnAction(e -> textArea.undo());
        
        MenuItem redo = new MenuItem("Redo");
        redo.setAccelerator(KeyCombination.keyCombination("Ctrl+Y"));
        redo.setOnAction(e -> textArea.redo());
        
        SeparatorMenuItem separator2 = new SeparatorMenuItem();
        
        MenuItem cut = new MenuItem("Cut");
        cut.setAccelerator(KeyCombination.keyCombination("Ctrl+X"));
        cut.setOnAction(e -> textArea.cut());
        
        MenuItem copy = new MenuItem("Copy");
        copy.setAccelerator(KeyCombination.keyCombination("Ctrl+C"));
        copy.setOnAction(e -> textArea.copy());
          
        MenuItem paste = new MenuItem("Paste");
        paste.setAccelerator(KeyCombination.keyCombination("Ctrl+V"));
        paste.setOnAction(e -> textArea.paste());
        
        MenuItem delete = new MenuItem("Delete");
        delete.setAccelerator(KeyCombination.keyCombination("Del"));
        delete.setOnAction(e -> textArea.deleteText(textArea.getSelection()));
        
        SeparatorMenuItem separator3 = new SeparatorMenuItem();
        
        MenuItem selectAll = new MenuItem("Select All");
        selectAll.setAccelerator(KeyCombination.keyCombination("Ctrl+A"));
        selectAll.setOnAction(e -> textArea.selectAll());
        
        editMenu.getItems().addAll(undo, redo, separator2, cut, copy, paste, delete, separator3, selectAll);
        
        Menu formatMenu = new Menu("Format");
        
        Menu fontMenu = new Menu("Font");
        
        MenuItem increaseFont = new MenuItem("Increase Font Size");
        increaseFont.setOnAction(e -> changeFontSize(2));
        
        MenuItem decreaseFont = new MenuItem("Decrease Font Size");
        decreaseFont.setOnAction(e -> changeFontSize(-2));
        
        SeparatorMenuItem separator4 = new SeparatorMenuItem();
        
        MenuItem wordWrap = new MenuItem("Toggle Word Wrap");
        wordWrap.setOnAction(e -> toggleWordWrap());
        
        fontMenu.getItems().addAll(increaseFont, decreaseFont);
        formatMenu.getItems().addAll(fontMenu, separator4, wordWrap);
        
        Menu viewMenu = new Menu("View");
        
        MenuItem statusBar = new MenuItem("Show/Hide Status Bar");
        statusBar.setOnAction(e -> toggleStatusBar());
        
        viewMenu.getItems().add(statusBar);
        
        Menu helpMenu = new Menu("Help");
        
        MenuItem about = new MenuItem("About");
        about.setOnAction(e -> showAbout());
        
        helpMenu.getItems().add(about);
        
        menuBar.getMenus().addAll(fileMenu, editMenu, formatMenu, viewMenu, helpMenu);
        
        ToolBar toolBar = new ToolBar();
        
        Button newBtn = new Button("New");
        newBtn.setOnAction(e -> newFile());
        
        Button openBtn = new Button("Open");
        openBtn.setOnAction(e -> openFile());
        
        Button saveBtn = new Button("Save");
        saveBtn.setOnAction(e -> saveFile());
        
        Separator separator5 = new Separator();
        separator5.setOrientation(Orientation.VERTICAL);
        
        Button cutBtn = new Button("Cut");
        cutBtn.setOnAction(e -> textArea.cut());
        
        Button copyBtn = new Button("Copy");
        copyBtn.setOnAction(e -> textArea.copy());
        
        Button pasteBtn = new Button("Paste");
        pasteBtn.setOnAction(e -> textArea.paste());
        
        toolBar.getItems().addAll(newBtn, openBtn, saveBtn, separator5, cutBtn, copyBtn, pasteBtn);
        
        VBox topContainer = new VBox();
        topContainer.getChildren().addAll(menuBar, toolBar);
        root.setTop(topContainer);
        
        Label statusLabel = new Label("Ready");
        root.setBottom(statusLabel);
        
        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.show();
        
        primaryStage.setOnCloseRequest(e -> {
            if (!handleSaveOnClose()) {
                e.consume();
            }
        });
    }
    private void newFile() {
        if (handleSaveOnClose()) {
            textArea.clear();
            currentFile = null;
            textChanged = false;
            primaryStage.setTitle("JavaFX Notepad - Untitled");
        }
    }
    private void openFile() {
        if (!handleSaveOnClose()) {
            return;
        } 
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open File");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        File file = fileChooser.showOpenDialog(primaryStage);
        if (file != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append("\n");
                }
                textArea.setText(content.toString());
                currentFile = file;
                textChanged = false;
                updateTitle();
                showStatus("File opened: " + file.getName());
            } catch (IOException e) {
                showError("Error opening file", e.getMessage());
            }
        }
    }
    
    private void saveFile() {
        if (currentFile == null) {
            saveAsFile();
        } else {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentFile))) {
                writer.write(textArea.getText());
                textChanged = false;
                updateTitle();
                showStatus("File saved: " + currentFile.getName());
            } catch (IOException e) {
                showError("Error saving file", e.getMessage());
            }
        }
    }
    
    private void saveAsFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File As");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        File file = fileChooser.showSaveDialog(primaryStage);
        if (file != null) {
            currentFile = file;
            saveFile();
        }
    }
    
    private boolean handleSaveOnClose() {
        if (textChanged) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Changes");
            alert.setHeaderText("Do you want to save the changes?");
            alert.setContentText("Your changes will be lost if you don't save them.");
            
            ButtonType saveBtn = new ButtonType("Save");
            ButtonType dontSaveBtn = new ButtonType("Don't Save");
            ButtonType cancelBtn = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);
            
            alert.getButtonTypes().setAll(saveBtn, dontSaveBtn, cancelBtn);
            
            ButtonType result = alert.showAndWait().orElse(cancelBtn);
            
            if (result == saveBtn) {
                saveFile();
                return !textChanged;
            } else if (result == dontSaveBtn) {
                return true;
            } else {
                return false;
            }
        }
        return true;
    }
    
    private void exitApplication() {
        if (handleSaveOnClose()) {
            primaryStage.close();
        }
    }
    
    private void updateTitle() {
        String title = "JavaFX Notepad";
        if (currentFile != null) {
            title += " - " + currentFile.getName();
        } else {
            title += " - Untitled";
        }
        if (textChanged) {
            title += " *";
        }
        primaryStage.setTitle(title);
    }
    
    private void changeFontSize(int delta) {
        Font currentFont = textArea.getFont();
        double newSize = currentFont.getSize() + delta;
        if (newSize >= 8 && newSize <= 72) {
            textArea.setFont(Font.font(currentFont.getFamily(), newSize));
            showStatus("Font size: " + (int)newSize);
        }
    }
    
    private void toggleWordWrap() {
        textArea.setWrapText(!textArea.isWrapText());
        showStatus("Word Wrap: " + (textArea.isWrapText() ? "On" : "Off"));
    }
    
    private void toggleStatusBar() {
        BorderPane root = (BorderPane) textArea.getParent().getParent();
        if (root.getBottom() != null) {
            root.setBottom(null);
            showStatus("Status Bar Hidden");
        } else {
            Label statusLabel = new Label("Ready");
            root.setBottom(statusLabel);
            showStatus("Status Bar Shown");
        }
    }
    
    private void showAbout() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("About JavaFX Notepad");
        alert.setHeaderText("JavaFX Notepad");
        alert.setContentText("Version 1.0\n\nA simple notepad application built with JavaFX.\n\n© 2024");
        alert.showAndWait();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showStatus(String message) {
        BorderPane root = (BorderPane) textArea.getParent().getParent();
        if (root.getBottom() != null) {
            Label statusLabel = (Label) root.getBottom();
            statusLabel.setText(message);
        }
        System.out.println(message);
    }
 
    public static void main(String[] args) {
        launch(args);
    }
}
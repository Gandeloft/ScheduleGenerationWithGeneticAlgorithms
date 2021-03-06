package main.JavaFX;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Line;
import javafx.stage.Modality;
import javafx.stage.Stage;
import main.JavaFX.classGroups.ClassGroupDataModel;
import main.JavaFX.classGroups.ClassGroupsTabController;
import main.JavaFX.courses.CoursesDataModel;
import main.JavaFX.courses.CoursesTabController;
import main.JavaFX.lectureHalls.LectureHallInputController;
import main.JavaFX.lectureHalls.LectureHallsDataModel;
import main.JavaFX.lectureHalls.LectureHallsTabController;
import main.JavaFX.lecturers.LecturersDataModel;
import main.JavaFX.lecturers.LecturersTabController;
import main.JavaFX.scheduleDisplay.ScheduleDisplayController;
import main.JavaFX.vocations.VocationsDataModel;
import main.JavaFX.vocations.VocationsTabController;
import main.classes.ClassGroup;
import main.classes.Course;
import main.classes.LectureHall;
import main.classes.Lecturer;
import main.geneticAlgoritmClasses.DynamicEvolveAndSolve;
import main.geneticAlgoritmClasses.DynamicSchedule;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Array;
import java.sql.SQLException;
import java.util.*;

public class MainWindowController {

    @FXML
    private MenuItem retrieveResultsMenuItem;
    @FXML
    MenuItem produceScheduleMenuItem;
    @FXML
    private VocationsTabController vocationsTabController;
    @FXML
    private ClassGroupsTabController classGroupsTabController;
    @FXML
    private CoursesTabController coursesTabController;
    @FXML
    private LecturersTabController lecturersTabController;
    @FXML
    private LectureHallsTabController lectureHallsTabController;
    @FXML
    private MenuItem testiranjeCrtanjaRasporeda;
    @FXML
    private AnchorPane menuAndContextContainerAnchorPane;
    @FXML
    private BorderPane mainWindowBorderPane;
    @FXML
    private MenuBar menuBar;
    @FXML
    private Button maximizeButton;
    @FXML
    private Button minimizeButton;
    @FXML
    private Button closeButton;

    private VocationsDataModel vocationsDataModel;
    private ClassGroupDataModel classGroupDataModel;
    private CoursesDataModel coursesDataModel;
    private LectureHallsDataModel lectureHallsDataModel;
    private LecturersDataModel lecturersDataModel;

    private double xOffset = 0;
    private double yOffset = 0;

    public void initialize() throws SQLException, IOException {

        minimizeButton.setOnAction(actionEvent -> {
            Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
            // is stage minimizable into task bar. (true | false)
            stage.setIconified(true);
        });
        maximizeButton.setOnAction(actionEvent -> {
            Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
            stage.setMaximized(!stage.isMaximized());
        });
        closeButton.setOnAction(actionEvent -> {
            Stage stage = (Stage)((Button)actionEvent.getSource()).getScene().getWindow();
            MainWindow.mainStage.close();
        });

        mainWindowBorderPane.setPrefHeight(MainWindow.mainStage.getHeight());
        mainWindowBorderPane.setPrefWidth(MainWindow.mainStage.getWidth());

        // Mimicking Windows's method of moving windows by making it possible to move the window by
        // clicking on the menu bar and dragging it.
       // https://stackoverflow.com/a/13460743/10299831
        menuBar.setOnMousePressed(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                xOffset = event.getSceneX();
                yOffset = event.getSceneY();
            }
        });
        menuBar.setOnMouseDragged(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                MainWindow.mainStage.setX(event.getScreenX() - xOffset);
                MainWindow.mainStage.setY(event.getScreenY() - yOffset);
            }
        });


        menuAndContextContainerAnchorPane.prefWidthProperty().bind(mainWindowBorderPane.widthProperty());

        this.vocationsDataModel = new VocationsDataModel();
        this.classGroupDataModel = new ClassGroupDataModel();
        this.coursesDataModel = new CoursesDataModel();
        this.lectureHallsDataModel = new LectureHallsDataModel();
        this.lecturersDataModel = new LecturersDataModel();

        this.vocationsTabController.injectMainWindowController(this);
        this.classGroupsTabController.injectMainWindowController(this);
        this.coursesTabController.injectMainWindowController(this);
        this.lecturersTabController.injectMainWindowController(this);
        this.lectureHallsTabController.injectMainWindowController(this);

        produceScheduleMenuItem.setOnAction(actionEvent -> {
            try {
                DynamicSchedule generatedSchedule = DynamicEvolveAndSolve.execute(classGroupDataModel.getClassGroupsArrayList(),
                        coursesDataModel.getCoursesArrayList(), lecturersDataModel.getLecturersArrayList(),
                        lectureHallsDataModel.getClassGroupsArrayList());
                openScheduleDisplay(generatedSchedule);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });

        testiranjeCrtanjaRasporeda.setOnAction(actionEvent -> {
            try {
                final int NUMBER_OF_SAMPLES = 1000;
                ArrayList<LinkedList<Number>> allResults = new ArrayList<>();
                for(int i = 0; i<NUMBER_OF_SAMPLES; i++) {
                    LinkedList<Number> result = DynamicEvolveAndSolve.executeMeasuringPerformance(classGroupDataModel.getClassGroupsArrayList(),
                            coursesDataModel.getCoursesArrayList(), lecturersDataModel.getLecturersArrayList(),
                            lectureHallsDataModel.getClassGroupsArrayList());
                    allResults.add(result);
                }
                calculateStatistics(allResults);

                FileOutputStream fos = new FileOutputStream("t.tmp");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(allResults);
                oos.close();

            } catch (SQLException | IOException throwables) {
                throwables.printStackTrace();
            }
        });

        retrieveResultsMenuItem.setOnAction(actionEvent -> {
            try {
                calculateDistribution(readFile());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        });

    }

    private void calculateStatistics(ArrayList<LinkedList<Number>> retrievedResults) {
        int evolutionsCounter = 0;
        int evolutionsCounter500 = 0;
        double timeCounter = 0;
        double timeCounter500 = 0;
        double fitnessCounter = 0;
        int failedCounter = 0;
        for (LinkedList<Number> ll : retrievedResults) {
            evolutionsCounter += (int) ll.get(0);
            timeCounter += (double) ll.get(1);
            fitnessCounter += (double) ll.get(2);
            if ((double) ll.get(2) <500) {
                failedCounter += 1;
            } else {
                evolutionsCounter500 += (int) ll.get(0);
                timeCounter500 += (double) ll.get(1);
            }
        }
        System.out.println(evolutionsCounter + " : Total evolutions. " + evolutionsCounter/retrievedResults.size() + " : Average evolutions per sample.");
        System.out.println(timeCounter + " : Total time. " + timeCounter/retrievedResults.size() + " : Average time per sample.");
        System.out.println(evolutionsCounter500 + " : Total 500 evolutions. " + evolutionsCounter500/(retrievedResults.size()-failedCounter) + " : Average evolutions per 500 sample.");
        System.out.println(timeCounter500 + " : Total 500 time. " + timeCounter500/(retrievedResults.size()-failedCounter) + " : Average time per 500 sample.");
        System.out.println(fitnessCounter/retrievedResults.size() + " : Average fitness achieved (per sample)");
        System.out.println(failedCounter + " : Number of failures.");

    }

    private void calculateDistribution(ArrayList<LinkedList<Number>> retrievedResults) {
        ArrayList<Integer> numberOfEvolutionsFor500Solutions = new ArrayList<>();
        int maximumEvolutions = 0;
        int minimumEvolutions = 500;
        for (LinkedList<Number> ll : retrievedResults) {
            if ((double) ll.get(2) == 500) {
                System.out.println(ll.get(0));
                numberOfEvolutionsFor500Solutions.add((int)ll.get(0));
                if(minimumEvolutions > (int) ll.get(0)){
                    minimumEvolutions = (int) ll.get(0);
                }
                if(maximumEvolutions < (int) ll.get(0)){
                    maximumEvolutions = (int) ll.get(0);
                }
            }
        }
        HashMap<Integer, Integer> distributionMap = new HashMap<>();
        double ratio = ((double)(maximumEvolutions-minimumEvolutions)/20);

        for (int i : numberOfEvolutionsFor500Solutions) {
            int integerValue = (int)((i-minimumEvolutions)/ratio)/1;
            if(integerValue== 20) {
                System.out.println(i + " evo 6 vrijednosti");
            }
            distributionMap.putIfAbsent(integerValue,0);
            int currentAmountOfResultsInTheSpectrum = distributionMap.get(integerValue);
            currentAmountOfResultsInTheSpectrum += 1;
            distributionMap.put(integerValue,currentAmountOfResultsInTheSpectrum);
        }
       /* for (Map.Entry<Integer,Integer> e : distributionMap.entrySet()) {
            System.out.println("U spektru: " + e.getKey() + " se nalazi " + e.getValue() + " rezultata.");
        }*/
    }

    private ArrayList<LinkedList<Number>> readFile() throws IOException, ClassNotFoundException {
        FileInputStream fis = new FileInputStream("t.tmp");
        ObjectInputStream ois = new ObjectInputStream(fis);
        ArrayList<LinkedList<Number>> retrievedResults = (ArrayList<LinkedList<Number>>) ois.readObject();
        ois.close();
        return retrievedResults;
    }

    private void openScheduleDisplay(DynamicSchedule dynamicSchedule) {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("scheduleDisplay/scheduleDisplay.fxml"));
        Stage stage = new Stage();
        Parent root = null;
        try {
            root = loader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
        ScheduleDisplayController scheduleDisplayController = loader.getController();
        scheduleDisplayController.setThisStage(stage);
        scheduleDisplayController.setDynamicScheduleItem(dynamicSchedule);
        stage.setScene(new Scene(root));
        stage.setResizable(false);

        stage.show();
    }

    public VocationsDataModel getVocationsDataModel() {
        return vocationsDataModel;
    }

    public ClassGroupDataModel getClassGroupDataModel() {
        return classGroupDataModel;
    }

    public CoursesDataModel getCoursesDataModel() {
        return coursesDataModel;
    }

    public LectureHallsDataModel getLectureHallsDataModel() {
        return lectureHallsDataModel;
    }

    public LecturersDataModel getLecturersDataModel() {
        return lecturersDataModel;
    }

}
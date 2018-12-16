package application;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import admin.adminController;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import parent.parentController;
import student.studentController;
import teacher.teacherController;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

public class Main extends Application {
	@FXML
	private TextField txtID;
	@FXML
	private Button btnLogIn;
	@FXML
	private ComboBox<String> views;
	@FXML
	private PasswordField passField;
	@FXML
	private CheckBox showPassword;
	@FXML
	private Label lblPassword, lblError;

	private ObservableList<String> listViews = FXCollections.observableArrayList("teacher", "student", "parent",
			"admin");
	private static Stage window;
	private static FXMLLoader loader;
	private static Scene scene;
	static FlowPane fp;
	static URL url;

	// JDBC driver name and database URL
	static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
	static final String DB_URL = "jdbc:mysql://localhost/primary1?useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

	// Database credentials
	static final String USER = "ali";
	static final String PASS = "12345";

	private static Connection connect;
	private static Statement statement;
	private static ResultSet resultSet;
	private static String loggedName;

	@Override
	public void start(Stage primaryStage) {
		try {
			// Register JDBC driver
			Class.forName(JDBC_DRIVER);

			// Open a connection
			System.out.println("Connecting to a selected database...");
			connect = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			window = primaryStage;
			url = getClass().getResource("mainView.fxml");
			loader = new FXMLLoader(url);
			fp = loader.load();
			scene = new Scene(fp);
			window.setScene(scene);
			window.setTitle("Student Evaluation System");
			window.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void switchToMain() throws IOException {
		loader = new FXMLLoader(url);
		fp = loader.load();
		scene = new Scene(fp);
		window.setScene(scene);
	}

	@FXML
	public void onLogIn() throws IOException {
		String id = txtID.getText();
		String password = passField.getText();
		String view = views.getValue();

		if (checkInput(view, id, password)) {
			String resource = String.format("/%s/%sView.fxml", view, view);
			FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
			if (view.equals("teacher")) {
				teacherController.setPrimaryStage(window);
				teacherController.setStatement(statement);
				teacherController.setName(loggedName);
				teacherController.setID(id);
			} else if (view.equals("student")) {
				studentController.setID(id);
				studentController.setName(loggedName);
				studentController.setStatement(statement);
			} else if (view.equals("parent")) {
				parentController.setID(id);
				parentController.setName(loggedName);
				parentController.setStatement(statement);
			} else if (view.equals("admin")) {
				adminController.setID(id);
				adminController.setName(loggedName);
				adminController.setStatement(statement);
				adminController.setPrimaryStage(window);
			}

			window.setScene(new Scene(loader.load()));
		}

		else {
			lblError.setText("incorrect id or password");
			lblError.setTextFill(Color.RED);
			lblError.setVisible(true);
		}
	}

	public boolean checkInput(String view, String id, String password) throws FileNotFoundException {
		try {
			String selectUser = String.format("SELECT %s_id, %s_password, %s_name FROM %ss;", view, view, view, view);
			statement = connect.createStatement();
			resultSet = statement.executeQuery(selectUser);
			while (resultSet.next()) {
				if (resultSet.getString(1).equals(id) && resultSet.getString(2).equals(password)) {
					loggedName = resultSet.getString(view + "_name");
					// System.out.println(loggedName + " has logged in");
					return true;
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return false;
	}

	@FXML
	public void initialize() {
		views.setValue("admin");
		views.setItems(listViews);
		lblPassword.visibleProperty().bind(showPassword.selectedProperty());
		lblPassword.textProperty().bind(passField.textProperty());
	}

	public static void main(String[] args) {
		launch(args);
	}
}

package application;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public class LoginController implements Initializable{
	public LoginModel loginModel = new LoginModel();
	
	@FXML
	private Label isConnected;
	
	@FXML
	private TextField txtUsername;
	
	@FXML
	private TextField txtPassword;
	
	@Override
	public void initialize(URL location, ResourceBundle recources) {
		if(loginModel.DbConnected()) {
			isConnected.setText("Connected");
		}else {
			isConnected.setText("NOT CONNECTED");
		}
	}
	public void Login (ActionEvent event) {
		try {
			if (loginModel.isLogin(txtUsername.getText(), txtPassword.getText())) {
				isConnected.setText("Login Successful");
				
				Stage primaryStage = new Stage();
				FXMLLoader loader = new FXMLLoader();
				Pane root = loader.load(getClass().getResource("/application/ClientGUI.fxml").openStream());
				ClientGUIController clientGUIController = (ClientGUIController)loader.getController();
				clientGUIController.GetUser(txtUsername.getText());
				Scene scene = new Scene(root);
				scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
				primaryStage.setScene(scene);
				primaryStage.show();
				
				
			}else {
				isConnected.setText("Login failed");
			}
		} catch (SQLException e) {
			isConnected.setText("Login failed");
			e.printStackTrace();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}

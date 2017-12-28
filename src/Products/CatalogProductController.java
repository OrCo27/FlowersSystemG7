package Products;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import PacketSender.Command;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class CatalogProductController implements Initializable {

	@FXML
	private TextField txtID;
	@FXML
	private TextField txtName;
	@FXML
	private TextField txtType;
	@FXML
	private Button btnSave;
	@FXML
	private ComboBox<String> cmbProducts;

	private ArrayList<Product> productsList;

	private ObservableList<String> observelist;
	
	public void start(Stage primaryStage) throws Exception {

		String title = "Products";
		String srcFXML = "/Products/App.fxml";
		String srcCSS = "/Products/application.css";
		
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource(srcFXML));
			Parent root = loader.load();
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource(srcCSS).toExternalForm());
			primaryStage.setTitle(title);
			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			// TODO: handle exception
			System.out.println(e);
			e.printStackTrace();
		}
		
		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			
			@Override
			public void handle(WindowEvent event) {
				// TODO Auto-generated method stub
				Platform.exit();
			}
		});

	}
	

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		
		Packet packet = new Packet();
		packet.addCommand(Command.getCatalogProducts);
		packet.addCommand(Command.getFlowers);
		
		// create the thread for send to server the message
		SystemSender send = new SystemSender(packet);

		// register the handler that occurs when the data arrived from the server
		send.registerHandler(new IResultHandler() {
			@Override
			public void onWaitingForResult() {
			}

			@Override
			public void onReceivingResult(Packet p)
			{
				if (p.getResultState())
				{
					ArrayList<CatalogProduct> pList = p.<CatalogProduct>convertedResultListForCommand(Command.getCatalogProducts);
					ArrayList<FlowerInProduct> fList = p.<FlowerInProduct>convertedResultListForCommand(Command.getFlowers);
				
					for (CatalogProduct pro : pList)
					{
						System.out.println("<Product> ID: " + pro.getId() + " Name: " + pro.getName() + " Price: " + pro.getPrice() + " Discount: " + pro.getSaleDiscountPercent());
						ArrayList<FlowerInProduct> flowersInProduct = new ArrayList<>();
						for (FlowerInProduct fp : fList)
						{
							if (fp.getProductId() == pro.getId())
								flowersInProduct.add(fp);
						}
					
						pro.setFlowerInProductList(flowersInProduct);
					}
					
					System.out.println();
				}
				else
					System.out.println("Fail: " + p.getExceptionMessage());
			}
		});
		send.start();
	}


}
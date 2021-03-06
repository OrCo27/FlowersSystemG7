package Products;

import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

import Customers.Account;
import Customers.Customer;
import Login.CustomerMenuController;
import Login.LoginController;
import Orders.OrderController;
import PacketSender.Command;
import PacketSender.IResultHandler;
import PacketSender.Packet;
import PacketSender.SystemSender;
import Products.SelectProductController.CatalogProductDetails;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
/**
 * Controller
 * Handle cart product quantity and manage products in order
 *
 */
public class CartController implements Initializable
{
      @FXML
      private Button btnBackToCatalog;
    
	  @FXML
	  private Button btnPurchase;
	  @FXML
	  private Button btnClearOrder;

	  @FXML
	  private Label lblPrice;

	  @FXML
      private ListView<Product> lstCart;
	  
	   @FXML
	   private Button btnAddCustomProduct;
	
	   /** Map for save all both catalog products and custom product and their quantity for the cart  */
	  public static LinkedHashMap<Product, Integer> cartProducts = new LinkedHashMap<>();
	  /**
	   * dynamic list with products to display
	   */
	  private ObservableList<Product> data;
	  
	  private static boolean comesFromCatalog = false;
	  
	  private static Stage mainStage;
	  /**
	   * list with all flowers
	   */
	  private static ArrayList<Flower> flowerList = new ArrayList<>();
	  /**
	   * calculated total price
	   */
	  private static double totalPrice = 0.0;
	  
	  /**
	   * Initialize the state if the controller called from catalog, for back button
	   * @param comesFromCatalogState True - if called from Catalog Viewer, False - else
	   */
	  public void setComesFromCatalog(boolean comesFromCatalogState)
	  {
		  comesFromCatalog = comesFromCatalogState;
	  }
	  
	  /**
	   * Add Collection of products to the map with default quantity as 1
	   * @param productList The product list
	   */
	  public void addProductsToCartMap(ArrayList<Product> productList)
	  {
		  for (Product pro : productList)
		  {
			  if (!cartProducts.containsKey(pro))
				  cartProducts.put(pro, 1);
		  }
	  }
	  
	  	/**
		 * Fill all items to the customize listview dynamically
		 */
		public void fillCatalogItems()
		{
			data = FXCollections.observableArrayList(new ArrayList<>(cartProducts.keySet()));
			lstCart.setCellFactory(new Callback<ListView<Product>, ListCell<Product>>() {
				
				@Override
				public ListCell<Product> call(ListView<Product> param) {
					return new ListCell<Product>() {
						
					private void setCellHandler(Product pro)
					{
						int count = cartProducts.get(pro);
						
						// define catalog image or custom image
						ImageView imgView = new ImageView();
						Image img;
						
						// name of product
						Text proName = new Text();
						proName.setFont(new Font(16));
						proName.setStyle("-fx-font-weight: bold");
						
						// price of product
						Text price = new Text(String.format("%.2f$", (pro.getPrice() * count)));
						price.setFont(new Font(14));
						
						VBox productDetails = null;
						
						if (pro instanceof CatalogProduct)
						{
							CatalogProductDetails proDetails = SelectProductController.catalogProductWithAdditionalDetails.get(pro);
							try
							{
								img = proDetails.catalogImage.getImageInstance();
							}
							catch (IOException e) 
							{ 
								img = proDetails.catalogImage.getImage();
							}
							
							proName.setText(((CatalogProduct)pro).getName());
							
							boolean hasSale = proDetails.catalogSale != null;
							
							// if there is a discount
							if (hasSale)
							{
								price.setStrikethrough(true);
								price.setFill(Color.RED);
								
								// add text for price after discount
								double finalPrice = getFinalPrice(pro) * count;
								Text sale = new Text(String.format("%.2f$", finalPrice));
								sale.setFill(Color.GREEN);
								sale.setFont(new Font(14));
								sale.setStyle("-fx-font-weight: bold");
								
								Text saleDis = new Text(proDetails.catalogSale.getDiscount() + "%" + " FREE!");
								saleDis.setFont(new Font(13.5));
								saleDis.setFill(Color.RED);
								saleDis.setStyle("-fx-font-weight: bold");
								
								HBox priceFields = new HBox(sale, price);
								priceFields.setSpacing(10);
								priceFields.setPadding(new Insets(0, 10, 0, 0));
								
								ImageView saleImg = new ImageView();
								try
								{
									saleImg.setImage(new Image("/sale.png"));
									saleImg.setFitWidth(30);
									saleImg.setFitHeight(30);
								}
								catch (Exception e) { }
								
								HBox proNameBox = new HBox(proName, saleImg);
								proNameBox.setSpacing(7);
								
								productDetails = new VBox(proNameBox, priceFields, saleDis);
							}
						}
						else
						{
							proName.setText("Custom product");
							img = new Image("/customProduct.png");
						}
						
						imgView.setImage(img);
						imgView.setFitWidth(70);
						imgView.setFitHeight(70);
					
						//
						price.setStyle("-fx-font-weight: bold");
						price.setFill(Color.GREEN);
						
						if (productDetails == null)
						{
							productDetails = new VBox(proName, price);
							productDetails.setSpacing(5);
						}
						
						VBox productImage = new VBox(imgView);
						productImage.setPadding(new Insets(0, 20, 0, 0));
						

					    Region region1 = new Region();
					    HBox.setHgrow(region1, Priority.ALWAYS);
					    
					  
					    Button incButton = new Button();
					    Image imageInc = new Image("addQty.png");
					    ImageView viewInc = new ImageView(imageInc);
						viewInc.setFitWidth(10);
						viewInc.setFitHeight(10);
						incButton.setGraphic(viewInc);
						incButton.setPrefWidth(12);
					
						Text qty = new Text("" + cartProducts.get(pro));
						qty.setFont(new Font(13.5));
						qty.setFill(Color.RED);
						qty.setTextAlignment(TextAlignment.CENTER);
						
						Button decButton = new Button();
						Image imageDec = new Image("minusQty.png");
				        ImageView viewDec = new ImageView(imageDec);
						viewDec.setFitWidth(10);
						viewDec.setFitHeight(10);
						decButton.setGraphic(viewDec);
						decButton.setPrefWidth(12);
					    
						decButton.setOnMouseClicked((event) -> {
							if (cartProducts.get(pro) > 1)
							{
								cartProducts.put(pro, cartProducts.get(pro) - 1);
								qty.setText("" + cartProducts.get(pro));
								updateTotalPrice();
							}
						});
						
						incButton.setOnMouseClicked((event) -> {
							cartProducts.put(pro, cartProducts.get(pro) + 1);
							qty.setText("" + cartProducts.get(pro));
							updateTotalPrice();
						});
					
						HBox qtyOptions = new HBox(decButton, qty, incButton);
						qtyOptions.setSpacing(8);
						qtyOptions.setAlignment(Pos.CENTER);

						Button delButton = new Button();
						Image imageDel = new Image("delete.png");
				        ImageView viewDel = new ImageView(imageDel);
				        viewDel.setFitWidth(20);
						viewDel.setFitHeight(20);
						delButton.setGraphic(viewDel);
						delButton.setPrefWidth(20);
						
						delButton.setOnMouseClicked((event) -> {
							cartProducts.remove(pro);
							SelectProductController.productsSelected.remove(pro);
							fillCatalogItems();
							updateTotalPrice();
						});
						
						VBox delBox = new VBox(delButton);
						delBox.setAlignment(Pos.CENTER);
						Text flowersTitle = new Text("Flowers Collection:");
						flowersTitle.setUnderline(true);
						flowersTitle.setFont(new Font(14));
						flowersTitle.setStyle("-fx-font-weight: bold");
						VBox flowers = new VBox(flowersTitle);
					
						for (FlowerInProduct fp : pro.getFlowerInProductList())
						{
							Flower flowerFound = flowerList.stream().filter(c->c.getId()==fp.getFlowerId()).findFirst().orElse(null);
							if (flowerFound != null)
							{
								Text flower = new Text(String.format("%s, Qty: %d", flowerFound.getName(), fp.getQuantity()));
								flower.setFont(new Font(13.5));
								flowers.getChildren().add(flower);
							}
						}
						
						flowers.setSpacing(2);
					 	HBox hBox = new HBox(delBox, productImage, productDetails ,region1,flowers, qtyOptions);
					 	hBox.setStyle("-fx-border-style: solid inside;"
					 	        + "-fx-border-width: 1;" + "-fx-border-insets: 5;"
					 	        + "-fx-border-radius: 5;");
	                    hBox.setSpacing(10);
	                    hBox.setPadding(new Insets(10));
	                    
	                    
	                    setGraphic(hBox);
					}

					
				    @Override
					protected void updateItem(Product item, boolean empty) {
						//super.updateItem(item, empty);
							
						 if (item != null) {	
							 	setCellHandler(item);
	                        }
					}};
				}
			});
			lstCart.setItems(data);
			
			
			lstCart.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {

	            @Override
	            public void handle(MouseEvent event) {
	            	//listViewCatalog.getSelectionModel().select(-1);
	                event.consume();
	            }
	        });
		}
		
		
		/**
		 * Calculate the final price, even after the discount if have
		 * @param pro product to get price
		 * @return final price
		 */
		private double getFinalPrice(Product pro)
		{
			DecimalFormat ceil = new DecimalFormat("#.##"); 
			CatalogProductDetails productDetails = SelectProductController.catalogProductWithAdditionalDetails.get(pro);
			if (productDetails.catalogSale == null)
				return pro.getPrice();
			
			int discount = productDetails.catalogSale.getDiscount();
			double percantage = (double)discount / 100.0;
			
			double priceAfterDiscount = pro.getPrice() * (1 - percantage);
			
			return Double.valueOf(ceil.format(priceAfterDiscount));
		}
		
		/**
		 * Update the label of total price
		 */
		private void updateTotalPrice()
		{
			totalPrice=0;
			int totalItems = 0;
			for (Map.Entry<Product, Integer> entry : cartProducts.entrySet())
			{
				if(entry.getKey() instanceof CatalogProduct)
					totalPrice += getFinalPrice((CatalogProduct)entry.getKey()) * entry.getValue();
				else
					totalPrice+=((CustomProduct)entry.getKey()).getPrice()*entry.getValue();
				totalItems += entry.getValue();
			}
			if(totalItems==0)
				btnPurchase.setDisable(true);
			lblPrice.setText(String.format("Total Price: %.2f$ , Total Items: %d", totalPrice, totalItems));
		}
		
		/**
		 * Open Catalog Viewer Form
		 */
		private void showSelectProductFrm()
		{
			try 
    		{
    			Stage primaryStage = new Stage();
    			SelectProductController catalogProductController = new SelectProductController();
    			catalogProductController.setForCart();
    			catalogProductController.start(primaryStage);
    			mainStage.close();
    		}
    		catch (Exception e) 
    		{
    			ConstantData.displayAlert(AlertType.ERROR, "Error", "Exception when trying to open Select Catalog Window", e.getMessage());
    		}
		}
	
		/**
		 * Register an event that occurs when clicking on Back To Catalog button
		 */
		public void registerBackToCatalogButton()
		{
			btnBackToCatalog.setOnAction(new EventHandler<ActionEvent>() {
	            @Override
	            public void handle(ActionEvent event) {
	            	showSelectProductFrm();
	            }
	        });
		}
		
		/**
		 * Event that occurs when closing the form
		 */
		public void onClosingForm()
		{
			try
			{
				
				if (comesFromCatalog)
				{
					showSelectProductFrm();
				}
				else
				{
					mainStage.close();
					CustomerMenuController menu = new CustomerMenuController();
					menu.start(new Stage());
				}
				
			}
			catch (Exception e) 
			{
				ConstantData.displayAlert(AlertType.ERROR, "Error", "Exception when trying to open Menu Window", e.getMessage());
			}
		}
	/**
	 * create window
	 * @param primaryStage - current stage to build
	 * @throws Exception error message
	 */
	  public void start(Stage primaryStage) throws Exception {	
		  	mainStage = primaryStage;
			String srcFXML = "/Products/CartUI.fxml";
			String srcCSS = "/Products/application.css";
			
			Parent root = FXMLLoader.load(getClass().getResource(srcFXML));
			
			Scene scene = new Scene(root);
			scene.getStylesheets().add(getClass().getResource(srcCSS).toExternalForm());
			primaryStage.setTitle("Cart Shopping");
			primaryStage.setScene(scene);		
			primaryStage.show();
			
			primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
		          public void handle(WindowEvent we) {
		            onClosingForm();
		          }
		         });
		}
	/**
	 * set up controllers and get relevant data for the window
	 */
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		getFlowers();
		registerBackToCatalogButton();
		registerCreateCustomProductBtn();
		registerPurchaseBtn();
		registerClearOrderButton();
		addProductsToCartMap(SelectProductController.productsSelected);
		updateTotalPrice();
		if(cartProducts.isEmpty())
			btnPurchase.setDisable(true);
		if (comesFromCatalog)
			btnBackToCatalog.setText("Back To Catalog");
		else
			btnBackToCatalog.setText("Add From Catalog");
	}
	private void registerPurchaseBtn() {
		btnPurchase.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				mainStage.close(); //hiding primary window
    			Stage primaryStage = new Stage();
    			OrderController orderController = new OrderController();
    			try {
					orderController.start(primaryStage);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		});
		
	}
	/**
	 * 
	 * @return totalPrice
	 */
	public static double getTotalPrice()
	{
		return totalPrice;
	}
	/**
	 * set action for clear order button
	 */
	private void registerClearOrderButton() {
		btnClearOrder.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				cartProducts.clear();
				btnPurchase.setDisable(true);
				SelectProductController.productsSelected.clear();
				fillCatalogItems();
				updateTotalPrice();
			}
		});
	}
	/**
	 * get all flowers from the server
	 */
	private void getFlowers()
	{
		Packet packet = new Packet();//create packet to send
		packet.addCommand(Command.getFlowers);//add command
		
		// create the thread for send to server the message
		SystemSender send = new SystemSender(packet);

		// register the handler that occurs when the data arrived from the server
		send.registerHandler(new IResultHandler() {

			@Override
			public void onWaitingForResult() {//waiting when send
			}

			@Override
			public void onReceivingResult(Packet p)//set combobox values
			{
				if (p.getResultState()) {
					flowerList= p.<Flower>convertedResultListForCommand(Command.getFlowers);
					fillCatalogItems();
				}
				else//if it was error in connection
					ConstantData.displayAlert(AlertType.ERROR, "Error", "Failed!", "Get flower"+p.getExceptionMessage());
			}
		});
		send.start();
	}
	/**
	 * set action for create custom product button
	 */
	private void registerCreateCustomProductBtn() {
		btnAddCustomProduct.setOnAction(new EventHandler<ActionEvent>() {
			
			@Override
			public void handle(ActionEvent event) {
				try 
	    		{
	    			mainStage.close(); //hiding primary window
	    			Stage primaryStage = new Stage();
	    			CustomProductController catalogProductController = new CustomProductController();
	    			catalogProductController.start(primaryStage);
	    		}
	    		catch (Exception e) 
	    		{
	    			ConstantData.displayAlert(AlertType.ERROR, "Error", "Exception when trying to open Select Catalog Window", e.getMessage());
	    		}
				
			}
		});
		
	}
	/**
	 * add new product if not exist
	 * @param product new product to add to the order
	 */
	public void addProductsToCartMap(Product product) {
		if (!cartProducts.containsKey(product))
			  cartProducts.put(product, 1);
	}

	
}

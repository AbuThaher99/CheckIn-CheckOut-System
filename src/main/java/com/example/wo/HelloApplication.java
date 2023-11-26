package com.example.wo;

import com.itextpdf.text.pdf.languages.LanguageProcessor;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.DoubleStringConverter;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;


import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.border.SolidBorder;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.property.TextAlignment;
import com.itextpdf.layout.property.UnitValue;
import com.itextpdf.layout.renderer.DrawContext;
import com.itextpdf.text.pdf.BidiLine;
import com.itextpdf.text.pdf.languages.ArabicLigaturizer;

public class HelloApplication extends Application {
    ObservableList<WorkingDay> workingDays = FXCollections.observableArrayList();
    ObservableList<Employee> EmployeeData = FXCollections.observableArrayList();
    ObservableList<Employee> EmployeeSalary = FXCollections.observableArrayList();
    File f = null;
    String path = null;
    private static final String CORRECT_USERNAME = "adel";
    private static final String CORRECT_PASSWORD = "adel2002";
    private Stage datePickerStage;
    private Stage LoginStage;
    DataBaseConnection db = new DataBaseConnection();


    private String showDatePickerDialog(Stage primaryStage) {
        datePickerStage = new Stage();
        datePickerStage.initModality(Modality.APPLICATION_MODAL);
        datePickerStage.setResizable(false);
        datePickerStage.initStyle(StageStyle.UTILITY);
        datePickerStage.initOwner(primaryStage);
        datePickerStage.setTitle("Select a Date");

        DatePicker datePicker = new DatePicker();
        Button okButton = new Button("OK");
        okButton.setOnAction(e -> {
            LocalDate selectedDate = datePicker.getValue();
            if (selectedDate != null) {
                System.out.println("Selected Date: " + selectedDate);
                // You can use the selected date here
            }
            datePickerStage.close();
        });

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> datePickerStage.close());

        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(okButton, cancelButton);

        VBox dialogVBox = new VBox(10);
        dialogVBox.setAlignment(Pos.CENTER);
        dialogVBox.getChildren().addAll(datePicker, buttonBox);

        Scene dialogScene = new Scene(dialogVBox, 250, 120);
        datePickerStage.setScene(dialogScene);
        datePickerStage.showAndWait();
        // make a string from the date with the day of the week
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE, yyyy-MM-dd");



        String formattedDate = datePicker.getValue().format(formatter);
        return formattedDate;
    }

   private boolean setLoginStage(Stage primaryStage) {
        LoginStage = new Stage();
        LoginStage.initModality(Modality.APPLICATION_MODAL);
        LoginStage.setResizable(false);
        LoginStage.initStyle(StageStyle.UTILITY);
        LoginStage.initOwner(primaryStage);
        LoginStage.setTitle("Login");

        Label username = new Label("Username");
        username.setLayoutX(20);
        username.setLayoutY(20);
        username.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        TextField usernameField = new TextField();
        usernameField.setLayoutX(20);
        usernameField.setLayoutY(50);
        usernameField.setPromptText("Username");
        usernameField.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        Label password = new Label("Password");
        password.setLayoutX(20);
        password.setLayoutY(80);
        password.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        PasswordField passwordField = new PasswordField();
        passwordField.setLayoutX(20);
        passwordField.setLayoutY(110);
        passwordField.setPromptText("Password");

        Button login = new Button("Login");
        login.setLayoutX(20);
        login.setLayoutY(150);

        AtomicBoolean loginSuccess = new AtomicBoolean(false);

        login.setOnAction(e -> {
            if (usernameField.getText().equals(CORRECT_USERNAME) && passwordField.getText().equals(CORRECT_PASSWORD)) {
                LoginStage.close();
                loginSuccess.set(true);
            }else {
                Alert alert = new Alert(Alert.AlertType.ERROR, "خطأ في اسم المستخدم أو كلمة المرور", ButtonType.OK);
                alert.showAndWait();
            }
        });

        Pane root = new Pane();
        root.getChildren().addAll(username, usernameField, password, passwordField, login);
        Scene scene = new Scene(root, 200, 200);
        LoginStage.setScene(scene);
        LoginStage.showAndWait();

        return loginSuccess.get();
    }
    public Employee findEmployee(String input) {
        for (Employee employee : EmployeeData) {
            if (employee.getId().equals(input) ) {
                return employee; // Found a matching employee
            }
        }
        return null; // Employee not found
    }

    private void insertCheckInTime(String employeeId, String checkInTime) {
        try {
            Connection con = db.getConnection().connectDB();
            String sql = "INSERT INTO WorkingDay (Begintime, id_Employee,dateCheckin) VALUES (?, ?,?)";

            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, checkInTime);
            preparedStatement.setString(2, employeeId);
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE, yyyy-MM-dd");
            String formattedDate = dateFormat.format(new java.util.Date());
            preparedStatement.setString(3, formattedDate);

            int rowsInserted = preparedStatement.executeUpdate();

            if (rowsInserted > 0) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("نجاح");
                successAlert.setHeaderText("تم تسجيل دخولك بنجاح");
                successAlert.showAndWait();
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }

    public void readEmployeeData() throws SQLException, ClassNotFoundException {
        try {
            Connection con = db.getConnection().connectDB();
            String sql = "SELECT * FROM Employee";
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                String id = rs.getString(1);
                String name = rs.getString(2);
                byte[] imageBytes = rs.getBytes(3); // Retrieve the image data as bytes
                double rate = rs.getDouble(4);
                boolean isWorking = rs.getBoolean(5);

                // Convert the byte array to an Image
                Image img = new Image(new ByteArrayInputStream(imageBytes));
                ImageView imageView = new ImageView(img);
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);

                EmployeeData.add(new Employee(id, name, imageView, rate, isWorking));
            }
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }
    }


    Scene Main,addEmployee,Employees, checkIn, checkOut;
    @Override
    public void start(Stage primaryStage) throws IOException, SQLException, ClassNotFoundException {
        ImageView mah1 = new ImageView(new Image("main.jpg"));
        readEmployeeData();
        Pane root = new Pane();
        Label label = new Label("welcome");
        label.setPrefWidth(154);
        label.setPrefHeight(53);
        label.setLayoutX(307);
        label.setLayoutY(76);
        label.setFont(new Font("System Bold", 36));
        // set the color of the label
        label.setStyle("-fx-text-fill: #ffffff;");


        Button check_in = new Button("تسجيل دخول   ",new ImageView(new Image("arrow.png")));
        check_in.setPrefWidth(140);
        check_in.setPrefHeight(45);
        check_in.setLayoutX(326);
        check_in.setLayoutY(195);


        check_in.setOnAction(e -> {
           //check if the employee  exists by take the id or the name from database and check if he exists appear his image and ask him if this him or not if not let him to try again

// Prompt the user to enter the employee's ID or name
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("تسجيل دخول");
            dialog.setHeaderText("الرجاء إدخال رقم الموظف ");
            dialog.setContentText("الرقم :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String input = result.get();

                // Search the EmployeeData list or database for the employee by ID or name
                Employee foundEmployee = findEmployee(input); // Implement this function to search for the employee

                if (foundEmployee != null) {
                    // Employee found, display their information and image
                  //  displayEmployeeInformation(foundEmployee);

                    // Ask for confirmation
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("تأكيد الهوية");
                    confirmationAlert.setHeaderText("هل هذا أنت؟");
                    confirmationAlert.setContentText("الاسم: " + foundEmployee.getName());
                    confirmationAlert.setGraphic(foundEmployee.getImage());

                    Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();
                  boolean check =  checkState(foundEmployee.getId());
                    if (confirmationResult.isPresent() && check==false && confirmationResult.get() == ButtonType.OK) {
                        // Employee confirmed, perform check-in actions
                        // You can add your check-in logic here
                        updataEmployeeStatus(foundEmployee.getId() , true);
                        LocalTime currentTime = LocalTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                        String currentTimeFormatted = currentTime.format(formatter);

                        insertCheckInTime(foundEmployee.getId(), currentTimeFormatted);




                    } else if(check==true) {
                        // the employee is already checked in
                        Alert alreadyCheckedInAlert = new Alert(Alert.AlertType.ERROR);
                        alreadyCheckedInAlert.setTitle("خطأ");
                        alreadyCheckedInAlert.setHeaderText("لقد سجلت بالفعل الدخول");
                        alreadyCheckedInAlert.setContentText("الرجاء تسجيل الخروج أولاً.");
                        alreadyCheckedInAlert.showAndWait();

                    }else {

                    }
                } else {
                    // Employee not found, inform the user
                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
                    notFoundAlert.setTitle("خطأ");
                    notFoundAlert.setHeaderText("لم يتم العثور على الموظف");
                    notFoundAlert.setContentText("الرجاء المحاولة مرة أخرى أو الاتصال بالإدارة.");
                    notFoundAlert.showAndWait();
                }
            }


        });

        Button check_out = new Button("تسجيل خروج   ",new  ImageView(new Image("arrow1.png")));
        check_out.setPrefWidth(140);
        check_out.setPrefHeight(45);
        check_out.setLayoutX(326);
        check_out.setLayoutY(279);

        check_out.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("تسجيل خروج");
            dialog.setHeaderText("الرجاء إدخال رقم الموظف ");
            dialog.setContentText("الرقم :");

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                String input = result.get();

                // Search the EmployeeData list or database for the employee by ID or name
                Employee foundEmployee = findEmployee(input);

                if (foundEmployee != null) {

                    // Ask for confirmation
                    Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
                    confirmationAlert.setTitle("تأكيد الهوية");
                    confirmationAlert.setHeaderText("هل هذا أنت؟");
                    confirmationAlert.setContentText("الاسم: " + foundEmployee.getName());
                    confirmationAlert.setGraphic(foundEmployee.getImage());

                    Optional<ButtonType> confirmationResult = confirmationAlert.showAndWait();


                    // Employee found, check if they are already checked in
                    boolean isCheckedIn = checkState(foundEmployee.getId());


                    if (confirmationResult.isPresent() && isCheckedIn == true && confirmationResult.get() == ButtonType.OK) {
                        // Employee is checked in, proceed with check-out
                        LocalTime currentTime = LocalTime.now();
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
                        String currentTimeFormatted = currentTime.format(formatter);

                        // Insert the check-out time into the database
                        insertCheckOutTime(foundEmployee.getId(), currentTimeFormatted);


                        updataEmployeeStatus(foundEmployee.getId(), false);

                        Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                        successAlert.setTitle("نجاح");
                        successAlert.setHeaderText("تم تسجيل خروجك بنجاح");
                        successAlert.showAndWait();
                    } else {
                        // Employee is not checked in
                        Alert notCheckedInAlert = new Alert(Alert.AlertType.ERROR);
                        notCheckedInAlert.setTitle("خطأ");
                        notCheckedInAlert.setHeaderText("لم تقم بتسجيل الدخول بعد");
                        notCheckedInAlert.setContentText("الرجاء تسجيل الدخول أولاً.");
                        notCheckedInAlert.showAndWait();
                    }
                } else {
                    // Employee not found, inform the user
                    Alert notFoundAlert = new Alert(Alert.AlertType.ERROR);
                    notFoundAlert.setTitle("خطأ");
                    notFoundAlert.setHeaderText("لم يتم العثور على الموظف");
                    notFoundAlert.setContentText("الرجاء المحاولة مرة أخرى أو الاتصال بالإدارة.");
                    notFoundAlert.showAndWait();
                }
            }
        });


        Hyperlink link = new Hyperlink("تسجيل موظف جديد");
        link.setLayoutX(52);
        link.setLayoutY(469);
        link.setOnAction(e -> {

                boolean loginSuccess = setLoginStage(primaryStage);



            if(loginSuccess == true){
                primaryStage.setScene(addEmployee);
                primaryStage.setTitle("اضافة موظف");
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "ليس لديك الصلاحية للوصول الى هذه الميزة", ButtonType.OK);
                alert.showAndWait();
            }



        });
        link.setStyle("-fx-text-fill: #000000;");
        // set the font size of the hyperlink
        link.setFont(new Font("System Bold", 18));

        Hyperlink link2 = new Hyperlink("الموظفين");
        link2.setLayoutX(700);
        link2.setLayoutY(469);
         link2.setStyle("-fx-text-fill: #000000;");
        link2.setFont(new Font("System Bold", 18));

        link2.setOnAction(e -> {
            boolean loginSuccess = setLoginStage(primaryStage);

            if(loginSuccess == true){
                primaryStage.setScene(Employees);
                primaryStage.setTitle("الموظفين");
            }else{
                Alert alert = new Alert(Alert.AlertType.ERROR, "ليس لديك الصلاحية للوصول الى هذه الميزة", ButtonType.OK);
                alert.showAndWait();
            }

        });
        root.setStyle("-fx-background-image: url('main.jpg'); -fx-background-size: cover;");
        root.getChildren().addAll(label, check_in, check_out,link,link2);
        Main = new Scene(root, 800, 521);
        Main.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
   //========================= add Employee   ==========================================================================

        Pane gridPane = new Pane();
        gridPane.setStyle("-fx-background-image: url('main.jpg'); -fx-background-size: cover;");

        Label addlable = new Label("اضافة موظف");
        addlable.setPrefWidth(225);
        addlable.setPrefHeight(45);
        addlable.setLayoutX(305);
        addlable.setLayoutY(45);
        addlable.setFont(new Font("System Bold", 36));
        // set the color of the label
        addlable.setStyle("-fx-text-fill: #ffffff;");




        Label id = new Label("الرقم الوظيفي :");
        id.setLayoutX(501);
        id.setLayoutY(172);
        id.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        TextField idField = new TextField();
        idField.setLayoutX(295);
        idField.setLayoutY(168);
        idField.setPromptText("الرقم الوظيفي");
        idField.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Label name = new Label("الاسم :");
        name.setLayoutX(501);
        name.setLayoutY(228);
        name.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        TextField nameField = new TextField();
        nameField.setLayoutX(295);
        nameField.setLayoutY(224);
        nameField.setPromptText("الاسم");
        nameField.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Label image = new Label("الصورة :");
        image.setLayoutX(501);
        image.setLayoutY(283);
        image.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        TextField imageField = new TextField();
        imageField.setLayoutX(295);
        imageField.setLayoutY(279);
        imageField.setPromptText("الصورة");
        imageField.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
        imageField.setOnMouseClicked(e->{

            try {
                FileChooser filechooser = new FileChooser();
                f = filechooser.showOpenDialog(primaryStage);

                path = f.getAbsolutePath();
                path = path.replace("\\", "\\\\");
                imageField.setText("تم اختيار الصورة");
            }catch (NullPointerException ex){
                System.out.println("no image selected");
            }



        });

        Label rate = new Label("معدل الراتب :");
        rate.setLayoutX(501);
        rate.setLayoutY(338);
        rate.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");

        TextField rateField = new TextField();
        rateField.setLayoutX(295);
        rateField.setLayoutY(334);
        rateField.setPromptText("معدل الراتب");
        rateField.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");

        Button add = new Button("اضافة",new ImageView(new Image("add.png")));
        add.setLayoutX(410);
        add.setLayoutY(387);

        add.setOnAction(e -> {
            if(idField.getText().isEmpty() || nameField.getText().isEmpty() || imageField.getText().isEmpty() || rateField.getText().isEmpty()){
                Alert alert = new Alert(Alert.AlertType.ERROR, "الرجاء ملئ جميع الحقول", ButtonType.OK);
                alert.showAndWait();

                return;
            }
            else if(EmployeesFound(idField.getText().toString()) == true){
                Alert alert = new Alert(Alert.AlertType.ERROR, "تكرار في الرقم الوظيفي حاول مرة اخرى", ButtonType.OK);
                alert.showAndWait();
            }
            else

            try {
                // Load the image from the file
                File f = new File(path);
                FileInputStream inputStream = new FileInputStream(f);

                // Prepare a SQL INSERT statement with a parameter for the image
                Connection con = db.getConnection().connectDB();
                String sql = "INSERT INTO Employee (id, name, image, rate,stat) VALUES (?, ?, ?, ?,?)";

                // Use a PreparedStatement to set parameters and execute the query
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, idField.getText());
                preparedStatement.setString(2, nameField.getText());
                preparedStatement.setBinaryStream(3, inputStream, (int) f.length()); // Set the image as a BLOB
                preparedStatement.setDouble(4, Double.parseDouble(rateField.getText()));
                preparedStatement.setBoolean(5, false);

                int rowsInserted = preparedStatement.executeUpdate();

                if (rowsInserted > 0) {
                    ImageView imageView = new ImageView(new Image(f.toURI().toString()));
                    imageView.setFitHeight(100);
                    imageView.setFitWidth(100);
                    Employee employee = new Employee(idField.getText(), nameField.getText(),imageView, Double.parseDouble(rateField.getText()) , false);
                    EmployeeData.add(employee);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم اضافة العامل", ButtonType.OK);
                    alert.showAndWait();
                    idField.clear();
                    nameField.clear();
                    imageField.clear();
                    rateField.clear();
                    path = null;
                }

                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        });

        Button back = new Button("رجوع",new ImageView(new Image("back.png")));
        back.setLayoutX(295);
        back.setLayoutY(387);
        back.setOnAction(e -> {
            primaryStage.setScene(Main);
            primaryStage.setTitle("الصفحة الرئيسية");
            idField.clear();
            nameField.clear();
            imageField.clear();
            rateField.clear();
            path = null;
        });




        gridPane.getChildren().addAll(addlable,id,idField,name,nameField,image,imageField,rate,rateField,add,back);

         addEmployee = new Scene(gridPane, 800, 521);
        addEmployee.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
//========================= Employee   ==========================================================================
        Pane employeePane = new Pane();

        employeePane.setStyle("-fx-background-image: url('main.jpg'); -fx-background-size: cover;");
        Label employeeLabel = new Label("الموظفين");
        employeeLabel.setPrefWidth(154);
        employeeLabel.setPrefHeight(53);
        employeeLabel.setLayoutX(323);
        employeeLabel.setLayoutY(32);
// set the color of the label
        employeeLabel.setStyle("-fx-text-fill: #ffffff;");
        employeeLabel.setFont(new Font("System Bold", 36));
        TableView<Employee> employeeTableView = new TableView<>();
        employeeTableView.setPrefWidth(440);
        employeeTableView.setPrefHeight(301);
        employeeTableView.setEditable(true);

        TableColumn<Employee, String> idColumn = new TableColumn<>("الرقم الوظيفي");
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        idColumn.setPrefWidth(100);


        TableColumn<Employee, String> nameColumn = new TableColumn<>("الاسم");
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameColumn.setPrefWidth(100);
        nameColumn.setCellFactory(TextFieldTableCell.forTableColumn());
        nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Employee, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Employee, String> arg0) {
                arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).setName(arg0.getNewValue());
                String id = arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).getId();
                String name = arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).getName();
                try {
                    Connection con = db.getConnection().connectDB();
                    String sql = "UPDATE Employee set Name ='" + name + "'  WHERE id='" + id + "'";
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sql);

                    con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });




        TableColumn<Employee, ImageView> imageColumn = new TableColumn<>("الصورة");
        imageColumn.setCellValueFactory(new PropertyValueFactory<>("image"));
        imageColumn.setPrefWidth(140);
        imageColumn.setCellFactory(column -> {
            TableCell<Employee, ImageView> cell = new TableCell<Employee, ImageView>() {
                @Override
                protected void updateItem(ImageView item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setGraphic(null);
                    } else {
                        setGraphic(item);
                    }
                }
            };

            // Create an image view to display the image
            ImageView imageView = new ImageView();
            // Handle edit event when the user double-clicks the image
            cell.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && !cell.isEmpty()) {
                    Employee employee = cell.getTableView().getItems().get(cell.getIndex());
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
                    File file = fileChooser.showOpenDialog(null);
                    if (file != null) {
                        Image newImage = new Image(file.toURI().toString());
                        imageView.setImage(newImage);
                        employee.setImage(new ImageView(newImage));

                        // Update the database with the new image (you need to implement this part)
                        updateEmployeeImageInDatabase(employee.getId(), file,employeeTableView);
                    }
                }
            });

            cell.setGraphic(imageView);
            return cell;
        });





        TableColumn<Employee, Double> rateColumn = new TableColumn<>("معدل الراتب");
        rateColumn.setCellValueFactory(new PropertyValueFactory<>("rate"));
        rateColumn.setPrefWidth(100);
        rateColumn.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        rateColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<Employee, Double>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<Employee, Double> arg0) {
                arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).setRate(arg0.getNewValue());
                String id = arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).getId();
                double name = arg0.getTableView().getItems().get(arg0.getTablePosition().getRow()).getRate();
                try {
                    Connection con = db.getConnection().connectDB();
                    String sql = "UPDATE Employee set rate ='" + name + "'  WHERE id='" + id + "'";
                    Statement stmt = con.createStatement();
                    stmt.executeUpdate(sql);

                    con.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new RuntimeException(ex);
                }
            }
        });


        employeeTableView.getColumns().addAll(idColumn, nameColumn,  rateColumn,imageColumn);

        employeeTableView.setItems(EmployeeData);

        employeeTableView.setLayoutX(190);
        employeeTableView.setLayoutY(110);




        Button deleteEmployeeButton = new Button("حذف موظف",new ImageView(new Image("delete.png")));
        deleteEmployeeButton.setPrefWidth(140);
        deleteEmployeeButton.setPrefHeight(45);
        deleteEmployeeButton.setLayoutX(490);
        deleteEmployeeButton.setLayoutY(425);

        deleteEmployeeButton.setOnAction(e -> {
            Employee employee = employeeTableView.getSelectionModel().getSelectedItem();
            try {
                Connection con = db.getConnection().connectDB();
                String sql = "DELETE FROM Employee WHERE id = ?";
                PreparedStatement preparedStatement = con.prepareStatement(sql);
                preparedStatement.setString(1, employee.getId());
                int rowsDeleted = preparedStatement.executeUpdate();
                if (rowsDeleted > 0) {
                    EmployeeData.remove(employee);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم حذف العامل", ButtonType.OK);
                    alert.showAndWait();
                }
                con.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException(ex);
            }
        });

        Button back2 = new Button("رجوع",new ImageView(new Image("back.png")));
        back2.setPrefWidth(140);
        back2.setPrefHeight(45);
        back2.setLayoutX(196);
        back2.setLayoutY(425);
        back2.setOnAction(e -> {
            primaryStage.setScene(Main);
            primaryStage.setTitle("الصفحة الرئيسية");
        });

        Button calculate = new Button("حساب الراتب",new ImageView(new Image("keys.png")));
        calculate.setPrefWidth(140);
        calculate.setPrefHeight(45);
        calculate.setLayoutX(343);
        calculate.setLayoutY(425);


        calculate.setOnAction(e->{
            workingDays.clear();
            if(employeeTableView.getSelectionModel().getSelectedItem() == null){
                Alert alert = new Alert(Alert.AlertType.ERROR, "الرجاء اختيار موظف", ButtonType.OK);
                alert.showAndWait();
                return;
            }
            Employee employee = employeeTableView.getSelectionModel().getSelectedItem();

            int ids = Integer.parseInt(employee.getId());

            try {
                Connection con = db.getConnection().connectDB();
                String sql = "SELECT * FROM WorkingDay WHERE id_Employee='" + ids + "'";
                Statement stmt = con.createStatement();
                ResultSet rs = stmt.executeQuery(sql);
                while (rs.next()) {
                    String beginTime = rs.getString(2);
                    String endTime = rs.getString(3);
                    String date = rs.getString(6);


                    java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("hh:mm a");
                    java.util.Date beginTimeUtil = dateFormat.parse(beginTime);
                    java.util.Date endTimeUtil = dateFormat.parse(endTime);

                    if (endTimeUtil.before(beginTimeUtil)) {
                        // Adjust the end time to be on the following day
                        endTimeUtil.setTime(endTimeUtil.getTime() + 24 * 60 * 60 * 1000);
                    }

// Convert java.util.Date to java.sql.Date
                    java.sql.Date beginTimeSql = new java.sql.Date(beginTimeUtil.getTime());
                    java.sql.Date endTimeSql = new java.sql.Date(endTimeUtil.getTime());

                    long millisecondsWorked = endTimeSql.getTime() - beginTimeSql.getTime();
                    double hoursWorked = millisecondsWorked / (60.0 * 60.0 * 1000.0);

                    double totalPayment = hoursWorked * employee.getRate();
                    workingDays.add(new WorkingDay(beginTime, endTime, totalPayment,date));
                }
                con.close();
            } catch (Exception e2) {
                System.out.println(e2);
            }


            Stage stage2 = new Stage();
            Pane root2 = new Pane();
            Label label2 = new Label("حساب الراتب");
            label2.setPrefWidth(195);
            label2.setPrefHeight(53);
            label2.setLayoutX(297);
            label2.setLayoutY(32);
            label2.setFont(new Font("System Bold", 36));
            // set the color of the label
            label2.setStyle("-fx-text-fill: #ffffff;");

            TableView<WorkingDay> workingDayTableView = new TableView<>();
            workingDayTableView.setPrefWidth(550);
            workingDayTableView.setPrefHeight(301);

            TableColumn<WorkingDay, String> beginTimeColumn = new TableColumn<>("وقت البدء");
            beginTimeColumn.setCellValueFactory(new PropertyValueFactory<>("beginTime"));
            // set the width of the column
            beginTimeColumn.setPrefWidth(100);

            TableColumn<WorkingDay, String> endTimeColumn = new TableColumn<>("وقت الانتهاء");
            endTimeColumn.setCellValueFactory(new PropertyValueFactory<>("endTime"));
            endTimeColumn.setPrefWidth(100);

            TableColumn<WorkingDay, Double> totalPaymentColumn = new TableColumn<>("المجموع");
            totalPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("totalPayment"));
            totalPaymentColumn.setPrefWidth(100);


            TableColumn<WorkingDay, String> dateColumn = new TableColumn<>("التاريخ");
            dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));
            dateColumn.setPrefWidth(150);

            TableColumn<WorkingDay,Double> totalHoursColumn = new TableColumn<>("الساعات الكلية");
            totalHoursColumn.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
            totalHoursColumn.setPrefWidth(100);



            Button back3 = new Button("اضافة تسجيل",new ImageView(new Image("add.png")));
            back3.setLayoutX(209);
            back3.setLayoutY(460);
            back3.setPrefWidth(140);
            back3.setPrefHeight(45);






            workingDayTableView.getColumns().addAll(beginTimeColumn, endTimeColumn,totalHoursColumn ,dateColumn,totalPaymentColumn);

            workingDayTableView.setItems(workingDays);

            workingDayTableView.setLayoutX(125);
            workingDayTableView.setLayoutY(153);

            Label lebel3 = new Label("الراتب الكلي :");
            lebel3.setStyle("-fx-text-fill: #ffffff; -fx-font-weight: bold;");
            lebel3.setLayoutX(670);
            lebel3.setLayoutY(110);

            TextField totalPayment = new TextField();
            totalPayment.setLayoutX(420);
            totalPayment.setLayoutY(97);
            // set the text color to black bold
            totalPayment.setStyle("-fx-text-fill: #000000; -fx-font-weight: bold;");
            totalPayment.setEditable(false);
            double sum = 0;
            for (WorkingDay workingDay : workingDays) {
                try {
                    sum += workingDay.getTotalPayment();
                } catch (ParseException ex) {
                    throw new RuntimeException(ex);
                }
                totalPayment.setText(String.valueOf(sum));
            }



            back3.setOnAction(e3 -> {

                // Create input dialogs for begin and end times
                TextInputDialog beginTimeDialog = new TextInputDialog();
                beginTimeDialog.setHeaderText("Enter Begin Time (hh:mm a):");
                Optional<String> beginTimeResult = beginTimeDialog.showAndWait();

                TextInputDialog endTimeDialog = new TextInputDialog();
                endTimeDialog.setHeaderText("Enter End Time (hh:mm a):");
                Optional<String> endTimeResult = endTimeDialog.showAndWait();



                if (beginTimeResult.isPresent() && endTimeResult.isPresent()) {
                    try {
                        String beginTime = beginTimeResult.get();
                        String endTime = endTimeResult.get();

                        // Parse the times and calculate salary
                        java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("hh:mm a");
                        java.util.Date beginTimeUtil = dateFormat.parse(beginTime);
                        java.util.Date endTimeUtil = dateFormat.parse(endTime);

                        if (endTimeUtil.before(beginTimeUtil)) {
                            // Adjust the end time to be on the following day
                            endTimeUtil.setTime(endTimeUtil.getTime() + 24 * 60 * 60 * 1000);
                        }

                        java.sql.Date beginTimeSql = new java.sql.Date(beginTimeUtil.getTime());
                        java.sql.Date endTimeSql = new java.sql.Date(endTimeUtil.getTime());

                        long millisecondsWorked = endTimeSql.getTime() - beginTimeSql.getTime();
                        double hoursWorked = millisecondsWorked / (60.0 * 60.0 * 1000.0);

                        double totalPayments = hoursWorked * employee.getRate();

                        // Get the current date
                  //      String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                   String currentDate =   showDatePickerDialog(stage2);

                        // Add the entry to the workingDays list
                        workingDays.add(new WorkingDay(beginTime, endTime, totalPayments, currentDate));

                        workingDayTableView.setItems(workingDays);
                        // add to the database
                        try {
                            Connection con = db.getConnection().connectDB();


                            // Prepare an SQL UPDATE statement to set the image data
                            String updateSql = "INSERT INTO WorkingDay (Begintime, Endtime, totalPayment, id_Employee, dateCheckin) VALUES (?, ?, ?, ?, ?)";
                            PreparedStatement preparedStatement = con.prepareStatement(updateSql);
                            preparedStatement.setString(1, beginTime);
                            preparedStatement.setString(2, endTime);
                            preparedStatement.setDouble(3, totalPayments);
                            preparedStatement.setInt(4, ids);
                            preparedStatement.setString(5, currentDate);
                            int rowsUpdated = preparedStatement.executeUpdate();

                            if (rowsUpdated > 0) {
                                Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم اضافة التسجيل", ButtonType.OK);
                                alert.showAndWait();
                            }


                            con.close();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            throw new RuntimeException(ex);
                        }




                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                }

                double sums = 0;
                for (WorkingDay workingDay : workingDays) {
                    try {
                        sums += workingDay.getTotalPayment();
                    } catch (ParseException ex) {
                        throw new RuntimeException(ex);
                    }
                    totalPayment.setText(String.valueOf(sums));
                }

            });


            Button pay = new Button("دفع",new ImageView(new Image("pay.png")));
            pay.setLayoutX(460);
            pay.setLayoutY(460);
            pay.setPrefWidth(140);
            pay.setPrefHeight(45);

            pay.setOnAction(ws->{
                try {
                    Connection con = db.getConnection().connectDB();

                    String sql = "DELETE FROM WorkingDay WHERE id_Employee = ?";
                    PreparedStatement preparedStatement = con.prepareStatement(sql);
                    preparedStatement.setString(1, employee.getId());
                    int rowsDeleted = preparedStatement.executeUpdate();
                    if (rowsDeleted > 0) {
                        Alert alert = new Alert(Alert.AlertType.INFORMATION, "تم دفع الراتب", ButtonType.OK);
                        alert.showAndWait();
                    }
                    con.close();
                    workingDays.clear();
                    workingDayTableView.refresh();


                }catch (Exception esc){
                    esc.printStackTrace();
                }





            });

            root2.setStyle("-fx-background-image: url('main.jpg'); -fx-background-size: cover;");
            root2.getChildren().addAll(label2, workingDayTableView, lebel3, totalPayment,back3,pay);
            Scene scene2 = new Scene(root2, 800, 521);
            scene2.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

            stage2.setScene(scene2);
            stage2.getIcons().add(new Image("candidates.png"));
            stage2.setResizable(false);
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initOwner(primaryStage);
            stage2.setTitle("حساب الراتب");
            stage2.show();


        });

        Hyperlink link3 = new Hyperlink("حساب جميع الرواتب");
        link3.setLayoutX(32);
        link3.setLayoutY(470);
        link3.setStyle("-fx-text-fill: #000000;");
        link3.setFont(new Font("System Bold", 18));
        link3.setOnAction(e->{
            EmployeeSalary.clear();

            try {
                Connection con = db.getConnection().connectDB();
                String selectQuery = "SELECT w.id,w.id_Employee, w.Begintime, w.Endtime, e.rate FROM WorkingDay w INNER JOIN Employee e ON w.id_Employee = e.id";
                 Statement statement = con.createStatement();
                 ResultSet rs = statement.executeQuery(selectQuery);
                 while (rs.next()){
                     int workid = rs.getInt("id");
                     String idEmployee = rs.getString("id_Employee");
                     String beginTime = rs.getString("Begintime");
                     String endTime = rs.getString("Endtime");
                     double rateEm = rs.getDouble("rate");

                     java.text.SimpleDateFormat dateFormat = new java.text.SimpleDateFormat("hh:mm a");
                     java.util.Date beginTimeUtil = dateFormat.parse(beginTime);
                     java.util.Date endTimeUtil = dateFormat.parse(endTime);

                     if (endTimeUtil.before(beginTimeUtil)) {
                         endTimeUtil.setTime(endTimeUtil.getTime() + 24 * 60 * 60 * 1000);
                     }

                     java.sql.Date beginTimeSql = new java.sql.Date(beginTimeUtil.getTime());
                     java.sql.Date endTimeSql = new java.sql.Date(endTimeUtil.getTime());

                     long millisecondsWorked = endTimeSql.getTime() - beginTimeSql.getTime();
                     double hoursWorked = millisecondsWorked / (60.0 * 60.0 * 1000.0);

                     double totalPayment = hoursWorked * rateEm;

                     setEmplysalary(workid ,  totalPayment,hoursWorked);


                 }
                 con.close();



            } catch (Exception ec) {
                System.out.println(ec);
            }

            try {
                Connection con = db.getConnection().connectDB();

                // SQL query to group and sum totalPayment by employee
                String query = "SELECT e.Name, w.id_Employee, SUM(w.totalPayment) AS totalSalary ,SUM(w.hoursWorked) AS totalHourSalary  FROM WorkingDay w INNER JOIN Employee e ON w.id_Employee = e.id GROUP BY w.id_Employee, e.Name";

                Statement statement = con.createStatement();
                ResultSet rs = statement.executeQuery(query);

                while (rs.next()) {
                    String names = rs.getString("Name");
                    double totalSalary = rs.getDouble("totalSalary");
                    double totalHourSalary = rs.getDouble("totalHourSalary");


                        EmployeeSalary.add(new Employee(names,totalSalary,totalHourSalary));

                }

                con.close();
            } catch (Exception ec) {
                System.out.println(ec);
            }



            Pane root2 = new Pane();
            Label label2 = new Label("حساب الرواتب");
            label2.setPrefWidth(225);
            label2.setPrefHeight(53);
            label2.setLayoutX(297);
            label2.setLayoutY(32);
            label2.setFont(new Font("System Bold", 36));

            // set the color of the label
            label2.setStyle("-fx-text-fill: #ffffff;");

            TableView<Employee> workingDayTableView = new TableView<>();
            workingDayTableView.setPrefWidth(421);
            workingDayTableView.setPrefHeight(301);

            // the column name and the total payment

            TableColumn<Employee, String> nameColumns = new TableColumn<>("الاسم");
            nameColumns.setCellValueFactory(new PropertyValueFactory<>("name"));
            // set the width of the column
            nameColumns.setPrefWidth(140);

            TableColumn<Employee, Double> totalHoursColumn = new TableColumn<>("الساعات الكلية");
            totalHoursColumn.setCellValueFactory(new PropertyValueFactory<>("totalHours"));
            totalHoursColumn.setPrefWidth(140);

            TableColumn<Employee, Double> totalPaymentColumn = new TableColumn<>("الراتب الكلي");
            totalPaymentColumn.setCellValueFactory(new PropertyValueFactory<>("totalPayment"));
            totalPaymentColumn.setPrefWidth(140);


            workingDayTableView.getColumns().addAll(nameColumns,totalHoursColumn,totalPaymentColumn);

            workingDayTableView.setItems(EmployeeSalary);

            workingDayTableView.setLayoutX(190);
            workingDayTableView.setLayoutY(150);

            Button back3 = new Button("دفع الرواتب",new ImageView(new Image("pay.png")));
            back3.setLayoutX(325);
            back3.setLayoutY(460);
            back3.setPrefWidth(140);
            back3.setPrefHeight(45);
            back3.setOnAction(es->{

                String desktopPath = System.getProperty("user.home") + "/Desktop/";
                // get the current date
                String currentDate = new SimpleDateFormat("yyyy-MM-dd").format(new java.util.Date());
                String pdfFilePath = desktopPath + "SalaryReport-"+currentDate+".pdf";

                try {
                    // Create a PDF document
                    PdfDocument pdfDocument = new PdfDocument(new PdfWriter(pdfFilePath));
                    Document document = new Document(pdfDocument, PageSize.A4);

                    // Create a table
                    Table table = new Table(new float[]{2, 2, 2}); // 3 columns

                    // Set up Arabic font for table content
                    PdfFont arabicFont = PdfFontFactory.createFont("C:\\ARIALUNI.TTF", "Identity-H", true);

                    // Add headers to the table
                    table.addCell(new Cell().add(new Paragraph("Name")).setBold().setBorder(new SolidBorder(1)));
                    table.addCell(new Cell().add(new Paragraph("Total Hours")).setBold().setBorder(new SolidBorder(1)));
                    table.addCell(new Cell().add(new Paragraph("Total Payment")).setBold().setBorder(new SolidBorder(1)));

                    // Add data to the table
                    for (Employee employee : EmployeeSalary) {
                        table.addCell(new Cell().add(new Paragraph(employee.getName()).setFont(arabicFont)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(employee.getTotalHours())).setFont(arabicFont)));
                        table.addCell(new Cell().add(new Paragraph(String.valueOf(employee.getTotalPayment())).setFont(arabicFont)));
                    }

                    document.add(table);

                    // Close the document
                    document.close();

                    System.out.println("PDF report saved as SalaryReport.pdf on the desktop.");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                // Delete all the data from the database
                try {
                    Connection con = db.getConnection().connectDB();
                    // You can use a DELETE statement to delete all records in the table
                    String deleteQuery = "DELETE FROM WorkingDay";
                    Statement deleteStatement = con.createStatement();
                    int rowsDeleted = deleteStatement.executeUpdate(deleteQuery);

                    if (rowsDeleted > 0) {
                        System.out.println("All data deleted from the database.");
                    }
                    con.close();
                } catch (Exception ex) {
                    System.out.println(ex);
                }

                // Clear the TableView
                EmployeeSalary.clear();
            });

            // set the seceen
            root2.setStyle("-fx-background-image: url('main.jpg'); -fx-background-size: cover;");
            root2.getChildren().addAll(label2, workingDayTableView, back3);
            Scene scene2 = new Scene(root2, 800, 521);
            scene2.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
            Stage stage2 = new Stage();
            stage2.setTitle("حساب الرواتب");
            stage2.getIcons().add(new Image("candidates.png"));
            stage2.setResizable(false);
            stage2.initModality(Modality.APPLICATION_MODAL);
            stage2.initOwner(primaryStage);
            stage2.setScene(scene2);
            stage2.show();



        });



        employeePane.getChildren().addAll(employeeLabel, employeeTableView, deleteEmployeeButton , back2,calculate,link3);
        Employees = new Scene(employeePane, 800, 521);
        Employees.getStylesheets().add(getClass().getResource("style.css").toExternalForm());


        primaryStage.setScene(Main);
        primaryStage.setTitle("الصفحة الرئيسية");
        primaryStage.getIcons().add(new Image("candidates.png"));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    private void updateEmployeeImageInDatabase(String employeeId, File newImageFile, TableView<Employee> yourTableView) {
        try {
            Connection con = db.getConnection().connectDB();

            // Read the new image data
            FileInputStream inputStream = new FileInputStream(newImageFile);

            // Prepare an SQL UPDATE statement to set the image data
            String updateSql = "UPDATE Employee SET image = ? WHERE id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(updateSql);
            preparedStatement.setBinaryStream(1, inputStream, (int) newImageFile.length());
            preparedStatement.setString(2, employeeId);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                Alert successAlert = new Alert(Alert.AlertType.INFORMATION);
                successAlert.setTitle("نجاح");
                successAlert.setHeaderText("تم تحديث صورة الموظف بنجاح");
                successAlert.showAndWait();

                // Read the updated image from the file
                Image newImage = new Image(newImageFile.toURI().toString());

                ImageView imageView = new ImageView(newImage);
                imageView.setFitHeight(100);
                imageView.setFitWidth(100);
                for (Employee employee : EmployeeData) {
                    if (employee.getId().equals(employeeId)) {
                        employee.setImage(imageView);
                        break;
                    }
                }

                // Update the TableView
                yourTableView.refresh(); // Replace 'yourTableView' with your actual TableView object
            } else {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR);
                errorAlert.setTitle("خطأ");
                errorAlert.setHeaderText("فشل تحديث صورة الموظف");
                errorAlert.showAndWait();
            }

            con.close();
            path = null;
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }

    private boolean EmployeesFound(String idField) {
        try {
            Connection con = db.getConnection().connectDB();
            String sql = "SELECT id FROM Employee WHERE id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, idField);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                String id = rs.getString(1);
                if(id.equals(idField)){
                    return true;
                }
            }
            con.close();
        } catch (Exception e) {
            System.out.println(e);

        }
        return false;
    }

    private void setEmplysalary(int idEmployee, double totalPayment,double hoursWorked) {
        try{

            Connection con = db.getConnection().connectDB();
            String sql = "UPDATE WorkingDay SET totalPayment = ? , hoursWorked = ? WHERE id = ?";

            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setDouble(1, totalPayment);
            preparedStatement.setDouble(2, hoursWorked);
            preparedStatement.setInt(3, idEmployee);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Employee status updated successfully");
            }

            con.close();



        }catch (Exception w){
            System.out.println(w);
        }


    }

    private boolean checkState(String id) {
        // return the state of the employee from database

        try {
            Connection con = db.getConnection().connectDB();
            String sql = "SELECT stat FROM Employee WHERE id = ?";
            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, id);
            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                boolean state = rs.getBoolean(1);
                System.out.println("The State "+state);
                return state;
            }
            con.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return false;

    }
    private void updataEmployeeStatus(String id,boolean state) {
        // Update the employee's status to true

        try {
            Connection con = db.getConnection().connectDB();
            String sql = "UPDATE Employee SET stat = ? WHERE id = ?";

            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setBoolean(1, state);
            preparedStatement.setString(2, id);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Employee status updated successfully");
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }

    }
    private void insertCheckOutTime(String employeeId, String checkOutTime) {
        try {
            Connection con = db.getConnection().connectDB();
            String sql = "UPDATE WorkingDay SET Endtime = ? WHERE id_Employee = ? AND Endtime IS NULL";

            PreparedStatement preparedStatement = con.prepareStatement(sql);
            preparedStatement.setString(1, checkOutTime);
            preparedStatement.setString(2, employeeId);

            int rowsUpdated = preparedStatement.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Check-out time updated successfully");
            }

            con.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException(ex);
        }
    }
    public static void main(String[] args) {
        launch();
    }
}

package finance.app;

import finance.model.Expense;
import finance.model.ExpenseCategory;
import finance.service.ExpenseManager;
import finance.service.ExcelExporter;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.List;

/**
 * Главный класс графического приложения для учета личных расходов.
 *
 * <p>Предоставляет JavaFX интерфейс для:
 * <ul>
 *   <li>Добавления новых расходов с выбором даты</li>
 *   <li>Просмотра статистики по категориям с процентным соотношением</li>
 *   <li>Просмотра расходов за конкретную дату</li>
 *   <li>Экспорта данных в Excel по различным критериям</li>
 * </ul>
 *
 * <p>Наследуется от класса {@link javafx.application.Application} и
 * использует архитектуру MVC для разделения логики и представления.
 *
 */
public class MainFX extends Application {
    private static final Logger logger = LogManager.getLogger(MainFX.class);
    private static final ExpenseManager manager = new ExpenseManager();
    private Stage primaryStage;

    /**
     * Точка входа в графическое приложение.
     *
     * <p>Инициализирует главное окно приложения и запускает отображение главного меню.
     *
     * @param primaryStage главное окно приложения
     */
    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Personal Expense Tracker");

        showMainMenu();
    }

    /**
     * Отображает главное меню приложения с основными опциями.
     *
     * <p>Создает центральное меню с кнопками для всех основных функций приложения:
     * добавление расходов, просмотр статистики, просмотр по дате, экспорт в Excel.
     */
    private void showMainMenu() {
        VBox root = new VBox(15);
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);

        Label title = new Label("PERSONAL EXPENSE TRACKER");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        Button addBtn = createStyledButton("Add Expense");
        Button statsBtn = createStyledButton("View Statistics");
        Button viewDateBtn = createStyledButton("View Expenses by Date");
        Button exportBtn = createStyledButton("Export to Excel");
        Button exitBtn = createStyledButton("Exit");

        addBtn.setOnAction(e -> showAddExpenseForm());
        statsBtn.setOnAction(e -> showStatistics());
        viewDateBtn.setOnAction(e -> showExpensesByDateDialog());
        exportBtn.setOnAction(e -> showExportMenu());
        exitBtn.setOnAction(e -> primaryStage.close());

        root.getChildren().addAll(title, addBtn, statsBtn, viewDateBtn, exportBtn, exitBtn);

        Scene scene = new Scene(root, 400, 450);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /**
     * Создает стилизованную кнопку с эффектом подсветки при наведении.
     *
     * @param text текст для отображения на кнопке
     * @return настроенная кнопка с заданным текстом
     */
    private Button createStyledButton(String text) {
        Button button = new Button(text);
        button.setPrefSize(200, 40);
        button.setStyle(
                "-fx-background-color: #4a90e2; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        );
        button.setOnMouseEntered(e -> button.setStyle(
                "-fx-background-color: #3a70b2; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        ));
        button.setOnMouseExited(e -> button.setStyle(
                "-fx-background-color: #4a90e2; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 14px; " +
                        "-fx-font-weight: bold;"
        ));
        return button;
    }

    /**
     * Отображает форму для добавления нового расхода.
     *
     * <p>Создает диалоговое окно с полями для ввода:
     * <ul>
     *   <li>Суммы расхода</li>
     *   <li>Категории расхода</li>
     *   <li>Дата расхода (с возможностью выбора любой даты)</li>
     *   <li>Описание расхода</li>
     * </ul>
     */
    private void showAddExpenseForm() {
        Stage formStage = new Stage();
        formStage.setTitle("Add Expense");

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setVgap(10);
        grid.setHgap(10);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount in rubles");

        ComboBox<ExpenseCategory> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(ExpenseCategory.values());
        categoryCombo.setPromptText("Select category");

        TextField dateField = new TextField();
        dateField.setPromptText("Date (YYYY-MM-DD), leave empty for today");
        dateField.setText(LocalDate.now().toString()); // Предзаполнение текущей датой

        TextField descField = new TextField();
        descField.setPromptText("Description (optional)");

        Button saveBtn = createStyledButton("Save");
        Button cancelBtn = createStyledButton("Cancel");

        saveBtn.setOnAction(e -> {
            try {
                double amount = Double.parseDouble(amountField.getText());
                ExpenseCategory category = categoryCombo.getValue();
                String description = descField.getText();
                String dateInput = dateField.getText().trim();

                if (amount <= 0) {
                    showError("Error", "Amount must be positive");
                    return;
                }

                if (category == null) {
                    showError("Error", "Select a category");
                    return;
                }

                LocalDate expenseDate;
                if (dateInput.isEmpty()) {
                    expenseDate = LocalDate.now();
                } else {
                    try {
                        expenseDate = LocalDate.parse(dateInput);
                    } catch (java.time.format.DateTimeParseException ex) {
                        showError("Error", "Invalid date format. Use YYYY-MM-DD");
                        return;
                    }
                }

                if (expenseDate.isAfter(LocalDate.now())) {
                    showError("Error", "Cannot add expense for future date");
                    return;
                }

                Expense expense = new Expense(amount, category, expenseDate, description);
                manager.addExpense(expense);
                showInfo("Success", "Expense successfully added:\n" + expense);
                formStage.close();
            } catch (NumberFormatException ex) {
                showError("Error", "Enter a valid number");
            } catch (Exception ex) {
                showError("Error", ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> formStage.close());

        // Добавляем поле для даты
        grid.add(new Label("Amount (rub.):"), 0, 0);
        grid.add(amountField, 1, 0);
        grid.add(new Label("Category:"), 0, 1);
        grid.add(categoryCombo, 1, 1);
        grid.add(new Label("Date:"), 0, 2);
        grid.add(dateField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descField, 1, 3);
        grid.add(saveBtn, 0, 4);
        grid.add(cancelBtn, 1, 4);

        Scene scene = new Scene(grid, 400, 300);
        formStage.setScene(scene);
        formStage.show();
    }

    /**
     * Отображает статистику по всем расходам.
     *
     * <p>Показывает общую сумму расходов и детализацию по категориям
     * с процентным соотношением для каждой категории.
     */
    private void showStatistics() {
        Stage statsStage = new Stage();
        statsStage.setTitle("Expense Statistics");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        double total = manager.getTotalExpenses();
        Label totalLabel = new Label(String.format("Total expenses: %.2f rub.", total));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        vbox.getChildren().add(totalLabel);

        if (total > 0) {
            vbox.getChildren().add(new Label("\nExpenses by category:"));

            var stats = manager.getCategoryStatistics();
            stats.entrySet().stream()
                    .sorted((e1, e2) -> Double.compare(e2.getValue(), e1.getValue()))
                    .forEach(entry -> {
                        double percentage = (entry.getValue() / total) * 100;
                        Label label = new Label(String.format("- %s: %.2f rub. (%.1f%%)",
                                entry.getKey(), entry.getValue(), percentage));
                        vbox.getChildren().add(label);
                    });
        } else {
            vbox.getChildren().add(new Label("No expenses recorded yet"));
        }

        Button closeBtn = createStyledButton("Close");
        closeBtn.setOnAction(e -> statsStage.close());
        vbox.getChildren().add(closeBtn);

        Scene scene = new Scene(vbox, 450, 400);
        statsStage.setScene(scene);
        statsStage.show();
    }

    /**
     * Отображает диалог для ввода даты для просмотра расходов.
     *
     * <p>Создает окно с полем ввода даты и кнопкой для подтверждения.
     * После ввода корректной даты вызывает метод для отображения расходов за эту дату.
     */
    private void showExpensesByDateDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("View Expenses by Date");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label label = new Label("Enter date (YYYY-MM-DD):");
        label.setStyle("-fx-font-weight: bold;");

        TextField dateField = new TextField();
        dateField.setPromptText("Example: 2025-12-08");

        Button viewBtn = createStyledButton("View");
        Button cancelBtn = createStyledButton("Cancel");

        viewBtn.setOnAction(e -> {
            try {
                String dateStr = dateField.getText().trim();
                if (dateStr.isEmpty()) {
                    showError("Error", "Date cannot be empty");
                    return;
                }
                LocalDate date = LocalDate.parse(dateStr);
                showExpensesForDate(date);
                dialog.close();
            } catch (Exception ex) {
                showError("Error", "Invalid date format. Use YYYY-MM-DD");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        vbox.getChildren().addAll(label, dateField, viewBtn, cancelBtn);

        Scene scene = new Scene(vbox, 350, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Отображает расходы за указанную дату.
     *
     * <p>Создает окно со списком всех расходов за указанную дату,
     * включая их описание, категорию и сумму.
     *
     * @param date дата для фильтрации расходов
     */
    private void showExpensesForDate(LocalDate date) {
        Stage statsStage = new Stage();
        statsStage.setTitle("Expenses for " + date);

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label dateLabel = new Label("Expenses for " + date);
        dateLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        vbox.getChildren().add(dateLabel);

        List<Expense> expenses = manager.getExpensesByDate(date);
        double total = manager.getTotalExpensesByDate(date);

        if (expenses.isEmpty()) {
            vbox.getChildren().add(new Label("No expenses found for this date"));
        } else {
            vbox.getChildren().add(new Label(String.format("Total: %.2f rub.", total)));
            vbox.getChildren().add(new Label("\nExpenses:"));

            for (Expense expense : expenses) {
                Label expenseLabel = new Label(String.format("- %s in %s: %.2f rub.",
                        expense.getDescription().isEmpty() ? "No description" : expense.getDescription(),
                        expense.getCategory(),
                        expense.getAmount()));
                vbox.getChildren().add(expenseLabel);
            }
        }

        Button closeBtn = createStyledButton("Close");
        closeBtn.setOnAction(e -> statsStage.close());
        vbox.getChildren().add(closeBtn);

        ScrollPane scrollPane = new ScrollPane(vbox);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane, 450, 400);
        statsStage.setScene(scene);
        statsStage.show();
    }

    /**
     * Отображает меню для выбора типа экспорта в Excel.
     *
     * <p>Предоставляет три варианта экспорта:
     * <ul>
     *   <li>Все расходы</li>
     *   <li>Расходы по выбранной категории</li>
     *   <li>Расходы за выбранную дату</li>
     * </ul>
     */
    private void showExportMenu() {
        Stage exportStage = new Stage();
        exportStage.setTitle("Export to Excel");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));
        vbox.setAlignment(Pos.CENTER);

        Label label = new Label("Select export type:");
        label.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Button allBtn = createStyledButton("All expenses");
        Button categoryBtn = createStyledButton("By category");
        Button dateBtn = createStyledButton("By date");
        Button closeBtn = createStyledButton("Cancel");

        allBtn.setOnAction(e -> {
            try {
                List<Expense> expenses = manager.getAllExpenses();
                if (expenses.isEmpty()) {
                    showError("Error", "No expenses to export");
                    return;
                }
                String path = ExcelExporter.exportExpenses(expenses);
                showInfo("Success", "Data exported to:\n" + path);
            } catch (Exception ex) {
                showError("Export error", ex.getMessage());
            }
        });

        categoryBtn.setOnAction(e -> showExportByCategoryDialog());
        dateBtn.setOnAction(e -> showExportByDateDialog());
        closeBtn.setOnAction(e -> exportStage.close());

        vbox.getChildren().addAll(label, allBtn, categoryBtn, dateBtn, closeBtn);

        Scene scene = new Scene(vbox, 350, 350);
        exportStage.setScene(scene);
        exportStage.show();
    }

    /**
     * Отображает диалог для выбора категории при экспорте в Excel.
     *
     * <p>Создает окно с выпадающим списком категорий и кнопкой экспорта.
     */
    private void showExportByCategoryDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Export by category");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label label = new Label("Select category for export:");
        label.setStyle("-fx-font-weight: bold;");

        ComboBox<ExpenseCategory> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll(ExpenseCategory.values());
        categoryCombo.setPromptText("Select category");

        Button exportBtn = createStyledButton("Export");
        Button cancelBtn = createStyledButton("Cancel");

        exportBtn.setOnAction(e -> {
            try {
                ExpenseCategory category = categoryCombo.getValue();
                if (category == null) {
                    showError("Error", "Select a category");
                    return;
                }
                List<Expense> expenses = manager.getAllExpenses();
                String path = ExcelExporter.exportByCategory(expenses, category);
                showInfo("Success", String.format("Data for category %s exported to:\n%s", category, path));
                dialog.close();
            } catch (Exception ex) {
                showError("Export error", ex.getMessage());
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        vbox.getChildren().addAll(label, categoryCombo, exportBtn, cancelBtn);

        Scene scene = new Scene(vbox, 350, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Отображает диалог для выбора даты при экспорте в Excel.
     *
     * <p>Создает окно с полем ввода даты и кнопкой экспорта.
     */
    private void showExportByDateDialog() {
        Stage dialog = new Stage();
        dialog.setTitle("Export by date");

        VBox vbox = new VBox(15);
        vbox.setPadding(new Insets(20));

        Label label = new Label("Enter date for export (YYYY-MM-DD):");
        label.setStyle("-fx-font-weight: bold;");

        TextField dateField = new TextField();
        dateField.setPromptText("Example: 2025-12-08");

        Button exportBtn = createStyledButton("Export");
        Button cancelBtn = createStyledButton("Cancel");

        exportBtn.setOnAction(e -> {
            try {
                String dateStr = dateField.getText().trim();
                if (dateStr.isEmpty()) {
                    showError("Error", "Enter a date");
                    return;
                }
                LocalDate date = LocalDate.parse(dateStr);
                List<Expense> expenses = manager.getAllExpenses();
                String path = ExcelExporter.exportByDate(expenses, date);
                showInfo("Success", String.format("Data for %s exported to:\n%s", date, path));
                dialog.close();
            } catch (Exception ex) {
                showError("Error", "Invalid date format. Use YYYY-MM-DD");
            }
        });

        cancelBtn.setOnAction(e -> dialog.close());

        vbox.getChildren().addAll(label, dateField, exportBtn, cancelBtn);

        Scene scene = new Scene(vbox, 350, 200);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Отображает всплывающее окно с сообщением об ошибке.
     *
     * @param title заголовок окна ошибки
     * @param message текст сообщения об ошибке
     */
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Отображает всплывающее окно с информационным сообщением.
     *
     * @param title заголовок информационного окна
     * @param message текст информационного сообщения
     */
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    /**
     * Статический метод для запуска JavaFX приложения.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
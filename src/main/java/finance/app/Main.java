package finance.app;

import finance.model.Expense;
import finance.model.ExpenseCategory;
import finance.service.ExpenseManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import finance.service.ExcelExporter;
import java.io.IOException;
import java.nio.file.Path;

/**
 * Главный класс приложения для учета личных расходов.
 *
 * <p>Предоставляет консольный интерфейс для управления расходами:
 * <ul>
 *   <li>Добавление новых расходов</li>
 *   <li>Просмотр статистики по категориям</li>
 *   <li>Фильтрация расходов по дате</li>
 *   <li>Экспорт данных в Excel</li>
 * </ul>
 *
 */

public class Main {
    private static final Logger logger = LogManager.getLogger(Main.class);
    private static final ExpenseManager manager = new ExpenseManager();
    private static Scanner scanner;

    /**
     * Точка входа в приложение.
     *
     * <p>Инициализирует сканер для чтения пользовательского ввода,
     * настраивает логирование и запускает главное меню.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        scanner = new Scanner(System.in);
        logger.info("Application started");
        showMainMenu();
        scanner.close();
    }

    /**
     * Отображает главное меню приложения и обрабатывает выбор пользователя.
     *
     * <p>Предоставляет 5 опций:
     * <ul>
     *   <li>Добавить расход</li>
     *   <li>Просмотреть статистику</li>
     *   <li>Просмотреть расходы по дате</li>
     *   <li>Экспортировать данные в Excel</li>
     *   <li>Выход из приложения</li>
     * </ul>
     */
    private static void showMainMenu() {
        boolean running = true;

        while (running) {
            System.out.println("\n=== PERSONAL EXPENSE TRACKER ===");
            System.out.println("1. Add expense");
            System.out.println("2. View statistics");
            System.out.println("3. View expenses by date");
            System.out.println("4. Export to Excel");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            try {
                if (!scanner.hasNextLine()) {
                    System.out.println("\nInput stream closed unexpectedly. Exiting.");
                    break;
                }

                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Please enter a number.");
                    continue;
                }

                int choice;
                try {
                    choice = Integer.parseInt(input);
                } catch (NumberFormatException e) {
                    System.out.println("Invalid input. Please enter a number.");
                    continue;
                }

                switch (choice) {
                    case 1:
                        addExpense();
                        break;
                    case 2:
                        showStatistics();
                        break;
                    case 3:
                        showExpensesByDate();
                        break;
                    case 4:
                        exportToExcel();
                        break;
                    case 5:
                        running = false;
                        logger.info("Application terminated by user");
                        break;
                    default:
                        System.out.println("Invalid option. Please choose 1-5.");
                }
            } catch (Exception e) {
                logger.error("Error in main menu", e);
                System.out.println("An unexpected error occurred: " + e.getMessage());
                System.out.println("Please try again.");

                if (scanner != null && scanner.ioException() != null) {
                    System.out.println("Input stream corrupted. Reinitializing...");
                    scanner.close();
                    scanner = new Scanner(System.in);
                }
            }
        }
    }

    /**
     * Добавляет новый расход в систему с возможностью выбора даты.
     *
     * <p>Запрашивает у пользователя:
     * <ul>
     *   <li>Сумму расхода</li>
     *   <li>Категорию расхода</li>
     *   <li>Дату расхода (опционально, по умолчанию сегодняшняя)</li>
     *   <li>Описание расхода (опционально)</li>
     * </ul>
     * <p>Выполняет валидацию введенных данных перед добавлением.
     */
    private static void addExpense() {
        System.out.println("\n--- ADDING NEW EXPENSE ---");

        try {
            Double amount = null;
            while (amount == null) {
                System.out.print("Enter amount: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Amount cannot be empty");
                    continue;
                }

                try {
                    amount = Double.parseDouble(input);
                    if (amount <= 0) {
                        System.out.println("Amount must be positive");
                        amount = null;
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Please use format like: 10.50");
                }
            }

            System.out.println("\nSelect category:");
            ExpenseCategory[] categories = ExpenseCategory.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.printf("%d. %s%n", i + 1, categories[i]);
            }

            ExpenseCategory category = null;
            while (category == null) {
                System.out.print("Choose category number: ");
                String input = scanner.nextLine().trim();

                if (input.isEmpty()) {
                    System.out.println("Category selection cannot be empty");
                    continue;
                }

                try {
                    int categoryIndex = Integer.parseInt(input) - 1;
                    if (categoryIndex < 0 || categoryIndex >= categories.length) {
                        System.out.println("Please enter a number between 1 and " + categories.length);
                        continue;
                    }
                    category = categories[categoryIndex];
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Please enter a number.");
                }
            }

            // Запрос даты расхода
            LocalDate expenseDate = LocalDate.now();
            System.out.print("Enter date for expense (YYYY-MM-DD) or press Enter for today: ");
            String dateInput = scanner.nextLine().trim();
            if (!dateInput.isEmpty()) {
                try {
                    expenseDate = LocalDate.parse(dateInput);
                    if (expenseDate.isAfter(LocalDate.now())) {
                        System.out.println("Warning: Date is in the future. Is this intentional? (y/n): ");
                        String confirm = scanner.nextLine().trim();
                        if (!confirm.equalsIgnoreCase("y")) {
                            expenseDate = LocalDate.now();
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Invalid date format. Using today's date.");
                }
            }

            System.out.print("Enter description (optional): ");
            String description = scanner.nextLine().trim();
            Expense expense = new Expense(amount, category, expenseDate, description);
            manager.addExpense(expense);

            System.out.println("\nExpense added successfully!");
            System.out.println(expense);
        } catch (Exception e) {
            logger.error("Error adding expense", e);
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    /**
     * Отображает статистику расходов по категориям.
     *
     * <p>Показывает:
     * <ul>
     *   <li>Общую сумму всех расходов</li>
     *   <li>Сумму расходов по каждой категории</li>
     *   <li>Процентное соотношение каждой категории от общего расхода</li>
     * </ul>
     */
    private static void showStatistics() {
        System.out.println("\n--- EXPENSE STATISTICS ---");

        double total = manager.getTotalExpenses();
        System.out.printf("Total expenses: $%.2f%n", total);

        if (total == 0) {
            System.out.println("No expenses recorded yet.");
            return;
        }

        System.out.println("\nExpenses by category:");
        Map<ExpenseCategory, Double> stats = manager.getCategoryStatistics();

        stats.entrySet().stream()
                .sorted(Map.Entry.<ExpenseCategory, Double>comparingByValue().reversed())
                .forEach(entry -> {
                    double percentage = (entry.getValue() / total) * 100;
                    System.out.printf("- %s: $%.2f (%.1f%%)%n",
                            entry.getKey(), entry.getValue(), percentage);
                });
    }

    /**
     * Отображает расходы за указанную дату.
     *
     * <p>Запрашивает у пользователя дату в формате YYYY-MM-DD
     * и показывает все расходы за эту дату с общей суммой.
     */
    private static void showExpensesByDate() {
        System.out.println("\n--- EXPENSES BY DATE ---");
        System.out.print("Enter date (YYYY-MM-DD): ");

        try {
            String dateString = scanner.nextLine().trim();

            if (dateString.isEmpty()) {
                System.out.println("Date cannot be empty");
                return;
            }

            LocalDate date = LocalDate.parse(dateString);

            List<Expense> expenses = manager.getExpensesByDate(date);
            double total = manager.getTotalExpensesByDate(date);

            System.out.printf("\nExpenses for %s:%n", date);
            if (expenses.isEmpty()) {
                System.out.println("No expenses found for this date.");
            } else {
                System.out.printf("Total: $%.2f%n", total);
                System.out.println("------------------------");
                for (Expense expense : expenses) {
                    System.out.println(expense);
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing date", e);
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
        }
    }

    /**
     * Отображает расходы за указанную дату.
     *
     * <p>Запрашивает у пользователя дату в формате YYYY-MM-DD
     * и показывает все расходы за эту дату с общей суммой.
     */
    private static void exportToExcel() {
        System.out.println("\n--- EXPORT TO EXCEL ---");
        System.out.println("1. Export all expenses");
        System.out.println("2. Export by category");
        System.out.println("3. Export by date");
        System.out.print("Choose export option: ");

        try {
            int choice = scanner.nextInt();
            scanner.nextLine();

            List<Expense> allExpenses = manager.getAllExpenses();

            switch (choice) {
                case 1:
                    if (allExpenses.isEmpty()) {
                        System.out.println("No expenses to export.");
                        return;
                    }
                    exportAllExpenses(allExpenses);
                    break;
                case 2:
                    exportByCategory(allExpenses);
                    break;
                case 3:
                    exportByDate(allExpenses);
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-3.");
            }
        } catch (Exception e) {
            logger.error("Error in export menu", e);
            System.out.println("Error: " + e.getMessage());
            System.out.println("Please try again.");
        }
    }

    /**
     * Экспортирует все расходы в Excel-файл.
     *
     * <p>Использует класс ExcelExporter для создания файла
     * в директории 'exports' с автоматически сгенерированным именем.
     *
     * @param expenses список всех расходов для экспорта
     */
    private static void exportAllExpenses(List<Expense> expenses) {
        try {
            String filePath = ExcelExporter.exportExpenses(expenses);
            System.out.println("\nSuccessfully exported all expenses!");
            System.out.println("File created: " + filePath);
            System.out.println("You can open this file with Excel or any spreadsheet application.");
        } catch (IOException e) {
            logger.error("Failed to export all expenses", e);
            System.out.println("Export failed: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Экспортирует расходы по выбранной категории в Excel-файл.
     *
     * <p>Запрашивает у пользователя выбор категории и использует
     * класс ExcelExporter для создания файла.
     *
     * @param expenses список всех расходов для фильтрации
     */
    private static void exportByCategory(List<Expense> expenses) {
        try {
            // Выбор категории
            System.out.println("\nSelect category to export:");
            ExpenseCategory[] categories = ExpenseCategory.values();
            for (int i = 0; i < categories.length; i++) {
                System.out.printf("%d. %s%n", i + 1, categories[i]);
            }
            System.out.print("Choose category number: ");

            int categoryIndex = scanner.nextInt() - 1;
            scanner.nextLine(); // Очистка буфера

            if (categoryIndex < 0 || categoryIndex >= categories.length) {
                System.out.println("Invalid category number.");
                return;
            }

            ExpenseCategory category = categories[categoryIndex];

            try {
                String filePath = ExcelExporter.exportByCategory(expenses, category);
                System.out.println("\nSuccessfully exported expenses for category: " + category);
                System.out.println("File created: " + filePath);
            } catch (IOException e) {
                logger.error("Failed to export category {}", category, e);
                System.out.println("Export failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error exporting by category", e);
            System.out.println("Error: " + e.getMessage());
        }
    }

    /**
     * Экспортирует расходы за указанную дату в Excel-файл.
     *
     * <p>Запрашивает у пользователя дату и использует
     * класс ExcelExporter для создания файла.
     *
     * @param expenses список всех расходов для фильтрации
     */
    private static void exportByDate(List<Expense> expenses) {
        try {
            System.out.print("\nEnter date to export (YYYY-MM-DD): ");
            String dateString = scanner.nextLine().trim();

            if (dateString.isEmpty()) {
                System.out.println("Date cannot be empty");
                return;
            }

            LocalDate date = LocalDate.parse(dateString);

            try {
                String filePath = ExcelExporter.exportByDate(expenses, date);
                System.out.println("\nSuccessfully exported expenses for date: " + date);
                System.out.println("File created: " + filePath);
            } catch (IOException e) {
                logger.error("Failed to export date {}", date, e);
                System.out.println("Export failed: " + e.getMessage());
            } catch (IllegalArgumentException e) {
                System.out.println(e.getMessage());
            }
        } catch (Exception e) {
            logger.error("Error exporting by date", e);
            System.out.println("Invalid date format. Please use YYYY-MM-DD format.");
        }
    }
}
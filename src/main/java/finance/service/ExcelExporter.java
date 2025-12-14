package finance.service;

import finance.model.Expense;
import finance.model.ExpenseCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Класс для экспорта данных о расходах в Excel-файлы с использованием Apache POI
 *
 * <p>Предоставляет функциональность для экспорта всех расходов или расходов по конкретной категории
 * в формате XLSX с автоматическим форматированием таблицы.</p>
 */
public class ExcelExporter {
    private static final Logger logger = LogManager.getLogger(ExcelExporter.class);
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DEFAULT_EXPORT_DIR = "exports";

    /**
     * Создает директорию для экспорта, если она не существует
     *
     * @throws IOException если не удалось создать директорию
     */
    private static void ensureExportDirectoryExists() throws IOException {
        Path exportDir = Paths.get(DEFAULT_EXPORT_DIR);
        if (!Files.exists(exportDir)) {
            Files.createDirectories(exportDir);
            logger.info("Created export directory: {}", exportDir.toAbsolutePath());
        }
    }

    /**
     * Генерирует имя файла на основе текущей даты и времени
     *
     * @param prefix префикс имени файла
     * @return сгенерированное имя файла в формате "prefix_YYYY-MM-DD_HH-mm-ss.xlsx"
     */
    private static String generateFileName(String prefix) {
        String timestamp = java.time.LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss"));
        return String.format("%s/%s_%s.xlsx", DEFAULT_EXPORT_DIR, prefix, timestamp);
    }

    /**
     * Создает стиль для заголовков таблицы
     *
     * @param workbook рабочая книга Excel
     * @return стиль для заголовков
     */
    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);

        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);

        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }

    /**
     * Создает стиль для денежных значений
     *
     * @param workbook рабочая книга Excel
     * @return стиль для денежных значений
     */
    private static CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        CreationHelper createHelper = workbook.getCreationHelper();
        style.setDataFormat(createHelper.createDataFormat().getFormat("#,##0.00"));

        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);

        return style;
    }

    /**
     * Создает стиль для обычных ячеек
     *
     * @param workbook рабочая книга Excel
     * @return стиль для обычных ячеек
     */
    private static CellStyle createDefaultCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        return style;
    }

    /**
     * Создает заголовок таблицы
     *
     * @param sheet лист Excel
     */
    private static void createHeaderRow(Sheet sheet) {
        Row headerRow = sheet.createRow(0);
        Workbook workbook = sheet.getWorkbook();
        CellStyle headerStyle = createHeaderStyle(workbook);

        String[] headers = {"Date", "Amount", "Category", "Description"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }

    /**
     * Заполняет строки данными о расходах
     *
     * @param sheet лист Excel
     * @param expenses список расходов для экспорта
     */
    private static void fillDataRows(Sheet sheet, List<Expense> expenses) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle currencyStyle = createCurrencyStyle(workbook);
        CellStyle defaultStyle = createDefaultCellStyle(workbook);

        for (int i = 0; i < expenses.size(); i++) {
            Expense expense = expenses.get(i);
            Row row = sheet.createRow(i + 1);

            // Date column
            Cell dateCell = row.createCell(0);
            dateCell.setCellValue(expense.getDate().format(DATE_FORMAT));
            dateCell.setCellStyle(defaultStyle);

            // Amount column
            Cell amountCell = row.createCell(1);
            amountCell.setCellValue(expense.getAmount());
            amountCell.setCellStyle(currencyStyle);

            // Category column
            Cell categoryCell = row.createCell(2);
            categoryCell.setCellValue(expense.getCategory().toString());
            categoryCell.setCellStyle(defaultStyle);

            // Description column
            Cell descCell = row.createCell(3);
            descCell.setCellValue(expense.getDescription());
            descCell.setCellStyle(defaultStyle);
        }
    }

    /**
     * Автоматически подбирает ширину колонок
     *
     * @param sheet лист Excel
     */
    private static void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < 4; i++) {
            sheet.autoSizeColumn(i);
            // Устанавливаем минимальную ширину для лучшего отображения
            if (sheet.getColumnWidth(i) < 2000) {
                sheet.setColumnWidth(i, 3000);
            }
        }
    }

    /**
     * Экспортирует список расходов в Excel-файл
     *
     * <p>Создает файл в директории 'exports' с автоматически сгенерированным именем.
     * Если директория не существует, она будет создана.</p>
     *
     * @param expenses список расходов для экспорта
     * @return путь к созданному файлу
     * @throws IOException если возникла ошибка при создании или записи файла
     * @throws NullPointerException если список расходов равен null
     * @throws IllegalArgumentException если список расходов пуст
     */
    public static String exportExpenses(List<Expense> expenses) throws IOException {
        if (expenses == null) {
            throw new NullPointerException("Expenses list cannot be null");
        }
        if (expenses.isEmpty()) {
            throw new IllegalArgumentException("Expenses list cannot be empty");
        }

        ensureExportDirectoryExists();
        String filePath = generateFileName("all_expenses");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("All Expenses");
            createHeaderRow(sheet);
            fillDataRows(sheet, expenses);
            autoSizeColumns(sheet);

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            logger.info("Successfully exported {} expenses to {}", expenses.size(), filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to export expenses to Excel file: {}", filePath, e);
            throw new IOException("Error exporting to Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Экспортирует расходы по конкретной категории в Excel-файл
     *
     * <p>Создает файл в директории 'exports' с автоматически сгенерированным именем,
     * содержащим название категории.</p>
     *
     * @param expenses список всех расходов
     * @param category категория для фильтрации
     * @return путь к созданному файлу
     * @throws IOException если возникла ошибка при создании или записи файла
     * @throws NullPointerException если список расходов или категория равны null
     * @throws IllegalArgumentException если в списке нет расходов указанной категории
     */
    public static String exportByCategory(List<Expense> expenses, ExpenseCategory category) throws IOException {
        if (expenses == null) {
            throw new NullPointerException("Expenses list cannot be null");
        }
        if (category == null) {
            throw new NullPointerException("Category cannot be null");
        }

        // Фильтруем расходы по категории
        List<Expense> filteredExpenses = expenses.stream()
                .filter(expense -> expense.getCategory() == category)
                .toList();

        if (filteredExpenses.isEmpty()) {
            throw new IllegalArgumentException("No expenses found for category: " + category);
        }

        ensureExportDirectoryExists();
        String safeCategoryName = category.toString().replaceAll("[\\\\/:*?\"<>|]", "_");
        String filePath = generateFileName("category_" + safeCategoryName);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(safeCategoryName);
            createHeaderRow(sheet);
            fillDataRows(sheet, filteredExpenses);
            autoSizeColumns(sheet);

            // Добавляем сводную информацию
            Row summaryRow = sheet.createRow(filteredExpenses.size() + 2);
            summaryRow.createCell(0).setCellValue("Total for category:");
            summaryRow.createCell(1).setCellValue(filteredExpenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum());
            summaryRow.getCell(1).setCellStyle(createCurrencyStyle(workbook));

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            logger.info("Exported {} expenses for category {} to {}",
                    filteredExpenses.size(), category, filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to export category {} to Excel file: {}", category, filePath, e);
            throw new IOException("Error exporting category to Excel: " + e.getMessage(), e);
        }
    }

    /**
     * Экспортирует расходы за конкретную дату в Excel-файл
     *
     * <p>Создает файл в директории 'exports' с автоматически сгенерированным именем,
     * содержащим дату.</p>
     *
     * @param expenses список всех расходов
     * @param date дата для фильтрации
     * @return путь к созданному файлу
     * @throws IOException если возникла ошибка при создании или записи файла
     * @throws NullPointerException если список расходов или дата равны null
     * @throws IllegalArgumentException если в списке нет расходов за указанную дату
     */
    public static String exportByDate(List<Expense> expenses, java.time.LocalDate date) throws IOException {
        if (expenses == null) {
            throw new NullPointerException("Expenses list cannot be null");
        }
        if (date == null) {
            throw new NullPointerException("Date cannot be null");
        }

        // Фильтруем расходы по дате
        List<Expense> filteredExpenses = expenses.stream()
                .filter(expense -> expense.getDate().isEqual(date))
                .toList();

        if (filteredExpenses.isEmpty()) {
            throw new IllegalArgumentException("No expenses found for date: " + date);
        }

        ensureExportDirectoryExists();
        String formattedDate = date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String filePath = generateFileName("date_" + formattedDate);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Date: " + formattedDate);
            createHeaderRow(sheet);
            fillDataRows(sheet, filteredExpenses);
            autoSizeColumns(sheet);

            // Добавляем сводную информацию
            Row summaryRow = sheet.createRow(filteredExpenses.size() + 2);
            summaryRow.createCell(0).setCellValue("Total for date:");
            summaryRow.createCell(1).setCellValue(filteredExpenses.stream()
                    .mapToDouble(Expense::getAmount)
                    .sum());
            summaryRow.getCell(1).setCellStyle(createCurrencyStyle(workbook));

            try (FileOutputStream fileOut = new FileOutputStream(filePath)) {
                workbook.write(fileOut);
            }

            logger.info("Exported {} expenses for date {} to {}",
                    filteredExpenses.size(), date, filePath);
            return filePath;
        } catch (IOException e) {
            logger.error("Failed to export date {} to Excel file: {}", date, filePath, e);
            throw new IOException("Error exporting date to Excel: " + e.getMessage(), e);
        }
    }
}
package finance.service;

import finance.model.Expense;
import finance.model.ExpenseCategory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Класс для управления списком расходов и получения статистики.
 *
 * <p>Предоставляет функциональность для:
 * <ul>
 *   <li>Добавления новых расходов</li>
 *   <li>Получения общей суммы расходов</li>
 *   <li>Получения статистики по категориям</li>
 *   <li>Фильтрации расходов по дате</li>
 * </ul>
 *
 * <p>Все суммы в рублях, формат даты dd-MM-yyyy</p>
 *
 * <p>Пример использования:
 * <pre>{@code
 * ExpenseManager manager = new ExpenseManager();
 * manager.addExpense(new Expense(100.0, ExpenseCategory.PRODUCTS, LocalDate.now(), "Продукты"));
 * double total = manager.getTotalExpenses();
 * Map<ExpenseCategory, Double> stats = manager.getCategoryStatistics();
 * }</pre>
 * @see Expense
 * @see ExpenseCategory
 */
public class ExpenseManager {
    private static final Logger logger = LogManager.getLogger(ExpenseManager.class);
    private final List<Expense> expenses = new ArrayList<>();

    /**
     * Добавляет новую запись о расходах.
     *
     * <p>Выполняет валидацию входных данных и логирует операцию на уровне INFO.</p>
     *
     * @param expense объект расхода для добавления
     * @throws NullPointerException если параметр expense равен null
     * @throws IllegalArgumentException если сумма расхода меньше или равна нулю
     * @see #getAllExpenses()
     */
    public void addExpense(Expense expense) {
        if (expense == null) {
            logger.error("Attempted to add null expense");
            throw new NullPointerException("Expense cannot be null");
        }

        if (expense.getAmount() <= 0) {
            logger.error("Attempted to add expense with non-positive amount: {}", expense.getAmount());
            throw new IllegalArgumentException("Amount must be positive");
        }

        expenses.add(expense);
        logger.info("Added new expense: {} руб. in category {}", expense.getAmount(), expense.getCategory());
    }

    /**
     * Возвращает общую сумму всех расходов.
     *
     * <p>Если расходов нет, возвращает 0.0.</p>
     *
     * @return общая сумма расходов в рублях
     * @see #getCategoryStatistics()
     */
    public double getTotalExpenses() {
        double total = expenses.stream()
                .mapToDouble(Expense::getAmount)
                .sum();
        logger.debug("Calculated total expenses: {} руб.", total);
        return total;
    }

    /**
     * Возвращает общую сумму расходов за указанную дату.
     *
     * <p>Выполняет фильтрацию расходов по дате и суммирование.</p>
     *
     * @param date дата для фильтрации расходов
     * @return сумма расходов за указанную дату в рублях
     * @throws NullPointerException если параметр date равен null
     * @see #getExpensesByDate(LocalDate)
     */
    public double getTotalExpensesByDate(LocalDate date) {
        if (date == null) {
            logger.error("Attempted to get expenses by null date");
            throw new NullPointerException("Date cannot be null");
        }

        double total = expenses.stream()
                .filter(expense -> expense.getDate().isEqual(date))
                .mapToDouble(Expense::getAmount)
                .sum();

        logger.debug("Calculated total expenses for date {}: {} руб.", date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")), total);
        return total;
    }

    /**
     * Возвращает статистику расходов по категориям.
     *
     * <p>Возвращает отображение категории на сумму расходов в этой категории.</p>
     *
     * @return Map с категориями и соответствующими суммами расходов в рублях
     * @see #getTotalExpenses()
     */
    public Map<ExpenseCategory, Double> getCategoryStatistics() {
        Map<ExpenseCategory, Double> stats = expenses.stream()
                .collect(Collectors.groupingBy(
                        Expense::getCategory,
                        Collectors.summingDouble(Expense::getAmount)
                ));

        logger.debug("Calculated category statistics for {} expenses", expenses.size());
        return stats;
    }

    /**
     * Возвращает неизменяемый список всех расходов.
     *
     * <p>Возвращает копию внутреннего списка для предотвращения модификации данных извне.</p>
     *
     * @return список всех расходов
     * @see #getExpensesByDate(LocalDate)
     */
    public List<Expense> getAllExpenses() {
        logger.debug("Returning all expenses (count: {})", expenses.size());
        return Collections.unmodifiableList(new ArrayList<>(expenses));
    }

    /**
     * Возвращает список расходов за указанную дату.
     *
     * <p>Возвращает неизменяемый список расходов, отфильтрованных по дате.</p>
     *
     * @param date дата для фильтрации расходов
     * @return список расходов за указанную дату
     * @throws NullPointerException если параметр date равен null
     * @see #getTotalExpensesByDate(LocalDate)
     */
    public List<Expense> getExpensesByDate(LocalDate date) {
        if (date == null) {
            logger.error("Attempted to get expenses by null date");
            throw new NullPointerException("Date cannot be null");
        }

        List<Expense> filteredExpenses = expenses.stream()
                .filter(expense -> expense.getDate().isEqual(date))
                .collect(Collectors.toList());

        logger.debug("Found {} expenses for date {}", filteredExpenses.size(), date.format(java.time.format.DateTimeFormatter.ofPattern("dd-MM-yyyy")));
        return Collections.unmodifiableList(filteredExpenses);
    }

    /**
     * Возвращает количество расходов.
     *
     * @return количество записей о расходах
     */
    public int getExpenseCount() {
        return expenses.size();
    }
}
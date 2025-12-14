package finance.model;

import java.time.LocalDate;

/**
 * Класс для представления одной записи о расходах.
 */

public class Expense {
    private final double amount;
    private final ExpenseCategory category;
    private final LocalDate date;
    private final String description;

    /**
     * Конструктор для создания записи о расходах
     *
     * @param amount сумма расхода
     * @param category категория расхода
     * @param date дата расхода
     * @param description описание расхода
     * @throws IllegalArgumentException если сумма больше/равна 0 или категория/дата не указаны
     */

    public Expense(double amount, ExpenseCategory category, LocalDate date, String description) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
        if (category == null) {
            throw new IllegalArgumentException("Category cannot be null");
        }
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }

        this.amount = amount;
        this.category = category;
        this.date = date;
        this.description = description != null ? description : "";
    }

    /**
     * Возвращает сумму расхода.
     *
     * @return сумма расхода в рублях
     */
    public double getAmount() {
        return amount;
    }

    /**
     * Возвращает категорию расхода.
     *
     * @return категория расхода
     */
    public ExpenseCategory getCategory() {
        return category;
    }

    /**
     * Возвращает дату расхода.
     *
     * @return дата расхода
     */
    public LocalDate getDate() {
        return date;
    }

    /**
     * Возвращает описание расхода.
     *
     * @return описание расхода или пустая строка, если описание отсутствует
     */
    public String getDescription() {
        return description;
    }

    /**
     * Возвращает строковое представление расхода.
     *
     * @return строка в формате "Expense: X rub., Category: Y, Date: Z, Description: W"
     */
    @Override
    public String toString() {
        return String.format("Expense: %.2f rub., Category: %s, Date: %s, Description: %s",
                amount, category, date, description.isEmpty() ? "No description" : description);
    }
}
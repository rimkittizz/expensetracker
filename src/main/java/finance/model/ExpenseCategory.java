package finance.model;

/**
 * Enum, представляющий категории личных расходов.
 *
 * <p>Содержит 15 предопределенных категорий для классификации расходов.
 * </p>
 *
 * <p>Пример использования:
 * <pre>{@code
 * ExpenseCategory category = ExpenseCategory.PRODUCTS;
 * System.out.println(category); // Выведет: "Products"
 * }</pre>
 */

public enum ExpenseCategory {
    PRODUCTS("Products"), //Продукты
    CAFE("Cafe and Restaurants"), //Кафе и рестораны
    TAXI("Taxi"), //Такси
    PUBLIC_TRANSPORT("Public Transport"), //Общ. транспорт
    INTERNET("Internet and Mobile Communications"), //Мобильная свзяь и интернет
    CLOTHES("Clothes and Shoes"), //Одежда и обувь
    ELECTRONICS("Electronics"), //Техника
    BEAUTY("Beauty and Health"), //Красота и здоровье
    SPORT("Sport and Fitness"), //Спорт и фитнесс
    UTILITIES("Utilities"), //ЖКХ
    EDUCATION("Education"), //Образование
    CAR("Car"), //Автомобиль
    ENTERTAINMENT("Entertainment"), //Развлечения
    CHARITY("Charity"), //Благотворительность
    OTHER("Other"); //Прочее

    private final String displayName;

    ExpenseCategory(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
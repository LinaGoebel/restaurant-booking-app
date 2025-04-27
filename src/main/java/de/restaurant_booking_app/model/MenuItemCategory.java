package de.restaurant_booking_app.model;

public enum MenuItemCategory {
    APPETIZER("Закуски"),
    SOUP("Супы"),
    SALAD("Салаты"),
    MAIN_COURSE("Основные блюда"),
    SIDE_DISH("Гарниры"),
    DESSERT("Десерты"),
    BEVERAGE("Напитки"),
    ALCOHOL("Алкогольные напитки"),
    SPECIAL("Фирменные блюда");

    private final String displayName;

    MenuItemCategory(String displayName) {
        this.displayName = displayName;
    }

}

package de.restaurant_booking_app.repository;

import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;


public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    // Поиск блюд по категории
    List<MenuItem> findByCategory(MenuItemCategory category);

    // Поиск доступных блюд
    List<MenuItem> findByAvailableTrue();

    // Поиск доступных блюд по категории
    List<MenuItem> findByCategoryAndAvailableTrue(MenuItemCategory category);

    // Поиск вегетарианских блюд
    List<MenuItem> findByIsVegetarianTrue();

    // Поиск веганских блюд
    List<MenuItem> findByIsVeganTrue();

    // Поиск безглютеновых блюд
    List<MenuItem> findByIsGlutenFreeTrue();

    // Поиск блюд по названию или описанию (для поиска)
    List<MenuItem> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String description);

    // Поиск специальных предложений (фирменных блюд)
    @Query("SELECT m FROM MenuItem m WHERE m.category = de.restaurant_booking_app.model.MenuItemCategory.SPECIAL")
    List<MenuItem> findSpecialOffers();

    // Получение популярных блюд на основе заказов
    @Query(value = "SELECT mi.* FROM menu_items mi " +
            "JOIN order_details od ON mi.id = od.menu_item_id " +
            "GROUP BY mi.id " +
            "ORDER BY COUNT(od.id) DESC " +
            "LIMIT :limit",
            nativeQuery = true)
    List<MenuItem> findPopularItems(@Param("limit") int limit);
}

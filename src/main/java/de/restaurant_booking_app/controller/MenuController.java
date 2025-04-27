package de.restaurant_booking_app.controller;

import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import de.restaurant_booking_app.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menu")
@Slf4j
public class MenuController {

    private final MenuService menuService;

    @Autowired
    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Получение всего меню
     */
    @GetMapping
    public ResponseEntity<List<MenuItem>> getAllMenuItems(
            @RequestParam(required = false) Boolean available) {
        log.info("Запрос на получение всех пунктов меню, available={}", available);

        List<MenuItem> menuItems;
        if (available != null && available) {
            menuItems = menuService.getAvailableMenuItems();
        } else {
            menuItems = menuService.getAllMenuItems();
        }

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Получение меню по категориям
     */
    @GetMapping("/by-category")
    public ResponseEntity<Map<MenuItemCategory, List<MenuItem>>> getMenuByCategories() {
        log.info("Запрос на получение меню по категориям");
        return ResponseEntity.ok(menuService.getMenuByCategories());
    }

    /**
     * Получение пункта меню по ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<MenuItem> getMenuItemById(@PathVariable Long id) {
        log.info("Запрос на получение пункта меню с ID: {}", id);
        return ResponseEntity.ok(menuService.getMenuItemById(id));
    }

    /**
     * Получение пунктов меню по категории
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<List<MenuItem>> getMenuItemsByCategory(
            @PathVariable MenuItemCategory category,
            @RequestParam(required = false) Boolean available) {
        log.info("Запрос на получение пунктов меню категории {}, available={}", category, available);

        List<MenuItem> menuItems;
        if (available != null && available) {
            menuItems = menuService.getAvailableMenuItemsByCategory(category);
        } else {
            menuItems = menuService.getMenuItemsByCategory(category);
        }

        return ResponseEntity.ok(menuItems);
    }

    /**
     * Поиск блюд
     */
    @GetMapping("/search")
    public ResponseEntity<List<MenuItem>> searchMenuItems(@RequestParam String query) {
        log.info("Поиск блюд по запросу: {}", query);
        return ResponseEntity.ok(menuService.searchMenuItems(query));
    }

    /**
     * Получение вегетарианских блюд
     */
    @GetMapping("/vegetarian")
    public ResponseEntity<List<MenuItem>> getVegetarianItems() {
        log.info("Запрос на получение вегетарианских блюд");
        return ResponseEntity.ok(menuService.getVegetarianItems());
    }

    /**
     * Получение веганских блюд
     */
    @GetMapping("/vegan")
    public ResponseEntity<List<MenuItem>> getVeganItems() {
        log.info("Запрос на получение веганских блюд");
        return ResponseEntity.ok(menuService.getVeganItems());
    }

    /**
     * Получение безглютеновых блюд
     */
    @GetMapping("/gluten-free")
    public ResponseEntity<List<MenuItem>> getGlutenFreeItems() {
        log.info("Запрос на получение безглютеновых блюд");
        return ResponseEntity.ok(menuService.getGlutenFreeItems());
    }

    /**
     * Получение популярных блюд
     */
    @GetMapping("/popular")
    public ResponseEntity<List<MenuItem>> getPopularItems(
            @RequestParam(defaultValue = "5") int limit) {
        log.info("Запрос на получение топ-{} популярных блюд", limit);
        return ResponseEntity.ok(menuService.getPopularItems(limit));
    }

    /**
     * Создание нового пункта меню
     */
    @PostMapping
    public ResponseEntity<MenuItem> createMenuItem(@RequestBody MenuItem menuItem) {
        log.info("Создание нового пункта меню: {}", menuItem.getName());
        MenuItem createdMenuItem = menuService.createMenuItem(menuItem);
        return new ResponseEntity<>(createdMenuItem, HttpStatus.CREATED);
    }

    /**
     * Обновление пункта меню
     */
    @PutMapping("/{id}")
    public ResponseEntity<MenuItem> updateMenuItem(
            @PathVariable Long id,
            @RequestBody MenuItem menuItem) {
        log.info("Обновление пункта меню с ID: {}", id);
        return ResponseEntity.ok(menuService.updateMenuItem(id, menuItem));
    }

    /**
     * Изменение доступности пункта меню
     */
    @PatchMapping("/{id}/toggle-availability")
    public ResponseEntity<MenuItem> toggleMenuItemAvailability(@PathVariable Long id) {
        log.info("Изменение доступности пункта меню с ID: {}", id);
        return ResponseEntity.ok(menuService.toggleMenuItemAvailability(id));
    }

    /**
     * Удаление пункта меню
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMenuItem(@PathVariable Long id) {
        log.info("Удаление пункта меню с ID: {}", id);
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}

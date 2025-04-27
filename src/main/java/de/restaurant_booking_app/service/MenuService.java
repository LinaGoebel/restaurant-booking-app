package de.restaurant_booking_app.service;

import de.restaurant_booking_app.exception.ResourceNotFoundException;
import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import de.restaurant_booking_app.repository.MenuItemRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;

    @Autowired
    public MenuService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    /**
     * Получение всех пунктов меню
     */
    public List<MenuItem> getAllMenuItems() {
        log.debug("Запрос на получение всех пунктов меню");
        return menuItemRepository.findAll();
    }

    /**
     * Получение всех доступных пунктов меню
     */
    public List<MenuItem> getAvailableMenuItems() {
        log.debug("Запрос на получение доступных пунктов меню");
        return menuItemRepository.findByAvailableTrue();
    }

    /**
     * Получение пункта меню по ID
     */
    public MenuItem getMenuItemById(Long id) {
        log.debug("Запрос на получение пункта меню с ID: {}", id);
        return menuItemRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Пункт меню с ID {} не найден", id);
                    return new ResourceNotFoundException("Пункт меню с ID " + id + " не найден");
                });
    }

    /**
     * Получение пунктов меню по категории
     */
    public List<MenuItem> getMenuItemsByCategory(MenuItemCategory category) {
        log.debug("Запрос на получение пунктов меню категории: {}", category);
        return menuItemRepository.findByCategory(category);
    }

    /**
     * Получение доступных пунктов меню по категории
     */
    public List<MenuItem> getAvailableMenuItemsByCategory(MenuItemCategory category) {
        log.debug("Запрос на получение доступных пунктов меню категории: {}", category);
        return menuItemRepository.findByCategoryAndAvailableTrue(category);
    }

    /**
     * Получение меню по категориям (для отображения на сайте)
     */
    public Map<MenuItemCategory, List<MenuItem>> getMenuByCategories() {
        log.debug("Запрос на получение меню, сгруппированного по категориям");
        List<MenuItem> availableItems = menuItemRepository.findByAvailableTrue();

        return availableItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
    }

    /**
     * Получение вегетарианских блюд
     */
    public List<MenuItem> getVegetarianItems() {
        log.debug("Запрос на получение вегетарианских блюд");
        return menuItemRepository.findByIsVegetarianTrue();
    }

    /**
     * Получение веганских блюд
     */
    public List<MenuItem> getVeganItems() {
        log.debug("Запрос на получение веганских блюд");
        return menuItemRepository.findByIsVeganTrue();
    }

    /**
     * Получение безглютеновых блюд
     */
    public List<MenuItem> getGlutenFreeItems() {
        log.debug("Запрос на получение безглютеновых блюд");
        return menuItemRepository.findByIsGlutenFreeTrue();
    }

    /**
     * Поиск блюд
     */
    public List<MenuItem> searchMenuItems(String query) {
        log.debug("Поиск блюд по запросу: {}", query);
        return menuItemRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(query, query);
    }

    /**
     * Создание нового пункта меню
     */
    @Transactional
    public MenuItem createMenuItem(MenuItem menuItem) {
        log.debug("Создание нового пункта меню: {}", menuItem.getName());
        return menuItemRepository.save(menuItem);
    }

    /**
     * Обновление пункта меню
     */
    @Transactional
    public MenuItem updateMenuItem(Long id, MenuItem menuItemDetails) {
        log.debug("Обновление пункта меню с ID: {}", id);

        MenuItem menuItem = getMenuItemById(id);

        menuItem.setName(menuItemDetails.getName());
        menuItem.setDescription(menuItemDetails.getDescription());
        menuItem.setPrice(menuItemDetails.getPrice());
        menuItem.setImageUrl(menuItemDetails.getImageUrl());
        menuItem.setCategory(menuItemDetails.getCategory());
        menuItem.setAvailable(menuItemDetails.isAvailable());
        menuItem.setVegetarian(menuItemDetails.isVegetarian());
        menuItem.setVegan(menuItemDetails.isVegan());
        menuItem.setGlutenFree(menuItemDetails.isGlutenFree());

        return menuItemRepository.save(menuItem);
    }

    /**
     * Изменение доступности пункта меню
     */
    @Transactional
    public MenuItem toggleMenuItemAvailability(Long id) {
        log.debug("Изменение доступности пункта меню с ID: {}", id);

        MenuItem menuItem = getMenuItemById(id);
        menuItem.setAvailable(!menuItem.isAvailable());

        return menuItemRepository.save(menuItem);
    }

    /**
     * Удаление пункта меню
     */
    @Transactional
    public void deleteMenuItem(Long id) {
        log.debug("Удаление пункта меню с ID: {}", id);

        MenuItem menuItem = getMenuItemById(id);
        menuItemRepository.delete(menuItem);

        log.info("Пункт меню с ID {} удален", id);
    }

    /**
     * Получение популярных блюд
     */
    public List<MenuItem> getPopularItems(int limit) {
        log.debug("Запрос на получение топ-{} популярных блюд", limit);
        return menuItemRepository.findPopularItems(limit);
    }
}

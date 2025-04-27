package de.restaurant_booking_app.service;

import de.restaurant_booking_app.exception.ResourceNotFoundException;
import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import de.restaurant_booking_app.repository.MenuItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class MenuServiceTest {

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private MenuService menuService;

    private MenuItem testMenuItem1;
    private MenuItem testMenuItem2;

    @BeforeEach
    void setUp() {
        // Создаем тестовые пункты меню
        testMenuItem1 = MenuItem.builder()
                .id(1L)
                .name("Цезарь с курицей")
                .description("Салат с курицей, сыром пармезан и соусом Цезарь")
                .price(new BigDecimal("550.00"))
                .category(MenuItemCategory.SALAD)
                .available(true)
                .isVegetarian(false)
                .build();

        testMenuItem2 = MenuItem.builder()
                .id(2L)
                .name("Борщ")
                .description("Традиционный борщ со сметаной")
                .price(new BigDecimal("450.00"))
                .category(MenuItemCategory.SOUP)
                .available(true)
                .isVegetarian(false)
                .build();
    }

    @Test
    @DisplayName("Получение всех пунктов меню")
    void getAllMenuItems() {
        // Подготовка
        List<MenuItem> expectedMenuItems = Arrays.asList(testMenuItem1, testMenuItem2);
        when(menuItemRepository.findAll()).thenReturn(expectedMenuItems);

        // Выполнение
        List<MenuItem> actualMenuItems = menuService.getAllMenuItems();

        // Проверка
        assertEquals(expectedMenuItems, actualMenuItems);
        verify(menuItemRepository).findAll();
    }

    @Test
    @DisplayName("Получение доступных пунктов меню")
    void getAvailableMenuItems() {
        // Подготовка
        List<MenuItem> expectedMenuItems = Arrays.asList(testMenuItem1, testMenuItem2);
        when(menuItemRepository.findByAvailableTrue()).thenReturn(expectedMenuItems);

        // Выполнение
        List<MenuItem> actualMenuItems = menuService.getAvailableMenuItems();

        // Проверка
        assertEquals(expectedMenuItems, actualMenuItems);
        verify(menuItemRepository).findByAvailableTrue();
    }

    @Test
    @DisplayName("Получение пункта меню по ID - успешно")
    void getMenuItemById_WhenExists() {
        // Подготовка
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem1));

        // Выполнение
        MenuItem actualMenuItem = menuService.getMenuItemById(1L);

        // Проверка
        assertEquals(testMenuItem1, actualMenuItem);
        verify(menuItemRepository).findById(1L);
    }

    @Test
    @DisplayName("Получение пункта меню по ID - не найден")
    void getMenuItemById_WhenNotExists() {
        // Подготовка
        when(menuItemRepository.findById(anyLong())).thenReturn(Optional.empty());

        // Выполнение и проверка
        assertThrows(ResourceNotFoundException.class, () -> menuService.getMenuItemById(999L));
        verify(menuItemRepository).findById(999L);
    }

    @Test
    @DisplayName("Получение пунктов меню по категории")
    void getMenuItemsByCategory() {
        // Подготовка
        List<MenuItem> expectedMenuItems = Collections.singletonList(testMenuItem1);
        when(menuItemRepository.findByCategory(MenuItemCategory.SALAD)).thenReturn(expectedMenuItems);

        // Выполнение
        List<MenuItem> actualMenuItems = menuService.getMenuItemsByCategory(MenuItemCategory.SALAD);

        // Проверка
        assertEquals(expectedMenuItems, actualMenuItems);
        verify(menuItemRepository).findByCategory(MenuItemCategory.SALAD);
    }

    @Test
    @DisplayName("Получение меню по категориям")
    void getMenuByCategories() {
        // Подготовка
        List<MenuItem> availableItems = Arrays.asList(testMenuItem1, testMenuItem2);
        when(menuItemRepository.findByAvailableTrue()).thenReturn(availableItems);

        // Выполнение
        Map<MenuItemCategory, List<MenuItem>> menuByCategories = menuService.getMenuByCategories();

        // Проверка
        assertEquals(2, menuByCategories.size());
        assertTrue(menuByCategories.containsKey(MenuItemCategory.SALAD));
        assertTrue(menuByCategories.containsKey(MenuItemCategory.SOUP));
        assertEquals(1, menuByCategories.get(MenuItemCategory.SALAD).size());
        assertEquals(1, menuByCategories.get(MenuItemCategory.SOUP).size());
        verify(menuItemRepository).findByAvailableTrue();
    }

    @Test
    @DisplayName("Создание нового пункта меню")
    void createMenuItem() {
        // Подготовка
        MenuItem newMenuItem = MenuItem.builder()
                .name("Новое блюдо")
                .description("Описание нового блюда")
                .price(new BigDecimal("600.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(true)
                .build();

        MenuItem savedMenuItem = MenuItem.builder()
                .id(3L)
                .name("Новое блюдо")
                .description("Описание нового блюда")
                .price(new BigDecimal("600.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(true)
                .build();

        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(savedMenuItem);

        // Выполнение
        MenuItem actualMenuItem = menuService.createMenuItem(newMenuItem);

        // Проверка
        assertEquals(savedMenuItem, actualMenuItem);
        verify(menuItemRepository).save(newMenuItem);
    }

    @Test
    @DisplayName("Обновление пункта меню")
    void updateMenuItem() {
        // Подготовка
        MenuItem updatedDetails = MenuItem.builder()
                .name("Обновленное название")
                .description("Обновленное описание")
                .price(new BigDecimal("700.00"))
                .category(MenuItemCategory.SALAD)
                .available(true)
                .isVegetarian(true)
                .build();

        MenuItem existingMenuItem = testMenuItem1;
        MenuItem expectedMenuItem = MenuItem.builder()
                .id(1L)
                .name("Обновленное название")
                .description("Обновленное описание")
                .price(new BigDecimal("700.00"))
                .category(MenuItemCategory.SALAD)
                .available(true)
                .isVegetarian(true)
                .build();

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(existingMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(expectedMenuItem);

        // Выполнение
        MenuItem actualMenuItem = menuService.updateMenuItem(1L, updatedDetails);

        // Проверка
        assertEquals(expectedMenuItem, actualMenuItem);
        verify(menuItemRepository).findById(1L);
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Изменение доступности пункта меню")
    void toggleMenuItemAvailability() {
        // Подготовка
        MenuItem existingMenuItem = testMenuItem1; // изначально available = true
        MenuItem expectedMenuItem = MenuItem.builder()
                .id(1L)
                .name("Цезарь с курицей")
                .description("Салат с курицей, сыром пармезан и соусом Цезарь")
                .price(new BigDecimal("550.00"))
                .category(MenuItemCategory.SALAD)
                .available(false) // должно измениться на false
                .isVegetarian(false)
                .build();

        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(existingMenuItem));
        when(menuItemRepository.save(any(MenuItem.class))).thenReturn(expectedMenuItem);

        // Выполнение
        MenuItem actualMenuItem = menuService.toggleMenuItemAvailability(1L);

        // Проверка
        assertEquals(expectedMenuItem, actualMenuItem);
        assertFalse(actualMenuItem.isAvailable()); // проверяем, что доступность изменилась
        verify(menuItemRepository).findById(1L);
        verify(menuItemRepository).save(any(MenuItem.class));
    }

    @Test
    @DisplayName("Удаление пункта меню")
    void deleteMenuItem() {
        // Подготовка
        when(menuItemRepository.findById(1L)).thenReturn(Optional.of(testMenuItem1));
        doNothing().when(menuItemRepository).delete(any(MenuItem.class));

        // Выполнение
        menuService.deleteMenuItem(1L);

        // Проверка
        verify(menuItemRepository).findById(1L);
        verify(menuItemRepository).delete(testMenuItem1);
    }
}

package de.restaurant_booking_app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import de.restaurant_booking_app.repository.MenuItemRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class MenuControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MenuItemRepository menuItemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private MenuItem testMenuItem;

    @BeforeEach
    void setUp() {
        // Очищаем репозиторий перед каждым тестом
        menuItemRepository.deleteAll();

        // Создаем тестовый пункт меню
        testMenuItem = MenuItem.builder()
                .name("Тестовое блюдо")
                .description("Описание тестового блюда")
                .price(new BigDecimal("450.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(true)
                .isVegetarian(false)
                .isVegan(false)
                .isGlutenFree(false)
                .build();

        testMenuItem = menuItemRepository.save(testMenuItem);
    }

    @AfterEach
    void tearDown() {
        // Очищаем репозиторий после каждого теста
        menuItemRepository.deleteAll();
    }

    @Test
    @DisplayName("Получение всех пунктов меню")
    void getAllMenuItems() throws Exception {
        mockMvc.perform(get("/api/menu"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(testMenuItem.getId().intValue())))
                .andExpect(jsonPath("$[0].name", is("Тестовое блюдо")))
                .andExpect(jsonPath("$[0].category", is("MAIN_COURSE")));
    }

    @Test
    @DisplayName("Получение только доступных пунктов меню")
    void getAvailableMenuItems() throws Exception {
        // Создаем недоступный пункт меню
        MenuItem unavailableItem = MenuItem.builder()
                .name("Недоступное блюдо")
                .description("Это блюдо недоступно")
                .price(new BigDecimal("500.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(false)
                .build();

        menuItemRepository.save(unavailableItem);

        mockMvc.perform(get("/api/menu?available=true"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Тестовое блюдо")))
                .andExpect(jsonPath("$[0].available", is(true)));
    }

    @Test
    @DisplayName("Получение пункта меню по ID")
    void getMenuItemById() throws Exception {
        mockMvc.perform(get("/api/menu/{id}", testMenuItem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testMenuItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Тестовое блюдо")))
                .andExpect(jsonPath("$.description", is("Описание тестового блюда")))
                .andExpect(jsonPath("$.price", is(450.00)));
    }

    @Test
    @DisplayName("Получение пункта меню по несуществующему ID")
    void getMenuItemByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/menu/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Получение пунктов меню по категории")
    void getMenuItemsByCategory() throws Exception {
        mockMvc.perform(get("/api/menu/category/{category}", "MAIN_COURSE"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Тестовое блюдо")))
                .andExpect(jsonPath("$[0].category", is("MAIN_COURSE")));
    }

    @Test
    @DisplayName("Создание нового пункта меню")
    void createMenuItem() throws Exception {
        MenuItem newMenuItem = MenuItem.builder()
                .name("Новое блюдо")
                .description("Описание нового блюда")
                .price(new BigDecimal("650.00"))
                .category(MenuItemCategory.DESSERT)
                .available(true)
                .isVegetarian(true)
                .build();

        String requestJson = objectMapper.writeValueAsString(newMenuItem);

        MvcResult result = mockMvc.perform(post("/api/menu")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Новое блюдо")))
                .andExpect(jsonPath("$.category", is("DESSERT")))
                .andExpect(jsonPath("$.price", is(650.00)))
                .andExpect(jsonPath("$.vegetarian", is(true)))
                .andReturn();

        // Проверяем, что запись создана в базе данных
        String responseJson = result.getResponse().getContentAsString();
        MenuItem createdMenuItem = objectMapper.readValue(responseJson, MenuItem.class);

        assertTrue(menuItemRepository.findById(createdMenuItem.getId()).isPresent());
    }

    @Test
    @DisplayName("Обновление пункта меню")
    void updateMenuItem() throws Exception {
        // Подготовка обновленных данных
        MenuItem updatedMenuItem = MenuItem.builder()
                .name("Обновленное блюдо")
                .description("Новое описание")
                .price(new BigDecimal("550.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(true)
                .isVegetarian(true)
                .build();

        String requestJson = objectMapper.writeValueAsString(updatedMenuItem);

        mockMvc.perform(put("/api/menu/{id}", testMenuItem.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testMenuItem.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Обновленное блюдо")))
                .andExpect(jsonPath("$.description", is("Новое описание")))
                .andExpect(jsonPath("$.price", is(550.00)))
                .andExpect(jsonPath("$.vegetarian", is(true)));

        // Проверяем, что запись обновлена в базе данных
        MenuItem menuItemFromDb = menuItemRepository.findById(testMenuItem.getId()).orElse(null);
        assertNotNull(menuItemFromDb);
        assertEquals("Обновленное блюдо", menuItemFromDb.getName());
        assertEquals("Новое описание", menuItemFromDb.getDescription());
        assertEquals(0, new BigDecimal("550.00").compareTo(menuItemFromDb.getPrice()));
        assertTrue(menuItemFromDb.isVegetarian());
    }

    @Test
    @DisplayName("Изменение доступности пункта меню")
    void toggleMenuItemAvailability() throws Exception {
        mockMvc.perform(patch("/api/menu/{id}/toggle-availability", testMenuItem.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(testMenuItem.getId().intValue())))
                .andExpect(jsonPath("$.available", is(false))); // должно измениться с true на false

        // Проверяем, что доступность изменена в базе данных
        MenuItem menuItemFromDb = menuItemRepository.findById(testMenuItem.getId()).orElse(null);
        assertNotNull(menuItemFromDb);
        assertFalse(menuItemFromDb.isAvailable());
    }

    @Test
    @DisplayName("Удаление пункта меню")
    void deleteMenuItem() throws Exception {
        mockMvc.perform(delete("/api/menu/{id}", testMenuItem.getId()))
                .andExpect(status().isNoContent());

        // Проверяем, что запись удалена из базы данных
        assertFalse(menuItemRepository.findById(testMenuItem.getId()).isPresent());
    }

    @Test
    @DisplayName("Поиск блюд по запросу")
    void searchMenuItems() throws Exception {
        // Создаем дополнительные пункты меню для поиска
        MenuItem searchItem1 = MenuItem.builder()
                .name("Паста карбонара")
                .description("Итальянская паста с беконом, яйцом и сыром")
                .price(new BigDecimal("550.00"))
                .category(MenuItemCategory.MAIN_COURSE)
                .available(true)
                .build();

        MenuItem searchItem2 = MenuItem.builder()
                .name("Салат Оливье")
                .description("Традиционный русский салат с картофелем, морковью и колбасой")
                .price(new BigDecimal("350.00"))
                .category(MenuItemCategory.SALAD)
                .available(true)
                .build();

        menuItemRepository.save(searchItem1);
        menuItemRepository.save(searchItem2);

        // Поиск по названию
        mockMvc.perform(get("/api/menu/search?query=паста"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Паста карбонара")));

        // Поиск по описанию
        mockMvc.perform(get("/api/menu/search?query=картофель"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Салат Оливье")));
    }
}
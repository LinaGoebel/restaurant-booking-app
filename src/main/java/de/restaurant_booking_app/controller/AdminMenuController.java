package de.restaurant_booking_app.controller;

import de.restaurant_booking_app.model.MenuItem;
import de.restaurant_booking_app.model.MenuItemCategory;
import de.restaurant_booking_app.service.MenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;

import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/admin/menu")
@Slf4j
public class AdminMenuController {

    private final MenuService menuService;

    @Autowired
    public AdminMenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    /**
     * Страница со списком всех пунктов меню
     */
    @GetMapping
    public String menuItems(
            @RequestParam(required = false) MenuItemCategory category,
            Model model) {

        List<MenuItem> menuItems;
        if (category != null) {
            menuItems = menuService.getMenuItemsByCategory(category);
            model.addAttribute("currentCategory", category);
        } else {
            menuItems = menuService.getAllMenuItems();
        }

        model.addAttribute("menuItems", menuItems);
        model.addAttribute("categories", MenuItemCategory.values());

        return "admin/menu-items";
    }

    /**
     * Страница просмотра пункта меню
     */
    @GetMapping("/{id}")
    public String viewMenuItem(@PathVariable Long id, Model model) {
        MenuItem menuItem = menuService.getMenuItemById(id);
        model.addAttribute("menuItem", menuItem);
        return "admin/menu-item-details";
    }

    /**
     * Форма создания нового пункта меню
     */
    @GetMapping("/create")
    public String createMenuItemForm(Model model) {
        model.addAttribute("menuItem", new MenuItem());
        model.addAttribute("categories", MenuItemCategory.values());
        model.addAttribute("mode", "create");
        return "admin/menu-item-form";
    }

    /**
     * Обработка создания нового пункта меню
     */
    @PostMapping("/create")
    public String createMenuItem(
            @Valid @ModelAttribute("menuItem") MenuItem menuItem,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", MenuItemCategory.values());
            model.addAttribute("mode", "create");
            return "admin/menu-item-form";
        }

        try {
            MenuItem createdMenuItem = menuService.createMenuItem(menuItem);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пункт меню \"" + createdMenuItem.getName() + "\" успешно создан");
            return "redirect:/admin/menu";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", MenuItemCategory.values());
            model.addAttribute("mode", "create");
            return "admin/menu-item-form";
        }
    }

    /**
     * Форма редактирования пункта меню
     */
    @GetMapping("/{id}/edit")
    public String editMenuItemForm(@PathVariable Long id, Model model) {
        MenuItem menuItem = menuService.getMenuItemById(id);
        model.addAttribute("menuItem", menuItem);
        model.addAttribute("categories", MenuItemCategory.values());
        model.addAttribute("mode", "edit");
        return "admin/menu-item-form";
    }

    /**
     * Обработка редактирования пункта меню
     */
    @PostMapping("/{id}/edit")
    public String editMenuItem(
            @PathVariable Long id,
            @Valid @ModelAttribute("menuItem") MenuItem menuItem,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("categories", MenuItemCategory.values());
            model.addAttribute("mode", "edit");
            return "admin/menu-item-form";
        }

        try {
            MenuItem updatedMenuItem = menuService.updateMenuItem(id, menuItem);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пункт меню \"" + updatedMenuItem.getName() + "\" успешно обновлен");
            return "redirect:/admin/menu";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("categories", MenuItemCategory.values());
            model.addAttribute("mode", "edit");
            return "admin/menu-item-form";
        }
    }

    /**
     * Изменение доступности пункта меню
     */
    @PostMapping("/{id}/toggle-availability")
    public String toggleMenuItemAvailability(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            MenuItem menuItem = menuService.toggleMenuItemAvailability(id);
            String statusMessage = menuItem.isAvailable() ? "доступен" : "недоступен";
            redirectAttributes.addFlashAttribute("successMessage",
                    "Пункт меню \"" + menuItem.getName() + "\" теперь " + statusMessage);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/menu";
    }

    /**
     * Удаление пункта меню
     */
    @PostMapping("/{id}/delete")
    public String deleteMenuItem(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        try {
            MenuItem menuItem = menuService.getMenuItemById(id);
            String menuItemName = menuItem.getName();

            menuService.deleteMenuItem(id);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Пункт меню \"" + menuItemName + "\" успешно удален");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/admin/menu";
    }
}

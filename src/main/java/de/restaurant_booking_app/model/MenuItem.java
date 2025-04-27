package de.restaurant_booking_app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "menu_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "image_url")
    private String imageUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MenuItemCategory category;

    @Builder.Default
    @Column(nullable = false)
    private boolean available = true;

    @Builder.Default
    @Column(name = "is_vegetarian")
    private boolean isVegetarian = false;

    @Builder.Default
    @Column(name = "is_vegan")
    private boolean isVegan = false;

    @Builder.Default
    @Column(name = "is_gluten_free")
    private boolean isGlutenFree = false;
}

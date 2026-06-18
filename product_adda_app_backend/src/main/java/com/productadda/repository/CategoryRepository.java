package com.productadda.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.productadda.entity.Category;

public interface CategoryRepository
        extends JpaRepository<Category, UUID> {

        // Fetch top-level parent categories (e.g., Electronics, Apparel)
        List<Category> findByFkParentCategoryIsNullAndIsActiveTrueOrderByDisplayOrderAsc();

        // Fetch subcategories belonging to a specific parent (e.g., Laptops under Electronics)
        List<Category> findByFkParentCategoryAndIsActiveTrueOrderByDisplayOrderAsc(Category parentCategory);
}
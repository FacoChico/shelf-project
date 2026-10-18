package ru.shelf.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.shelf.domain.ShelfItem;

public interface ShelfItemRepository extends JpaRepository<ShelfItem, Long> {

    List<ShelfItem> findAllByOwnerIdOrderByCreatedAtDescIdDesc(String ownerId);

    Optional<ShelfItem> findByIdAndOwnerId(Long id, String ownerId);
}

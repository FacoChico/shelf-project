package ru.shelf.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.shelf.domain.ItemStatus;
import ru.shelf.domain.ShelfItem;
import ru.shelf.repository.ShelfItemRepository;
import ru.shelf.service.dto.ItemCommand;
import ru.shelf.service.dto.ShelfFilter;
import ru.shelf.service.dto.ShelfItemData;
import ru.shelf.service.dto.ShelfOverview;
import ru.shelf.service.dto.ShelfStats;

@Service
@Validated
@Transactional(readOnly = true)
public class ShelfService {
    private final ShelfItemRepository repository;

    public ShelfService(ShelfItemRepository repository) {
        this.repository = repository;
    }

    public ShelfOverview find(@NotBlank String ownerId, @NotNull ShelfFilter filter) {
        List<ShelfItem> items = repository.findAllByOwnerIdOrderByCreatedAtDescIdDesc(ownerId);
        List<ShelfItemData> matching =
            items.stream()
                .filter(
                    item ->
                        filter.category() == null
                            || item.getCategory() == filter.category())
                .filter(
                    item ->
                        filter.status() == null
                            || item.getStatus() == filter.status())
                .filter(
                    item ->
                        item.getTitle()
                            .toLowerCase(Locale.ROOT)
                            .contains(filter.query()))
                .map(ShelfService::toData)
                .toList();
        return new ShelfOverview(matching, summarize(items));
    }

    public ShelfStats stats(@NotBlank String ownerId) {
        return summarize(repository.findAllByOwnerIdOrderByCreatedAtDescIdDesc(ownerId));
    }

    public ShelfItemData get(@NotBlank String ownerId, @NotNull Long id) {
        return toData(findOwnedItem(ownerId, id));
    }

    @Transactional
    public ShelfItemData create(@NotBlank String ownerId, @NotNull @Valid ItemCommand command) {
        ShelfItem item =
            new ShelfItem(
                ownerId,
                command.title(),
                command.category(),
                command.status(),
                command.note());
        return toData(repository.save(item));
    }

    @Transactional
    public void update(
        @NotBlank String ownerId, @NotNull Long id, @NotNull @Valid ItemCommand command) {
        findOwnedItem(ownerId, id)
            .update(command.title(), command.category(), command.status(), command.note());
    }

    @Transactional
    public void delete(@NotBlank String ownerId, @NotNull Long id) {
        repository.delete(findOwnedItem(ownerId, id));
    }

    private ShelfItem findOwnedItem(String ownerId, Long id) {
        return repository.findByIdAndOwnerId(id, ownerId).orElseThrow(ItemNotFoundException::new);
    }

    private static ShelfItemData toData(ShelfItem item) {
        return new ShelfItemData(
            item.getId(),
            item.getTitle(),
            item.getCategory(),
            item.getStatus(),
            item.getNote(),
            item.getCreatedAt(),
            item.getUpdatedAt());
    }

    private static ShelfStats summarize(List<ShelfItem> items) {
        return new ShelfStats(
            items.size(),
            count(items, ItemStatus.PLANNED),
            count(items, ItemStatus.IN_PROGRESS),
            count(items, ItemStatus.DONE));
    }

    private static long count(List<ShelfItem> items, ItemStatus status) {
        return items.stream().filter(item -> item.getStatus() == status).count();
    }
}

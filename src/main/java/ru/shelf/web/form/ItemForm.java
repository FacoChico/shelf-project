package ru.shelf.web.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ru.shelf.domain.Category;
import ru.shelf.domain.ItemStatus;
import ru.shelf.service.dto.ItemCommand;
import ru.shelf.service.dto.ShelfItemData;

public class ItemForm {
    @NotBlank(message = "Название обязательно")
    @Size(max = 160, message = "Название должно быть не длиннее 160 символов")
    private String title = "";

    @NotNull(message = "Категория обязательна")
    private Category category = Category.BOOK;

    @NotNull(message = "Статус обязателен")
    private ItemStatus status = ItemStatus.PLANNED;

    @Size(max = 2000, message = "Заметка должна быть не длиннее 2000 символов")
    private String note = "";

    public ItemForm() {
    }

    public static ItemForm from(ShelfItemData item) {
        ItemForm form = new ItemForm();
        form.title = item.title();
        form.category = item.category();
        form.status = item.status();
        form.note = item.note();
        return form;
    }

    public ItemCommand toCommand() {
        return new ItemCommand(title, category, status, note);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ItemStatus getStatus() {
        return status;
    }

    public void setStatus(ItemStatus status) {
        this.status = status;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}

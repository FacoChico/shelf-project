package ru.shelf.service;

public class ItemNotFoundException extends RuntimeException {

    public ItemNotFoundException() {
        super("Shelf item not found");
    }
}

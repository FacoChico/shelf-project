package ru.shelf.service.dto;

public record ShelfStats(long total, long planned, long inProgress, long done) {

    public int progress() {
        return total == 0
            ? 0
            : (int) (done * 100 / total);
    }
}

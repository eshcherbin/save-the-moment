package ru.spbau.savethemoment.datamanagers;

public class IllegalDriveStateException extends RuntimeException {
    public IllegalDriveStateException() {
    }

    public IllegalDriveStateException(String message) {
        super(message);
    }
}

package com.morecruit.domain.exception;

/**
 * Interface to represent a wrapper around an {@link Exception} to manage errors.
 *
 * @author yifan.zhai
 * @version 1.0.0
 */
public interface ErrorBundle {
    Exception getException();

    String getErrorMessage();
}

package com.topher.brockman;

/**
 * Created by topher on 31/07/16.
 */
public class TaskStatus {
    public boolean success = true;

    public TaskStatus fail() {
        success = false;
        return this;
    }
}
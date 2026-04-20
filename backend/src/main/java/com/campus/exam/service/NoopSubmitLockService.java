package com.campus.exam.service;

public class NoopSubmitLockService implements SubmitLockService {

    @Override
    public boolean tryAcquireSubmitLock(Long attemptId) {
        return true;
    }
}

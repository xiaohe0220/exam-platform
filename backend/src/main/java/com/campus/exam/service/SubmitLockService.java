package com.campus.exam.service;

/**
 * 分布式下防止重复提交（SETNX）；单机可配合 synchronized 使用。
 */
public interface SubmitLockService {

    /**
     * @return true 表示本请求获得提交令牌，false 表示可能由他机/他请求已处理
     */
    boolean tryAcquireSubmitLock(Long attemptId);
}

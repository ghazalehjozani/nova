package ir.dotin.loan.trade.adapters.driven.fcbmessaging.dto;

/**
 * Key-value parameter for JMS request messages. Mirrors the existing FCB {@code Parameter} DTO concept used in HTTP
 * adapters.
 */
public record FcbJmsParameter(String key, String value) {}

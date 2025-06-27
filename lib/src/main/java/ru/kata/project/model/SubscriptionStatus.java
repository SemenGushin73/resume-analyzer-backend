package ru.kata.project.model;

/**
 * Перечисления статусов подписки
 **/
public enum SubscriptionStatus {
    ACTIVE,   // Подписка активна
    INACTIVE  // Подписка неактивна (ожидает оплаты, отменена, истек срок, ошибка создания)
}
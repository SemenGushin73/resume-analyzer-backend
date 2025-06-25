package webapp.subscriptionservice.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.kata.project.exception.PaymentGatewayException;
import ru.kata.project.exception.ResourceNotFoundException;
import ru.kata.project.model.*;
import ru.kata.project.subscriptionDto.PaymentSessionResponseDto;
import ru.kata.project.subscriptionDto.SubscriptionHistoryDto;
import ru.kata.project.subscriptionDto.UserSubscriptionDto;
import webapp.subscriptionservice.application.mapper.SubscriptionMapper;
import webapp.subscriptionservice.application.util.SubscriptionMetaJsonHelper;
import webapp.subscriptionservice.domain.model.SubscriptionHistory;
import webapp.subscriptionservice.domain.model.UserSubscription;
import webapp.subscriptionservice.domain.repository.SubscriptionHistoryRepository;
import webapp.subscriptionservice.domain.repository.UserSubscriptionRepository;
import webapp.subscriptionservice.domain.service.PaymentGatewayService;
import webapp.subscriptionservice.infrastructure.persistence.entity.SubscriptionHistoryEntity;
import webapp.subscriptionservice.infrastructure.persistence.entity.UserSubscriptionEntity;
import webapp.subscriptionservice.infrastructure.persistence.mapper.SubscriptionHistoryMapper;
import webapp.subscriptionservice.infrastructure.persistence.mapper.UserSubscriptionMapper;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Тестовый класс для subscription-service.
 * Проверка бизнес логики в различных сценариях.
 */
@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    /**
     * Мок-объекты для имитации
     **/
    @Mock
    private UserSubscriptionRepository userSubscriptionRepository;
    @Mock
    private SubscriptionHistoryRepository subscriptionHistoryRepository;
    @Mock
    private PaymentGatewayService paymentGatewayService;
    @Mock
    private SubscriptionMapper subscriptionMapper;
    @Mock
    private UserSubscriptionMapper userSubscriptionPersistenceMapper;
    @Mock
    private SubscriptionHistoryMapper subscriptionHistoryPersistenceMapper;
    @Mock
    private SubscriptionMetaJsonHelper subscriptionMetaJsonHelper;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private final UUID userId = UUID.randomUUID();
    private final SubscriptionType type = SubscriptionType.PRO;
    private final LocalDateTime now = LocalDateTime.now();
    private final ObjectMapper realObjectMapper = new ObjectMapper();

    /**
     * Метод настраивает поведение моков Objectmapper и subscriptionMetaJsonHelper перед каждым тестовым методом.
     */
    @BeforeEach
    void setUp() {
        lenient().when(objectMapper.createObjectNode()).thenReturn(realObjectMapper.createObjectNode());
        lenient().when(subscriptionMetaJsonHelper.createSubscriptionMetaNode(any(), any(), any(), any()))
                .thenReturn(realObjectMapper.createObjectNode());
        lenient().when(subscriptionMetaJsonHelper.prepareCallbackMetaNode(any(), any(), any(), any()))
                .thenReturn(realObjectMapper.createObjectNode());
    }

    /**
     * Тест: создание подписки, когда у пользователя уже есть активная подписка.
     * Ожидается, что будет выброшено исключение IllegalArgumentException.
     */
    @Test
    void createSubscription_whenActiveExists_shouldThrow() {
        // Given
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(new UserSubscriptionEntity()));

        // When & Then
        assertThrows(IllegalArgumentException.class, () ->
                subscriptionService.createSubscription(userId, type));
    }

    /**
     * Тест: создание подписки, когда у пользователя нет активной подписки.
     * Ожидается успешное создание подписки и платежной сессии.
     *
     * @throws Exception в случае непредвиденных ошибок, например, связанных с CompletableFuture.
     */
    @Test
    void createSubscription_whenNoActive_shouldCreate() throws Exception {
        // Given
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.INACTIVE);

        when(userSubscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.save(any())).thenReturn(entity);

        PaymentGatewayService.PaymentSessionResponse response =
                new PaymentGatewayService.PaymentSessionResponse("pay1", "url", "qr");
        when(paymentGatewayService.createPaymentSession(any(), any(), anyDouble(), anyString()))
                .thenReturn(CompletableFuture.completedFuture(response));

        // When
        PaymentSessionResponseDto result = subscriptionService.createSubscription(userId, type);

        // Then
        verify(userSubscriptionRepository, times(2)).save(any());
        verify(subscriptionHistoryRepository, atLeastOnce()).save(any());
        assertEquals("pay1", result.getPaymentId());
    }

    /**
     * Тест: создание подписки, когда платежный шлюз завершается с ошибкой.
     * Ожидается, что будет выброшено исключение PaymentGatewayException.
     */
    @Test
    void createSubscription_whenGatewayFails_shouldThrow() throws Exception {
        // Given
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.INACTIVE);

        when(userSubscriptionRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userSubscriptionRepository.save(any())).thenReturn(entity);

        PaymentGatewayService.PaymentSessionResponse response =
                new PaymentGatewayService.PaymentSessionResponse("pay1", "url", "qr");
        when(paymentGatewayService.createPaymentSession(any(), any(), anyDouble(), anyString()))
                .thenThrow(new PaymentGatewayException("Gateway error"));

        SubscriptionHistoryEntity historyEntity = new SubscriptionHistoryEntity();
        when(subscriptionHistoryPersistenceMapper.toEntity(any())).thenReturn(historyEntity);

        // When & Then
        assertThrows(PaymentGatewayException.class, () ->
                subscriptionService.createSubscription(userId, type));

        verify(subscriptionHistoryRepository, atLeastOnce()).save(any());
    }

    /**
     * Тест: отмена активной подписки.
     * Ожидается, что подписка будет успешно отменена (статус станет INACTIVE).
     *
     * @throws Exception в случае непредвиденных ошибок.
     */
    @Test
    void cancelSubscription_whenActiveExists_shouldCancel() throws Exception {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setExternalPaymentId("pay1");

        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.ACTIVE);
        domain.setExternalPaymentId("pay1");

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);

        when(paymentGatewayService.cancelPayment(anyString()))
                .thenReturn(CompletableFuture.completedFuture(true));

        // When
        subscriptionService.cancelSubscription(userId);

        // Then
        verify(userSubscriptionRepository).save(argThat(e ->
                e.getStatus() == SubscriptionStatus.INACTIVE));
        verify(subscriptionHistoryRepository, atLeast(2)).save(any());
    }

    /**
     * Тест: отмена подписки, когда активная подписка не найдена.
     * Ожидается, что будет выброшено исключение ResourceNotFoundException.
     */
    @Test
    void cancelSubscription_whenNoActive_shouldThrow() {
        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () ->
                subscriptionService.cancelSubscription(userId));
    }

    /**
     * Тест: отмена подписки, когда платежный шлюз завершается с ошибкой.
     * Ожидается, что подписка все равно будет отменена локально, но выбросится PaymentGatewayException.
     *
     * @throws Exception в случае непредвиденных ошибок.
     */
    @Test
    void cancelSubscription_whenGatewayFails_shouldStillCancel() throws Exception {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.ACTIVE);
        entity.setExternalPaymentId("pay1"); // Добавляем paymentId для вызова отмены

        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.ACTIVE);
        domain.setExternalPaymentId("pay1");

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);

        when(paymentGatewayService.cancelPayment(any()))
                .thenThrow(new PaymentGatewayException("Error"));

        SubscriptionHistoryEntity historyEntity = new SubscriptionHistoryEntity();
        when(subscriptionHistoryPersistenceMapper.toEntity(any())).thenReturn(historyEntity);
        when(subscriptionHistoryRepository.save(any())).thenReturn(historyEntity);

        // When & Then
        assertThrows(PaymentGatewayException.class, () ->
                subscriptionService.cancelSubscription(userId));

        verify(userSubscriptionRepository).save(argThat(e ->
                e.getStatus() == SubscriptionStatus.INACTIVE));
    }

    /**
     * Тест: обработка коллбэка платежного шлюза при успешном платеже.
     * Ожидается, что подписка изменит статус на ACTIVE и будет установлена дата окончания.
     */
    @Test
    void handlePaymentCallback_whenSucceeded_shouldActivate() {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.INACTIVE);

        when(userSubscriptionRepository.findByExternalPaymentId("pay1"))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.INACTIVE);

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);

        // When
        subscriptionService.handlePaymentCallback("pay1", PaymentStatus.SUCCEEDED, "{}");

        // Then
        verify(userSubscriptionRepository).save(argThat(e ->
                e.getStatus() == SubscriptionStatus.ACTIVE &&
                        e.getExpiresAt().isAfter(LocalDateTime.now())));
    }

    /**
     * Тест: обработка коллбэка платежного шлюза при неудачном платеже.
     * Ожидается, что подписка изменит статус на INACTIVE.
     */
    @Test
    void handlePaymentCallback_whenFailed_shouldDeactivate() {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionRepository.findByExternalPaymentId("pay1"))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);

        // When
        subscriptionService.handlePaymentCallback("pay1", PaymentStatus.FAILED, "{}");

        // Then
        verify(userSubscriptionRepository).save(argThat(e ->
                e.getStatus() == SubscriptionStatus.INACTIVE));
    }

    /**
     * Тест: обработка коллбэка платежного шлюза при неизвестном статусе.
     * Ожидается, что подписка изменит статус на INACTIVE.
     */
    @Test
    void handlePaymentCallback_whenUnknownStatus_shouldDeactivate() {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionRepository.findByExternalPaymentId("pay1"))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);

        // When
        subscriptionService.handlePaymentCallback("pay1", PaymentStatus.UNKNOWN, "{}");

        // Then
        verify(userSubscriptionRepository).save(argThat(e ->
                e.getStatus() == SubscriptionStatus.INACTIVE));
    }

    /**
     * Тест: получение активной подписки, когда она существует.
     * Ожидается, что будет возвращен Optional, содержащий UserSubscriptionDto.
     */
    @Test
    void getActiveSubscription_whenExists_shouldReturnDto() {
        // Given
        UserSubscriptionEntity entity = new UserSubscriptionEntity();
        entity.setId(UUID.randomUUID());
        entity.setUserId(userId);
        entity.setType(type);
        entity.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionRepository.findByUserIdAndStatus(userId, SubscriptionStatus.ACTIVE))
                .thenReturn(Optional.of(entity));

        UserSubscription domain = new UserSubscription();
        domain.setId(entity.getId());
        domain.setUserId(userId);
        domain.setType(type);
        domain.setStatus(SubscriptionStatus.ACTIVE);

        when(userSubscriptionPersistenceMapper.toDomainModel(entity)).thenReturn(domain);
        when(subscriptionMapper.toUserSubscriptionDto(domain))
                .thenReturn(new UserSubscriptionDto());

        // When
        Optional<UserSubscriptionDto> result = subscriptionService.getActiveSubscription(userId);

        // Then
        assertTrue(result.isPresent());
    }

    /**
     * Тест: получение истории подписок.
     * Ожидается, что будет возвращен отсортированный по дате создания в убывающем порядке список DTO.
     */
    @Test
    void getHistory_shouldReturnSortedList() {
        // Given
        SubscriptionHistoryEntity oldEntity = new SubscriptionHistoryEntity();
        oldEntity.setId(UUID.randomUUID());
        oldEntity.setUserId(userId);
        oldEntity.setType(type);
        oldEntity.setCreatedAt(now.minusDays(1));

        SubscriptionHistoryEntity newEntity = new SubscriptionHistoryEntity();
        newEntity.setId(UUID.randomUUID());
        newEntity.setUserId(userId);
        newEntity.setType(type);
        newEntity.setCreatedAt(now);

        List<SubscriptionHistoryEntity> entities = Arrays.asList(newEntity, oldEntity);

        when(subscriptionHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(entities);

        when(subscriptionHistoryPersistenceMapper.toDomainModel(any()))
                .thenAnswer(inv -> {
                    SubscriptionHistoryEntity e = inv.getArgument(0);
                    // Создаем объект с помощью правильного конструктора
                    SubscriptionHistory history = new SubscriptionHistory();
                    history.setId(e.getId());
                    history.setUserId(e.getUserId());
                    history.setType(e.getType());
                    history.setEventType(e.getEventType());
                    history.setMeta(e.getMeta());
                    history.setCreatedAt(e.getCreatedAt());
                    return history;
                });

        when(subscriptionMapper.toHistoryDto(any()))
                .thenAnswer(inv -> {
                    SubscriptionHistory history = inv.getArgument(0);
                    SubscriptionHistoryDto dto = new SubscriptionHistoryDto();
                    dto.setCreatedAt(history.getCreatedAt());
                    return dto;
                });

        // When
        List<SubscriptionHistoryDto> result = subscriptionService.getSubscriptionHistory(userId);

        // Then
        assertEquals(2, result.size());
        assertTrue(result.get(0).getCreatedAt().isAfter(result.get(1).getCreatedAt()));
    }
}
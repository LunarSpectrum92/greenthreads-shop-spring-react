package com.konopka.ecommerce.services;

import com.Konopka.eCommerce.DTO.*;
import com.Konopka.eCommerce.kafka.OrderMessageProducer;
import com.Konopka.eCommerce.models.*;
import com.Konopka.eCommerce.repositories.OrderProductRepository;
import com.Konopka.eCommerce.repositories.OrderRepository;
import com.Konopka.eCommerce.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProductRepository orderProductRepository;

    @Mock
    private OrderMessageProducer orderMessageProducer;

    @Mock
    private ClientFeign clientFeign;

    @Mock
    private ProductFeign productFeign;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private OrderService orderService;

    private ClientDto clientDto;
    private OrderProductDto orderProductDto;
    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        clientDto = new ClientDto(1, "123-456-789", "keycloak-user-123", LocalDateTime.now(), null, null);

        orderProductDto = new OrderProductDto(1, 2, new BigDecimal("49.99"));

        orderRequest = new OrderRequest("keycloak-user-123", List.of(orderProductDto));

        order = Order.builder()
                .orderId(1)
                .clientId("1")
                .totalAmount(new BigDecimal("49.99"))
                .status(Status.WAITING_FOR_PAYMENT)
                .orderDate(LocalDateTime.now())
                .orderProductsList(List.of())
                .build();
    }


    @Test
    void createOrder_whenCalledByAdminForAnyClient_returnsCreated() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.of(new ProductDto(1, "Test Product", "Description", "Brand", 49.99, 10, List.of(), 0, List.of())));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        verify(orderMessageProducer).produceOrderMessage(any(OrderDto.class));
    }

    @Test
    void createOrder_whenCalledByOwner_returnsCreated() {
        mockUserAuth("keycloak-user-123");
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.of(new ProductDto(1, "Test Product", "Description", "Brand", 49.99, 10, List.of(), 0, List.of())));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    }

    @Test
    void createOrder_whenNonOwnerNonAdmin_returnsForbidden() {
        mockUserAuth("other-user-456");

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        verifyNoInteractions(clientFeign, productFeign, orderRepository);
    }

    @Test
    void createOrder_whenOrderRequestIsNull_returnsBadRequest() {
        mockAdminAuth();

        ResponseEntity<OrderDto> response = orderService.createOrder(null, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createOrder_whenOrderProductsListIsEmpty_returnsBadRequest() {
        mockAdminAuth();
        OrderRequest emptyRequest = new OrderRequest("keycloak-user-123", List.of());

        ResponseEntity<OrderDto> response = orderService.createOrder(emptyRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void createOrder_whenClientNotFound_returnsNotFound() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.notFound().build());

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void createOrder_whenProductNotFound_returnsNotFound() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.empty());

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getHeaders().getFirst("Error-Message")).isEqualTo("Product not found");
    }

    @Test
    void createOrder_whenOrderRepositoryThrowsException_returnsInternalServerError() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.of(new ProductDto(1, "Test Product", "Description", "Brand", 49.99, 10, List.of(), 0, List.of())));
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createOrder_whenProductRepositoryThrowsException_returnsInternalServerError() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.of(new ProductDto(1, "Test Product", "Description", "Brand", 49.99, 10, List.of(), 0, List.of())));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderProductRepository.save(any(OrderProduct.class))).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<OrderDto> response = orderService.createOrder(orderRequest, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Test
    void createOrder_whenOrderCreated_sendsKafkaMessage() {
        mockAdminAuth();
        when(clientFeign.getClientByKeycloakId(anyString())).thenReturn(ResponseEntity.ok(clientDto));
        when(productFeign.findById(anyInt(), anyInt())).thenReturn(Optional.of(new ProductDto(1, "Test Product", "Description", "Brand", 49.99, 10, List.of(), 0, List.of())));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderService.createOrder(orderRequest, authentication);

        verify(orderMessageProducer, times(1)).produceOrderMessage(any(OrderDto.class));
    }


    @Test
    void getOrder_whenOrderExistsAndUserIsOwner_returnsOk() {
        mockUserAuth("keycloak-user-123");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(clientFeign.getClient(anyInt())).thenReturn(ResponseEntity.ok(clientDto));

        ResponseEntity<OrderDto> response = orderService.getOrder(1, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void getOrder_whenOrderExistsAndUserIsAdmin_returnsOk() {
        mockAdminAuth();
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(clientFeign.getClient(anyInt())).thenReturn(ResponseEntity.ok(clientDto));

        ResponseEntity<OrderDto> response = orderService.getOrder(1, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void getOrder_whenOrderNotFound_returnsNotFound() {
        when(orderRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<OrderDto> response = orderService.getOrder(99, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrder_whenUserIsNotOwnerAndNotAdmin_returnsForbidden() {
        mockUserAuth("other-user-456");
        when(orderRepository.findById(1)).thenReturn(Optional.of(order));
        when(clientFeign.getClient(anyInt())).thenReturn(ResponseEntity.ok(clientDto));

        ResponseEntity<OrderDto> response = orderService.getOrder(1, authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void getAllOrders_whenOrdersExist_returnsOkWithSortedList() {
        Order order1 = Order.builder().orderId(1).orderDate(LocalDateTime.now().minusDays(1)).orderProductsList(List.of()).build();
        Order order2 = Order.builder().orderId(2).orderDate(LocalDateTime.now()).orderProductsList(List.of()).build();
        when(orderRepository.findAll()).thenReturn(List.of(order2, order1));

        ResponseEntity<List<OrderDto>> response = orderService.getAllOrders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody().get(0).orderId()).isEqualTo(1);
    }

    @Test
    void getAllOrders_whenNoOrdersExist_returnsNotFound() {
        when(orderRepository.findAll()).thenReturn(List.of());

        ResponseEntity<List<OrderDto>> response = orderService.getAllOrders();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }


    @Test
    void getOrdersByClientId_whenOrdersExistAndUserIsOwner_returnsOkWithSortedList() {
        mockUserAuth("keycloak-user-123");
        when(orderRepository.findAllByClientId("1")).thenReturn(List.of(order));
        when(clientFeign.getClient(anyInt())).thenReturn(ResponseEntity.ok(clientDto));

        ResponseEntity<List<OrderDto>> response = orderService.getOrdersByClientId("1", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(1);
    }

    @Test
    void getOrdersByClientId_whenNoOrdersFound_returnsNotFound() {
        when(orderRepository.findAllByClientId("99")).thenReturn(List.of());

        ResponseEntity<List<OrderDto>> response = orderService.getOrdersByClientId("99", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getOrdersByClientId_whenUserIsNotOwnerAndNotAdmin_returnsForbidden() {
        mockUserAuth("other-user-456");
        when(orderRepository.findAllByClientId("1")).thenReturn(List.of(order));
        when(clientFeign.getClient(anyInt())).thenReturn(ResponseEntity.ok(clientDto));

        ResponseEntity<List<OrderDto>> response = orderService.getOrdersByClientId("1", authentication);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }


    @Test
    void checkIfAllowed_whenUserIsAdmin_returnsTrue() {
        mockAdminAuth();
        when(clientFeign.getClient(1)).thenReturn(ResponseEntity.ok(clientDto));

        boolean result = orderService.checkIfAllowed(1, authentication);

        assertThat(result).isTrue();
    }

    @Test
    void checkIfAllowed_whenUserIsOwner_returnsTrue() {
        mockUserAuth("keycloak-user-123");
        when(clientFeign.getClient(1)).thenReturn(ResponseEntity.ok(clientDto));

        boolean result = orderService.checkIfAllowed(1, authentication);

        assertThat(result).isTrue();
    }

    @Test
    void checkIfAllowed_whenUserIsNeitherAdminNorOwner_returnsFalse() {
        mockUserAuth("other-user-456");
        when(clientFeign.getClient(1)).thenReturn(ResponseEntity.ok(clientDto));

        boolean result = orderService.checkIfAllowed(1, authentication);

        assertThat(result).isFalse();
    }

    @Test
    void checkIfAllowed_whenClientFeignReturnsError_returnsFalse() {
        when(clientFeign.getClient(1)).thenReturn(ResponseEntity.notFound().build());

        boolean result = orderService.checkIfAllowed(1, authentication);

        assertThat(result).isFalse();
    }

    @Test
    void checkIfAllowed_whenClientFeignReturnsNullBody_returnsFalse() {
        when(clientFeign.getClient(1)).thenReturn(ResponseEntity.ok(null));

        boolean result = orderService.checkIfAllowed(1, authentication);

        assertThat(result).isFalse();
    }


    private void mockAdminAuth() {
        GrantedAuthority adminAuthority = () -> "ROLE_Admin";
        doReturn((Collection<GrantedAuthority>) List.of(adminAuthority))
                .when(authentication).getAuthorities();
        lenient().when(authentication.getName()).thenReturn("admin-user");
    }

    private void mockUserAuth(String keycloakId) {
        GrantedAuthority userAuthority = () -> "ROLE_User";
        doReturn((Collection<GrantedAuthority>) List.of(userAuthority))
                .when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(keycloakId);
    }
}
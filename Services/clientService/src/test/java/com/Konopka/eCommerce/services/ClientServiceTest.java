package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.DTO.ClientRequest;
import com.Konopka.eCommerce.models.Client;
import com.Konopka.eCommerce.repositories.ClientRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private Authentication auth;

    @InjectMocks
    private ClientService clientService;

    private Client mockClient;
    private final String KEYCLOAK_ID = "user-123";

    @BeforeEach
    void setUp() {
        mockClient = Client.builder()
                .keycloakId(KEYCLOAK_ID)
                .phone("123456789")
                .build();
    }

    @Test
    void getClients_Always_ReturnsList() {
        when(clientRepository.findAll()).thenReturn(List.of(mockClient));

        List<Client> result = clientService.getClients();

        assertEquals(1, result.size());
        verify(clientRepository).findAll();
    }

    @Test
    void getClientById_AdminAccess_ReturnsOk() {
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_Admin")))
                .when(auth).getAuthorities();
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));

        ResponseEntity<Client> response = clientService.getClientById(1, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockClient, response.getBody());
    }

    @Test
    void getClientById_UserUnauthorizedAccess_ReturnsForbidden() {
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        when(auth.getName()).thenReturn("other-user");
        when(clientRepository.findById(1)).thenReturn(Optional.of(mockClient));

        ResponseEntity<Client> response = clientService.getClientById(1, auth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void getClientById_NotFound_ReturnsNotFound() {
        lenient().doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_Admin")))
                .when(auth).getAuthorities();
        when(clientRepository.findById(99)).thenReturn(Optional.empty());

        ResponseEntity<Client> response = clientService.getClientById(99, auth);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void createClient_ValidData_ReturnsCreated() {
        ClientRequest request = new ClientRequest("987654321", null);
        when(auth.getName()).thenReturn(KEYCLOAK_ID);
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        ResponseEntity<Client> response = clientService.createClient(request, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
    }


    @Test
    void updateClientData_UserExists_ReturnsOk() {
        ClientRequest request = new ClientRequest("555", null);
        when(auth.getName()).thenReturn(KEYCLOAK_ID);
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(mockClient));
        when(clientRepository.save(any(Client.class))).thenReturn(mockClient);

        ResponseEntity<Client> response = clientService.updateClientData(request, auth);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("555", mockClient.getPhone());
    }


    @Test
    void deleteClientByKeycloakId_AdminAccess_ReturnsOk() {
        doReturn(Collections.singletonList(new SimpleGrantedAuthority("ROLE_Admin")))
                .when(auth).getAuthorities();
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(mockClient));

        ResponseEntity<Client> response = clientService.deleteClientByKeycloakId(KEYCLOAK_ID, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(clientRepository).delete(mockClient);
    }

    @Test
    void deleteClientByKeycloakId_Forbidden_ReturnsForbidden() {
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        when(auth.getName()).thenReturn("wrong-user");
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(mockClient));

        ResponseEntity<Client> response = clientService.deleteClientByKeycloakId(KEYCLOAK_ID, auth);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(clientRepository, never()).delete(any());
    }

    @Test
    void getClientByKeycloakId_ValidAccess_ReturnsOk() {
        when(auth.getName()).thenReturn(KEYCLOAK_ID);
        when(auth.getAuthorities()).thenReturn(Collections.emptyList());
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(mockClient));

        ResponseEntity<Client> response = clientService.getClientByKeycloakId(KEYCLOAK_ID, auth);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}
package com.Konopka.eCommerce.services;

import com.Konopka.eCommerce.models.Address;
import com.Konopka.eCommerce.models.Client;
import com.Konopka.eCommerce.repositories.AddressRepository;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AddressServiceTest {

    @Mock
    private AddressRepository addressRepository;
    @Mock
    private ClientRepository clientRepository;
    @Mock
    private Authentication authentication;

    @InjectMocks
    private AddressService addressService;

    private Address address;
    private Client client;
    private final String KEYCLOAK_ID = "user-123";

    @BeforeEach
    void setUp() {
        address = Address.builder()
                .addressId(100)
                .country("Poland")
                .city("Warszawa")
                .street("Wiejska")
                .houseNumber("12")
                .flatNumber("12")
                .postalCode("00-001")
                .build();

        client = new Client();
        client.setKeycloakId(KEYCLOAK_ID);
        client.setAddress(address);
    }

    @Test
    void updateAddress_AsAdmin_Success() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_Admin")))
                .when(authentication).getAuthorities();
        when(addressRepository.existsById(address.getAddressId())).thenReturn(true);
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        ResponseEntity<Address> response = addressService.updateAddress(address, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(address, response.getBody());
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_AsClient_Success() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_User")))
                .when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(KEYCLOAK_ID);
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(client));
        when(addressRepository.existsById(address.getAddressId())).thenReturn(true);
        when(addressRepository.save(any(Address.class))).thenReturn(address);

        ResponseEntity<Address> response = addressService.updateAddress(address, authentication);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(addressRepository).save(address);
    }

    @Test
    void updateAddress_AsClient_Forbidden_WrongAddressId() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_User")))
                .when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(KEYCLOAK_ID);

        Address differentAddress = Address.builder().addressId(999).build();
        client.setAddress(differentAddress);

        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.of(client));

        ResponseEntity<Address> response = addressService.updateAddress(address, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        verify(addressRepository, never()).save(any());
    }

    @Test
    void updateAddress_ClientNotFound_ReturnsForbidden() {
        doReturn(Collections.emptyList()).when(authentication).getAuthorities();
        when(authentication.getName()).thenReturn(KEYCLOAK_ID);
        when(clientRepository.findByKeycloakId(KEYCLOAK_ID)).thenReturn(Optional.empty());

        ResponseEntity<Address> response = addressService.updateAddress(address, authentication);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    void updateAddress_AddressDoesNotExist_ReturnsNotFound() {
        doReturn(List.of(new SimpleGrantedAuthority("ROLE_Admin")))
                .when(authentication).getAuthorities();
        when(addressRepository.existsById(address.getAddressId())).thenReturn(false);

        ResponseEntity<Address> response = addressService.updateAddress(address, authentication);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(addressRepository, never()).save(any());
    }
}
package com.Konopka.eCommerce.models;


import com.Konopka.eCommerce.DTO.ClientDto;
import com.Konopka.eCommerce.configurations.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "CLIENTSERVICE",
        url = "http://${CLIENT_HOST:localhost}:9010/api/clients")
public interface ClientFeign {

    @GetMapping("/client/{userId}")
    ResponseEntity<ClientDto> getClient(@PathVariable Integer userId);


    @GetMapping("/client/keycloak/{keycloakId}")
    ResponseEntity<ClientDto> getClientByKeycloakId(@PathVariable String keycloakId);


}

package com.Konopka.eCommerce.controllers;


import com.Konopka.eCommerce.DTO.PhotoDto;
import com.Konopka.eCommerce.DTO.ClientRequest;
import com.Konopka.eCommerce.models.Client;
import com.Konopka.eCommerce.models.PhotoFeign;
import com.Konopka.eCommerce.services.ClientService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/clients")
public class ClientController {

    ClientService clientService;


    @Autowired
    public ClientController(ClientService clientService, PhotoFeign photoFeign) {
        this.clientService = clientService;
    }

    //get all clients
    @GetMapping("/clients")
    @PreAuthorize("hasRole('Admin')")
    public List<Client> getClients() {
        return clientService.getClients();
    }


    //get client by id
    @GetMapping("/client/{userId}")
    public ResponseEntity<Client> getClient(@PathVariable Integer userId, Authentication auth) {
        return clientService.getClientById(userId, auth);
    }


    @GetMapping("/client/keycloak/{keycloakId}")
    @PreAuthorize("hasRole('Admin') or authentication.name == #keycloakId")
    public ResponseEntity<Client> getClientByKeycloakId(@PathVariable String keycloakId, Authentication auth) {
        return clientService.getClientByKeycloakId(keycloakId, auth);
    }


    //create client
    @PostMapping("/client")
    @PreAuthorize("hasRole('Admin')")
    public ResponseEntity<Client> createClient(@Valid @RequestBody ClientRequest client, Authentication authentication) {
        return clientService.createClient(client, authentication);
    }


    //add avatar
    //value = "/photo", consumes = MediaType.MULTIPART_FORM_DATA_VALUE
    @PostMapping("/client/photo")
    public ResponseEntity<PhotoDto> addAvatar(@RequestParam("id") int id, @RequestParam("file") @Valid MultipartFile photo) {
        return clientService.addAvatar(id, photo);
    }


    // get photo from client
    @GetMapping("/client/photo/{id}")
    public ResponseEntity<String> findPhotoById(@PathVariable int id) {
        return clientService.findAvatarById(id);
    }


    //update client
    @PutMapping("/client")
    public ResponseEntity<Client> updateClient(@Valid @RequestBody ClientRequest client, Authentication authentication) {
        return clientService.updateClientData(client, authentication);
    }


    //remove client
    @DeleteMapping("/client")
    public ResponseEntity<Client> deleteClient(@RequestBody String clientKeycloakId, Authentication auth) {
        return clientService.deleteClientByKeycloakId(clientKeycloakId, auth);
    }


}

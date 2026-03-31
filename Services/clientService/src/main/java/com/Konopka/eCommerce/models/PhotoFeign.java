package com.Konopka.eCommerce.models;


import com.Konopka.eCommerce.DTO.PhotoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

@FeignClient(name = "PHOTOSERVICE",
        url = "http://${PHOTO_HOST:localhost}:8761/api/photos",
        configuration = FeignSupportConfig.class)
public interface PhotoFeign {


    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    ResponseEntity<PhotoDto> addPhoto(@RequestPart("file") MultipartFile photo);


    @GetMapping("/photo/{id}")
    ResponseEntity<String> findPhotoById(@PathVariable int id);


}

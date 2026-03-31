package com.Konopka.eCommerce.models;

import com.Konopka.eCommerce.DTO.PhotoFullDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

@FeignClient(name = "PHOTOSERVICE",
        url = "http://${PHOTO_HOST:localhost}:9030/api/photos",
        configuration = FeignSupportConfig.class)
public interface PhotoFeign {


    @PostMapping(value = "/photo", consumes = "multipart/form-data")
    ResponseEntity<PhotoFullDto> addPhoto(@RequestPart("file") MultipartFile photo);


    @GetMapping("/photo/{id}")
    ResponseEntity<Resource> findPhotoById(@PathVariable int id);


    @GetMapping("/photos")
    ResponseEntity<Set<String>> findPhotosByIds(@RequestParam List<Integer> ids);

}
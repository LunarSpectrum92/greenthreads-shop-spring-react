package com.Konopka.eCommerce.Controller;


import com.Konopka.eCommerce.models.Photo;
import com.Konopka.eCommerce.service.PhotoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {

    PhotoService photoService;

    @Autowired
    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @PostMapping("/photo")
    public ResponseEntity<Photo> addPhoto(@RequestParam("file") MultipartFile photo) {
        String contentType = photo.getContentType();
        System.out.println(contentType);
        try {
            return photoService.addPhoto(photo);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    @GetMapping("/photos/{id}")
    public ResponseEntity<Resource> findPhotoById(@PathVariable int id) {
        return photoService.findPhotoById(id);
    }


    @GetMapping("/photos")
    public ResponseEntity<Set<String>> findPhotosByIds(@RequestParam List<Integer> ids) {
        return photoService.findPhotosByIds(ids);
    }


}

package com.Konopka.eCommerce.PhotoService.service;

import com.Konopka.eCommerce.models.Photo;
import com.Konopka.eCommerce.PhotoService.Repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;

@Service
public class PhotoService {



    private static final String FilePath = "C:\\Users\\JK\\Documents\\projekty\\projektZdpai\\Services\\PhotoService\\src\\main\\java\\com\\Konopka\\eCommerce\\PhotoService\\photos";
    PhotoRepository photoRepository;

    @Autowired
    public PhotoService(PhotoRepository photoRepository) {
        this.photoRepository = photoRepository;
    }


    public ResponseEntity<Photo> addPhoto(MultipartFile photoFile) throws IOException {
        if(photoFile.isEmpty()){
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        String FileName = photoFile.getOriginalFilename();

        if (FileName == null || !FileName.contains(".")) {
            throw new RuntimeException("wrong name of file");
        }

        String FileExt = FileName.substring(FileName.lastIndexOf(".") + 1).toLowerCase();
        if (!FileExt.equals("jpg") && !FileExt.equals("jpeg") && !FileExt.equals("png")) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String uniqueFileName = UUID.randomUUID().toString() + "." + FileExt;

        Path targetPath = Paths.get(FilePath).resolve(uniqueFileName).normalize();


        if (!targetPath.startsWith(Paths.get(FilePath))) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Files.copy(photoFile.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Photo photo = Photo.builder()
                .photoPath(targetPath.toString())
                .photoName(FileName)
                .build();
        photoRepository.save(photo);

        return new ResponseEntity<>(photo, HttpStatus.CREATED);
    }



//    public ResponseEntity<byte[]> findPhotoById(Integer photoId) {
//        Optional<Photo> photoOpt = photoRepository.findById(photoId);
//        if (photoOpt.isEmpty()) {
//            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//        }
//
//        Path photoPath = Path.of(photoOpt.get().getPhotoPath()).normalize();
//        try {
//            Resource resource = new UrlResource(photoPath.toUri());
//            if (resource.exists() && resource.isReadable()) {
//                byte[] fileContent = Files.readAllBytes(photoPath);
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//                headers.setContentDisposition(ContentDisposition.builder("attachment")
//                        .filename(photoPath.getFileName().toString())
//                        .build());
//                return new ResponseEntity<>(fileContent, headers, HttpStatus.OK);
//            } else {
//                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
//            }
//        } catch (IOException e) {
//            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
//        }
//    }

//    public ResponseEntity<Resource> findPhotoById(Integer photoId) {
//        Optional<Photo> photoOpt = photoRepository.findById(photoId);
//        if (photoOpt.isEmpty()) {
//            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//        }
//
//        Path photoPath = Path.of(photoOpt.get().getPhotoPath()).normalize();
//        try {
//            Resource resource = new UrlResource(photoPath.toUri());
//            if (resource.exists() && resource.isReadable()) {
//                HttpHeaders headers = new HttpHeaders();
//                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
//                headers.setContentDisposition(ContentDisposition.builder("attachment")
//                        .filename(photoPath.getFileName().toString())
//                        .build());
//                return ResponseEntity.ok()
//                        .headers(headers)
//                        .body(resource);
//            } else {
//                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
//            }
//        } catch (IOException e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//        }
//    }



    public ResponseEntity<Resource> findPhotoById(Integer photoId) {
        Optional<Photo> photo = photoRepository.findById(photoId);
        if (photo.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        String photoPath = photo.get().getPhotoPath();
        if (photoPath == null || photoPath.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Path path = Path.of(photoPath).normalize();
        Resource resource = new FileSystemResource(path.toFile());

        if (!resource.exists() || !resource.isReadable()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        HttpHeaders headers = new HttpHeaders();
        String contentType = "image/" + photoPath.substring(photoPath.lastIndexOf('.') + 1).toLowerCase();
        headers.setContentType(MediaType.valueOf(contentType));
        headers.setContentDisposition(ContentDisposition.builder("inline")
                .filename(path.getFileName().toString())
                .build());

        return ResponseEntity.ok()
                .headers(headers)
                .body(resource);
    }



    public ResponseEntity<Set<String>> findPhotosByIds(List<Integer> photoIds) {
        if (photoIds == null || photoIds.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Set<Integer> uniquePhotoIds = new HashSet<>(photoIds);
        List<Photo> photoList = photoRepository.findAllById(uniquePhotoIds);

        if (photoList.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        Set<String> photoUrls = new HashSet<>();
        for (Photo photo : photoList) {
            String photoPath = photo.getPhotoPath();

            if (photoPath == null || photoPath.isEmpty()) {
                continue;
            }
            String photoUrl = "http://localhost:9030/api/photos/photos/" + photo.getPhotoId();
            photoUrls.add(photoUrl);
        }

        return new ResponseEntity<>(photoUrls, HttpStatus.OK);
    }




}

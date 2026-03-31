package com.Konopka.eCommerce.PhotoService.service;

import com.Konopka.eCommerce.Repository.PhotoRepository;
import com.Konopka.eCommerce.models.Photo;
import com.Konopka.eCommerce.service.PhotoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PhotoServiceTest {

    @Mock
    private PhotoRepository photoRepository;

    @InjectMocks
    private PhotoService photoService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(photoService, "FilePath", tempDir.toString());
    }


    @Test
    void addPhoto_fileIsEmpty_returnsBadRequest() throws IOException {
        MockMultipartFile emptyFile = new MockMultipartFile("photo", "", "image/jpeg", new byte[0]);

        ResponseEntity<Photo> response = photoService.addPhoto(emptyFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addPhoto_wrongExtension_returnsBadRequest() throws IOException {
        MockMultipartFile txtFile = new MockMultipartFile("photo", "test.txt", "text/plain", "data".getBytes());

        ResponseEntity<Photo> response = photoService.addPhoto(txtFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void addPhoto_noExtension_throwsRuntimeException() {
        MockMultipartFile noExtFile = new MockMultipartFile("photo", "filenameWithoutDot", "image/jpeg", "data".getBytes());

        assertThatThrownBy(() -> photoService.addPhoto(noExtFile))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("wrong name of file");
    }

    @Test
    void addPhoto_validFile_returnsCreatedAndSavesToRepo() throws IOException {
        MockMultipartFile validFile = new MockMultipartFile("photo", "image.jpg", "image/jpeg", "image content".getBytes());
        when(photoRepository.save(any(Photo.class))).thenAnswer(i -> i.getArguments()[0]);
        ResponseEntity<Photo> response = photoService.addPhoto(validFile);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getPhotoName()).isEqualTo("image.jpg");
        assertThat(response.getBody().getPhotoPath()).contains(tempDir.toString());
        verify(photoRepository, times(1)).save(any(Photo.class));
    }


    @Test
    void findPhotoById_photoNotFoundInDb_returnsNotFound() {
        when(photoRepository.findById(1)).thenReturn(Optional.empty());

        ResponseEntity<Resource> response = photoService.findPhotoById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findPhotoById_fileDoesNotExistOnDisk_returnsNotFound() {
        Photo photo = Photo.builder().photoId(1).photoPath("non/existent/path.jpg").build();
        when(photoRepository.findById(1)).thenReturn(Optional.of(photo));

        ResponseEntity<Resource> response = photoService.findPhotoById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findPhotoById_validPhoto_returnsOkWithResource() throws IOException {
        Path file = tempDir.resolve("test.png");
        Files.write(file, "content".getBytes());
        
        Photo photo = Photo.builder()
                .photoId(1)
                .photoPath(file.toString())
                .photoName("test.png")
                .build();
        
        when(photoRepository.findById(1)).thenReturn(Optional.of(photo));

        ResponseEntity<Resource> response = photoService.findPhotoById(1);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getHeaders().getContentType().toString()).isEqualTo("image/png");
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().exists()).isTrue();
    }


    @Test
    void findPhotosByIds_emptyListProvided_returnsBadRequest() {
        ResponseEntity<Set<String>> response = photoService.findPhotosByIds(Collections.emptyList());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void findPhotosByIds_noPhotosFoundInDb_returnsNotFound() {
        List<Integer> ids = List.of(1, 2);
        when(photoRepository.findAllById(anySet())).thenReturn(Collections.emptyList());

        ResponseEntity<Set<String>> response = photoService.findPhotosByIds(ids);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void findPhotosByIds_validIds_returnsSetOfUrls() {
        List<Integer> ids = List.of(1, 2);
        Photo p1 = Photo.builder().photoId(1).photoPath("path1.jpg").build();
        Photo p2 = Photo.builder().photoId(2).photoPath("path2.jpg").build();
        
        when(photoRepository.findAllById(anySet())).thenReturn(List.of(p1, p2));

        ResponseEntity<Set<String>> response = photoService.findPhotosByIds(ids);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).hasSize(2);
        assertThat(response.getBody()).containsExactlyInAnyOrder(
                "http://localhost:9030/api/photos/photos/1",
                "http://localhost:9030/api/photos/photos/2"
        );
    }
}
package studio.manga.upload;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

  private final Path uploadDir;

  public UploadController() {
    this.uploadDir = Paths.get("uploads").toAbsolutePath().normalize();
    try {
      Files.createDirectories(this.uploadDir);
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory", e);
    }
  }

  @PostMapping
  public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
    try {
      String originalFilename = file.getOriginalFilename();
      String extension = "";
      if (originalFilename != null && originalFilename.contains(".")) {
        extension = originalFilename.substring(originalFilename.lastIndexOf("."));
      }
      String filename = UUID.randomUUID().toString() + extension;
      Path targetLocation = this.uploadDir.resolve(filename);
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

      String fileUrl = "http://localhost:8080/uploads/" + filename;
      return ResponseEntity.ok(new UploadResponse(fileUrl));
    } catch (IOException e) {
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }
  }

  public record UploadResponse(String url) {}
}

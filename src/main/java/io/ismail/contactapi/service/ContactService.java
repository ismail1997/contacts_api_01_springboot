package io.ismail.contactapi.service;


import io.ismail.contactapi.domain.Contact;
import io.ismail.contactapi.repository.IContactRepository;
import io.ismail.contactapi.utility.Constant;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

@Transactional(rollbackOn = Exception.class)
@Slf4j
@Service
@RequiredArgsConstructor
public class ContactService {
    private final IContactRepository contactRepository;

    public Page<Contact> getAllContacts(int page,int size)
    {
        return contactRepository.findAll(PageRequest.of(page,size, Sort.by("name")));
    }

    public Contact getContact(String id){
        return contactRepository.findById(id).orElseThrow(()->new RuntimeException("Contact Not Found"));
    }

    public Contact createContact(Contact contact)
    {
        return contactRepository.save(contact);
    }

    public void deleteContact(Contact contact)
    {
        this.contactRepository.delete(contact);
    }

    public String uploadPhoto(String id, MultipartFile file)
    {
        log.info("Saving photo ...");
        Contact contact = getContact(id);
        String photoUrl = photoFunction.apply(id,file);
        contact.setPhotoUrl(photoUrl);
        contactRepository.save(contact);
        return photoUrl;
    }

    private final Function<String,String> fileExtension = filename -> Optional.of(filename).filter(name->name.contains("."))
      .map(name->"."+name.substring(filename.lastIndexOf(".")+1)).orElse(".png");

    private final BiFunction<String,MultipartFile,String> photoFunction = (id,image)->{
      try{
          Path fileStorageLocation  = Paths.get(Constant.PHOTO_DIRECTORY_LOCATION).toAbsolutePath().normalize();
          if(!Files.exists(fileStorageLocation))
          {
              Files.createDirectories(fileStorageLocation);
          }
          Files.copy(image.getInputStream(),fileStorageLocation.resolve(id+fileExtension.apply(image.getOriginalFilename())), StandardCopyOption.REPLACE_EXISTING);
          return ServletUriComponentsBuilder.fromCurrentContextPath().path("/contacts/image/"+id+fileExtension.apply(image.getOriginalFilename())).toUriString();
      }catch(Exception exception){
          throw new RuntimeException("Unable to save image");
      }

    };
}

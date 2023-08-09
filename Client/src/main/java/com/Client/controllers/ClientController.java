package com.Client.controllers;

import com.Client.services.BookAuthorClientService;
import com.google.protobuf.Descriptors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gRpc")
public class ClientController {

    private BookAuthorClientService bookAuthorClientService;

    @Autowired
    ClientController(BookAuthorClientService bookAuthorClientService){
        this.bookAuthorClientService= bookAuthorClientService ;
    }

    @GetMapping("{author-id}")
    public Map<Descriptors.FieldDescriptor, Object> getAuthorById(@PathVariable(value = "author-id") int id){
        return bookAuthorClientService.getAuthor(id);
    }
    @GetMapping("/books/{author-id}")
    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(@PathVariable(value = "author-id") int authorId) throws InterruptedException {
        return bookAuthorClientService.getBooksByAuthor(authorId);
    }

    @GetMapping()
    public Map<String,Map<Descriptors.FieldDescriptor,Object>> getExpensiveBooks() throws InterruptedException{
        return bookAuthorClientService.getExpensiveBooks();
    }

    @GetMapping("/books/{gender}")
    public List<Map<Descriptors.FieldDescriptor,Object>>getBooksByGender(@PathVariable String gender) throws InterruptedException{
        return bookAuthorClientService.getBooksByGender(gender);
    }

}

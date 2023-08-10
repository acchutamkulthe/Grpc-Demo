package com.Client.services;


import com.Client.db.TempDb;
import com.gRpc.Author;
import com.gRpc.Book;
import com.gRpc.BookAuthorServiceGrpc;
import com.google.protobuf.Descriptors;

import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class BookAuthorClientService {

    @GrpcClient("grpc-client")
    BookAuthorServiceGrpc.BookAuthorServiceBlockingStub synchronousClient;
    @GrpcClient("grpc-client")
    BookAuthorServiceGrpc.BookAuthorServiceStub asynchronousClient;

    public Map<Descriptors.FieldDescriptor, Object> getAuthor(int authorId) {
        Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        Author authorResponse = synchronousClient.getAuthor(authorRequest);
        return authorResponse.getAllFields();
    }

    public List<Map<Descriptors.FieldDescriptor, Object>> getBooksByAuthor(int authorId) throws InterruptedException {
        final Author authorRequest = Author.newBuilder().setAuthorId(authorId).build();
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final List<Map<Descriptors.FieldDescriptor, Object>> response = new ArrayList<>();
        asynchronousClient.getBooksByAuthor(authorRequest, new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyList();
    }

    public Map<String,Map<Descriptors.FieldDescriptor,Object>> getExpensiveBooks() throws InterruptedException {
        Map<String,Map<Descriptors.FieldDescriptor,Object>> response = new HashMap<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<Book> responseObserver = asynchronousClient.getExpensiveBook(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.put("Expensive Book" , book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDb.getBooksFromTempDb().stream().forEach(responseObserver::onNext);
        responseObserver.onCompleted();
        boolean await = countDownLatch.await(1, TimeUnit.MINUTES);
        return await ? response : Collections.emptyMap();
    }

    public List<Map<Descriptors.FieldDescriptor,Object>>getBooksByGender(String gender) throws InterruptedException {
        List<Map<Descriptors.FieldDescriptor,Object>> response = new ArrayList<>();
        CountDownLatch countDownLatch = new CountDownLatch(1);
        StreamObserver<Book> responseObserver = asynchronousClient.getBooksByGender(new StreamObserver<Book>() {
            @Override
            public void onNext(Book book) {
                response.add(book.getAllFields());
            }

            @Override
            public void onError(Throwable throwable) {
                countDownLatch.countDown();
            }

            @Override
            public void onCompleted() {
                countDownLatch.countDown();
            }
        });
        TempDb.getAuthorsFromTempDb()
                .stream()
                .filter(author->author.getGender().equalsIgnoreCase(gender))
                .forEach(author->responseObserver.onNext(Book.newBuilder().setAuthorId(author.getAuthorId()).build()));
        boolean await = countDownLatch.await(1,TimeUnit.MINUTES);

        return await?response : Collections.emptyList();
    }


}

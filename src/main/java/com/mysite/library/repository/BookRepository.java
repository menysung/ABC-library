package com.mysite.library.repository;

import com.mysite.library.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookRepository extends JpaRepository<Book, String> {
    Page<Book> findByIsbnContaining(String isbn, Pageable pageable);
    Page<Book> findByTitleContaining(String title, Pageable pageable);
    Page<Book> findByDescriptionContaining(String description, Pageable pageable);
    Page<Book> findByAuthorContaining(String author, Pageable pageable);
    Page<Book> findByPublisherContaining(String publisher, Pageable pageable);
    Page<Book> findAllByOrderByCreatedAtDesc(Pageable pageable);
}

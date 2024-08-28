package com.mysite.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Date;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rsvIdx;

    @ManyToOne
    @JoinColumn(name = "rsv_user_idx")
    private UserEntity userEntity;

    @ManyToOne
    @JoinColumn(name = "rsv_book_isbn")
    private Book book;

    @ManyToOne
    @JoinColumn(name = "rsv_book_title")
    public Book rsvBookTitle;

    @ManyToOne
    @JoinColumn(name = "rsv_book_author")
    public Book rsvBookAuthor;

    private Date rsvDate;
    private Date rsvConfirmDate;
    private Date rsvDueDate;
    private Date rsvCclDate;
}

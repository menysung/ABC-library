package com.mysite.library.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;


import java.time.LocalDateTime;
import java.util.Date;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Book {

    @Id
    //    @GeneratedValue(strategy = GenerationType.IDENTITY) 풀면 전체 리스트 안나옴
    private String isbn; // 도서번호

    private String image; // 이미지 파일
    private String title; // 도서명
    private String author; // 작가명
    private String publisher; // 출판사

    @Column(nullable = false)
    private Boolean rentAvailable = true; // 기본값 설정
    private Boolean rsvAvailable = false;

    @Column(length = 1000)
    private String description; // 도서 설명

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created_at", nullable = false, updatable = false)
    private Date createdAt; //도서 create 순으로 정렬

    private boolean rentedByCurrentUser;
    private boolean reservedByCurrentUser;
}
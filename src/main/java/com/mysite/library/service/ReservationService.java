package com.mysite.library.service;

import com.mysite.library.dto.ReservationDTO;
import com.mysite.library.entity.Book;
import com.mysite.library.entity.Reservation;
import com.mysite.library.entity.UserEntity;
import com.mysite.library.repository.BookRepository;
import com.mysite.library.repository.ReservationRepository;
import com.mysite.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
    private final ReservationRepository reservationRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    public boolean reserveBook(UserEntity user, String isbn) {
        // 중복 예약 체크
        Optional<Reservation> existingReservation = reservationRepository.findByUserEntityIdxAndBookIsbn(user.getIdx(), isbn);
        if (existingReservation.isPresent()) {
            return false; // 이미 예약된 경우
        }
        return true;
    }

    public List<Reservation> findAllReservations() {
        return reservationRepository.findAll();
    }

    public Reservation saveReservation(ReservationDTO reservationDTO) {
        Reservation reservation = new Reservation();
        reservation.setRsvDate(reservationDTO.getRsvDate());

        UserEntity user = userRepository.findById(reservationDTO.getRsvUserIdx())
                .orElseThrow(() -> new RuntimeException("User Not Found"));
        reservation.setUserEntity(user);

        Book book = bookRepository.findById(reservationDTO.getRsvBookIsbn())
                .orElseThrow(() -> new RuntimeException("Book not found"));
        reservation.setBook(book);
        return reservationRepository.save(reservation);
    }

    public List<ReservationDTO> getRsvInfo(String name) {
        Optional<UserEntity> user = userRepository.findByName(name);
        if (user.isPresent()) {
            List<Reservation> rsvs = reservationRepository.findByUserEntityIdx(user.get().getIdx());
            return rsvs.stream().map(rsv -> {
                return getReservationDTO(rsv);
            }).collect(Collectors.toList());
        }
        return List.of();
    }

    static ReservationDTO getReservationDTO(Reservation rsv) {
        ReservationDTO dto = new ReservationDTO();
        dto.setRsvIdx(rsv.getRsvIdx());
        dto.setRsvUserIdx(rsv.getUserEntity().getIdx());
        dto.setRsvBookIsbn(rsv.getBook().getIsbn());
        dto.setRsvDate(rsv.getRsvDate());
        dto.setRsvDueDate(rsv.getRsvDueDate());
        dto.setRsvConfirmDate(rsv.getRsvConfirmDate());
        dto.setRsvCclDate(rsv.getRsvCclDate());
        dto.setRsvBookTitle(rsv.getBook().getTitle());
        dto.setRsvBookAuthor(rsv.getBook().getAuthor());
        return dto;
    }
}

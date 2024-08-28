package com.mysite.library.controller;


import com.mysite.library.dto.ReservationDTO;
import com.mysite.library.entity.UserEntity;
import com.mysite.library.repository.UserRepository;
import com.mysite.library.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.sql.Date;
import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;
    private final UserRepository userRepository;

    // 회원이 예약 버튼 누르면 관리자 페이지로 예약 내역 전달 a로 걸려있기 때문에 GET메소드
    @GetMapping("/reserve")
    public String reserveBook(Principal principal,
                              @RequestParam("rsvBookIsbn") String bookIsbn,
                              Model model) {
        if (principal == null) {
            return "redirect:/user/login";
        } else {


            UserEntity user = userRepository.findByName(principal.getName()).orElseThrow(()->new RuntimeException("유저가 없음"));


            // 중복 예약 체크
            boolean success = reservationService.reserveBook(user, bookIsbn);
            if (success) {
                ReservationDTO reservationDTO = new ReservationDTO();
                reservationDTO.setRsvUserIdx(user.getIdx());
                reservationDTO.setRsvBookIsbn(bookIsbn);
                reservationDTO.setRsvDate(Date.valueOf(LocalDate.now()));
                reservationService.saveReservation(reservationDTO);
                model.addAttribute("message", "예약이 성공적으로 완료되었습니다.");
            } else {
                model.addAttribute("message", "이미 해당 책을 예약하셨습니다.");
            }
        }
        // return "redirect:/reservation-list";
        return "user/reservationResult";
    }

    }



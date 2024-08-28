package com.mysite.library.controller;

import com.mysite.library.dto.RentDTO;
import com.mysite.library.dto.ReservationDTO;
import com.mysite.library.entity.Rent;
import com.mysite.library.service.AdminService;
import com.mysite.library.service.RentService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.sql.Date;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController1 {

    private final RentService rentService;
    private final AdminService adminService;

    @GetMapping("/rents")
    public String listRents(
            @RequestParam(value = "rentdate", required = false) String rent_date,
            @RequestParam(value = "duedate", required = false) String due_date,
            @RequestParam(value = "rentUserIdx", required = false) Long rentUserIdx,
            @RequestParam(value = "rentBookIsbn", required = false) String rentBookIsbn,
            Model model) {
        List<RentDTO> rents = adminService.searchRentsAdmin(rentBookIsbn, rentUserIdx, rent_date, due_date);
        model.addAttribute("rents", rents);
        model.addAttribute("rentBookIsbn", rentBookIsbn);
        model.addAttribute("rentUserIdx", rentUserIdx);
        model.addAttribute("rentdate", rent_date);
        model.addAttribute("duedate", due_date);
        return "admin/rental_list";
    }


    @GetMapping("/reservations")
    public String viewReservations(@RequestParam(value = "rsvBookIsbn", required = false) String rsvBookIsbn,
                                   @RequestParam(value = "rsvUserIdx", required = false) Long rsvUserIdx,
                                   @RequestParam(value = "rsvDate", required = false) String rsvDate,
                                   @RequestParam(required = false, defaultValue = "false") boolean includeCancelled,
                                   Model model) {
        List<ReservationDTO> reservations = adminService.searchReservations(rsvBookIsbn, rsvUserIdx, rsvDate, includeCancelled);
        model.addAttribute("reservations", reservations);
        model.addAttribute("rsvBookIsbn", rsvBookIsbn);
        model.addAttribute("rsvUserIdx", rsvUserIdx);
        model.addAttribute("rsvDate", rsvDate);
        model.addAttribute("includeCancelled", includeCancelled);
        return "admin/reservation_list";
    }


    @PostMapping("/confirmReservation/{rsvIdx}")
    public String confirmReservation(@PathVariable Integer rsvIdx) {
        adminService.confirmReservation(rsvIdx);
        return "redirect:/admin/reservations";
    }

    @PostMapping("/cancelReservation/{rsvIdx}")
    public String cancelReservation(@PathVariable Integer rsvIdx) {
        adminService.cancelReservation(rsvIdx);
        return "redirect:/admin/reservations";
    }


}

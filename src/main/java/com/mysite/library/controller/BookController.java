package com.mysite.library.controller;

import com.mysite.library.dto.BookDTO;
import com.mysite.library.dto.BookSearchDTO;
import com.mysite.library.entity.Book;
import com.mysite.library.service.BookAPIService;
import com.mysite.library.service.BookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/books")
public class BookController {

    @Autowired
    private BookService bookService;

    @Autowired
    private BookAPIService bookAPIService; //api 테스트 할 때 필요함

    @Value("${file.upload-dir}")
    private String uploadDir;

    // 도서 등록하기
    @GetMapping("/create")
    public String showForm(@RequestParam(value = "isbn", required = false) String isbn, Model model) {
        BookDTO bookDTO = new BookDTO();
        if (isbn != null) {
            Optional<Book> book = bookService.findById(isbn);
            if (book.isPresent()) {
                bookDTO.setIsbn(book.get().getIsbn());
                bookDTO.setTitle(book.get().getTitle());
                bookDTO.setAuthor(book.get().getAuthor());
                bookDTO.setPublisher(book.get().getPublisher());
                bookDTO.setDescription(book.get().getDescription());
                bookDTO.setImage(book.get().getImage());
            }
        }
        model.addAttribute("book", bookDTO);
        return "books/create";
    }

    // 도서 등록 후 list 페이지 이동
    @PostMapping("/save")
    public String saveBook(@ModelAttribute("book") BookDTO bookDTO, @RequestParam("imageFile") MultipartFile imageFile, Model model) {
        Book book = new Book();
        book.setIsbn(bookDTO.getIsbn());
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublisher(bookDTO.getPublisher());
        book.setDescription(bookDTO.getDescription());

        if (!imageFile.isEmpty()) {
            try {
                File directory = new File(uploadDir);
                if (!directory.exists()) {
                    directory.mkdirs();
                }

                byte[] bytes = imageFile.getBytes();
                Path path = Paths.get(uploadDir + File.separator + imageFile.getOriginalFilename());
                Files.write(path, bytes);
                book.setImage("/uploads/" + imageFile.getOriginalFilename());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        bookService.save(book);
        model.addAttribute("message", "등록되었습니다");
        return "redirect:/books/list";
    }

    // 도서 목록
    @GetMapping("/list")
    public String listBooks(Model model,
                            @RequestParam(value = "page", defaultValue = "1") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        page = page < 1 ? 1 : page; // 페이지 번호가 1보다 작지 않도록 조정
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Book> bookPage = bookService.findBooks(pageable);

        model.addAttribute("books", bookPage.getContent());
        model.addAttribute("paging", bookPage);
        model.addAttribute("size", size);
        model.addAttribute("currentPage", page);
        return "books/list";
    }

    // 도서 삭제
    @PostMapping("/delete/{isbn}")
    public String deleteBook(@PathVariable("isbn") String isbn, @RequestParam(value = "currentPage", defaultValue = "1") int currentPage) {
        bookService.deleteById(isbn);
        currentPage = currentPage < 1 ? 1 : currentPage; // 페이지 번호가 1보다 작지 않도록 조정
        return "redirect:/books/list?page=" + currentPage;
    }

    // 도서 수정 폼
    @GetMapping("/edit/{isbn}")
    public String editBook(@PathVariable String isbn, @RequestParam(value = "page", defaultValue = "1") int page, Model model) {
        page = page < 1 ? 1 : page; // 페이지 번호가 1보다 작지 않도록 조정
        Optional<Book> book = bookService.findById(isbn);
        if (book.isPresent()) {
            BookDTO bookDTO = new BookDTO();
            bookDTO.setIsbn(book.get().getIsbn());
            bookDTO.setTitle(book.get().getTitle());
            bookDTO.setAuthor(book.get().getAuthor());
            bookDTO.setPublisher(book.get().getPublisher());
            bookDTO.setDescription(book.get().getDescription());
            bookDTO.setImage(book.get().getImage());
            model.addAttribute("book", bookDTO);
            model.addAttribute("currentPage", page);
            return "books/edit";
        }
        return "redirect:/books/list?page=" + page;
    }

    // 도서 업데이트
    @PostMapping("/update")
    public String updateBook(@ModelAttribute("book") BookDTO bookDTO, @RequestParam("imageFile") MultipartFile imageFile, @RequestParam(value = "currentPage", defaultValue = "1") int currentPage) {
        currentPage = currentPage < 1 ? 1 : currentPage; // 페이지 번호가 1보다 작지 않도록 조정
        Optional<Book> bookOpt = bookService.findById(bookDTO.getIsbn());
        if (bookOpt.isPresent()) {
            Book book = bookOpt.get();
            book.setTitle(bookDTO.getTitle());
            book.setAuthor(bookDTO.getAuthor());
            book.setPublisher(bookDTO.getPublisher());
            book.setDescription(bookDTO.getDescription());

            if (!imageFile.isEmpty()) {
                try {
                    File directory = new File(uploadDir);
                    if (!directory.exists()) {
                        directory.mkdirs();
                    }

                    byte[] bytes = imageFile.getBytes();
                    Path path = Paths.get(uploadDir + File.separator + imageFile.getOriginalFilename());
                    Files.write(path, bytes);
                    book.setImage("/uploads/" + imageFile.getOriginalFilename());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            bookService.save(book);
        }
        return "redirect:/books/list?page=" + currentPage;
    }

    // 도서 검색
    @GetMapping("/search")
    public String showSearchForm(Model model) {
        model.addAttribute("searchDto", new BookSearchDTO());
        return "books/search";
    }

    // 도서 검색 결과
    @PostMapping("/search/results")
    public String searchBooksPost(@ModelAttribute("searchDto") BookSearchDTO searchDto,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(defaultValue = "10") int size,
                                  Model model) throws IOException {
        return searchBooks(searchDto, page, size, model);
    }

    @GetMapping("/search/results")
    public String searchBooksGet(@ModelAttribute("searchDto") BookSearchDTO searchDto,
                                 @RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size,
                                 Model model) throws IOException {
        return searchBooks(searchDto, page, size, model);
    }

    private String searchBooks(BookSearchDTO searchDto, int page, int size, Model model) throws IOException {
        page = page < 1 ? 1 : page; // 페이지 번호가 1보다 작지 않도록 조정
        if (searchDto.getKeyword() == null || searchDto.getKeyword().trim().isEmpty()) {
            model.addAttribute("noResults", true);
            return "books/result";
        }

        Page<Book> books = bookService.searchBooks(searchDto.getSearchBy(), searchDto.getKeyword(), page - 1, size);
        model.addAttribute("books", books.getContent());
        model.addAttribute("paging", books);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", books.getTotalPages());
        model.addAttribute("size", size);
        model.addAttribute("searchDto", searchDto);
        if (books.isEmpty()) {
            model.addAttribute("noResults", true);
        }
        return "books/result";
    }

    //API 리스트 불러오기 테스트
    @GetMapping("/test")
    public void test() throws IOException {
        bookAPIService.searchAndSave();
    }

}
package com.mysite.library.service;

import com.mysite.library.repository.BookRepository;
import com.mysite.library.entity.Book;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class BookAPIService {

    private final BookRepository bookRepository;

    @Autowired
    public BookAPIService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @Transactional
    public void searchAndSave() throws IOException {
        String apiKey = System.getenv("ALADIN_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("API key not found in environment variables");
        }


        URL url = new URL("https://www.aladin.co.kr/ttb/api/ItemList.aspx?ttbkey=ttbmenysung94940906001&QueryType=BestSeller&MaxResults=200&start=1&SearchTarget=Book&output=js&Version=20131101");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();

        conn.setRequestMethod("GET");
        conn.setRequestProperty("Content-type", "application/json");

        BufferedReader rd;
        if (conn.getResponseCode() >= 200 && conn.getResponseCode() <= 300) {
            rd = new BufferedReader(new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8));
        } else {
            rd = new BufferedReader(new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8));
        }
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = rd.readLine()) != null) {
            sb.append(line);
        }
        rd.close();
        conn.disconnect();

        String response = sb.toString();
        System.out.println("API Response: " + response);  // API 응답 출력

        try {
            JSONTokener tokener = new JSONTokener(response);
            JSONObject jsonObject = new JSONObject(tokener);

            // 먼저 API 응답 형식을 확인
            System.out.println("Parsed JSON Object: " + jsonObject.toString(2));

            // 예시로 "items" 또는 "item" 키가 있는지 확인
            if (jsonObject.has("items")) { // "items" 키를 사용
                JSONArray arr = jsonObject.getJSONArray("items");
                List<Book> books = new ArrayList<>();

                for (Object one : arr) {
                    JSONObject ob = (JSONObject) one;
                    Book book = new Book();
                    book.setIsbn(ob.getString("isbn"));
                    book.setTitle(ob.getString("title"));
                    book.setAuthor(ob.getString("author"));
                    book.setPublisher(ob.getString("publisher"));
                    book.setImage(ob.getString("cover"));
                    book.setDescription(ob.getString("description"));

                    // 디버깅: 각 필드 값을 출력
                    System.out.println("Book Info: ISBN=" + book.getIsbn() + ", Title=" + book.getTitle() + ", Description=" + book.getDescription());

                    books.add(book);
                }

                try {
                    bookRepository.saveAll(books);
                    System.out.println("Books saved successfully.");
                } catch (Exception e) {
                    System.err.println("Error saving books: " + e.getMessage());
                }
            } else if (jsonObject.has("item")) { // "item" 키를 사용
                JSONArray arr = jsonObject.getJSONArray("item");
                List<Book> books = new ArrayList<>();

                for (Object one : arr) {
                    JSONObject ob = (JSONObject) one;
                    Book book = new Book();
                    book.setIsbn(ob.getString("isbn"));
                    book.setTitle(ob.getString("title"));
                    book.setAuthor(ob.getString("author"));
                    book.setPublisher(ob.getString("publisher"));
                    book.setImage(ob.getString("cover")); // url 사용
                    book.setDescription(ob.getString("description"));

                    // 디버깅: 각 필드 값을 출력
                    System.out.println("Book Info: ISBN=" + book.getIsbn() + ", Title=" + book.getTitle() + ", Description=" + book.getDescription());

                    books.add(book);
                }

                try {
                    bookRepository.saveAll(books);
                    System.out.println("Books saved successfully.");
                } catch (Exception e) {
                    System.err.println("Error saving books: " + e.getMessage());
                }
            } else {
                System.err.println("Error: 'items' or 'item' key not found in the API response.");
            }
        } catch (JSONException e) {
            System.err.println("Error parsing JSON: " + e.getMessage());
        }
    }
}
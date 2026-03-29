package com.newstickr.newstickr.news.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.newstickr.newstickr.news.dto.ReqPostNewsDto;
import com.newstickr.newstickr.news.dto.ResGetNewsDto;
import com.newstickr.newstickr.news.entity.News;
import com.newstickr.newstickr.news.repository.NewsRepository;
import com.newstickr.newstickr.user.entity.User;
import com.newstickr.newstickr.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Repository
@Slf4j
public class NewsServiceImpl implements NewsService {

    // 네이버 관련
    @Value("${api.naver.clientId}")
    private String clientId;
    @Value("${api.naver.clientSecret}")
    private String clientSecret;
    @Value("${api.naver.naverApiUrl}")
    private String naverApiUrl;

    // Groq 관련
    @Value("${api.grog.apiKey}")
    private String apiKey;
    @Value("${api.grog.grogApiUrl}")
    private String groqApiUrl;

    private final NewsRepository newsRepository;
    private final UserRepository userRepository;


    @Override
    public JsonNode searchNews(String query) {

        RestTemplate restTemplate = new RestTemplate();

        // 요청 URL 생성
        String url = UriComponentsBuilder.fromUriString(naverApiUrl)
                .queryParam("query", query)
                .queryParam("display", 8)
                .queryParam("sort", "sim")
                .toUriString();

        // HTTP 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Naver-Client-Id", clientId);
        headers.set("X-Naver-Client-Secret", clientSecret);

        // HTTP 요청 생성
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        // JSON 파싱
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = null;
        try {
            jsonNode = objectMapper.readTree(response.getBody());
        } catch (Exception e) {
            log.error(e.getMessage());
        }

        return jsonNode;
    }

    @Override
    @Transactional
    public void createNewsPost(ReqPostNewsDto reqPostNewsDto, Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if(userOptional.isEmpty()){
            throw new RuntimeException("사용자 없음.");
        }

        News news = News.builder()
                .link(reqPostNewsDto.link())
                .title(reqPostNewsDto.title())
                .description(reqPostNewsDto.description())
                .analysis(reqPostNewsDto.analysis())
                .content(reqPostNewsDto.content())
                .createdAt(LocalDateTime.now())
                .user(userOptional.get())
                .build();

        newsRepository.save(news);
    }

    @Override
    public List<ResGetNewsDto> searchNewsByUserId(Long userId) {
        List<News> newsList = newsRepository.findAllByUserIdOrderByNewsIdDesc(userId);

        return newsList.stream()
                .map(ResGetNewsDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResGetNewsDto> getAllNews() {
        List<News> newsList = newsRepository.findAllByOrderByNewsIdDesc();
        return newsList.stream()
                .map(ResGetNewsDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<ResGetNewsDto> searchNewsByTitle(String title) {
        List<News> newsList = newsRepository.findByTitleContainingIgnoreCase(title);
        return newsList.stream()
                .map(ResGetNewsDto::fromEntity)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public void deleteNewsPost(Long id) {
        Optional<News> newsOptional = newsRepository.findById(id);
        if (newsOptional.isEmpty()) {
            throw new IllegalArgumentException("해당 ID의 게시글이 존재하지 않습니다.");
        }
        News news = newsOptional.get();

        newsRepository.delete(news);
    }

    @Override
    public String analyzeSentiment(String summary) {
        RestTemplate restTemplate = new RestTemplate();

        // Groq API 요청 JSON 생성
        Map<String, Object> requestBody = Map.of(
                "model", "llama-3.1-8b-instant",
                "messages", List.of(
                        Map.of("role", "system", "content", "You are an AI that performs sentiment analysis on news summaries."),
                        Map.of("role", "user", "content", "기사 요약 : " + summary + "\n2~3줄로 이 기사에 대한 감정 분석을 한국어로 해줘.")
                )
        );
        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(requestBody, headers);
        try{
            ResponseEntity<String> response = restTemplate.exchange(
                    groqApiUrl, HttpMethod.POST, requestEntity, String.class
            );

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseJson = objectMapper.readTree(response.getBody());
            return responseJson.get("choices").get(0).get("message").get("content").asText();
        } catch (Exception e) {
            throw new RuntimeException("Groq API 요청 실패 : " + e.getMessage(), e);
        }
    }
}

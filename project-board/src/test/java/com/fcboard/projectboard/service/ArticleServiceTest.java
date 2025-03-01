package com.fcboard.projectboard.service;

import com.fcboard.projectboard.domain.Article;
import com.fcboard.projectboard.domain.UserAccount;
import com.fcboard.projectboard.domain.type.SearchType;
import com.fcboard.projectboard.dto.ArticleDto;
import com.fcboard.projectboard.dto.ArticleWithCommentsDto;
import com.fcboard.projectboard.dto.UserAccountDto;
import com.fcboard.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 게시글")
@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks private ArticleService sut;
    @Mock private ArticleRepository articleRepository;

    /*
    검색
    각 게시글 페이지로 이동
    페이지네이션
    홈 버튼 -> 게시판 페이지로 리다이렉션
    정렬 기능
     */

    @DisplayName("검색어없이 게시글을 검색하면, 게시글 페이지를 반환")
    @Test
    void givenNoSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
        //Given
        Pageable pageable = Pageable.ofSize(10);
        given(articleRepository.findAll(pageable)).willReturn(Page.empty());

        //When
        Page<ArticleDto> articles = sut.searchArticles(null, null, pageable); //제목, 본문 ,ID, 닉네임, 해시태그

        //Then
        assertThat(articles).isNotNull();
        assertThat(articles).isEmpty();
        then(articleRepository).should().findAll(pageable);
    }

    @DisplayName("검색어와 함께 게시글을 검색하면, 게시글 페이지를 반환")
    @Test
    void givenSearchParameters_whenSearchingArticles_thenReturnsArticlePage() {
        //Given
        SearchType searchType = SearchType.TITLE;
        String searchKeyword = "title";
        Pageable pageable = Pageable.ofSize(20);
        given(articleRepository.findByTitle(searchKeyword, pageable)).willReturn(Page.empty());

        //When
        Page<ArticleDto> articles = sut.searchArticles(searchType, searchKeyword, pageable); //제목, 본문 ,ID, 닉네임, 해시태그

        //Then
        assertThat(articles).isEmpty();
        then(articleRepository).should().findByTitle(searchKeyword, pageable);
    }

    @DisplayName("게시글을 조회하면, 게시글을 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticle_thenReturnsArticle() {
        //Given
        Long articleId = 1L;
        Article article = createArticle();
        given(articleRepository.findById(articleId)).willReturn(Optional.of(article));

        //When
        ArticleWithCommentsDto dto = sut.getArticle(articleId);

        //Then
        assertThat(dto)
                .hasFieldOrPropertyWithValue("title", article.getTitle())
                .hasFieldOrPropertyWithValue("content", article.getContent())
                .hasFieldOrPropertyWithValue("hashtag", article.getHashtag());
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("없는 게시물을 조회하면, 예외를 던진다.")
    @Test
    void givenNoneExistingArticleId_whenSearchingArticle_thenThrowsException() {
        //Given
        Long articleId = 0L;
        given(articleRepository.findById(articleId)).willReturn(Optional.empty());

        //When
        Throwable t = catchThrowable(() -> sut.getArticle(articleId));

        //Then
        assertThat(t)
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("게시글이 없습니다 - articleId: " + articleId);
        then(articleRepository).should().findById(articleId);
    }

    @DisplayName("게시글 정보를 입력하면, 게시글을 생성한다")
    @Test
    void givenArticleInfo_whenSavingArticle_thenSavesArticle() {
        //Given
        ArticleDto dto = createArticleDto();
        given(articleRepository.save(any(Article.class))).willReturn(createArticle());

        //When
        sut.saveArticle(dto);

        //Then
        then(articleRepository).should().save(any(Article.class));
    }

    @DisplayName("게시글의 수정정보를 입력하면, 게시글을 수정한다")
    @Test
    void givenModifiedArticleInfo_whenUpdatingArticle_thenUpdatesArticle() {
        //Given
        Article article = createArticle();
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willReturn(article);

        //When
        sut.updateArticle(dto);

        //Then
        assertThat(article)
                .hasFieldOrPropertyWithValue("title", dto.title())
                .hasFieldOrPropertyWithValue("content", dto.content())
                .hasFieldOrPropertyWithValue("hashtag", dto.hashtag());
        then(articleRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("없는 게시글의 수정 정보를 입력하면, 경고 로그를 찍고 아무 것도 하지 않는다.")
    @Test
    void givenNonexistentArticleInfo_whenUpdatingArticle_thenLogsWarningAndDoesNothing() {
        // Given
        ArticleDto dto = createArticleDto("새 타이틀", "새 내용", "#springboot");
        given(articleRepository.getReferenceById(dto.id())).willThrow(EntityNotFoundException.class);

        // When
        sut.updateArticle(dto);

        // Then
        then(articleRepository).should().getReferenceById(dto.id());
    }

    @DisplayName("게시글의 ID를 입력하면, 게시글을 삭제한다")
    @Test
    void givenArticleId_whenDeletingArticle_thenDeletesArticle() {
        //Given
        Long articleId = 1L;
        willDoNothing().given(articleRepository).deleteById(articleId);

        //When
        sut.deleteArticle(1L);

        //Then
        then(articleRepository).should().deleteById(articleId);
    }

    private UserAccount createUserAccount() {
        return UserAccount.of(
                "uno",
                "password",
                "uno@email.com",
                "Uno",
                null
        );
    }

    private UserAccountDto createUserAccountDto() {
        return UserAccountDto.of(
                1L,
                "uno",
                "password",
                "uno@mail.com",
                "Uno",
                "This is memo",
                LocalDateTime.now(),
                "uno",
                LocalDateTime.now(),
                "uno"
        );
    }
    private Article createArticle() {
        return Article.of(
                createUserAccount(),
                "title",
                "content",
                "hashtag"
        );
    }


    private ArticleDto createArticleDto(String title, String content, String hashtag) {
        return ArticleDto.of(1L,
                createUserAccountDto(),
                title,
                content,
                hashtag,
                LocalDateTime.now(),
                "Uno",
                LocalDateTime.now(),
                "Uno");
    }
    private ArticleDto createArticleDto() {
        return createArticleDto("title", "content", "hashtag");
    }
}


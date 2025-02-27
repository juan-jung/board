package com.fcboard.projectboard.service;

import com.fcboard.projectboard.domain.Article;
import com.fcboard.projectboard.domain.ArticleComment;
import com.fcboard.projectboard.dto.ArticleCommentDto;
import com.fcboard.projectboard.repository.ArticleCommentRepository;
import com.fcboard.projectboard.repository.ArticleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.BDDMockito;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DisplayName("비즈니스 로직 - 댓글")
@ExtendWith(MockitoExtension.class)
class ArticleCommentServiceTest {

    @InjectMocks ArticleCommentService sut;

    @Mock ArticleRepository articleRepository;
    @Mock ArticleCommentRepository articleCommentRepository;

    @DisplayName("게시글 Id로 조회하면, 해당하는 댓글 리스트를 반환한다.")
    @Test
    void givenArticleId_whenSearchingArticleComments_thenReturnsArticleComments() {
        Optional.of(
                Article.of("title","title","hashtag"));
        //Given
        Long articleId = 1L;
        BDDMockito.given(articleRepository.findById(articleId)).willReturn(Optional.of(Article.of("title","content","hashtag")));

        //When
        List<ArticleCommentDto> articleComments = sut.searchArticleComment(articleId);

        //Then
        assertThat(articleComments).isNotNull();
        BDDMockito.then(articleRepository).should().findById(articleId);
    }

    @DisplayName("댓글 정보를 입력하면, 댓글을 저장한다.")
    @Test
    void givenArticleCommentInfo_whenSavingArticleComment_thenSavesArticleComment() {
        // Given
        BDDMockito.given(articleCommentRepository.save(ArgumentMatchers.any(ArticleComment.class))).willReturn(null);

        // When
        sut.saveArticleComment(ArticleCommentDto.of(LocalDateTime.now(), "Uno", LocalDateTime.now(), "Uno", "comment"));

        // Then
        BDDMockito.then(articleCommentRepository).should().save(ArgumentMatchers.any(ArticleComment.class));
    }
}


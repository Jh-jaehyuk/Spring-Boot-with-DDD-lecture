package com.example.demo.board.repository;

import com.example.demo.board.entity.Board;
import com.example.demo.account_profile.entity.AccountProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class BoardRepositoryTests {

    @Mock
    private BoardRepository boardRepository; // Mock Repository

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this); // Mock 초기화
    }

    @Test
    void testFindAllWithWriterMock() {
        // Given
        PageRequest pageable = PageRequest.of(0, 10);
        AccountProfile writer = new AccountProfile(null, "MockUser");
        Board mockBoard = new Board("Mock Title", writer, "Mock Content");

        Page<Board> mockPage = new PageImpl<>(List.of(mockBoard));
        when(boardRepository.findAllWithWriter(pageable)).thenReturn(mockPage);

        // When
        Page<Board> result = boardRepository.findAllWithWriter(pageable);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).getTitle()).isEqualTo("Mock Title");

        // Verify
        verify(boardRepository, times(1)).findAllWithWriter(pageable);
    }

    @Test
    void testFindByIdWithWriterMock() {
        // Given
        AccountProfile writer = new AccountProfile(null, "MockUser");
        Board mockBoard = new Board("Mock Title", writer, "Mock Content");
        when(boardRepository.findByIdWithWriter(1L)).thenReturn(Optional.of(mockBoard));

        // When
        Optional<Board> result = boardRepository.findByIdWithWriter(1L);

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Mock Title");
        assertThat(result.get().getWriter().getNickname()).isEqualTo("MockUser");

        // Verify
        verify(boardRepository, times(1)).findByIdWithWriter(1L);
    }
}

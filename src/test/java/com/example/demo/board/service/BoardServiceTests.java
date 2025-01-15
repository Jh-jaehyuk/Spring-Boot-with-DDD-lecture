package com.example.demo.board.service;


import com.example.demo.account.entity.Account;
import com.example.demo.account.entity.AccountRoleType;
import com.example.demo.account.entity.RoleType;
import com.example.demo.account.repository.AccountRepository;
import com.example.demo.account_profile.entity.AccountProfile;
import com.example.demo.account_profile.repository.AccountProfileRepository;
import com.example.demo.board.entity.Board;
import com.example.demo.board.repository.BoardRepository;
import com.example.demo.board.service.request.CreateBoardRequest;
import com.example.demo.board.service.request.ListBoardRequest;
import com.example.demo.board.service.request.ModifyBoardRequest;
import com.example.demo.board.service.response.ListBoardResponse;
import com.example.demo.board.service.response.ModifyBoardResponse;
import com.example.demo.board.service.response.ReadBoardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class) // MockitoExtension을 추가하여 Mockito 활성화
class BoardServiceTests {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountProfileRepository accountProfileRepository;

    @InjectMocks
    private BoardServiceImpl boardService; // BoardServiceImpl에 mockBoardRepository를 주입

    private Board mockBoard;
    private AccountProfile mockAccountProfile;
    private Account mockAccount;
    private AccountRoleType mockRoleType;

    @BeforeEach
    void setUp() {
        // Mock 객체 설정
        mockRoleType = new AccountRoleType(RoleType.ADMIN);
        mockAccount = new Account("test@example.com", mockRoleType);
        ReflectionTestUtils.setField(mockAccount, "id", 1L);

        mockAccountProfile = new AccountProfile(mockAccount, "testProfile");
        mockAccountProfile.setId(1L);

        mockBoard = new Board("Test Board", mockAccountProfile, "Test content");
        ReflectionTestUtils.setField(mockBoard, "boardId", 1L);
        ReflectionTestUtils.setField(mockBoard, "createDate", LocalDateTime.now());
        ReflectionTestUtils.setField(mockBoard, "updateDate", LocalDateTime.now());
    }

    @Test
    void testListBoards() {
        // Given
        ListBoardRequest request = new ListBoardRequest(1, 10);
        when(boardRepository.findAllWithWriter(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(mockBoard)));

        // When
        ListBoardResponse response = boardService.list(request);

        // Then
        assertThat(response.getBoardListWithNicknames()).isNotEmpty();
        assertThat(response.getBoardListWithNicknames().get(0).get("title")).isEqualTo("Test Board");
        verify(boardRepository, times(1)).findAllWithWriter(any(Pageable.class));
    }

    @Test
    void testRegisterBoard() {
        // Given
        CreateBoardRequest createBoardRequest = new CreateBoardRequest("New Board", 1L, "New Content");
        when(accountRepository.findById(anyLong())).thenReturn(Optional.of(mockAccount));
        when(accountProfileRepository.findByAccount(any())).thenReturn(Optional.of(mockAccountProfile));

        // mockBoard 객체를 새로 생성하여 title을 명시적으로 설정
        mockBoard = new Board("New Board", mockAccountProfile, "New Content"); // 새 제목과 내용을 설정
        when(boardRepository.save(any(Board.class))).thenReturn(mockBoard);

        // When
        Board createdBoard = boardService.register(createBoardRequest);

        // Then
        assertThat(createdBoard.getTitle()).isEqualTo("New Board");
        assertThat(createdBoard.getContent()).isEqualTo("New Content");
        assertThat(createdBoard.getWriter().getNickname()).isEqualTo("testProfile");
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testReadBoard() {
        // Given
        when(boardRepository.findByIdWithWriter(anyLong())).thenReturn(Optional.of(mockBoard));

        // When
        ReadBoardResponse response = boardService.read(1L);

        // Then
        assertThat(response.getBoardId()).isEqualTo(1L);
        assertThat(response.getTitle()).isEqualTo("Test Board");
        assertThat(response.getContent()).isEqualTo("Test content");
        assertThat(response.getNickname()).isEqualTo("testProfile");
        verify(boardRepository, times(1)).findByIdWithWriter(anyLong());
    }

    @Test
    void testModifyBoard() {
        // Given
        ModifyBoardRequest modifyBoardRequest = new ModifyBoardRequest("Updated Title", "Updated Content");
        when(boardRepository.findByIdWithWriter(anyLong())).thenReturn(Optional.of(mockBoard));
        when(boardRepository.save(any(Board.class))).thenReturn(mockBoard);

        // When
        ModifyBoardResponse response = boardService.modify(1L, 1L, modifyBoardRequest);

        // Then
        assertThat(response.getTitle()).isEqualTo("Updated Title");
        assertThat(response.getContent()).isEqualTo("Updated Content");
        verify(boardRepository, times(1)).save(any(Board.class));
    }

    @Test
    void testDeleteBoard() {
        // Given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.of(mockBoard));
        doNothing().when(boardRepository).deleteById(anyLong());

        // When
        boolean result = boardService.delete(1L);

        // Then
        assertThat(result).isTrue();
        verify(boardRepository, times(1)).deleteById(anyLong());
    }

    @Test
    void testDeleteBoardNotFound() {
        // Given
        when(boardRepository.findById(anyLong())).thenReturn(Optional.empty());

        // When
        boolean result = boardService.delete(1L);

        // Then
        assertThat(result).isFalse();
        verify(boardRepository, times(0)).deleteById(anyLong());
    }
}

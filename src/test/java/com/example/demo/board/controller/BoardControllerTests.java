package com.example.demo.board.controller;

import com.example.demo.board.controller.request_form.CreateBoardRequestForm;
import com.example.demo.board.controller.request_form.ListBoardRequestForm;
import com.example.demo.board.controller.request_form.ModifyBoardRequestForm;
import com.example.demo.board.entity.Board;
import com.example.demo.board.service.BoardService;
import com.example.demo.board.service.response.ListBoardResponse;
import com.example.demo.board.service.response.ModifyBoardResponse;
import com.example.demo.board.service.response.ReadBoardResponse;
import com.example.demo.redis_cache.service.RedisCacheService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class BoardControllerTests {

    @Mock
    private BoardService boardService;  // Mock the BoardService

    @Mock
    private RedisCacheService redisCacheService;  // Mock the RedisCacheService

    @InjectMocks
    private BoardController boardController;  // Inject mocks into the BoardController

    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);  // Initialize mocks
        mockMvc = MockMvcBuilders.standaloneSetup(boardController).build();
    }

    @Test
    void testBoardList() throws Exception {
        // Given
        ListBoardRequestForm requestForm = new ListBoardRequestForm(1, 10);
        ListBoardResponse response = mock(ListBoardResponse.class);
        when(boardService.list(any())).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/board/list")
                        .param("page", "1")
                        .param("perPage", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalItems").exists())
                .andExpect(jsonPath("$.totalPages").exists());

        verify(boardService, times(1)).list(any());
    }

    @Test
    void testRegisterBoard() throws Exception {
        // Given
        CreateBoardRequestForm createBoardRequestForm = new CreateBoardRequestForm("New Board", "New Content", "userToken");
        when(redisCacheService.getValueByKey("userToken")).thenReturn(1L);
        Board board = new Board();
        board.setTitle("New Board");
        board.setContent("New Content");
        when(boardService.register(any())).thenReturn(board);

        // When & Then
        mockMvc.perform(post("/board/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"New Board\", \"content\":\"New Content\", \"userToken\":\"userToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("New Board"))
                .andExpect(jsonPath("$.content").value("New Content"));

        verify(boardService, times(1)).register(any());
    }

    @Test
    void testReadBoard() throws Exception {
        // Given
        ReadBoardResponse readBoardResponse = new ReadBoardResponse(
                1L,
                "Test Board",
                "testProfile",
                "Test content",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(boardService.read(1L)).thenReturn(readBoardResponse);

        // When & Then
        mockMvc.perform(get("/board/{boardId}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Test Board"))
                .andExpect(jsonPath("$.content").value("Test content"))
                .andExpect(jsonPath("$.nickname").value("testProfile"));

        verify(boardService, times(1)).read(1L);
    }

    @Test
    void testModifyBoard() throws Exception {
        // Given
        ModifyBoardRequestForm modifyBoardRequestForm = new ModifyBoardRequestForm("Updated Title", "Updated Content", "userToken");
        ModifyBoardResponse modifyBoardResponse = new ModifyBoardResponse(
                1L,
                "Updated Title",
                "testProfile",
                "Updated Content",
                LocalDateTime.now(),
                LocalDateTime.now()
        );
        when(redisCacheService.getValueByKey("userToken")).thenReturn(1L);
        when(boardService.modify(anyLong(), anyLong(), any())).thenReturn(modifyBoardResponse);

        // When & Then
        mockMvc.perform(put("/board/{boardId}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Updated Title\", \"content\":\"Updated Content\", \"userToken\":\"userToken\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.content").value("Updated Content"));

        verify(boardService, times(1)).modify(anyLong(), anyLong(), any());
    }

    @Test
    void testDeleteBoard() throws Exception {
        // Given
        when(boardService.delete(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/board/{boardId}", 1L))
                .andExpect(status().isOk());

        verify(boardService, times(1)).delete(1L);
    }

    @Test
    void testDeleteBoard_Failure() {
        Long boardId = 1L;

        // Mock 설정: 삭제 실패 시 false 반환
        Mockito.when(boardService.delete(boardId)).thenReturn(false);

        // 예외 검증
        RuntimeException exception = Assertions.assertThrows(RuntimeException.class, () -> {
            boardController.deleteBoard(boardId);
        });

        Assertions.assertEquals("게시글이 존재하지 않거나 이미 삭제되었습니다.", exception.getMessage());
        Mockito.verify(boardService, Mockito.times(1)).delete(boardId);
    }
}

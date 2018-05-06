package com.erfangc.sesamelab.document;

import com.erfangc.sesamelab.user.User;
import com.erfangc.sesamelab.user.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.Objects;

import static java.util.Collections.emptyList;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@WebMvcTest(controllers = DocumentController.class, secure = false)
public class DocumentControllerTest {
    @MockBean
    DynamoDBDocumentService dynamoDBDocumentService;
    @MockBean
    ElasticsearchDocumentService elasticsearchDocumentService;
    @MockBean
    UserService userService;
    @Autowired
    MockMvc mockMvc;
    private Document mockDocument;
    @Autowired
    private ObjectMapper objectMapper;

    @Before
    public void setUp() {
        mockDocument = new Document(
                "doc",
                "",
                1L,
                "user",
                "user@email",
                Instant.now(),
                Instant.now(),
                "user",
                "user@email",
                emptyList()
        );
    }

    @Test
    public void byCreator() throws Exception {
        mockMvc.perform(get("/api/v1/documents/by-creator/xiongxiong").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        Mockito.verify(elasticsearchDocumentService).searchByCreator(eq("xiongxiong"));
    }

    @Test
    public void byCorpus() throws Exception {
        mockMvc.perform(get("/api/v1/documents/by-corpus/1").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());
        Mockito.verify(elasticsearchDocumentService).searchByCorpusID(eq(1L), eq(null));
    }

    @WithMockUser
    @Test
    public void testDelete() throws Exception {
        Mockito.when(dynamoDBDocumentService.getById(anyString())).thenReturn(mockDocument);
        Mockito.when(userService.getUserFromAuthenticatedPrincipal(any())).thenReturn(new User("user", "user@email", ""));
        mockMvc
                .perform(delete("/api/v1/documents/some-id").principal(SecurityContextHolder.getContext().getAuthentication()))
                .andExpect(status().isOk());
        Mockito.verify(dynamoDBDocumentService).delete(eq("some-id"));
    }

    @Test
    public void testGet() throws Exception {
        mockMvc.perform(get("/api/v1/documents/some-id")).andExpect(status().isOk());
        Mockito.verify(dynamoDBDocumentService).getById(eq("some-id"));
    }

    @WithMockUser
    @Test
    public void testPut() throws Exception {
        Mockito.when(dynamoDBDocumentService.put(any())).thenReturn(mockDocument);
        Mockito.when(userService.getUserFromAuthenticatedPrincipal(any())).thenReturn(new User("user", "user@email", ""));
        mockMvc
                .perform(post("/api/v1/documents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(mockDocument))
                        .principal(SecurityContextHolder.getContext().getAuthentication())
                )
                .andExpect(status().isOk());
        Mockito.verify(dynamoDBDocumentService).put(argThat(document -> Objects.requireNonNull(document.getId()).equals(mockDocument.getId())));
    }
}

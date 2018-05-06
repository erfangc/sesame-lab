package com.erfangc.sesamelab.document;

import com.erfangc.sesamelab.user.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Test
    public void testDelete() throws Exception {
        mockMvc.perform(delete("/api/v1/documents/some-id")).andExpect(status().isOk());
        Mockito.verify(dynamoDBDocumentService).delete(eq("some-id"));
    }

    @Test
    public void testGet() throws Exception {
        mockMvc.perform(get("/api/v1/documents/some-id")).andExpect(status().isOk());
        Mockito.verify(dynamoDBDocumentService).getById(eq("some-id"));
    }

    @Test
    public void testPut() throws Exception {
    }
}

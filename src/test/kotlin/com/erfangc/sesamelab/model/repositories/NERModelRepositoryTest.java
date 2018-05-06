package com.erfangc.sesamelab.model.repositories;

import com.erfangc.sesamelab.corpus.entities.Corpus;
import com.erfangc.sesamelab.model.entities.NERModel;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@DataJpaTest
public class NERModelRepositoryTest {
    @Autowired
    NERModelRepository nerModelRepository;
    @Test
    public void testSave() {
        NERModel result = nerModelRepository.save(new NERModel().setCorpus(new Corpus().setId(1L)));
        assertNotNull(result.getId());
        assertEquals(1L, result.getCorpus().getId().longValue());
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    @Test
    public void testLoad() {
        NERModel result = nerModelRepository.save(new NERModel().setCorpus(new Corpus().setId(1L)));
        Optional<NERModel> model = nerModelRepository.findById(result.getId());
        assertTrue(model.isPresent());
    }
}
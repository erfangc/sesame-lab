package com.erfangc.sesamelab.corpus;

import com.erfangc.sesamelab.corpus.entities.Corpus;
import com.erfangc.sesamelab.user.User;
import com.erfangc.sesamelab.user.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.rest.core.annotation.HandleBeforeCreate;
import org.springframework.data.rest.core.annotation.RepositoryEventHandler;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RepositoryEventHandler
public class CorpusEventHandler {
    private UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(CorpusEventHandler.class);

    public CorpusEventHandler(UserService userService) {
        this.userService = userService;
    }

    @HandleBeforeCreate
    public void handleBeforeCreate(Corpus corpus) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!authentication.isAuthenticated()) {
            throw new RuntimeException("cannot create a corpus with an unauthenticated principal");
        }
        User user = userService.getUserFromAuthenticatedPrincipal(authentication);
        logger.info("Inject user id {} into corpus {}", user.getId(), corpus.getTitle());
        corpus.setUserID(user.getId());
    }
}

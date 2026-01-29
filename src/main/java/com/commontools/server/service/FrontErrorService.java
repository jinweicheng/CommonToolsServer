package com.commontools.server.service;

import com.commontools.server.dto.FrontErrorRequest;
import com.commontools.server.model.FrontError;
import com.commontools.server.repository.FrontErrorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FrontErrorService {
    private final FrontErrorRepository frontErrorRepository;

    @Transactional
    public FrontError record(FrontErrorRequest req) {
        FrontError e = new FrontError();
        e.setUserId(req.getUserId());
        e.setErrorMessage(req.getErrorMessage());

        return frontErrorRepository.save(e);
    }
}


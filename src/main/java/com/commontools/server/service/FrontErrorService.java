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
        // some clients send empty userId or action; provide sensible defaults
        String userId = req.getUserId();
        if (userId == null || userId.isBlank()) {
            userId = "anonymous";
        }
        e.setUserId(userId);

        e.setModule(req.getModule() != null ? req.getModule() : "unknown");

        String action = req.getAction();
        if (action == null || action.isBlank()) {
            action = "unknown";
        }
        e.setAction(action);

        e.setEndpoint(req.getEndpoint() != null ? req.getEndpoint() : "");
        e.setIpAddress(req.getIpAddress());
        e.setDeviceType(req.getDeviceType());
        e.setBrowser(req.getBrowser());
        e.setOs(req.getOs());
        e.setErrorMessage(req.getErrorMessage());

        return frontErrorRepository.save(e);
    }
}


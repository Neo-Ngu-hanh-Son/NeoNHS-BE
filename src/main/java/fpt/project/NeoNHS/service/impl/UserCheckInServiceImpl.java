package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.UserCheckInRepository;
import fpt.project.NeoNHS.service.UserCheckInService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserCheckInServiceImpl implements UserCheckInService {

    private final UserCheckInRepository userCheckInRepository;
}

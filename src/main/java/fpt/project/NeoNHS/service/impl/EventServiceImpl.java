package fpt.project.NeoNHS.service.impl;

import fpt.project.NeoNHS.repository.EventRepository;
import fpt.project.NeoNHS.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
}

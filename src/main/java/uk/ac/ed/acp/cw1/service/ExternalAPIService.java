package uk.ac.ed.acp.cw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw1.dto.Drone;

import java.util.ArrayList;
import java.util.List;

@Service
public class ExternalAPIService {
    private final RestTemplate restTemplate;
    private final String ilpEndpoint;

    @Autowired
    public ExternalAPIService(RestTemplate restTemplate, String ilpEndpoint) {
        this.restTemplate = restTemplate;
        this.ilpEndpoint = ilpEndpoint;

    }

    public List<Drone> getAllDrones(){
        try {
            String url = ilpEndpoint + "/drones";

            ResponseEntity<Drone[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    Drone[].class
            );
            //if response is okay and not null
            if (response.getBody() != null) {
                return List.of(response.getBody());
            }
            //if body is null
            return new ArrayList<>();
        }catch(RestClientException e) {
            //if there is error when fetching
            return new ArrayList<>();
        }
    }

}

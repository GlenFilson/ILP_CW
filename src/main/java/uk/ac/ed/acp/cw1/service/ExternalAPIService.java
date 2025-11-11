package uk.ac.ed.acp.cw1.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import uk.ac.ed.acp.cw1.dto.Drone;
import uk.ac.ed.acp.cw1.dto.DroneForServicePointResponse;
import uk.ac.ed.acp.cw1.dto.RestrictedArea;
import uk.ac.ed.acp.cw1.dto.ServicePoint;

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
            //research if i should use ResponseEntity or not
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

    public List<ServicePoint> getServicePoints(){
        try {
            String url = ilpEndpoint + "/service-points";
            //research if i should use ResponseEntity or not
            ResponseEntity<ServicePoint[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    ServicePoint[].class
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

    public List<DroneForServicePointResponse> getDronesForServicePoints(){
        try {
            String url = ilpEndpoint + "/drones-for-service-points";
            //research if i should use ResponseEntity or not
            ResponseEntity<DroneForServicePointResponse[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    DroneForServicePointResponse[].class
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

    public List<RestrictedArea> getRestrictedAreas(){
        try {
            String url = ilpEndpoint + "/restricted-areas";
            //research if i should use ResponseEntity or not
            ResponseEntity<RestrictedArea[]> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    RestrictedArea[].class
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

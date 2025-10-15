package uk.ac.ed.acp.cw1.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;


import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class ServiceControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testUID()  throws Exception {
        mockMvc.perform(get("/api/v1/uid"))
                .andExpect(status().isOk())
                .andExpect(content().string("s2539057"));
    }

    //distanceTo Tests
    @Test
    public void testDistanceTo_samePosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 5.0,
                            "lat": 4.0
                        },
                        "position2": {
                            "lng": 5.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("0.0"));
    }

    @Test
    public void testDistanceTo_differentPosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("5.0"));
    }

    @Test
    public void testDistanceTo_missingPosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDistanceTo_missingLatitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDistanceTo_missingLongitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }



    @Test
    public void testDistanceTo_missingPosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDistanceTo_missingLatitudePosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testDistanceTo_missingLongitudePosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/distanceTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    //isCloseTo tests
    @Test
    public void testIsCloseTo_samePosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 5.0,
                            "lat": 4.0
                        },
                        "position2": {
                            "lng": 5.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsCloseTo_differentPosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsCloseTo_onEdgePosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 0.0,
                            "lat": 0.00015
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsCloseTo_withinEdgePosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 0.0,
                            "lat": 0.000149
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }


    @Test
    public void testIsCloseTo_outsideEdgePosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 0.0,
                            "lat": 0.000151
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsCloseTo_missingPosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_missingLatitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_missingLongitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }



    @Test
    public void testIsCloseTo_missingPosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_missingLatitudePosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lng": 3.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_missingLongitudePosition2() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 0.0,
                            "lat": 0.0
                        },
                        "position2": {
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_nullPosition() throws Exception{
        String jsonRequest = """
                    {
                        "position1": null,
                        "position2": {
                            "lng": 5.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_nullLongitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": null,
                            "lat": 4.0
                        },
                        "position2": {
                            "lng": 5.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsCloseTo_nullLatitudePosition1() throws Exception{
        String jsonRequest = """
                    {
                        "position1": {
                            "lng": 5.0,
                            "lat": null
                        },
                        "position2": {
                            "lng": 5.0,
                            "lat": 4.0
                        }
                    }
    """;

        mockMvc.perform(post("/api/v1/isCloseTo")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }


    //nextPosition tests

    @Test
    public void testNextPosition_0Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 0.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.0, within(1e-6));
        assertThat(lng).isCloseTo(0.00015, within(1e-6));



    }

    @Test
    public void testNextPosition_90Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 90.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.00015, within(1e-6));
        assertThat(lng).isCloseTo(0.0, within(1e-6));



    }

    @Test
    public void testNextPosition_180Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 180.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.0, within(1e-6));
        assertThat(lng).isCloseTo(-0.00015, within(1e-6));



    }

    @Test
    public void testNextPosition_270Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 270.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(-0.00015, within(1e-6));
        assertThat(lng).isCloseTo(0.0, within(1e-6));



    }

    @Test
    public void testNextPosition_360Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 360.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.0, within(1e-6));
        assertThat(lng).isCloseTo(0.00015, within(1e-6));



    }

    @Test
    public void testNextPosition_450Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 450.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.00015, within(1e-6));
        assertThat(lng).isCloseTo(0.0, within(1e-6));



    }

    @Test
    public void testNextPosition_negative270Angle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": -270.0
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.00015, within(1e-6));
        assertThat(lng).isCloseTo(0.0, within(1e-6));



    }


    @Test
    public void testNextPosition_extraField() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0,
                "alt": 300.0
            },
            "angle": 0.0,
            "deadline": "now"
        }
    """;

        MvcResult result = mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode node = new ObjectMapper().readTree(result.getResponse().getContentAsString());

        double lat = node.get("lat").asDouble();
        double lng = node.get("lng").asDouble();

        assertThat(lat).isCloseTo(0.0, within(1e-6));
        assertThat(lng).isCloseTo(0.00015, within(1e-6));



    }

    @Test
    public void testNextPosition_invalidAngle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 32.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_negativeInvalidAngle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": -32.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_nullAngle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": null
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_nullStart() throws Exception{
        String jsonRequest = """
        {
            "start": null
            "angle": 32.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }


    @Test
    public void testNextPosition_nullLongitude() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": null,
                "lat": 0.0
            },
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_nullLatitude() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": null
            },
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_missingStart() throws Exception{
        String jsonRequest = """
        {
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_missingAngle() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lng": 0.0,
                "lat": 0.0
            },
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_missingLongitude() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lat": 0.0
            },
            angle: 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }


    @Test
    public void testNextPosition_missingLatitude() throws Exception{
        String jsonRequest = """
        {
            "start": {
                "lat": null
            },
            angle: 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }


    @Test
    public void testNextPosition_misspeltStart() throws Exception{
        String jsonRequest = """
        {
            "stark": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }


    @Test
    public void testNextPosition_misspeltAngle() throws Exception{
        String jsonRequest = """
        {
            "stark": {
                "lng": 0.0,
                "lat": 0.0
            },
            "angel": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }


    @Test
    public void testNextPosition_misspeltLongitude() throws Exception{
        String jsonRequest = """
        {
            "stark": {
                "long": 0.0,
                "lat": 0.0
            },
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    @Test
    public void testNextPosition_misspeltLatitude() throws Exception{
        String jsonRequest = """
        {
            "stark": {
                "lng": 0.0,
                "lateral": 0.0
            },
            "angle": 0.0
        }
    """;

        mockMvc.perform(post("/api/v1/nextPosition")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());


    }

    //isInRegion tests

    @Test
    public void testIsInRegion_insideRegion() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.192473, "lat": 55.944 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));



    }

    @Test
    public void testIsInRegion_outsideRegion() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -8.192473, "lat": 57.944 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_onRegionBorder() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942617 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsInRegion_justInsideRegionBorder() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942618 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    public void testIsInRegion_justOutsideRegionBorder() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942616 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    public void testIsInRegion_missingPosition() throws Exception{
        String jsonRequest = """
        {
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_missingRegion() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942616 }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_missingName() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942616 },
             "region": {
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }


    @Test
    public void testIsInRegion_missingVertices() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.184319, "lat": 55.942618 },
             "region": {
                 "name": "central"
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_emptyVertices() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -8.192473, "lat": 57.944 },
             "region": {
                 "name": "central",
                 "vertices": []
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_3Vertices() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.192473, "lat": 55.944 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.192473, "lat": 55.946233 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testIsInRegion_openPolygon() throws Exception{
        String jsonRequest = """
        {
             "position": { "lng": -3.192473, "lat": 55.944 },
             "region": {
                 "name": "central",
                 "vertices": [
                     { "lng": -3.192473, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.942617 },
                     { "lng": -3.184319, "lat": 55.946233 },
                     { "lng": -3.192473, "lat": 0.0 }
                 ]
             }
        }

    """;
        mockMvc.perform(post("/api/v1/isInRegion")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isBadRequest());
    }







}

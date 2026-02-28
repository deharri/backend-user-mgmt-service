package com.deharri.ums.worker;

import com.deharri.ums.worker.controller.WorkerPublicController;
import com.deharri.ums.worker.dto.response.WorkerTypeDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(WorkerPublicController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("WorkerPublicController Unit Tests")
class WorkerPublicControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WorkerService workerService;

    // ========================================================================
    // GET /public/api/v1/workers/types/all
    // ========================================================================

    @Nested
    @DisplayName("GET /public/api/v1/workers/types/all")
    class GetAllWorkerTypesTests {

        @Test
        @DisplayName("Should return 200 with list of worker types")
        void givenWorkerTypesExist_whenGetAllWorkerTypes_thenReturn200WithList() throws Exception {
            // given
            List<WorkerTypeDto> workerTypes = List.of(
                    new WorkerTypeDto("MECHANIC", "Mechanic", "Performs mechanical repairs and maintenance"),
                    new WorkerTypeDto("ELECTRICIAN", "Electrician", "Handles electrical installations and repairs"),
                    new WorkerTypeDto("PLUMBER", "Plumber", "Manages plumbing systems and fixtures")
            );

            when(workerService.getAllWorkerTypes()).thenReturn(workerTypes);

            // when / then
            mockMvc.perform(get("/public/api/v1/workers/types/all"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(3))
                    .andExpect(jsonPath("$[0].enumValue").value("MECHANIC"))
                    .andExpect(jsonPath("$[0].displayName").value("Mechanic"))
                    .andExpect(jsonPath("$[0].description").value("Performs mechanical repairs and maintenance"))
                    .andExpect(jsonPath("$[1].enumValue").value("ELECTRICIAN"))
                    .andExpect(jsonPath("$[2].enumValue").value("PLUMBER"));
        }
    }
}

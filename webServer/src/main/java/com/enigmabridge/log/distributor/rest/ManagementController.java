package com.enigmabridge.log.distributor.rest;

import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.api.response.ErrorResponse;
import com.enigmabridge.log.distributor.api.response.GeneralResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Create new API Key calls, common administration stuff.
 *
 * Created by dusanklinec on 01.08.16.
 */
@RestController
@PreAuthorize("hasAuthority('"+ ApiConfig.MANAGEMENT_ROLE+"')")
public class ManagementController {

    /**
     * Business controller calls this on new UO was created successfully
     * @param uoHandle UO that was created
     * @return response
     */
    @RequestMapping("/")
    public GeneralResponse uoCreated(
            @RequestParam() String uoHandle
    ) {
        return new ErrorResponse("Not implemented yet");
    }

}

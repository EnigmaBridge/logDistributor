package com.enigmabridge.log.distributor.rest;

import com.enigmabridge.log.distributor.api.ApiConfig;
import com.enigmabridge.log.distributor.api.response.ErrorResponse;
import com.enigmabridge.log.distributor.api.response.GeneralResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * EB Business controller informs us about changes on EB backend
 * mainly UO creation, modification, expiration, errors, ...
 *
 * Created by dusanklinec on 01.08.16.
 */
@RestController
@PreAuthorize("hasAuthority('"+ApiConfig.BUSINESS_ROLE+"')")
public class BusinessController {

    /**
     * Business controller calls this on new UO was created/updated successfully
     * @param handle UO that was created
     * @return response
     */
    @RequestMapping(value = ApiConfig.UO_EVENT_URL + "/{handle}", method = RequestMethod.PUT)
    public GeneralResponse uoUpdated(
            @PathVariable("handle") String handle
    ) {
        return new ErrorResponse("Not implemented yet");
    }

    /**
     * Business controller calls this on new UO was deleted successfully
     * @param handle UO that was changed
     * @return response
     */
    @RequestMapping(value = ApiConfig.UO_EVENT_URL + "/{handle}", method = RequestMethod.DELETE)
    public GeneralResponse uoDeleted(
            @PathVariable("handle") String handle
    ) {
        return new ErrorResponse("Not implemented yet");
    }


}

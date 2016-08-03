package com.enigmabridge.log.distributor.rest;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.OutputStream;
import java.security.NoSuchAlgorithmException;

/**
 *
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('ROLE_DOMAIN_USER')")
public class MainController {


    @RequestMapping("/raw")
    public void rawRequest(@RequestParam(value = "request") String request, OutputStream output) throws IOException, NoSuchAlgorithmException {

    }



}

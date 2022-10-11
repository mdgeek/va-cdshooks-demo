package edu.utah.kmm.opencds.security;

import org.opencds.config.api.xml.JAXBContextService;
import org.opencds.config.service.security.UserDetailsServiceImpl;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.util.ResourceUtils;

import java.io.IOException;

/**
 * Extension of {{@link UserDetailsServiceExImpl}} that uses Spring's resource utilities to resolve the
 * location of the OpenCDS security configuration file.
 */
public class UserDetailsServiceExImpl extends UserDetailsServiceImpl {
    public UserDetailsServiceExImpl(
            JAXBContextService jaxbContextService,
            String location,
            PasswordEncoder passwordEncoder) {
        super(jaxbContextService, resolveToAbsolutePath(location), passwordEncoder);
    }

    private static String resolveToAbsolutePath(String location) {
        try {
            return ResourceUtils.getFile(location).getAbsolutePath();
        } catch (IOException e) {
            return location;
        }
    }

}

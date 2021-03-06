/*
 * Copyright 2016 Wojciech Zankowski.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazarquant.bfp.core.mail.connection;

import com.google.inject.Inject;
import com.google.inject.name.Named;

/**
 * @author Wojciech Zankowski
 */
public class MailConnectionCredentials {

    private final String host;
    private final int port;
    private final String username;
    private final String password;

    @Inject
    public MailConnectionCredentials(
            @Named("mail.host") String host,
            @Named("mail.port") int port,
            @Named("mail.username") String username,
            @Named("mail.password") String password) {
        this.host = host;
        this.port = port;
        this.username = username;
        this.password = password;
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

}

/*
 * Copyright 2012 GitHub Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.mobile;

import org.eclipse.egit.github.core.client.GitHubClient;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import okhttp3.OkHttpClient;
import okhttp3.OkUrlFactory;

/**
 * Default client used to communicate with GitHub API
 */
public class DefaultClient extends GitHubClient {

    private static final String USER_AGENT = "ForkHub/1.2";

    /**
     * Create client
     */
    public DefaultClient() {
        super();

        setSerializeNulls(false);
        setUserAgent(USER_AGENT);
    }

    /**
     * Create connection to URI
     *
     * @param uri
     * @return connection
     * @throws IOException
     */
    @Override
    protected HttpURLConnection createConnection(String uri) throws IOException {
        // OkUrlFactory factory = new OkUrlFactory(new OkHttpClient());
        //
        // TODO: Look through code base to explore how project handles exceptions
        // generally, and what to do about the fact this connection has several
        // new ways it can throw an exception, instead of burrying the fault of KeyManagementException or
        // NoSuchAlgorithmException
        //
        // In terms of adding a switch to check for a version range between kitkat and the last version where
        // this problem exists before it is fixed by default
        // if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) { is a good start
        try {
                // NOTE: The deprecated signature below for sslSocketFactory()
                // will always result in an "unable to extract trust manager, IllegalStateException"
                // final OkHttpClient client = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory()).build();

                // public OkHttpClient.Builder sslSocketFactory(SSLSocketFactory sslSocketFactory,
                //                                             X509TrustManager trustManager)
                TrustManager[] trustManagers = new TrustManager[] { new TrustManagerManipulator() };

                SSLContext context = SSLContext.getInstance("TLS");
                context.init(null, trustManagers, new SecureRandom());
                final OkHttpClient client2 = new OkHttpClient.Builder().sslSocketFactory(new TLSSocketFactory(context.getSocketFactory()), (X509TrustManager) trustManagers[0]).build();
                URL url = new URL(createUri(uri));
                OkUrlFactory factory = new OkUrlFactory(client2);
                return factory.open(url);
            } catch (KeyManagementException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            } catch (NoSuchAlgorithmException e) {
                e.printStackTrace();
                throw new IOException(e.getMessage());
            }
    }
}

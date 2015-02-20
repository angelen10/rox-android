package com.grayfox.android.http;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@Deprecated
public class HttpClientRequestBuilder extends RequestBuilder {

    private static final String TAG = HttpClientRequestBuilder.class.getSimpleName();

    private final String url;
    private final HttpClient httpClient;
    private final List<BasicNameValuePair> formParams;
    private final List<BasicHeader> headers;
    private HttpRequestBase request;

    protected HttpClientRequestBuilder(String url) {
        Log.d(TAG, "URL=" + url);
        httpClient = new DefaultHttpClient();
        HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, DEFAULT_TIMEOUT);
        HttpConnectionParams.setSoTimeout(httpParams, DEFAULT_TIMEOUT);
        formParams = new ArrayList<>();
        headers = new ArrayList<>();
        this.url = url;
    }

    @Override
    public HttpClientRequestBuilder setTimeout(int timeout) {
        HttpParams httpParams = httpClient.getParams();
        HttpConnectionParams.setConnectionTimeout(httpParams, timeout);
        HttpConnectionParams.setSoTimeout(httpParams, timeout);
        return this;
    }

    @Override
    public HttpClientRequestBuilder setMethod(Method method) {
        Log.d(TAG, "Method=" + method);
        switch (method) {
            case GET:
                request = new HttpGet(url);
                break;
            case POST:
                request = new HttpPost(url);
                break;
            case PUT:
                request = new HttpPut(url);
                break;
            case DELETE:
                request = new HttpDelete(url);
                break;
        }

        return this;
    }

    @Override
    public HttpClientRequestBuilder setData(String data) {
        Log.d(TAG, "Data=" + data);
        try {
            StringEntity input = new StringEntity(data);
            ((HttpEntityEnclosingRequest) request).setEntity(input);
        } catch (UnsupportedEncodingException ex) {
            Log.e(TAG, "Error setting post data", ex);
        }
        return this;
    }

    @Override
    public HttpClientRequestBuilder addFormParam(String name, String value) {
        Log.d(TAG, "Param={" + name + ", " + value + "}");
        formParams.add(new BasicNameValuePair(name, value));
        return this;
    }

    @Override
    public HttpClientRequestBuilder setHeader(Header header, String value) {
        Log.d(TAG, "Header={" + header.getValue() + ", " + value + "}");
        headers.add(new BasicHeader(header.getValue(), value));
        return this;
    }

    @Override
    public Integer make() {
        try {
            setUpHeaders();
            setUpFormParams();
            HttpResponse response = httpClient.execute(request);
            Log.d(TAG, "responseCode=" + response.getStatusLine().getStatusCode());
            return response.getStatusLine().getStatusCode();
        } catch (Exception ex) {
            Log.e(TAG, "Error getting response code", ex);
            throw new RequestException("Error getting response code", ex);
        }
    }

    @Override
    public String makeForResult() {
        try {
            setUpHeaders();
            setUpFormParams();
            HttpResponse response = httpClient.execute(request);
            int responseCode = response.getStatusLine().getStatusCode();
            Log.d(TAG, "responseCode=" + responseCode);
            HttpEntity entity = response.getEntity();
            return entity != null ? EntityUtils.toString(entity, Charset.UTF_8.getValue()) : null;
        } catch (Exception ex) {
            Log.e(TAG, "Error getting response", ex);
            throw new RequestException("Error getting response", ex);
        }
    }

    private void setUpHeaders() {
        if (!headers.isEmpty()) {
            request.setHeaders(headers.toArray(new org.apache.http.Header[headers.size()]));
        }
    }

    private void setUpFormParams() throws UnsupportedEncodingException {
        if (!formParams.isEmpty() && request instanceof HttpEntityEnclosingRequest) {
            ((HttpEntityEnclosingRequest) request).setEntity(new UrlEncodedFormEntity(formParams));
        }
    }
}
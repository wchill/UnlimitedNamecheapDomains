package com.androchill;

import org.apache.http.Consts;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) throws Exception {
        String domain;
        Scanner scanner = new Scanner(System.in);
        while(true) {
            System.out.print("Domain: ");
            domain = scanner.nextLine();
            String email = "";
            String emaildomain = ".edu";
            System.setProperty("jsse.enableSNIExtension", "false");

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpget = new HttpGet("https://nc.me");
            CloseableHttpResponse response = httpclient.execute(httpget);
            String token = null;
            try {
                HttpEntity hp = response.getEntity();
                BufferedReader br = new BufferedReader(new InputStreamReader(hp.getContent()));
                String buf;
                while (!(buf = br.readLine()).contains("_token")) ;
                String before = "_token\" value=\"";
                int idx1 = buf.indexOf(before);
                int idx2 = buf.indexOf("\"", idx1 + before.length());
                token = buf.substring(idx1 + before.length(), idx2);
            } finally {
                response.close();
            }

            System.out.println("searching for domain " + domain + ".me");

            HttpPost post = new HttpPost("https://nc.me/search");
            List<NameValuePair> nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("domain", domain));
            nvps.add(new BasicNameValuePair("_token", token));
            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            response = httpclient.execute(post);
            response.close();

            post = new HttpPost("https://nc.me/search-me-domain");
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("_token", token));
            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            response = httpclient.execute(post);

            BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            if (!br.readLine().contains("\"true\"")) {
                System.err.println(domain + ".me domain not available");
                continue;
            }

            System.out.println("adding " + domain + ".me to cart");

            post = new HttpPost("https://nc.me/addtocart");
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("_token", token));
            nvps.add(new BasicNameValuePair("tld", "me"));
            nvps.add(new BasicNameValuePair("ask", "yes"));
            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            response = httpclient.execute(post);
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            if (!br.readLine().contains("\"success\"")) {
                System.err.println(domain + ".me domain not available");
                continue;
            }

            post = new HttpPost("https://nc.me/updatecart");
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("_token", token));
            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            response = httpclient.execute(post);
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            if (!br.readLine().contains("\"success\"")) {
                System.err.println(domain + ".me domain not available");
                continue;
            }

            System.out.println("checking out " + domain + ".me");

            httpget = new HttpGet("https://nc.me/order");
            response = httpclient.execute(httpget);
            response.close();

            post = new HttpPost("https://nc.me/verify");
            //free_service=github&domain_service=wchill.me&email=ericahn3%2Bwchill%40illinois.edu&_token=NmRZbHX8OJiW4o2NbfA1ZdhgNmKekVOp5GdKqvMu
            nvps = new ArrayList<NameValuePair>();
            nvps.add(new BasicNameValuePair("free_service", "github"));
            nvps.add(new BasicNameValuePair("domain_service", domain + ".me"));
            nvps.add(new BasicNameValuePair("email", email + "+" + domain + "@" + emaildomain));
            nvps.add(new BasicNameValuePair("_token", token));
            post.setEntity(new UrlEncodedFormEntity(nvps, Consts.UTF_8));
            response = httpclient.execute(post);
            br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
            String buf;
            try {
                while (!(buf = br.readLine()).contains("An email has been sent to")) ;
                System.out.println("registering domain " + domain + ".me success");
            } catch (NullPointerException e) {
                System.err.println("requesting domain " + domain + ".me failed");
            }
            System.out.println("Complete");
        }
    }
}

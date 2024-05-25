package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecData;


public class CATMFEClient {
    private String host;
    private String port;
    private String accessToken="";

    public CATMFEClient(String host, String port){
        this.host = host;
        this.port = port;
    }

    public OfferingSpecData GetOfferingSpecById(){
        OfferingSpecData metricsResp = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s/manage_system/metrics?draw=11&order[0][dir]=DESC&order[0][name]=UPDATED_DATE",
                    host,
                    port
                )
            );
            System.out.println("metric accessToken:"+accessToken);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Authorization", String.format("Bearer %s", accessToken));
            int responseCode = connection.getResponseCode();

            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                // Parse JSON response into MetricsResp object using ObjectMapper
                ObjectMapper objectMapper = new ObjectMapper();
                metricsResp = objectMapper.readValue(response.toString(), OfferingSpecData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return metricsResp;
    }

}

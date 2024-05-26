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
    private String context="/";

    public CATMFEClient(String host, String port, String context){
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public OfferingSpecData GetOfferingSpecByOfferingId(String offeringId){
        OfferingSpecData respData = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/offering/by_offering_id?offering_id=%s",
                    host,
                    port,
                    context,
                    offeringId
                )
            );
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
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
                respData = objectMapper.readValue(response.toString(), OfferingSpecData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }

}

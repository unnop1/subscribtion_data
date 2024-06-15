package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.CATMFE.OfferingSpecClientResp;
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

    public OfferingSpecClientResp GetOfferingSpecByOfferingId(String offeringId){
        OfferingSpecClientResp respData = new OfferingSpecClientResp();
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
                // System.out.println("offerings: \"" + response.toString()+"\"");
                if (response.toString().isBlank()){
                    return null;
                }
                OfferingSpecData data = objectMapper.readValue(response.toString(), OfferingSpecData.class);
                respData.setData(data);
            }
        } catch (Exception e) {
            e.printStackTrace();
            respData.setErr(e.getMessage());
        }
        return respData;
    }

}

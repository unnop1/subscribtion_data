package com.nt.subscribtion_data.client;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nt.subscribtion_data.model.dao.OMUSER.TransManageContractDTLData;

public class OMUSERClient {
    private String host;
    private String port;
    private String context;

    public OMUSERClient(String host, String port, String context){
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public TransManageContractDTLData GetTransManageContractById(String orderID){
        TransManageContractDTLData respData = null;
        try {
            URL url = new URL(String.format(
                    "http://%s:%s%s/omuser/trans_manage_contract/by_id?id=81",
                    host,
                    port,
                    context,
                    orderID
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
                respData = objectMapper.readValue(response.toString(), TransManageContractDTLData.class);
            } else {
                System.out.println("GET request failed.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return respData;
    }
}

/*
 * BiCadCommandLineClient.java
 *
 * Created on July 21, 2006, 10:22 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.vle.aid.bicad.client;

import java.io.*;

/**
 *
 * @author wrvhage
 */
public class BiCadCommandLineClient {
    private static String _repository_server   = ""; // the default server (localhost:8080/sesame)
    private static String _repository_name     = "mem-rdfs-db"; // the default Main Memory RDFS repository
    private static String _repository_username = "testuser";   // testuser has write access on the available repositories   
    private static String _repository_password = "opensesame";
    
    private static String _Namespace  = "http://www.tno.nl/bicad/clc#";

    private static String _ModelUri   = _Namespace + "DefaultModel";
    private static String _ContextUri = _Namespace + "DefaultContext";
    
    private static BQ_client client = null;

    public BiCadCommandLineClient() {

    }
    
    public static void main(String[] args) {
        BQ_client client = new BQ_client();        
        String line = "";
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        try {
            while ((line = stdin.readLine()) != null) {
                String[] fields = line.split("\\t");
                if (fields[0].equalsIgnoreCase("set")) {
                    String var = fields[1].trim();
                    String value = fields[2].trim();
                    if (var.equalsIgnoreCase("namespace")) {
                        _Namespace = value;
                    } else if (var.equalsIgnoreCase("model")) {
                        _ModelUri = value;
                    } else if (var.equalsIgnoreCase("context")) {
                        _ContextUri = value;
                    } else if (var.equalsIgnoreCase("service")) {
                        client._axisEndpoint = value;
                    } else if (var.equalsIgnoreCase("repository_server")) {
                        _repository_server = value;
                    } else if (var.equalsIgnoreCase("repository_name")) {
                        _repository_name = value;
                    } else if (var.equalsIgnoreCase("repository_username")) {
                        _repository_username = value;
                    } else if (var.equalsIgnoreCase("repository_password")) {
                        _repository_password = value;
                    } else {
                        System.out.println("usage: set (namespace|model|context|service|\n\trepository_server|repository_name|repository_username|repository_password) value\n\n");
                    }
                    client = new BQ_client();        
                } else if (fields[0].equalsIgnoreCase("add")) {
                    String doc = fields[1].trim();
                    String position = fields[2].trim();
                    String property = fields[3].trim();
                    String concept = fields[4].trim();
                    boolean result = client.addTag(_repository_server, _repository_name,
                        _repository_username, _repository_password, 
                        _ModelUri, _ContextUri,
                        doc, position, property, concept);
                } else if (fields[0].equalsIgnoreCase("get")) {
                    String property = fields[1].trim();
                    String concept = fields[2].trim();
                    String[][] result = client.getTagLocations(_repository_server, _repository_name,
                        _repository_username, _repository_password,
                        _ModelUri, _ContextUri, 
                        property, concept);
                    if (result != null) {
                        for (int i=0;i<result.length;i++) { 
                            for (int j=0;j<result.length-1;j++) {
                                System.out.print(result[i][j] + "\t");
                            }
                            if (result[i].length >= 1) {
                                System.out.println(result[i][result[i].length-1]);
                            }
                        }
                    }
                }
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
    
}

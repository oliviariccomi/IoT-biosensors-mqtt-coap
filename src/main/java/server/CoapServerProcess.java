package server;

import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.CoapServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.coap.CoapEcgResource;
import resources.coap.CoapEdaResource;
import resources.raw.EcgCsvSensor;
import resources.raw.EdaCsvSensor;
import resources.raw.SensorResource;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CoapServerProcess extends CoapServer {

    private static final Logger logger = LoggerFactory.getLogger(CoapServerProcess.class);

    private static final String TAG = "[COAP-SMARTOBJECT]";


    public CoapServerProcess() {
        super();
        String boardID = UUID.randomUUID().toString();
        this.add(createTelemetryResources());
    }

    private CoapResource createTelemetryResources(){

        CoapResource sensorsResource = new CoapResource("sensors");

        EcgCsvSensor ecgCsvSensor = new EcgCsvSensor(EcgCsvSensor.FILE_ECG);
        EdaCsvSensor edaCsvSensor = new EdaCsvSensor(EdaCsvSensor.FILE_EDA);

        CoapEcgResource coapEcgResource = new CoapEcgResource(ecgCsvSensor, ecgCsvSensor.getMacAddress(), "Ecg Sensor");
        CoapEdaResource coapEdaResource = new CoapEdaResource(edaCsvSensor, edaCsvSensor.getMacAddress(), "Eda Sensor" );
        logger.info("Defining and adding resources ...");

        sensorsResource.add(coapEcgResource);
        sensorsResource.add(coapEdaResource);

        return sensorsResource;
    }



    public CoapServerProcess(HashMap<String, SensorResource<?>> mapResource) {
        super();
        String boardID = UUID.randomUUID().toString();

        this.add(createTelemetryResources(mapResource));
    }


    private CoapResource createTelemetryResources(Map<String, SensorResource<?>> resourceMap){
        CoapResource sensorsResource = new CoapResource("Sensors");
        resourceMap.entrySet().forEach(resourceEntry ->{
            if(resourceEntry.getKey() != null && resourceEntry.getValue() != null){
                SensorResource sensorResource = resourceEntry.getValue();
                if(sensorResource.getType().equals(EcgCsvSensor.RESOURCE_TYPE)){
                    CoapEcgResource coapEcgResource = new CoapEcgResource((EcgCsvSensor) sensorResource, sensorResource.getMacAddress(), "Ecg Sensor");
                    sensorsResource.add(coapEcgResource);
                }
                if(sensorResource.getType().equals(EdaCsvSensor.RESOURCE_TYPE)) {
                    CoapEdaResource coapEdaResource = new CoapEdaResource((EdaCsvSensor) sensorResource, sensorResource.getMacAddress(), "Eda Sensor");
                    sensorsResource.add(coapEdaResource);
                }
            }
        });
        logger.info("Defining and adding resources ...");

        return sensorsResource;

    }

    public static void main(String[] args) {

        CoapServerProcess coapServerProcess = new CoapServerProcess();

        logger.info("Starting Coap Server ...");

        coapServerProcess.start();

        logger.info("Coap Server Started");


        coapServerProcess.getRoot().getChildren().stream().forEach(resource -> {
            logger.info("Resource {} -> URI: {} (Observable: {})", resource.getName(), resource.getURI(), resource.isObservable());
            if(!resource.getURI().equals("/.well-known")){
                resource.getChildren().stream().forEach(childResource -> {
                    logger.info("\t Resource {} -> URI: {} (Observable: {})", childResource.getName(), childResource.getURI(), childResource.isObservable());
                });
            }
        });
    }
}

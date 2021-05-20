package resources.coap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.EcgMessage;
import message.SenMLMessage;
import message.SensorMessage;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.raw.EcgCsvSensor;
import resources.raw.ResourceDataListener;
import resources.raw.SensorResource;
import utils.CoreInterfaces;
import utils.SenMLPack;
import utils.SenMLRecord;

import java.util.ArrayList;
import java.util.Optional;

public class CoapEcgResource extends CoapResource {
    private static final Logger logger = LoggerFactory.getLogger(CoapEdaResource.class);

    private EcgCsvSensor ecgCsvSensor;

    private String macAddress;

    private EcgMessage updatedEcgValue;

    private static final String OBJECT_TITLE = "EcgSensorResource";

    // To work with SenML you need an ObjMap
    private ObjectMapper objectMapper;

    public CoapEcgResource(EcgCsvSensor ecgCsvSensor, String macAddress, String name) {
        super(name);
        if(ecgCsvSensor != null) {
            this.macAddress = ecgCsvSensor.getMacAddress();
            this.ecgCsvSensor = ecgCsvSensor;
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", EcgCsvSensor.RESOURCE_TYPE);
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

        }
        else
            logger.error("Null Sensor Raw ref");

        this.ecgCsvSensor.addDataListener(new ResourceDataListener() {
          @Override
          public void onDataChanged(SensorResource<SensorMessage> resource, SensorMessage updatedValue) throws InterruptedException {
              updatedEcgValue = (EcgMessage) updatedValue;
              changed();
          }
        });
    }

    private SenMLMessage getSenMLMessage(EcgMessage ecgMessage) {
        return new SenMLMessage(ecgMessage);
    }



    @Override
    public void handleGET(CoapExchange exchange) {

        // the Max-Age value should match the update interval
        exchange.setMaxAge(EcgCsvSensor.UPDATE_PERIOD);

        //If the request specify the MediaType as JSON or JSON+SenML
        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON) {

            //Optional<String> senmlPayload = getJsonSenmlResponse();
            SenMLMessage senMLMessage = getSenMLMessage(updatedEcgValue);
            if (senMLMessage != null) {
                try {
                    Optional<String> response = Optional.ofNullable(objectMapper.writeValueAsString(senMLMessage));
                    if (response.isPresent())
                        exchange.respond(CoAP.ResponseCode.CONTENT, response.get(), exchange.getRequestOptions().getAccept());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }
            }

            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedEcgValue), MediaTypeRegistry.TEXT_PLAIN);
    }
}

package resources.coap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import message.EcgMessage;
import message.EdaMessage;
import message.SenMLMessage;
import message.SensorMessage;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.coap.MediaTypeRegistry;
import org.eclipse.californium.core.server.resources.CoapExchange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import resources.raw.EcgCsvSensor;
import resources.raw.EdaCsvSensor;
import resources.raw.ResourceDataListener;
import resources.raw.SensorResource;
import utils.CoreInterfaces;

import java.util.Optional;

public class CoapEdaResource extends CoapResource {
    private static final Logger logger = LoggerFactory.getLogger(CoapEdaResource.class);

    private EdaCsvSensor edaCsvSensor;

    private EdaMessage updatedEdaMessage;

    private String macAddress;

    private static final String OBJECT_TITLE = "EdaSensorResource";


    // To work with SenML you need an ObjMap
    private ObjectMapper objectMapper;

    public CoapEdaResource(EdaCsvSensor edaCsvSensor, String macAddress, String name) {
        super(name);
        if(edaCsvSensor != null && macAddress != null) {
            this.macAddress = edaCsvSensor.getMacAddress();
            this.edaCsvSensor = edaCsvSensor;
            this.objectMapper = new ObjectMapper();
            this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

            setObservable(true);
            setObserveType(CoAP.Type.CON);

            getAttributes().setTitle(OBJECT_TITLE);
            getAttributes().setObservable();
            getAttributes().addAttribute("rt", EdaCsvSensor.RESOURCE_TYPE);
            getAttributes().addAttribute("if", CoreInterfaces.CORE_S.getValue());
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.APPLICATION_SENML_JSON));
            getAttributes().addAttribute("ct", Integer.toString(MediaTypeRegistry.TEXT_PLAIN));

        }

        else
            logger.error("Null Sensor Raw ref");


        this.edaCsvSensor.addDataListener(new ResourceDataListener() {
            @Override
            public void onDataChanged(SensorResource<SensorMessage> resource, SensorMessage updatedValue) throws InterruptedException {
                updatedEdaMessage = (EdaMessage) updatedValue;
                changed();
            }
        });
    }

    private SenMLMessage getSenMLMessage() {
        SenMLMessage senMLMessage = new SenMLMessage(updatedEdaMessage);
        return senMLMessage;
    }


    public void handleGET(CoapExchange exchange) {
        // the Max-Age value should match the update interval
        exchange.setMaxAge(EcgCsvSensor.UPDATE_PERIOD);

        //If the request specify the MediaType as JSON or JSON+SenML
        if (exchange.getRequestOptions().getAccept() == MediaTypeRegistry.APPLICATION_SENML_JSON) {

            //Optional<String> senmlPayload = getJsonSenmlResponse();
            SenMLMessage senMLMessage = getSenMLMessage();
            if (senMLMessage != null)
                exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(senMLMessage), exchange.getRequestOptions().getAccept());
            else
                exchange.respond(CoAP.ResponseCode.INTERNAL_SERVER_ERROR);
        }

        //Otherwise respond with the default textplain payload
        else
            exchange.respond(CoAP.ResponseCode.CONTENT, String.valueOf(updatedEdaMessage), MediaTypeRegistry.TEXT_PLAIN);
    }
}




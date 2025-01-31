package proyecto.back_duoc_json_writer.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import proyecto.back_duoc_json_writer.model.AlertaMedica;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@SuppressWarnings("unused")
@Component
public class RabbitMQJsonListener {

    private static final String JSON_DIRECTORY = "/app/json/";
    private final ObjectMapper objectMapper;

    public RabbitMQJsonListener() {
        // Configurar ObjectMapper para manejar fechas correctamente
        this.objectMapper = new ObjectMapper()
                .registerModule(new JavaTimeModule()) // Soporte para Java 8 Date/Time API
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Crear el directorio si no existe
        try {
            Files.createDirectories(Paths.get(JSON_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = "queues_alertasmedicas")
    public void recibirAlertaMedica(AlertaMedica alerta) {
        guardarEnJson(alerta, "alerta_medica");
    }

    @RabbitListener(queues = "queues_alertasgraves")
    public void recibirAlertaGrave(AlertaMedica alerta) {
        guardarEnJson(alerta, "alerta_grave");
    }

    private void guardarEnJson(AlertaMedica alerta, String tipo) {
        try {
            String jsonString = objectMapper.writeValueAsString(alerta);
            String fileName = JSON_DIRECTORY + tipo + "_" + System.currentTimeMillis() + ".json";
            Files.write(Paths.get(fileName), jsonString.getBytes());
            System.out.println("✅ Alerta guardada en JSON: " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
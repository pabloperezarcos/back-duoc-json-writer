package proyecto.back_duoc_json_writer.listener;

import com.fasterxml.jackson.core.type.TypeReference;
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
import java.util.ArrayList;
import java.util.List;

@Component
public class RabbitMQJsonListener {

    private static final String JSON_DIRECTORY = "/app/json/";
    private static final String FILE_ALERTAS_MEDICAS = JSON_DIRECTORY + "alertas_medicas.json";
    private static final String FILE_ALERTAS_GRAVES = JSON_DIRECTORY + "alertas_graves.json";

    private final ObjectMapper objectMapper;

    public RabbitMQJsonListener() {
        // Configurar ObjectMapper con soporte para fechas
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // Crear el directorio si no existe
        try {
            Files.createDirectories(Paths.get(JSON_DIRECTORY));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @RabbitListener(queues = "queues_alertasmedicas")
    public void recibirAlertaMedica(AlertaMedica alerta) {
        guardarEnJson(alerta, FILE_ALERTAS_MEDICAS);
    }

    @RabbitListener(queues = "queues_alertasgraves")
    public void recibirAlertaGrave(AlertaMedica alerta) {
        guardarEnJson(alerta, FILE_ALERTAS_GRAVES);
    }

    private synchronized void guardarEnJson(AlertaMedica alerta, String filePath) {
        try {
            List<AlertaMedica> alertas = new ArrayList<>();
            File file = new File(filePath);

            // Leer el archivo existente si tiene contenido válido
            if (file.exists() && file.length() > 0) {
                try {
                    alertas = objectMapper.readValue(file, new TypeReference<List<AlertaMedica>>() {
                    });
                } catch (IOException e) {
                    System.err.println("⚠️ Error al leer el JSON, iniciando nuevo archivo: " + filePath);
                    alertas = new ArrayList<>();
                }
            }

            // Agregar la nueva alerta
            alertas.add(alerta);

            // Guardar la lista completa en el archivo
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, alertas);

            // Forzar escritura inmediata para evitar archivos corruptos
            System.out.flush();

            System.out.println("✅ Alerta agregada correctamente a " + filePath);

        } catch (IOException e) {
            System.err.println("❌ Error al escribir en JSON: " + e.getMessage());
        }
    }
}

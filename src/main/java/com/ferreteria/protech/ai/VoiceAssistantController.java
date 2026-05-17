package com.ferreteria.protech.ai;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/analista")
public class VoiceAssistantController {

    private final InventoryAssistant inventoryAssistant;

    public VoiceAssistantController(InventoryAssistant inventoryAssistant) {
        this.inventoryAssistant = inventoryAssistant;
    }

    /**
     * Endpoint que recibe el texto dictado por voz y devuelve una respuesta generada por IA.
     * Ejemplo de Request: { "mensaje": "¿Qué productos tienen bajo stock?" }
     * Ejemplo de Response: { "respuestaTexto": "Los siguientes productos están bajo stock: Taladro DeWalt, etc." }
     */
    @PostMapping("/conversar")
    public Map<String, String> conversar(@RequestBody Map<String, String> request) {
        String mensajeUsuario = request.get("mensaje");
        
        if (mensajeUsuario == null || mensajeUsuario.trim().isEmpty()) {
            return Map.of("respuestaTexto", "No he logrado escuchar tu mensaje. Por favor, intenta nuevamente.");
        }

        try {
            // Se envía el mensaje al agente de LangChain4j
            String respuestaAi = inventoryAssistant.conversar(mensajeUsuario);
            return Map.of("respuestaTexto", respuestaAi);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("respuestaTexto", "Lo siento, tuve un problema interno al analizar tu consulta: " + e.getMessage());
        }
    }
}

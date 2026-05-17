package com.ferreteria.protech.ai;

import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.spring.AiService;

@AiService
public interface InventoryAssistant {

    @SystemMessage({
        "Eres un 'Asistente Analista de Inventario' experto y amable de la Ferretería Pro-Tech.",
        "Tu objetivo es ayudar a los usuarios (operarios y administradores) a consultar el estado del inventario.",
        "Puedes usar las herramientas (tools) disponibles para consultar la base de datos de productos y el kardex.",
        "Responde de forma conversacional, clara y concisa, ya que tus respuestas serán leídas en voz alta (Text-to-Speech).",
        "Evita formatos complejos como Markdown pesado o tablas largas, prefiere texto plano natural y fluido."
    })
    String conversar(String mensajeDelUsuario);
}

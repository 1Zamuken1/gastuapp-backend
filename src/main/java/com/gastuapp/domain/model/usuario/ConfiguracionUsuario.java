package com.gastuapp.domain.model.usuario;

// Preferencias y configuración personalizada de cada usuario
import com.gastuapp.domain.model.usuario.TipoOnboarding;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguracionUsuario {
    // Atributos

    private Long id;
    private Long usuarioId; // FK a Usuario
    private Boolean notificacionesActivas;
    private Boolean celebracionesActivas;
    private TipoOnboarding onboardingCompletado;
    private String idiomaPreferido; // "es", "en"
    private Boolean modoOscuro;

    // Lógica
    // Valida que la configuración sea correcta
    public void validar(){
        if (usuarioId == null){
            throw new IllegalArgumentException("El ID de usuairo es obligatorio");
        }

        if (idiomaPreferido == null || idiomaPreferido.trim().isEmpty()) {
            throw new IllegalArgumentException("El idioma preferido es obligatorio");
        }

        if (!idiomaPreferido.equals("es") && !idiomaPreferido.equals("en")) {
            throw new IllegalArgumentException("Idioma no soportado. Idiomas válidos: es, en");
        }
    }

    // Marca el onboarding como completado
    public void completarOnboarding(TipoOnboarding tipo) {
        if (tipo == null) {
            throw new IllegalArgumentException("El tipo de onboarding no puede ser null");
        }
        this.onboardingCompletado = tipo;
    }

    // verifica si el usuario ha completado el onboarding
    public boolean haCompletadoOnboarding(){
        return onboardingCompletado != TipoOnboarding.NO_COMPLETADO;
    }

    // Inicia valores por defecto luego de la construcción
    public void iniciarValoresPorDefecto(){
        if (this.notificacionesActivas == null){
            this.notificacionesActivas = true;
        }
        if (this.celebracionesActivas == null){
            this.celebracionesActivas = true;
        }
        if (this.onboardingCompletado == null) {
            this.onboardingCompletado = TipoOnboarding.NO_COMPLETADO;
        }
        if (this.idiomaPreferido == null || this.idiomaPreferido.trim().isEmpty()) {
            this.idiomaPreferido = "es";
        }
        if (this.modoOscuro == null) {
            this.modoOscuro = false;
        }
    }
}

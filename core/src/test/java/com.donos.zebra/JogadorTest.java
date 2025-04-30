package com.donos.zebra;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JogadorTest {

    @Test
    void deveReceberPontoAoSubirDeNivel() {
        Jogador jogador = new Jogador();
        jogador.subirNivel();
        assertEquals(1, jogador.getPontosAtributo());
    }
}

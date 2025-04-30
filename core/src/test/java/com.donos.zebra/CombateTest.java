package com.donos.zebra;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class CombateTest {

    @Test
    void vidaDeveDiminuirAoReceberDano() {
        Jogador jogador = new Jogador(100);
        jogador.receberDano(30);
        assertEquals(70, jogador.getVida());
    }
}
